package org.inurl.redis.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;
import org.inurl.redis.uitl.Log;

import java.net.InetSocketAddress;


/**
 * @author raylax
 */
public class RedisServer {

    private static final int SO_BACKLOG_VALUE = 1024;

    private final InetSocketAddress bindAddress;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public RedisServer() {
        this("0.0.0.0", 6379);
    }

    public RedisServer(String host, int port) {
        this.bindAddress = new InetSocketAddress(host, port);
    }

    public RedisServer(String host) {
        this(host, 6379);
    }

    public RedisServer(int port) {
        this("0.0.0.0", port);
    }

    public void start() {
        Log.info("starting server");
        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, SO_BACKLOG_VALUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RedisDecoder());
                            pipeline.addLast(new RedisArrayAggregator());
                            pipeline.addLast(new CommandHandler());
                            pipeline.addLast(new RedisEncoder());
                        }
                    });
            ChannelFuture future = bootstrap.bind(bindAddress).sync();
            channel = future.channel();
            Log.info("server started");
        } catch (Exception ex) {
            Log.warn("failed to start server, message [%s]", ex.getMessage());
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        try {
            channel.close().sync();
        } catch (Exception ex) {
            Log.warn("failed to stop server, message [s]", ex.getMessage());
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new RedisServer().start();
    }

}
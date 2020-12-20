package org.inurl.redis.server.codec;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author raylax
 */
@ChannelHandler.Sharable
public class RedisCommandProcessor extends SimpleChannelInboundHandler<AggregatedRedisMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AggregatedRedisMessage msg) {
        System.out.println(msg.command() + " > " + msg.parameters().size());
    }

}

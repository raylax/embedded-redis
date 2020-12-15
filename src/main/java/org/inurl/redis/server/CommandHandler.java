package org.inurl.redis.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.DefaultLastBulkStringRedisContent;
import io.netty.handler.codec.redis.RedisMessage;

import java.util.List;

/**
 * @author raylax
 */
public class CommandHandler extends SimpleChannelInboundHandler<RedisMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RedisMessage msg) {
        if (msg instanceof ArrayRedisMessage) {
            handleArrayRedisMessage((ArrayRedisMessage) msg);
        }
        if (msg instanceof DefaultLastBulkStringRedisContent) {
            handleDefaultLastBulkStringRedisContent((DefaultLastBulkStringRedisContent) msg);
        }
    }

    private void handleDefaultLastBulkStringRedisContent(DefaultLastBulkStringRedisContent message) {
        System.out.println(toString(message));
    }

    private void handleArrayRedisMessage(ArrayRedisMessage arrayMessage) {
        List<RedisMessage> messages = arrayMessage.children();
        for (RedisMessage message : messages) {
            System.out.println(message.getClass());
        }
    }

    private String toString(ByteBufHolder message) {
        ByteBuf content = message.content();
        int n = content.readableBytes();
        byte[] bytes = new byte[n];
        content.readBytes(bytes);
        return new String(bytes);
    }

}

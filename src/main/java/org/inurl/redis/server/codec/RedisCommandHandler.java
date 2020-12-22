package org.inurl.redis.server.codec;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.redis.ErrorRedisMessage;
import io.netty.handler.codec.redis.RedisCodecException;
import org.inurl.redis.core.processor.CommandProcessor;
import org.inurl.redis.core.processor.CommandProcessorRegistry;

/**
 * @author raylax
 */
@ChannelHandler.Sharable
public class RedisCommandHandler extends SimpleChannelInboundHandler<RedisCommand> {

    private final CommandProcessorRegistry processorRegistry;

    public RedisCommandHandler(CommandProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RedisCommand command) {
        CommandProcessor processor = processorRegistry.getProcessor(command.name());
        if (processor == null) {
            ctx.writeAndFlush(new ErrorRedisMessage("unsupported command [" + command.name() + "]"));
            return;
        }
        try {
            processor.process(command, ctx);
        } catch (RedisCodecException ex) {
            ctx.write(new ErrorRedisMessage(ex.getMessage()));
        }
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.write(new ErrorRedisMessage(cause.getMessage()));
    }
}

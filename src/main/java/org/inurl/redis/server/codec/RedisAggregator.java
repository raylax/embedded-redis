package org.inurl.redis.server.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.redis.ArrayHeaderRedisMessage;
import io.netty.handler.codec.redis.BulkStringHeaderRedisMessage;
import io.netty.handler.codec.redis.BulkStringRedisContent;
import io.netty.handler.codec.redis.RedisMessage;

import java.util.List;

/**
 * @author raylax
 */
public class RedisAggregator extends MessageToMessageDecoder<RedisMessage> {

    private enum State {
        DECODE_COMMAND,
        DECODE_ARGS,
    }

    private State state;
    private RedisCommand message;
    private RedisCommand.Parameter parameter;
    private long parameters;
    private long totalParameters;

    public RedisAggregator() {
        reset();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, RedisMessage message, List<Object> out) {
        if (message instanceof ArrayHeaderRedisMessage) {
            handleArrayHeaderRedisMessage((ArrayHeaderRedisMessage) message);
        } else if (message instanceof BulkStringHeaderRedisMessage) {
            handleBulkStringHeaderRedisMessage((BulkStringHeaderRedisMessage) message);
        } else if (message instanceof BulkStringRedisContent) {
            handleBulkStringRedisContent((BulkStringRedisContent) message, out);
        }
    }

    private void handleArrayHeaderRedisMessage(ArrayHeaderRedisMessage message) {
        this.totalParameters = message.length() - 1;
        this.message = new RedisCommand(this.totalParameters);
        this.state = State.DECODE_COMMAND;
    }

    private void handleBulkStringHeaderRedisMessage(BulkStringHeaderRedisMessage message) {
        if (state == State.DECODE_ARGS) {
            this.parameter = new RedisCommand.Parameter(message.bulkStringLength());
        }
    }

    private void handleBulkStringRedisContent(BulkStringRedisContent content, List<Object> out) {
        switch (state) {
            case DECODE_COMMAND:
                String name = RedisUtil.toString(content);
                this.message.setName(name.toUpperCase());
                this.state = State.DECODE_ARGS;
                break;
            case DECODE_ARGS:
                this.parameter.setHolder(content.retain());
                this.message.addParameter(this.parameter);
                this.parameter = null;
                this.parameters++;
            default:
                // NOOP
        }

        // 如果没有参数或参数已经足够向后发送数据
        if (this.totalParameters == 0 || this.totalParameters == this.parameters) {
            out.add(this.message);
            reset();
        }
    }

    private void reset() {
        this.state = State.DECODE_COMMAND;
        this.message = null;
        this.parameter = null;
        this.totalParameters = 0;
        this.parameters = 0;
    }

}

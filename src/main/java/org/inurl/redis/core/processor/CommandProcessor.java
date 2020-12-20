package org.inurl.redis.core.processor;

import io.netty.channel.ChannelHandlerContext;
import org.inurl.redis.server.codec.RedisCommand;

import java.util.List;

/**
 * @author raylax
 */
public interface CommandProcessor {

    /**
     * 所有支持命令
     */
    List<String> supportedCommands();

    /**
     * 处理请求
     */
    void process(RedisCommand command, ChannelHandlerContext ctx);

}

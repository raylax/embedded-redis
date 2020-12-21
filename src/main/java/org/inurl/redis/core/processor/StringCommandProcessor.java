package org.inurl.redis.core.processor;

import io.netty.channel.ChannelHandlerContext;
import org.inurl.redis.server.codec.RedisCommand;

import java.util.Arrays;
import java.util.List;

/**
 * @author raylax
 */
public class StringCommandProcessor implements CommandProcessor {


    @Override
    public List<String> supportedCommands() {
        return Arrays.asList(
                "APPEND",
                "BITCOUNT",
                "BITOP",
                "DECR",
                "DECRBY",
                "GET",
                "GETBIT",
                "GETRANGE",
                "GETSET",
                "INCR",
                "INCRBY",
                "INCRBYFLOAT",
                "MGET",
                "MSET",
                "MSETNX",
                "PSETEX",
                "SET",
                "SETBIT",
                "SETEX",
                "SETNX",
                "SETRANGE",
                "STRLEN"
        );
    }

    @Override
    public void process0(RedisCommand command, ChannelHandlerContext ctx) {

    }

}

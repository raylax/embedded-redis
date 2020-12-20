package org.inurl.redis.core.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.redis.ErrorRedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import org.inurl.redis.server.codec.RedisCommand;
import org.inurl.redis.server.codec.RedisUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @author raylax
 */
public class ConnectionCommandProcessor implements CommandProcessor {

    @Override
    public List<String> supportedCommands() {
        return Arrays.asList("COMMAND", "ECHO", "PING", "QUIT");
    }

    @Override
    public void process(RedisCommand command, ChannelHandlerContext ctx) {
        switch (command.name()) {
            case "COMMAND":
                ctx.write(new ErrorRedisMessage("unsupported command"));
                return;
            case "ECHO":
            case "PING":
                List<RedisCommand.Parameter> parameters = command.parameters();
                String content = "";
                if (!parameters.isEmpty()) {
                    content = RedisUtil.toString(parameters.get(0).getHolder());
                }
                if ("PING".equals(command.name())) {
                    content = "PONG " + content;
                }
                ctx.write(new SimpleStringRedisMessage(content));
                return;
            case "QUIT":
                ctx.close();
                return;
        }
        throw new IllegalStateException();
    }

}

package org.inurl.redis.core.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.redis.ErrorRedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import org.inurl.redis.server.codec.RedisCommand;
import org.inurl.redis.server.codec.RedisUtil;

import java.util.Arrays;
import java.util.List;

import static org.inurl.redis.server.codec.RedisCommand.Name.COMMAND;
import static org.inurl.redis.server.codec.RedisCommand.Name.ECHO;
import static org.inurl.redis.server.codec.RedisCommand.Name.PING;
import static org.inurl.redis.server.codec.RedisCommand.Name.QUIT;

/**
 * @author raylax
 */
public class ConnectionCommandProcessor implements CommandProcessor {

    @Override
    public List<RedisCommand.Name> supportedCommands() {
        return Arrays.asList(COMMAND, ECHO, PING, QUIT);
    }

    @Override
    public void process0(RedisCommand command, ChannelHandlerContext ctx) {
        switch (command.name()) {
            case COMMAND:
                ctx.write(new ErrorRedisMessage("unsupported command"));
                return;
            case ECHO:
            case PING:
                List<RedisCommand.Parameter> parameters = command.parameters();
                String content = "";
                if (!parameters.isEmpty()) {
                    content = RedisUtil.toString(parameters.get(0).getHolder());
                }
                if (command.name() == PING) {
                    content = "PONG " + content;
                }
                ctx.write(new SimpleStringRedisMessage(content));
                return;
            case QUIT:
                ctx.close();
                return;
        }
        throw new IllegalStateException();
    }

}

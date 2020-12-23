package org.inurl.redis.core.processor;

import io.netty.channel.ChannelHandlerContext;
import org.inurl.redis.core.command.GetCommand;
import org.inurl.redis.core.command.SetCommand;
import org.inurl.redis.server.codec.RedisCommand;

import java.util.Arrays;
import java.util.List;

import static org.inurl.redis.core.command.CommandUtil.parse;
import static org.inurl.redis.core.data.Database.handleGetCommand;
import static org.inurl.redis.core.data.Database.handleSetCommand;
import static org.inurl.redis.server.codec.RedisCommand.Name.GET;
import static org.inurl.redis.server.codec.RedisCommand.Name.SET;

/**
 * @author raylax
 */
public class StringCommandProcessor implements CommandProcessor {


    @Override
    public List<RedisCommand.Name> supportedCommands() {
        return Arrays.asList(SET, GET);
    }

    @Override
    public void process0(RedisCommand command, ChannelHandlerContext ctx) {
        switch (command.name()) {
            case SET:
                handleSetCommand(ctx, parse(command, SetCommand.class));
                break;
            case GET:
                handleGetCommand(ctx, parse(command, GetCommand.class));
                break;
            default:
                // NOOP
        }
    }

}

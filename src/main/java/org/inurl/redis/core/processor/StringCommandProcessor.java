package org.inurl.redis.core.processor;

import io.netty.channel.ChannelHandlerContext;
import org.inurl.redis.core.command.CommandUtil;
import org.inurl.redis.core.command.SetCommand;
import org.inurl.redis.server.codec.RedisCommand;

import java.util.Arrays;
import java.util.List;

import static org.inurl.redis.core.Constants.MESSAGE_OK;
import static org.inurl.redis.server.codec.RedisCommand.Name.SET;

/**
 * @author raylax
 */
public class StringCommandProcessor implements CommandProcessor {


    @Override
    public List<RedisCommand.Name> supportedCommands() {
        return Arrays.asList(SET);
    }

    @Override
    public void process0(RedisCommand command, ChannelHandlerContext ctx) {
        switch (command.name()) {
            case SET:
                handleSetCommand(ctx, CommandUtil.parse(command, SetCommand.class));
                break;
        }
    }

    private void handleSetCommand(ChannelHandlerContext ctx, SetCommand command) {
        ctx.write(MESSAGE_OK);
    }

}

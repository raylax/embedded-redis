package org.inurl.redis.core.processor;

import org.inurl.redis.server.codec.RedisCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author raylax
 */
public class CommandProcessorRegistry {

    private final static CommandProcessorRegistry INSTANCE = new CommandProcessorRegistry();

    private final static Map<RedisCommand.Name, CommandProcessor> PROCESSOR_CACHE = new HashMap<>();

    static {
        INSTANCE.register(new ConnectionCommandProcessor());
        INSTANCE.register(new StringCommandProcessor());
    }

    public void register(CommandProcessor processor) {
        List<RedisCommand.Name> commands = processor.supportedCommands();
        Objects.requireNonNull(commands);
        for (RedisCommand.Name command : commands) {
            PROCESSOR_CACHE.put(command, processor);
        }
    }

    public CommandProcessor getProcessor(RedisCommand.Name command) {
        return PROCESSOR_CACHE.get(command);
    }

    public static CommandProcessorRegistry instance() {
        return INSTANCE;
    }
}

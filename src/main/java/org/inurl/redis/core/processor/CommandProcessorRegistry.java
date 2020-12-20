package org.inurl.redis.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author raylax
 */
public class CommandProcessorRegistry {

    private final static Map<String, CommandProcessor> PROCESSOR_CACHE = new HashMap<>();

    public void register(CommandProcessor processor) {
        List<String> commands = processor.supportedCommands();
        Objects.requireNonNull(commands);
        for (String command : commands) {
            PROCESSOR_CACHE.put(command.toUpperCase(), processor);
        }
    }

    public CommandProcessor getProcessor(String command) {
        return PROCESSOR_CACHE.get(command);
    }

}

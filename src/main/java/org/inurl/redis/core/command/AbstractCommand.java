package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class AbstractCommand implements Command {

    @CommandParameter(order = -1)
    private String key;

    @Override
    public String key() {
        return key;
    }

}

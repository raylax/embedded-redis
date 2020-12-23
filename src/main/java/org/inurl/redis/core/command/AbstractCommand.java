package org.inurl.redis.core.command;

import lombok.Getter;
import lombok.Setter;
import org.inurl.redis.core.string.SimpleDynamicString;

/**
 * @author raylax
 */
@Setter
@Getter
public class AbstractCommand implements Command {

    /**
     * key
     */
    @CommandParameter(order = -1)
    private SimpleDynamicString key;


}

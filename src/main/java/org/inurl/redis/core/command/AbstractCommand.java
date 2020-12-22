package org.inurl.redis.core.command;

import lombok.Getter;
import lombok.Setter;

/**
 * @author raylax
 */
@Setter
@Getter
public class AbstractCommand implements Command {

    @CommandParameter(order = -1)
    private String key;


}

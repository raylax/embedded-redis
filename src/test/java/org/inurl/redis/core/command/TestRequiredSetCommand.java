package org.inurl.redis.core.command;

import lombok.Getter;
import lombok.Setter;

/**
 * @author raylax
 */
@Getter
@Setter
public class TestRequiredSetCommand extends AbstractCommand {

    @CommandParameter(order = 1, enumClass = When.class, enumRequired = true)
    private EnumCommandParameter<When> when;

    public enum When {
        NX,
        XX,
        ;
    }


}

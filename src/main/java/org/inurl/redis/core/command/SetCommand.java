package org.inurl.redis.core.command;

import lombok.Getter;
import lombok.Setter;

/**
 * @author raylax
 */
@Getter
@Setter
public class SetCommand extends AbstractCommand {

    @CommandParameter(order = 1)
    private String value;

    @CommandParameter(order = 2)
    private LongOptionalCommandParameter ex;

    @CommandParameter(order = 3)
    private LongOptionalCommandParameter px;

    @CommandParameter(order = 4, enumClass = When.class)
    private EnumCommandParameter<When> when;

    public enum When {
        NX,
        XX,
        ;
    }


}

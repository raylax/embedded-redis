package org.inurl.redis.core.command;

import lombok.Getter;

/**
 * @author raylax
 */
@Getter
public class SetCommand extends AbstractCommand {

    @CommandParameter(order = 1)
    private String value;

    @CommandParameter(order = 2)
    private OptionalCommandParameter<Long> ex;

    @CommandParameter(order = 3)
    private OptionalCommandParameter<Long> px;

    @CommandParameter(order = 4, enumClass = When.class)
    private EnumCommandParameter<When> when;

    public enum When {
        NX,
        XX,
        ;
    }


}

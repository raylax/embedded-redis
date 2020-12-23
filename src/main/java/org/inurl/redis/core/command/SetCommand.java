package org.inurl.redis.core.command;

import lombok.Getter;
import lombok.Setter;
import org.inurl.redis.core.string.SimpleDynamicString;

/**
 * @author raylax
 */
@Getter
@Setter
public class SetCommand extends AbstractCommand {

    @CommandParameter(order = 1)
    private SimpleDynamicString value;

    @CommandParameter(order = 2)
    private IntegerOptionalCommandParameter ex;

    @CommandParameter(order = 3)
    private IntegerOptionalCommandParameter px;

    @CommandParameter(order = 4, enumClass = When.class)
    private EnumCommandParameter<When> when;

    public enum When {
        NX,
        XX,
        ;
    }


}

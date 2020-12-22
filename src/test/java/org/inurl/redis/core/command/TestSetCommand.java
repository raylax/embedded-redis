package org.inurl.redis.core.command;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * @author raylax
 */
@Getter
@Setter
public class TestSetCommand extends AbstractCommand {

    @CommandParameter(order = 1)
    private ByteBuf value;

    @CommandParameter(order = 2)
    private LongOptionalCommandParameter ex;

    @CommandParameter(value = "px", order = 3)
    private LongOptionalCommandParameter px1;

    @CommandParameter(order = 4, enumClass = When.class)
    private EnumCommandParameter<When> when;

    private int xxx;

    public enum When {
        NX,
        XX,
        ;
    }


}

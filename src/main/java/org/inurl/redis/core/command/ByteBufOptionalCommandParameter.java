package org.inurl.redis.core.command;

import io.netty.buffer.ByteBuf;

/**
 * @author raylax
 */
public class ByteBufOptionalCommandParameter extends OptionalCommandParameter<ByteBuf> {

    public ByteBufOptionalCommandParameter(boolean present) {
        super(present);
    }

    public ByteBufOptionalCommandParameter(ByteBuf value) {
        super(value);
    }
    
}

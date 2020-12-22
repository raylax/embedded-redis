package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class LongOptionalCommandParameter extends OptionalCommandParameter<Long> {

    public LongOptionalCommandParameter(boolean present) {
        super(present);
    }

    public LongOptionalCommandParameter(Long value) {
        super(value);
    }
    
}

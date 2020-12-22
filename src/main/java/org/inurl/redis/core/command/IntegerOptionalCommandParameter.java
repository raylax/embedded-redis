package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class IntegerOptionalCommandParameter extends OptionalCommandParameter<Integer> {

    public IntegerOptionalCommandParameter(boolean present) {
        super(present);
    }

    public IntegerOptionalCommandParameter(Integer value) {
        super(value);
    }
    
}

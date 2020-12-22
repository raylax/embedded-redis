package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class FloatOptionalCommandParameter extends OptionalCommandParameter<Float> {

    public FloatOptionalCommandParameter(boolean present) {
        super(present);
    }

    public FloatOptionalCommandParameter(Float value) {
        super(value);
    }
    
}

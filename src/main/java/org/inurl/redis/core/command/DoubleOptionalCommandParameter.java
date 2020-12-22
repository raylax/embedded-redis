package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class DoubleOptionalCommandParameter extends OptionalCommandParameter<Double> {

    public DoubleOptionalCommandParameter(boolean present) {
        super(present);
    }

    public DoubleOptionalCommandParameter(Double value) {
        super(value);
    }
    
}

package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class LongOptionalCommandParameter extends OptionalCommandParameter<String> {

    public LongOptionalCommandParameter(boolean present) {
        super(present);
    }

    public LongOptionalCommandParameter(boolean present, String value) {
        super(present, value);
    }
    
}

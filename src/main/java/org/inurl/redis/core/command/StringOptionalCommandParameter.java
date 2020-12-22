package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class StringOptionalCommandParameter extends OptionalCommandParameter<String> {

    public StringOptionalCommandParameter(boolean present) {
        super(present);
    }

    public StringOptionalCommandParameter(String value) {
        super(value);
    }

}

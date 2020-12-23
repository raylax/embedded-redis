package org.inurl.redis.core.command;

import org.inurl.redis.core.string.SimpleDynamicString;

/**
 * @author raylax
 */
public class SdsOptionalCommandParameter extends OptionalCommandParameter<SimpleDynamicString> {

    public SdsOptionalCommandParameter(boolean present) {
        super(present);
    }

    public SdsOptionalCommandParameter(SimpleDynamicString value) {
        super(value);
    }

}

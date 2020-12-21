package org.inurl.redis.core.command;

import lombok.Getter;

/**
 * @author raylax
 */
@Getter
public class EnumCommandParameter<T extends Enum<T>> extends OptionalCommandParameter<T> {


    public EnumCommandParameter(boolean present) {
        super(present);
    }

    public EnumCommandParameter(boolean present, T value) {
        super(present, value);
    }

}

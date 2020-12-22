package org.inurl.redis.core.command;

import lombok.Getter;

/**
 * @author raylax
 */
@Getter
public class EnumCommandParameter<T extends Enum<T>> extends OptionalCommandParameter<T> {

    public EnumCommandParameter() {
        super(null);
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object e) {
        this.present = e != null;
        this.value = (T) e;
    }

}

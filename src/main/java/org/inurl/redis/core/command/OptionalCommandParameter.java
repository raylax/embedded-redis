package org.inurl.redis.core.command;

import lombok.Getter;

/**
 * @author raylax
 */
public class OptionalCommandParameter<T> {

    private static final OptionalCommandParameter<Object> PRESENT = new OptionalCommandParameter<>(true);
    private static final OptionalCommandParameter<Object> ABSENT = new OptionalCommandParameter<>(false);

    @Getter
    protected boolean present;
    @Getter
    protected T value;

    public OptionalCommandParameter(boolean present) {
        this.present = present;
        this.value = null;
    }

    public OptionalCommandParameter(T value) {
        this.present = value != null;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> OptionalCommandParameter<T> preset() {
        return (OptionalCommandParameter<T>) PRESENT;
    }

    @SuppressWarnings("unchecked")
    public static <T> OptionalCommandParameter<T> absent() {
        return (OptionalCommandParameter<T>) ABSENT;
    }

}

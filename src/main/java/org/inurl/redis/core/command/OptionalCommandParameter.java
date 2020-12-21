package org.inurl.redis.core.command;

/**
 * @author raylax
 */
public class OptionalCommandParameter<T> {

    private static final OptionalCommandParameter<Object> PRESENT = new OptionalCommandParameter<>(true);
    private static final OptionalCommandParameter<Object> ABSENT = new OptionalCommandParameter<>(false);

    private final boolean present;
    private final T value;

    public OptionalCommandParameter(boolean present) {
        this(present, null);
    }

    public OptionalCommandParameter(boolean present, T value) {
        this.present = present;
        this.value = value;
    }

    public boolean isPresent() {
        return present;
    }

    public T value() {
        return value;
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

package org.inurl.redis.core.data.type;

import lombok.Getter;

/**
 * @author raylax
 */
@Getter
public abstract class AbstractData<V> {

    /**
     * 数据类型
     */
    protected Type type;

    /**
     * Time To Live 毫秒
     */
    protected TTL ttl;

    /**
     * 内容
     */
    protected V value;

    public AbstractData(Type type, TTL ttl, V value) {
        this.type = type;
        this.ttl = ttl;
        this.value = value;
    }

    public static class TTL {

        private long value;

        public TTL(int value) {
            this.value = value;
        }

        public static TTL none() {
            return new TTL(-1);
        }

        public static TTL ofSecond(int second) {
            return ofMillisecond(second * 1000);
        }

        public static TTL ofMillisecond(int millisecond) {
            return new TTL((int) (ts() + millisecond));
        }

        public int second() {
            if (isNone()) {
                return -1;
            }
            return millisecond() / 1000;
        }

        public int millisecond() {
            if (isNone()) {
                return -1;
            }
            return (int) (ts() - value);
        }

        public void setSecond(int second) {
            setMillisecond(second * 1000);
        }

        public void setMillisecond(int millisecond) {
            this.value = + ts() + millisecond;
        }

        public void setExpireAtSecond(int second) {
            setExpireAtMillisecond(second * 1000);
        }

        public void setExpireAtMillisecond(int millisecond) {
            this.value = millisecond - ts();
        }

        public boolean isNone() {
            return value == -1;
        }

        public boolean isExpired() {
            return !isNone() && ts() > value;
        }

        private static long ts() {
            return System.currentTimeMillis();
        }

    }

    public enum Type {

        STRING,
        ;

    }

}

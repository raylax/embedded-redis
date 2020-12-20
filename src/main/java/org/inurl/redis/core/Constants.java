package org.inurl.redis.core;

import io.netty.handler.codec.redis.RedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;

/**
 * @author raylax
 */
public class Constants {

    private static final int BYTES_BASE = 1024;

    /**
     * 1kb
     */
    public static final int BYTES_KB = BYTES_BASE;

    /**
     * 1mb
     */
    public static final int BYTES_MB = BYTES_BASE * BYTES_KB;

    /**
     * 1gb
     */
    public static final int BYTES_GB = BYTES_BASE * BYTES_MB;

    public static final RedisMessage REDIS_MESSAGE_OK = new SimpleStringRedisMessage("OK");

}
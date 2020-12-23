package org.inurl.redis.core;

import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.IntegerRedisMessage;
import io.netty.handler.codec.redis.RedisCodecException;
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

    public static final RedisMessage MESSAGE_OK = new SimpleStringRedisMessage("OK");

    public static final RedisMessage MESSAGE_NULL_BULK = FullBulkStringRedisMessage.NULL_INSTANCE;


    /**
     * 不存在
     */
    public static final RedisMessage MESSAGE_NOT_EXISTS = new IntegerRedisMessage(-2);

    /**
     * 未设置TTL
     */
    public static final RedisMessage MESSAGE_TTL_NOT_SET = new IntegerRedisMessage(-1);


    public static final RedisCodecException ERROR_SYNTAX = new RedisCodecException("syntax error");

}
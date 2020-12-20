package org.inurl.redis.server.codec;

import io.netty.buffer.ByteBufHolder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author raylax
 */
public class RedisUtil {

    public static String toString(ByteBufHolder holder) {
        return holder.content().toString(UTF_8);
    }

}

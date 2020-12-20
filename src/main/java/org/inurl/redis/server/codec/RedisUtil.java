package org.inurl.redis.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

/**
 * @author raylax
 */
public class RedisUtil {

    public static String toString(ByteBufHolder holder) {
        ByteBuf buf = holder.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return new String(bytes);
    }

}

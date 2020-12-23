package org.inurl.redis.core.data;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import org.inurl.redis.core.command.GetCommand;
import org.inurl.redis.core.command.OptionalCommandParameter;
import org.inurl.redis.core.command.SetCommand;
import org.inurl.redis.core.data.type.AbstractData;
import org.inurl.redis.core.data.type.StringData;
import org.inurl.redis.core.string.SimpleDynamicString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.inurl.redis.core.Constants.ERROR_WRONG_KIND;
import static org.inurl.redis.core.Constants.MESSAGE_NULL_BULK;
import static org.inurl.redis.core.Constants.MESSAGE_OK;
import static org.inurl.redis.core.command.SetCommand.When.NX;
import static org.inurl.redis.core.command.SetCommand.When.XX;

/**
 *
 *
 * @author raylax
 */
public class Database {

    private static final Map<SimpleDynamicString, Object> DATA = new ConcurrentHashMap<>();

    public static void handleSetCommand(ChannelHandlerContext ctx, SetCommand command) {
        SimpleDynamicString key = command.getKey();
        StringData data = getString(key);
        // check
        SetCommand.When when = getParameterValue(command.getWhen());
        if (when == XX && data == null) {
            ctx.write(MESSAGE_NULL_BULK);
            return;
        }
        if (when == NX && data != null) {
            ctx.write(MESSAGE_NULL_BULK);
            return;
        }
        data = new StringData(command.getValue());
        AbstractData.TTL ttl = data.getTtl();
        Integer ex = getParameterValue(command.getEx());
        if (ex != null) {
            ttl.setSecond(ex);
        }
        Integer px = getParameterValue(command.getPx());
        if (px != null) {
            ttl.setMillisecond(px);
        }
        DATA.put(key, data);
        ctx.write(MESSAGE_OK);
    }

    public static void handleGetCommand(ChannelHandlerContext ctx, GetCommand command) {
        StringData data = getString(command.getKey());
        if (data == null) {
            ctx.write(MESSAGE_NULL_BULK);
            return;
        }
        SimpleDynamicString value = data.getValue();
        ctx.write(new SimpleStringRedisMessage(value.toString()));
    }

    private static <T> T getParameterValue(OptionalCommandParameter<T> parameter) {
        if (parameter == null || !parameter.isPresent()) {
            return null;
        }
        return parameter.getValue();
    }

    private static StringData getString(SimpleDynamicString key) {
        return getData(key, StringData.class);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractData<?>> T getData(SimpleDynamicString key, Class<T> clazz) {
        Object o = DATA.get(key);
        if (o == null) {
            return null;
        }
        AbstractData<?> data = (AbstractData<?>) o;
        if (data.getTtl().isExpired()) {
            DATA.remove(key);
            return null;
        }
        if (clazz.isInstance(o)) {
            return (T) o;
        }
        throw ERROR_WRONG_KIND;
    }

}

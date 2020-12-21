package org.inurl.redis.core.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.redis.RedisCodecException;
import lombok.Getter;
import org.inurl.redis.server.codec.RedisCommand;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.ENUM;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.OPTIONAL;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.REQUIRED_ENUM;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.VALUE;

/**
 * @author raylax
 */
public class CommandUtil {

    private static final Map<Class<?>, List<ParameterField>> FIELDS_CACHE = new HashMap<>();

    public static <T extends Command> T parse(RedisCommand redisCommand, Class<T> clazz) {
        List<ParameterField> parameterFields = FIELDS_CACHE.computeIfAbsent(clazz, CommandUtil::makeParameterFields);
        List<RedisCommand.Parameter> parameters = redisCommand.parameters();
        if (parameterFields.size() > 0 && parameters.isEmpty()) {
            throw new RedisCodecException("ERR wrong number of arguments for '" + redisCommand.name() + "' command");
        }
        for (ParameterField parameterField : parameterFields) {

        }
        return null;
    }


    private static List<ParameterField> makeParameterFields(Class<?> clazz) {
        List<Field> fields = getFields(clazz);
        List<ParameterField> parameterFields = new ArrayList<>(fields.size());
        for (Field field : fields) {
            CommandParameter parameter = field.getAnnotation(CommandParameter.class);
            if (parameter == null) {
                continue;
            }
            Class<?> fieldType = field.getType();
            int order = parameter.order();
            String name = parameter.value();
            ParameterField parameterField = new ParameterField(order, name, field);
            if (fieldType == EnumCommandParameter.class) {
                parameterField.setEnumClass(parameter.enumClass());
                parameterField.setType(parameter.enumRequired() ? REQUIRED_ENUM : ENUM);
            } else if (fieldType == OptionalCommandParameter.class) {
                parameterField.setType(OPTIONAL);
            } else {
                parameterField.setType(VALUE);
            }
            parameterFields.add(parameterField);
        }
        parameterFields.sort(Comparator.comparingInt(x -> x.order));
        return parameterFields;
    }

    private static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> tempClazz = clazz;
        do {
            Field[] declaredFields = tempClazz.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            tempClazz = tempClazz.getSuperclass();
        } while (tempClazz != Object.class);
        return fields;
    }

    public static void main(String[] args) {
        RedisCommand redisCommand = new RedisCommand(5);

        ByteBuf key = Unpooled.wrappedBuffer("key".getBytes());
        RedisCommand.Parameter keyParameter = new RedisCommand.Parameter(key.readableBytes());
        redisCommand.addParameter(keyParameter);

        ByteBuf value = Unpooled.wrappedBuffer("value".getBytes());
        RedisCommand.Parameter valueParameter = new RedisCommand.Parameter(value.readableBytes());
        redisCommand.addParameter(valueParameter);

        ByteBuf ex = Unpooled.wrappedBuffer("ex".getBytes());
        RedisCommand.Parameter exParameter = new RedisCommand.Parameter(ex.readableBytes());
        redisCommand.addParameter(exParameter);

        ByteBuf exValue = Unpooled.wrappedBuffer("10".getBytes());
        RedisCommand.Parameter exValueParameter = new RedisCommand.Parameter(exValue.readableBytes());
        redisCommand.addParameter(exValueParameter);

        ByteBuf px = Unpooled.wrappedBuffer("px".getBytes());
        RedisCommand.Parameter pxParameter = new RedisCommand.Parameter(px.readableBytes());
        redisCommand.addParameter(pxParameter);

        ByteBuf pxValue = Unpooled.wrappedBuffer("10000".getBytes());
        RedisCommand.Parameter pxValueParameter = new RedisCommand.Parameter(pxValue.readableBytes());
        redisCommand.addParameter(pxValueParameter);

        ByteBuf nx = Unpooled.wrappedBuffer("nx".getBytes());
        RedisCommand.Parameter nxParameter = new RedisCommand.Parameter(nx.readableBytes());
        redisCommand.addParameter(nxParameter);


        parse(redisCommand, SetCommand.class);
    }

    static class ParameterField {

        private final String name;
        private final int order;
        private final Field field;
        private Type type;
        private Class<?> enumClass;

        public ParameterField(int order, String name, Field field) {
            this.order = order;
            this.field = field;
            if (name.length() > 0) {
                this.name = name;
            } else {
                this.name = field.getName();
            }
        }

        public void setType(Type type) {
            this.type = type;
        }

        public void setEnumClass(Class<?> enumClass) {
            this.enumClass = enumClass;
        }

        enum Type {
            VALUE(true),
            OPTIONAL(false),
            ENUM(false),
            REQUIRED_ENUM(false),
            ;

            @Getter
            private final boolean required;

            Type(boolean required) {
                this.required = required;
            }
        }
    }


}

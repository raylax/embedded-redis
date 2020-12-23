package org.inurl.redis.core.command;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.redis.RedisCodecException;
import lombok.Getter;
import org.inurl.redis.core.string.SimpleDynamicString;
import org.inurl.redis.server.codec.RedisCommand;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.inurl.redis.core.Constants.ERROR_SYNTAX;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.ENUM;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.OPTIONAL;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.REQUIRED_ENUM;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.VALUE;

/**
 * @author raylax
 */
public class CommandUtil {

    /**
     * {@code null}对象
     * 因为如果key不存在也会返回{@code null}，所以增加{@code NONE}来区分是不存在还是已存在为{@code null}
     */
    private static final Object NONE = new Object();
    /**
     * 类参数缓存
     */
    private static final Map<Class<?>, List<ParameterField>> FIELDS_CACHE = new ConcurrentHashMap<>();
    /**
     * setter方法缓存
     */
    private static final Map<Field, Method> SETTER_CACHE = new ConcurrentHashMap<>();
    /**
     * 枚举类缓存
     */
    private static final Map<Class<?>, Map<String, Object>> ENUM_CACHE = new ConcurrentHashMap<>();

    public static <T extends Command> T parse(RedisCommand redisCommand, Class<T> clazz) {
        List<ParameterField> parameterFields = FIELDS_CACHE.computeIfAbsent(clazz, CommandUtil::makeParameterFields);
        List<RedisCommand.Parameter> parameters = redisCommand.parameters();
        // 如果需要的参数非空并且传入参数为空直接抛异常
        if (!parameterFields.isEmpty() && parameters.isEmpty()) {
            redisCommand.release();
            throw new RedisCodecException("ERR wrong number of arguments for '" + redisCommand.name() + "' command");
        }

        T instance = newInstance(clazz);
        // 参数偏移
        int offset = 0;
        for (ParameterField parameterField : parameterFields) {
            ParameterField.Type type = parameterField.type;
            // 判断参数是否足够
            int rights = parameters.size() - offset - type.parameterCount;
            if (rights < 0) {
                // 如果参数是必须或者是OPTIONAL并且参数数量等于1则抛出异常
                if (type.isRequired() || (rights == -1 && type == OPTIONAL)) {
                    redisCommand.release();
                    throw ERROR_SYNTAX;
                }
                continue;
            }
            boolean parsed = false;
            switch (type) {
                case VALUE:
                    parsed = parseValue(instance, parameterField, parameters.get(offset));
                    break;
                case OPTIONAL:
                    parsed = parseOptionalValue(instance, parameterField, parameters.get(offset), parameters.get(offset + 1));
                    break;
                case ENUM:
                    parsed = parseEnumValue(instance, parameterField, parameters.get(offset), false);
                    break;
                case REQUIRED_ENUM:
                    parsed = parseEnumValue(instance, parameterField, parameters.get(offset), true);
                    break;
                default:
                    // NOOP
            }
            // 如果成功获取参数，增加参数偏移
            if (parsed) {
                offset += type.parameterCount;
            }
        }
        // 确保所有参数被释放
        redisCommand.release();
        return instance;
    }

    /**
     * 构造参数列表
     */
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
            // 如果注解不存在name参数则直接使用列名
            if (name.length() == 0) {
                name = field.getName();
            }
            ParameterField parameterField = new ParameterField(order, name, field);
            if (fieldType == EnumCommandParameter.class) {
                // 枚举类型，并且区分是否必须
                parameterField.setEnumClass(parameter.enumClass());
                parameterField.setType(parameter.enumRequired() ? REQUIRED_ENUM : ENUM);
            } else if (fieldType == OptionalCommandParameter.class
                    || OptionalCommandParameter.class.isAssignableFrom(fieldType)) {
                // 可选类型
                parameterField.setType(OPTIONAL);
            } else {
                // 普通类型
                parameterField.setType(VALUE);
            }
            parameterFields.add(parameterField);
        }
        // getDeclaredFields并不能保证每个jvm实现的顺序，所以进行重新排序
        // https://stackoverflow.com/questions/18325666/does-class-getdeclaredfields-return-members-in-a-consistent-order
        parameterFields.sort(Comparator.comparingInt(x -> x.order));
        return parameterFields;
    }

    /**
     * 解析普通参数
     */
    private static boolean parseValue(Object instance, ParameterField parameterField,
                                      RedisCommand.Parameter parameter) {
        Field field = parameterField.field;
        Class<?> type = field.getType();
        ByteBuf buf = parameter.getHolder().content();
        if (type == ByteBuf.class) {
            setValue(field, instance, buf.retain());
            return true;
        }
        String str = buf.toString(StandardCharsets.UTF_8);
        // 这里应该可以优化为直接从byteBuf转成char[]
        if (type == SimpleDynamicString.class) {
            SimpleDynamicString sds = SimpleDynamicString.create(str);
            setValue(field, instance, sds);
            return true;
        }
        Object val;
        try {
            if (type == String.class) {
                val = str;
            } else if (type == long.class) {
                val = Long.parseLong(str);
            } else if (type == int.class) {
                val = Integer.parseInt(str);
            } else if (type == float.class) {
                val = Float.parseFloat(str);
            } else if (type == double.class) {
                val = Double.parseDouble(str);
            } else {
                throw ERROR_SYNTAX;
            }
        } catch (RedisCodecException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ERROR_SYNTAX;
        }
        setValue(field, instance, val);
        return true;
    }

    /**
     * 解析可选参数
     */
    private static boolean parseOptionalValue(
            Object instance, ParameterField parameterField,
            RedisCommand.Parameter parameterName, RedisCommand.Parameter parameterValue) {
        Field field = parameterField.field;
        // 如果参数名不匹配则直接返回
        if (!parameterField.name.equalsIgnoreCase(parameterName.string())) {
            setValue(field, instance, OptionalCommandParameter.absent());
            return false;
        }
        Class<?> type = field.getType();
        if (type == ByteBufOptionalCommandParameter.class) {
            setValue(field, instance, parameterValue.getHolder().content().retain());
            return true;
        }
        String value = parameterValue.string();
        // 这里应该可以优化为直接从byteBuf转成char[]
        if (type == SimpleDynamicString.class) {
            SimpleDynamicString sds = SimpleDynamicString.create(value);
            setValue(field, instance, new SdsOptionalCommandParameter(sds));
            return true;
        }
        OptionalCommandParameter<?> val;
        try {
            if (type == StringOptionalCommandParameter.class) {
                val = new StringOptionalCommandParameter(value);
            } else if (type == LongOptionalCommandParameter.class) {
                val = new LongOptionalCommandParameter(Long.parseLong(value));
            } else if (type == IntegerOptionalCommandParameter.class) {
                val = new IntegerOptionalCommandParameter(Integer.parseInt(value));
            } else if (type == FloatOptionalCommandParameter.class) {
                val = new FloatOptionalCommandParameter(Float.parseFloat(value));
            } else if (type == DoubleOptionalCommandParameter.class) {
                val = new DoubleOptionalCommandParameter(Double.parseDouble(value));
            } else {
                throw ERROR_SYNTAX;
            }
        } catch (RedisCodecException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ERROR_SYNTAX;
        }
        setValue(field, instance, val);
        return true;
    }

    /**
     * 解析枚举参数
     */
    private static boolean parseEnumValue(Object instance, ParameterField parameterField,
                                          RedisCommand.Parameter parameter, boolean required) {
        String val = parameter.string().toUpperCase();
        Object enumVal = getEnumVal(parameterField.enumClass, val);
        EnumCommandParameter<?> ecp = newInstance(EnumCommandParameter.class);
        ecp.setValue(enumVal);
        if (!ecp.isPresent() && required) {
            throw ERROR_SYNTAX;
        }
        setValue(parameterField.field, instance, ecp);
        return ecp.isPresent();
    }

    private static Object getEnumVal(Class<?> clazz, String val) {
        Map<String, Object> classCache = ENUM_CACHE.get(clazz);
        if (classCache == null) {
            Object enumVal = getEnumVal0(clazz, val);
            classCache = new HashMap<>();
            classCache.put(val, enumVal == null ? NONE : enumVal);
            ENUM_CACHE.put(clazz, classCache);
            return enumVal;
        }
        Object enumCache = classCache.get(val);
        if (enumCache == null) {
            Object enumVal = getEnumVal0(clazz, val);
            classCache.put(val, enumVal == null ? NONE : enumVal);
            return enumVal;
        }
        return enumCache == NONE ? null : enumCache;
    }

    private static Object getEnumVal0(Class<?> clazz, String val) {
        Enum<?>[] enums = (Enum<?>[]) clazz.getEnumConstants();
        for (Enum<?> e : enums) {
            if (e.name().equals(val)) {
                return e;
            }
        }
        return null;
    }


    private static void setValue(Field field, Object obj, Object val) {
        try {
            Method setter = SETTER_CACHE.computeIfAbsent(field, CommandUtil::getSetter);
            setter.invoke(obj, val);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Method getSetter(Field field) {
        Class<?> clazz = field.getDeclaringClass();
        String name = field.getName();
        try {
            return clazz.getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), field.getType());
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> tempClazz = clazz;
        // 循环获取自身和父类字段
        do {
            Field[] declaredFields = tempClazz.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            tempClazz = tempClazz.getSuperclass();
        } while (tempClazz != Object.class);
        return fields;
    }

    static class ParameterField {

        /**
         * 参数名
         */
        private final String name;
        /**
         * 排序
         */
        private final int order;
        /**
         * 字段
         */
        private final Field field;
        /**
         * 类型
         */
        private Type type;
        /**
         * 所对应的枚举类
         */
        private Class<?> enumClass;

        public ParameterField(int order, String name, Field field) {
            this.order = order;
            this.field = field;
            this.name = name;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public void setEnumClass(Class<?> enumClass) {
            this.enumClass = enumClass;
        }

        enum Type {
            /**
             * 普通值类型
             */
            VALUE(true, 1),
            /**
             * 可选类型
             */
            OPTIONAL(false, 2),
            /**
             * 枚举类型
             */
            ENUM(false, 1),
            /**
             * 枚举类型（必须）
             */
            REQUIRED_ENUM(true, 1),
            ;

            /**
             * 是否必须
             */
            @Getter
            private final boolean required;

            /**
             * 参数数量
             */
            @Getter
            private final int parameterCount;

            Type(boolean required, int parameterCount) {
                this.required = required;
                this.parameterCount = parameterCount;
            }
        }
    }


}

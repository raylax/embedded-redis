package org.inurl.redis.core.command;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.redis.RedisCodecException;
import lombok.Getter;
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

import static org.inurl.redis.core.Constants.SYNTAX_ERROR;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.ENUM;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.OPTIONAL;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.REQUIRED_ENUM;
import static org.inurl.redis.core.command.CommandUtil.ParameterField.Type.VALUE;

/**
 * @author raylax
 */
public class CommandUtil {

    private static final Object NONE = new Object();
    private static final Map<Class<?>, List<ParameterField>> FIELDS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Field, Method> SETTER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Object>> ENUM_CACHE = new ConcurrentHashMap<>();

    public static <T extends Command> T parse(RedisCommand redisCommand, Class<T> clazz) {
        List<ParameterField> parameterFields = FIELDS_CACHE.computeIfAbsent(clazz, CommandUtil::makeParameterFields);
        List<RedisCommand.Parameter> parameters = redisCommand.parameters();
        if (parameterFields.size() > 0 && parameters.isEmpty()) {
            throw new RedisCodecException("ERR wrong number of arguments for '" + redisCommand.name() + "' command");
        }

        T instance = newInstance(clazz);
        int i = 0;
        for (ParameterField parameterField : parameterFields) {
            ParameterField.Type type = parameterField.type;
            int rights = parameters.size() - i - type.parameterCount;
            if (rights < 0) {
                //                       处理optional单个参数问题
                if (type.isRequired() || (rights == -1 && type == OPTIONAL)) {
                    throw SYNTAX_ERROR;
                }
                continue;
            }
            boolean parsed;
            switch (type) {
                case VALUE:
                    parsed = parseValue(instance, parameterField, parameters.get(i));
                    break;
                case OPTIONAL:
                    parsed = parseOptionalValue(instance, parameterField, parameters.get(i), parameters.get(i + 1));
                    break;
                case ENUM:
                    parsed = parseEnumValue(instance, parameterField, parameters.get(i), false);
                    break;
                case REQUIRED_ENUM:
                    parsed = parseEnumValue(instance, parameterField, parameters.get(i), true);
                    break;
                default:
                    throw new IllegalStateException();
            }
            if (parsed) {
                i += type.parameterCount;
            }
        }

        return instance;
    }

    private static boolean parseEnumValue(Object instance, ParameterField parameterField,
                                          RedisCommand.Parameter parameter, boolean required) {
        String val = parameter.string().toUpperCase();
        Object enumVal = getEnumVal(parameterField.enumClass, val);
        EnumCommandParameter<?> ecp = newInstance(EnumCommandParameter.class);
        ecp.setValue(enumVal);
        if (!ecp.isPresent() && required) {
            throw SYNTAX_ERROR;
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

    private static boolean parseOptionalValue(
            Object instance, ParameterField parameterField,
            RedisCommand.Parameter parameterName, RedisCommand.Parameter parameterValue) {
        Field field = parameterField.field;
        if (!parameterField.name.equalsIgnoreCase(parameterName.string())) {
            setValue(field, instance, OptionalCommandParameter.absent());
            return false;
        }
        Class<?> type = field.getType();
        String value = parameterValue.string();
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
                throw SYNTAX_ERROR;
            }
        } catch (RedisCodecException ex) {
            throw ex;
        } catch (Exception ex) {
            throw SYNTAX_ERROR;
        }
        setValue(field, instance, val);
        return true;
    }

    private static boolean parseValue(Object instance, ParameterField parameterField,
                                      RedisCommand.Parameter parameter) {
        Field field = parameterField.field;
        Class<?> type = field.getType();
        ByteBuf buf = parameter.getHolder().content();
        String str = buf.toString(StandardCharsets.UTF_8);
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
                throw SYNTAX_ERROR;
            }
        } catch (RedisCodecException ex) {
            throw ex;
        } catch (Exception ex) {
            throw SYNTAX_ERROR;
        }
        setValue(field, instance, val);
        return true;
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
            if (name.length() == 0) {
                name = field.getName();
            }
            ParameterField parameterField = new ParameterField(order, name, field);
            if (fieldType == EnumCommandParameter.class) {
                parameterField.setEnumClass(parameter.enumClass());
                parameterField.setType(parameter.enumRequired() ? REQUIRED_ENUM : ENUM);
            } else if (fieldType == OptionalCommandParameter.class
                    || OptionalCommandParameter.class.isAssignableFrom(fieldType)) {
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
            VALUE(true, 1),
            OPTIONAL(false, 2),
            ENUM(false, 1),
            REQUIRED_ENUM(false, 1),
            ;

            @Getter
            private final boolean required;
            @Getter
            private final int parameterCount;

            Type(boolean required, int parameterCount) {
                this.required = required;
                this.parameterCount = parameterCount;
            }
        }
    }


}

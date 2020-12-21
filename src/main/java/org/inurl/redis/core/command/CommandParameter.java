package org.inurl.redis.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author raylax
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParameter {


    /**
     * 参数名
     */
    String value() default "";

    /**
     * 排序
     */
    int order();

    /**
     * 枚举类
     */
    Class<?> enumClass() default Void.class;

    /**
     * 枚举是否必须存在
     */
    boolean enumRequired() default false;

}

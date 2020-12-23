package org.inurl.redis.server.codec;

import io.netty.buffer.ByteBufHolder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author raylax
 */
public class RedisCommand {

    /**
     * 命令名
     */
    private Name name;

    /**
     * 参数
     */
    private final List<Parameter> parameters;

    public RedisCommand(long parameters) {
        if (parameters < 1) {
            this.parameters = Collections.emptyList();
            return;
        }
        this.parameters = new ArrayList<>((int) parameters);
    }

    public void setName(String name) {
        this.name = Name.of(name);
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public Name name() {
        return name;
    }

    public List<Parameter> parameters() {
        return parameters;
    }



    public enum Name {
        // 基本命令
        COMMAND,
        ECHO,
        PING,
        QUIT,
        // string命令
        APPEND,
        BITCOUNT,
        BITOP,
        DECR,
        DECRBY,
        GET,
        GETBIT,
        GETRANGE,
        GETSET,
        INCR,
        INCRBY,
        INCRBYFLOAT,
        MGET,
        MSET,
        MSETNX,
        PSETEX,
        SET,
        SETBIT,
        SETEX,
        SETNX,
        SETRANGE,
        STRLEN,
        ;
        private static final Map<String, Name> CACHE = new HashMap<>();

        static {
            for (Name value : values()) {
                CACHE.put(value.name(), value);
            }
        }

        public static Name of(String name) {
            return CACHE.get(name);
        }
    }

    public static class Parameter {

        /**
         * 参数长度
         */
        private final int length;

        /**
         * byteBuf
         */
        private ByteBufHolder holder;

        /**
         * 是否已释放
         */
        private boolean released = false;

        /**
         * string缓存
         */
        private String stringCache;

        public Parameter(int length) {
            this.length = length;
        }
        public void setHolder(ByteBufHolder holder) {
            this.holder = holder;
        }

        public ByteBufHolder getHolder() {
            return holder;
        }

        public int getLength() {
            return length;
        }

        public void release() {
            if (!released && holder.refCnt() > 0) {
                holder.release();
                released = true;
            }
        }

        public String string() {
            if (this.stringCache != null) {
                return this.stringCache;
            }
            this.stringCache = holder.content().toString(StandardCharsets.UTF_8);
            release();
            return this.stringCache;
        }
    }

    public void release() {
        for (Parameter parameter : parameters) {
            parameter.release();
        }
    }

}

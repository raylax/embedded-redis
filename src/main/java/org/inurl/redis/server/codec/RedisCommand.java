package org.inurl.redis.server.codec;

import io.netty.buffer.ByteBufHolder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author raylax
 */
public class RedisCommand {

    /**
     * 命令名
     */
    private String name;

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
        this.name = name;
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public String name() {
        return name;
    }

    public List<Parameter> parameters() {
        return parameters;
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

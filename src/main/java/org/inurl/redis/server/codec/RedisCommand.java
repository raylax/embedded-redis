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

    private String name;

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
        private final int length;
        private ByteBufHolder holder;
        private boolean released = false;
        private String stringHolder;

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
            if (this.stringHolder != null) {
                return this.stringHolder;
            }
            this.stringHolder = holder.content().toString(StandardCharsets.UTF_8);
            release();
            return this.stringHolder;
        }
    }

    public void release() {
        for (Parameter parameter : parameters) {
            parameter.release();
        }
    }

}

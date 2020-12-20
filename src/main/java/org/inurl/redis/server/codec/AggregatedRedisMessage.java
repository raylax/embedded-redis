package org.inurl.redis.server.codec;

import io.netty.buffer.ByteBufHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author raylax
 */
public class AggregatedRedisMessage {

    private String command;

    private final List<Parameter> parameters;

    AggregatedRedisMessage(long parameters) {
        if (parameters < 1) {
            this.parameters = Collections.emptyList();
            return;
        }
        this.parameters = new ArrayList<>((int) parameters);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public String command() {
        return command;
    }

    public List<Parameter> parameters() {
        return parameters;
    }

    static class Parameter {
        private final int length;
        private ByteBufHolder holder;

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
    }

}

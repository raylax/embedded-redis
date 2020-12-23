package org.inurl.redis.core.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.redis.RedisCodecException;
import org.inurl.redis.server.codec.RedisCommand;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author raylax
 */
public class CommandUtilTest {

    @Test(expected = RedisCodecException.class)
    public void parse() {

        RedisCommand redisCommand = new RedisCommand(0);
        CommandUtil.parse(redisCommand, TestSetCommand.class);

    }

    @Test
    public void parse0() {

        RedisCommand redisCommand = new RedisCommand(5);

        ByteBuf key = Unpooled.wrappedBuffer("key".getBytes());
        RedisCommand.Parameter keyParameter = new RedisCommand.Parameter(key.readableBytes());
        keyParameter.setHolder(new DefaultByteBufHolder(key));
        redisCommand.addParameter(keyParameter);

        ByteBuf value = Unpooled.wrappedBuffer("value".getBytes());
        RedisCommand.Parameter valueParameter = new RedisCommand.Parameter(value.readableBytes());
        valueParameter.setHolder(new DefaultByteBufHolder(value));
        redisCommand.addParameter(valueParameter);

        ByteBuf ex = Unpooled.wrappedBuffer("ex".getBytes());
        RedisCommand.Parameter exParameter = new RedisCommand.Parameter(ex.readableBytes());
        exParameter.setHolder(new DefaultByteBufHolder(ex));
        redisCommand.addParameter(exParameter);

        ByteBuf exValue = Unpooled.wrappedBuffer("10".getBytes());
        RedisCommand.Parameter exValueParameter = new RedisCommand.Parameter(exValue.readableBytes());
        exValueParameter.setHolder(new DefaultByteBufHolder(exValue));
        redisCommand.addParameter(exValueParameter);

        ByteBuf px = Unpooled.wrappedBuffer("px".getBytes());
        RedisCommand.Parameter pxParameter = new RedisCommand.Parameter(px.readableBytes());
        pxParameter.setHolder(new DefaultByteBufHolder(px));
        redisCommand.addParameter(pxParameter);

        ByteBuf pxValue = Unpooled.wrappedBuffer("10000".getBytes());
        RedisCommand.Parameter pxValueParameter = new RedisCommand.Parameter(pxValue.readableBytes());
        pxValueParameter.setHolder(new DefaultByteBufHolder(pxValue));
        redisCommand.addParameter(pxValueParameter);

        ByteBuf nx = Unpooled.wrappedBuffer("nx".getBytes());
        RedisCommand.Parameter nxParameter = new RedisCommand.Parameter(nx.readableBytes());
        nxParameter.setHolder(new DefaultByteBufHolder(nx));
        redisCommand.addParameter(nxParameter);

        TestSetCommand setCommand = CommandUtil.parse(redisCommand, TestSetCommand.class);
        assertEquals("key", setCommand.getKey().toString());
        assertEquals("value", setCommand.getValue().toString(StandardCharsets.UTF_8));
        assertTrue(setCommand.getEx().isPresent());
        assertEquals(Integer.valueOf(10), setCommand.getEx().getValue());
        assertTrue(setCommand.getPx1().isPresent());
        assertEquals(Integer.valueOf(10000), setCommand.getPx1().getValue());
        assertEquals(TestSetCommand.When.NX, setCommand.getWhen().getValue());

    }

    @Test(expected = RedisCodecException.class)
    public void parse1() {

        RedisCommand redisCommand = new RedisCommand(1);


        ByteBuf key = Unpooled.wrappedBuffer("key".getBytes());
        RedisCommand.Parameter keyParameter = new RedisCommand.Parameter(key.readableBytes());
        keyParameter.setHolder(new DefaultByteBufHolder(key));
        redisCommand.addParameter(keyParameter);

        CommandUtil.parse(redisCommand, TestRequiredSetCommand.class);

    }

    @Test
    public void parse2() {

        for (int i = 0; i < 2; i++) {
            RedisCommand redisCommand = new RedisCommand(2);
            ByteBuf key = Unpooled.wrappedBuffer("key".getBytes());
            RedisCommand.Parameter keyParameter = new RedisCommand.Parameter(key.readableBytes());
            keyParameter.setHolder(new DefaultByteBufHolder(key));
            redisCommand.addParameter(keyParameter);
            ByteBuf nx = Unpooled.wrappedBuffer("nx".getBytes());
            RedisCommand.Parameter nxParameter = new RedisCommand.Parameter(nx.readableBytes());
            nxParameter.setHolder(new DefaultByteBufHolder(nx));
            redisCommand.addParameter(nxParameter);
            CommandUtil.parse(redisCommand, TestRequiredSetCommand.class);
        }

        for (int i = 0; i < 2; i++) {
            RedisCommand redisCommand = new RedisCommand(2);
            ByteBuf key = Unpooled.wrappedBuffer("key".getBytes());
            RedisCommand.Parameter keyParameter = new RedisCommand.Parameter(key.readableBytes());
            keyParameter.setHolder(new DefaultByteBufHolder(key));
            redisCommand.addParameter(keyParameter);
            ByteBuf nx = Unpooled.wrappedBuffer("xx".getBytes());
            RedisCommand.Parameter nxParameter = new RedisCommand.Parameter(nx.readableBytes());
            nxParameter.setHolder(new DefaultByteBufHolder(nx));
            redisCommand.addParameter(nxParameter);
            CommandUtil.parse(redisCommand, TestRequiredSetCommand.class);
        }

    }

}
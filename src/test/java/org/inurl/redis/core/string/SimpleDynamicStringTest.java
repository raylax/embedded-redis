package org.inurl.redis.core.string;

import org.inurl.redis.core.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @author raylax
 */
public class SimpleDynamicStringTest {

    @Test
    public void create() {
        String expected = "sds";
        SimpleDynamicString sds = SimpleDynamicString.create(expected.toCharArray());
        assertEquals(expected, sds.toString());
        assertEquals(expected.hashCode(), sds.hashCode());
    }

    @Test
    public void empty() {
        SimpleDynamicString sds = SimpleDynamicString.empty();
        assertEquals("", sds.toString());
        assertEquals(0, sds.length());
        assertEquals(0, sds.available());
    }

    @Test
    public void free() {
        SimpleDynamicString sds = SimpleDynamicString.empty();
        sds.free();
    }

    @Test
    public void duplicate() {
        SimpleDynamicString sds = SimpleDynamicString.create("sds".toCharArray());
        SimpleDynamicString dup = sds.duplicate();
        assertNotSame(sds, dup);
        assertEquals(sds.toString(), dup.toString());
    }

    @Test
    public void clear() {
        SimpleDynamicString sds = SimpleDynamicString.create("sds".toCharArray());
        sds.clear();
        assertEquals(0, sds.length());
        assertTrue(sds.available() > 0);
    }

    @Test
    public void concat() {
        SimpleDynamicString sds = SimpleDynamicString.create("sds".toCharArray());
        sds.concat("".toCharArray());
        assertEquals("sds", sds.toString());
        sds.concat(" hello".toCharArray());
        assertEquals("sds hello", sds.toString());
        sds.concat(SimpleDynamicString.create("!".toCharArray()));
        assertEquals("sds hello!", sds.toString());
        char[] data = new char[Constants.BYTES_MB];
        sds.concat(data);
        assertEquals(Constants.BYTES_MB / Character.BYTES, sds.available());
    }

    @Test
    public void range() {
        SimpleDynamicString sds = SimpleDynamicString.create("sds".toCharArray());
        sds.range(1, 2);
        assertEquals("ds", sds.toString());
        sds.range(1, 2);
        assertEquals("s", sds.toString());
        sds.range(99999, 1);
        assertEquals("", sds.toString());
    }

    @Test
    public void compare() {
        SimpleDynamicString sds = SimpleDynamicString.create("sds".toCharArray());
        SimpleDynamicString sds1 = SimpleDynamicString.create("sds".toCharArray());
        SimpleDynamicString sds2 = SimpleDynamicString.create("sds1".toCharArray());
        SimpleDynamicString sds3 = SimpleDynamicString.create("sbs".toCharArray());
        assertTrue(sds.compare(sds1));
        assertFalse(sds.compare(sds2));
        assertFalse(sds.compare(sds3));
        assertEquals(sds, sds1);
        assertNotEquals(sds, sds2);
        assertNotEquals(sds, sds3);
    }

    @Test
    public void replace() {
        SimpleDynamicString sds = SimpleDynamicString.create("sds".toCharArray());
        sds.replace("123".toCharArray());
        assertEquals("123", sds.toString());
    }

}
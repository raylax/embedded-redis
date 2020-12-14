package org.inurl.redis.core.string;

import org.inurl.redis.core.Constants;

/**
 * 对应redis SDS(Simple Dynamic String) 实现
 *
 * @author raylax
 */
public class SimpleDynamicString {

    /**
     * 最大预留空间为1M
     */
    private static final int MAX_FREE = Constants.BYTES_MB / Character.BYTES;

    /**
     * 最大容量
     */
    public static final int MAX_CAPACITY = 512 * Constants.BYTES_MB / Character.SIZE;

    /**
     * 空buf
     */
    private static final char[] EMPTY_BUF = new char[0];

    /**
     * 空SDS
     */
    private static final SimpleDynamicString EMPTY = new SimpleDynamicString(EMPTY_BUF);

    /**
     * {@link #buf}已用长度
     */
    private int len;

    /**
     * {@link #buf}空闲长度
     */
    private int free;

    /**
     * 用于保存字符串
     */
    private char[] buf;

    private SimpleDynamicString(char[] data, int length) {
        free = 0;
        len = length;
        buf = new char[length];
        System.arraycopy(data, 0, buf, 0, length);
    }

    private SimpleDynamicString(char[] data) {
        this(data, data.length);
    }

    /**
     * 创建SDS字符串
     * @param data 数据
     * @return SDS
     */
    public static SimpleDynamicString create(char[] data) {
        return new SimpleDynamicString(data);
    }

    /**
     * 空SDS
     * @return SDS
     */
    public static SimpleDynamicString empty() {
        return EMPTY;
    }

    /**
     * 返回字符串长度
     *
     * @return 字符串长度
     */
    public int length() {
        return len;
    }

    /**
     * 返回可用空间长度
     *
     * @return 可用空间长度
     */
    public int available() {
        return free;
    }

    /**
     * 释放
     */
    public void free() {
        buf = null;
    }

    /**
     * 复制
     * @return SDS
     */
    public SimpleDynamicString duplicate() {
        return new SimpleDynamicString(buf, len);
    }

    /**
     * 清空
     */
    public void clear() {
        len = 0;
        free = buf.length;
    }

    /**
     * 拼接
     * @param other 要拼接的数据
     */
    public void concat(SimpleDynamicString other) {
        concat(other.buf, other.length());
    }

    /**
     * 拼接
     * @param data 要拼接的数据
     */
    public void concat(char[] data) {
        concat(data, data.length);
    }

    /**
     * 替换原有数据
     * @param data 要替换的数据
     */
    public void replace(char[] data) {
        int length = data.length;
        System.arraycopy(data, 0, this.buf, 0, length);
        this.len = length;
        this.free = 0;
    }

    /**
     * 保留范围内数据
     * @param offset 偏移
     * @param length 长度
     */
    public void range(int offset, int length) {
        int len = this.len;
        // 越界
        if (len - offset < 1) {
            clear();
            return;
        }
        // 长度过长
        if (offset + length > len) {
            length = len - offset;
        }
        char[] newBuf = new char[length];
        System.arraycopy(this.buf, offset, newBuf, 0, length);
        this.buf = newBuf;
        this.len = length;
        this.free = 0;
    }

    /**
     * 比较是否相等
     * @param other 另一个sds
     * @return 是否相等
     */
    public boolean compare(SimpleDynamicString other) {
        int length = this.len;
        if (length != other.len) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (this.buf[i] != other.buf[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查长度
     * @param length 长度
     * @return 是否超过最大长度
     */
    public static boolean checkLength(int length) {
        return length > MAX_CAPACITY;
    }

    /*
     * 拼接
     */
    private void concat(char[] data, int length) {
        if (length == 0) {
            return;
        }
        if (available() < length) {
            ensureCapacity(this.len + length);
        }
        System.arraycopy(data, 0, this.buf, this.len, length);
        this.len += length;
        this.free -= length;
    }

    /*
     * 确保容量
     */
    private void ensureCapacity(int expectCapacity) {
        int capacity = expectCapacity < MAX_FREE
                ? expectCapacity * 2 // 如果实际长度小于1M扩容至二倍
                : expectCapacity + MAX_FREE; // 否则扩容至实际长度+1M
        int len = this.len;
        char[] newBuf = new char[capacity];
        System.arraycopy(this.buf, 0, newBuf, 0, len);
        this.buf = newBuf;
        this.free = capacity - len;
    }

    @Override
    public String toString() {
        if (len == 0) {
            return "";
        }
        return new String(buf, 0, len);
    }
}

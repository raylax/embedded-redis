package org.inurl.redis.core.data.type;

import org.inurl.redis.core.string.SimpleDynamicString;

/**
 * @author raylax
 */
public class StringData extends AbstractData<SimpleDynamicString> {

    public StringData(SimpleDynamicString value) {
        super(Type.STRING, TTL.none(), value);
    }

}

package org.inurl.redis.core.command;

import org.inurl.redis.core.string.SimpleDynamicString;

/**
 * @author raylax
 */
public interface Command {

    SimpleDynamicString getKey();

}

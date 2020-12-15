package org.inurl.redis.uitl;

import java.time.LocalDateTime;

/**
 * @author raylax
 */
public class Log {

    public static void debug(String message, Object... args) {
        print("D", message, args);
    }

    public static void info(String message, Object... args) {
        print("I", message, args);
    }

    public static void warn(String message, Object... args) {
        print("W", message, args);
    }

    public static void error(String message, Object... args) {
        print("E", message, args);
    }

    private static void print(String flag, String message, Object... args) {
        System.out.println("[" + LocalDateTime.now() + "] [" + flag + "] " + String.format(message, args));
    }

}

package com.github.leifoolsen.jerseyguicepersist.util;

/**
 * Throw even checked exceptions without being required
 * to declare them or catch them. Suggested idiom:
 * throw sneakyThrow( some exception );
 *
 * See: http://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
 */

public final class SneakyThrow {

    private SneakyThrow() {
    }

    public static void propagate(Throwable t) {
        SneakyThrow.<Error>sneakyThrow2(t);
    }

    /**
     * Exploits unsafety to throw an exception that the compiler wouldn't permit
     * but that the runtime doesn't check. See Java Puzzlers #43.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow2(Throwable t) throws T {
        throw (T) t;
    }
}
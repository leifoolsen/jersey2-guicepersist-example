package com.github.leifoolsen.jerseyguicepersist.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Redirect System.out and System.err to slf4j
 * See: http://stackoverflow.com/questions/11187461/redirect-system-out-and-system-err-to-slf4j
 * See: https://github.com/NetsOSS/embedded-jetty/blob/master/src/main/java/eu/nets/oss/jetty/StdoutRedirect.java
 */
public class SysStreamsLogger {

    public static final PrintStream sysout = System.out;
    public static final PrintStream syserr = System.err;

    public static void bindSystemStreams() {
        // Enable autoflush
        System.setOut(createLoggingProxy(System.out, "STDOUT"));
        System.setErr(createLoggingProxy(System.err, "STDERR"));
    }

    public static void unbindSystemStreams() {
        System.setOut(sysout);
        System.setErr(syserr);
    }

    private static PrintStream createLoggingProxy(final PrintStream realPrintStream, final String stream) {
        return new PrintStream(realPrintStream) {

            boolean stdout = "STDOUT".equals(stream);
            private Logger logger = LoggerFactory.getLogger(stdout ? "STDOUT" : "STDERR");


            public void print(final String string) {
                List<String> lines = Arrays.asList(string.split("[\r\n]+"));
                for (String line : lines) {
                    if (stdout)
                        logger.info(line);
                    else
                        logger.warn(line);
                }
            }

            @Override
            public void print(Object obj) {
                print(String.valueOf(obj));
            }

            @Override
            public void println(String s) {
                print(s);
            }

            @Override
            public void println(Object obj) {
                print(String.valueOf(obj));
            }
        };
    }
}
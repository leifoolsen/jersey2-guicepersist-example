package com.github.leifoolsen.jerseyguicepersist.util;

import com.google.common.base.Preconditions;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private FileUtil() {}

    public static Path jarPath(Class clazz) {
        Preconditions.checkNotNull(clazz, "Parameter 'clazz' may not be NULL!");
        Path p = Paths.get(toUri(clazz.getProtectionDomain().getCodeSource().getLocation())).normalize().toAbsolutePath();
        return p.toFile().isFile()
                ? p.getParent()
                : p;
    }

    public static boolean isExploded() {
        return Thread.currentThread().getContextClassLoader().getResource("") != null;
    }

    public static Path classesPath() {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if(url != null) {
            Path p = Paths.get(toUri(url));
            if(p.endsWith("test-classes")) {
                p = p.getParent().resolve("classes");
            }
            return p.normalize();
        }
        return null;
    }

    public static Path testClassesPath() {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if(url != null) {
            final Path p = Paths.get(toUri(url));
            return p.endsWith("test-classes") ? p.normalize() : null;
        }
        return null;
    }

    private static URI toUri(final URL url) {
        try {
            return url.toURI();
        }
        catch (URISyntaxException e) {
            SneakyThrow.propagate(e);
            throw new RuntimeException(); // Should not happen
        }
    }
}

package com.github.leifoolsen.jerseyguicepersist.util;

import com.google.common.base.Preconditions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private FileUtil() {}

    public static Path jarPath(Class clazz) {
        Preconditions.checkNotNull(clazz, "Parameter 'clazz' may not be NULL!");
        Path p = Paths.get(toURI(clazz.getProtectionDomain().getCodeSource().getLocation())).normalize().toAbsolutePath();
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
            Path p = Paths.get(toURI(url));
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
            final Path p = Paths.get(toURI(url));
            return p.endsWith("test-classes") ? p.normalize() : null;
        }
        return null;
    }

    public static URI toURI(final URL url) {
        try {
            return url.toURI();
        }
        catch (URISyntaxException e) {
            SneakyThrow.propagate(e);
            throw new RuntimeException(e); // Should not happen
        }
    }

    public static URL toURL(final URI uri) {
        try {
            return uri.toURL();
        }
        catch (MalformedURLException e) {
            SneakyThrow.propagate(e);
            throw new RuntimeException(e); // Should not happen
        }
    }
}

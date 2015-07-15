package com.github.leifoolsen.jerseyguicepersist.util;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {}

    public static Path jarPath(Class clazz) {
        Preconditions.checkNotNull(clazz, "Parameter 'clazz' may not be NULL!");
        Path p = Paths.get(toUri(clazz.getProtectionDomain().getCodeSource().getLocation())).normalize().toAbsolutePath();
        return p.toFile().isFile()
                ? p.getParent()
                : p;
    }

    public static Path applicationStartupPath() {
        return Paths.get("").normalize().toAbsolutePath();
    }

    private static URI toUri(final URL url) {
        try {
            return url.toURI();
        }
        catch (URISyntaxException e) {
            SneakyThrow.propagate(e);
            return null; // Should not happen
        }
    }
}

package com.github.leifoolsen.jerseyguicepersist.util;

import com.google.common.base.MoreObjects;

public class StringUtil {

    private StringUtil() {}

    public static String blankToNull(final String value) {
        String s = MoreObjects.firstNonNull(value, "").trim();
        return s.length() > 0 ? s : null;
    }
}

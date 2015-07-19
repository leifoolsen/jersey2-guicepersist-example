package com.github.leifoolsen.jerseyguicepersist.util;

import com.google.common.base.Strings;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// See: https://sites.google.com/site/gson/gson-type-adapters-for-common-classes
// See: https://sites.google.com/site/gson/gson-type-adapters-for-common-classes-1
// See: http://www.javacreed.com/gson-typeadapter-example/
// See: https://sites.google.com/site/gson/gson-user-guide#TOC-Writing-a-Deserializer
// See: https://github.com/gkopff/gson-javatime-serialisers/

public class GsonTypeAdapters {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private GsonTypeAdapters() {}

    public static JsonDeserializer<String> stringDeserializerEmptyToNull() {

        return (json, typeOfT, context) -> json == null
                ? null
                : Strings.emptyToNull(json.getAsString().trim());
    }

    public static JsonDeserializer<LocalDate> localDateDeserializer() {
        return (json, typeOfT, context) -> json == null
                ? null
                : FORMATTER.parse(json.getAsString(), LocalDate::from);
    }

    public static JsonSerializer<LocalDate> localDateSerializer() {
        return (src, typeOfSrc, context) -> src == null
                ? null
                : new JsonPrimitive(FORMATTER.format(src));
    }
}

package com.github.leifoolsen.jerseyguicepersist.util;

import com.google.common.base.Strings;
import com.google.gson.JsonDeserializer;

// See: https://sites.google.com/site/gson/gson-type-adapters-for-common-classes
// See: https://sites.google.com/site/gson/gson-type-adapters-for-common-classes-1
// See: http://www.javacreed.com/gson-typeadapter-example/
// See: https://sites.google.com/site/gson/gson-user-guide#TOC-Writing-a-Deserializer

public class GsonTypeAdapters {

    private GsonTypeAdapters() {}

    public static JsonDeserializer<String> stringDeserializerEmptyToNull() {

        return (json, typeOfT, context) -> json == null
                ? null
                : Strings.emptyToNull(json.getAsString().trim());
    }
}

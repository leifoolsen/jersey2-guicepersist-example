package com.github.leifoolsen.jerseyguicepersist.util;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class GsonTypeAdaptersTest {
    private static final String ISO_DATE = "2015-07-19";
    private static final String JSON_DATE = "\"2015-07-19\"";
    private static final LocalDate LOCAL_DATE = LocalDate.parse(ISO_DATE);

    private static final String ISO_DATE_TIME = "2015-07-19T13:14:15";
    private static final String JSON_DATE_TIME = "\"2015-07-19T13:14:15\"";
    private static final String JSON_DATE_TIME_AT_MIDNIGHT = "\"2015-07-19T00:00:00\"";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse(ISO_DATE_TIME);

    private static final String ISO_INSTANT = "2015-07-19T13:14:15Z";
    private static final String JSON_INSTANT = "\"2015-07-19T13:14:15Z\"";
    private static final Instant INSTANT = Instant.parse(ISO_INSTANT);

    @Test
    public void InstantDeserializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, GsonTypeAdapters.instantDeserializer())
                .create();

        final Instant instant = gson.fromJson(JSON_INSTANT, Instant.class);
        assertThat(instant, is(INSTANT));
    }

    @Test
    public void instantSerializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, GsonTypeAdapters.instantSerializer())
                .create();

        final String json = gson.toJson(INSTANT);
        assertThat(json, is(JSON_INSTANT));
    }

    @Test
    public void localDateTimeDeserializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, GsonTypeAdapters.localDateTimeDeserializer())
                .create();

        final LocalDateTime localDateTime = gson.fromJson(JSON_DATE_TIME, LocalDateTime.class);
        assertThat(localDateTime, is(LOCAL_DATE_TIME));

        final LocalDateTime localDateTimeAtMidnight = gson.fromJson(JSON_DATE, LocalDateTime.class);
        assertThat(localDateTimeAtMidnight.toLocalDate(), is(LOCAL_DATE));
    }

    @Test
    public void localDateTimeSerializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, GsonTypeAdapters.localDateTimeSerializer())
                .create();

        final String json = gson.toJson(LOCAL_DATE_TIME);
        assertThat(json, is(JSON_DATE_TIME));

        final String jsonAtMidnight = gson.toJson(LOCAL_DATE.atStartOfDay());
        assertThat(jsonAtMidnight, is(JSON_DATE_TIME_AT_MIDNIGHT));
    }

    @Test
    public void localDateDeserializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, GsonTypeAdapters.localDateDeserializer())
                .create();

        final LocalDate localDate = gson.fromJson(JSON_DATE, LocalDate.class);
        assertThat(localDate, is(LOCAL_DATE));
    }

    @Test
    public void localDateSerializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, GsonTypeAdapters.localDateSerializer())
                .create();

        final String json = gson.toJson(LOCAL_DATE);
        assertThat(json, is(JSON_DATE));
    }

    @Test
    public void fromJsonToObject() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, GsonTypeAdapters.stringDeserializerEmptyToNull())
                .registerTypeAdapter(LocalDate.class, GsonTypeAdapters.localDateDeserializer())
                .create();

        final JsonObject jsonObject = asJsonObject();
        final MyParent parent = gson.fromJson(jsonObject.toString(), MyParent.class);
        assertThat(parent.aDate, is(LOCAL_DATE));
        assertThat(parent.aString, is(nullValue()));
        assertThat(parent.anInt, is(0));
        assertThat(parent.myChild, notNullValue());
        assertThat(parent.myChild.aString, notNullValue());
        assertThat(parent.myChild.anInt, is(101));
    }

    @Test
    public void fromObjectToJson() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, GsonTypeAdapters.localDateSerializer())
                .create();

        final MyParent myParent = asMyParent();
        final String json = gson.toJson(myParent);

        assertThat(Splitter.on("aString").splitToList(json).size() - 1, is(1));
        assertThat(json, containsString(ISO_DATE));
    }

    private static JsonObject asJsonObject() {

        final JsonReader reader = Json.createReader(
                new StringReader(("{'aString':'foostring', 'anInt':101}").replace('\'', '"')));

        final JsonObject childAsJsonObject = reader.readObject();
        reader.close();

        // See: http://docs.oracle.com/javaee/7/api/javax/json/JsonObject.html
        final JsonBuilderFactory factory = Json.createBuilderFactory(null);
        return factory.createObjectBuilder()
                .add("aString", "")
                .add("aDate", ISO_DATE)
                .add("anInt", 0)
                .add("myChild", childAsJsonObject)
                .build();
    }

    private static MyParent asMyParent() {
        MyChild myChild = new MyChild();
        myChild.aString = "foostring";
        myChild.anInt = 101;

        MyParent myParent = new MyParent();
        myParent.aString = null;
        myParent.anInt = 0;
        myParent.myChild = myChild;

        return myParent;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MyParent {
        private LocalDate aDate = LOCAL_DATE;
        private String aString;
        private int anInt;
        private MyChild myChild;

        MyParent() {}
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MyChild {
        private String aString;
        private int anInt;

        MyChild() {}
    }
}

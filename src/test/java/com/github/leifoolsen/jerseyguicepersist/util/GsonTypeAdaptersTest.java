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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class GsonTypeAdaptersTest {
    private static final String ISO_DATE = "2015-07-19";
    private static final String JSON_DATE = "\"2015-07-19\"";
    private static final LocalDate LOCAL_DATE = LocalDate.parse(ISO_DATE, GsonTypeAdapters.DATE_FORMATTER);

    private static final String ISO_DATE_TIME = "2015-07-19T13:14:15";
    private static final String JSON_DATE_TIME = "\"2015-07-19T13:14:15\"";
    private static final String JSON_DATE_TIME_AT_MIDNIGHT = "\"2015-07-19T00:00:00\"";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse(ISO_DATE_TIME, GsonTypeAdapters.DATE_TIME_FORMATTER);

    private static final String ISO_INSTANT = "2015-07-19T13:14:15Z";
    private static final String JSON_INSTANT = "\"2015-07-19T13:14:15Z\"";
    private static final Instant INSTANT = Instant.parse(ISO_INSTANT);

    private static final String ISO_OFFSET_DATE_TIME = "2015-07-19T13:14:15+01:00";
    private static final String JSON_OFFSET_DATE_TIME = "\"2015-07-19T13:14:15+01:00\"";
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.parse(ISO_OFFSET_DATE_TIME, GsonTypeAdapters.OFFSET_DATE_TIME_FORMATTER);

    @Test
    public void offsetDateTimeDeserializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, GsonTypeAdapters.offsetDateTimeDeserializer())
                .create();

        final OffsetDateTime offsetDateTime = gson.fromJson(JSON_OFFSET_DATE_TIME, OffsetDateTime.class);
        assertThat(offsetDateTime, is(OFFSET_DATE_TIME));
    }

    @Test
    public void offsetDateTimeSerializer() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, GsonTypeAdapters.offsetDateTimeSerializer())
                .create();

        final String json = gson.toJson(OFFSET_DATE_TIME);
        assertThat(json, is(JSON_OFFSET_DATE_TIME));
    }

    @Test
    public void instantDeserializer() {
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
    public void julDateDeserializer() throws ParseException {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, GsonTypeAdapters.julDateDeserializer())
                .create();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sdf.parse(ISO_DATE);
        Date julDate = gson.fromJson(JSON_DATE, Date.class);
        assertThat(julDate, is(d));

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        d = sdf.parse(ISO_DATE_TIME);
        julDate = gson.fromJson(JSON_DATE_TIME, Date.class);
        assertThat(julDate, is(d));
    }

    @Test
    public void stringDeserializerEmptyToNull() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, GsonTypeAdapters.stringDeserializerEmptyToNull())
                .create();

        String input = null;
        String output = gson.fromJson(input, String.class);
        assertThat(output, is(nullValue()));

        input = "";
        output = gson.fromJson(input, String.class);
        assertThat(output, is(nullValue()));

        input = "foostring";
        output = gson.fromJson(input, String.class);
        assertThat(output, is("foostring"));

        input = "           foostring     ";
        output = gson.fromJson(input, String.class);
        assertThat(output, is("foostring"));
    }

    @Test
    public void stringSerializer() {
        final Gson gson = new GsonBuilder().create();

        String input = null;
        String output = gson.toJson(input);
        assertThat(output, is("null"));

        input = "";
        output = gson.fromJson(input, String.class);
        assertThat(output, is(nullValue()));

        input = "foostring";
        output = gson.toJson(input);
        assertThat(output, is("\"foostring\""));

        input = "        foostring  ";
        output = gson.toJson(input);
        assertThat(output, is("\"        foostring  \""));

        EmbeddedString embeddedString = new EmbeddedString();

        embeddedString.embedded = null;
        output = gson.toJson(embeddedString);
        assertThat(output, is("{}"));

        embeddedString.embedded = "";
        output = gson.toJson(embeddedString);
        assertThat(output, is("{\"embedded\":\"\"}"));

        embeddedString.embedded = "foostring";
        output = gson.toJson(embeddedString);
        assertThat(output, is("{\"embedded\":\"foostring\"}"));
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



    private static class EmbeddedString {
        String embedded;
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

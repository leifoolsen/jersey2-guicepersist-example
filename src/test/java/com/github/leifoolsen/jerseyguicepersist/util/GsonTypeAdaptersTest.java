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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class GsonTypeAdaptersTest {

    @Test
    public void fromJson() {

        JsonObject jsonObject = asJsonObject();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, GsonTypeAdapters.stringDeserializerEmptyToNull())
                .create();

        MyParent parent = gson.fromJson(jsonObject.toString(), MyParent.class);

        assertThat(parent.aString, is(nullValue()));
        assertThat(parent.anInt, is(0));
        assertThat(parent.myChild, notNullValue());
        assertThat(parent.myChild.aString, notNullValue());
        assertThat(parent.myChild.anInt, is(101));
    }

    @Test
    public void toJson() {

        Gson gson = new GsonBuilder().create();
        MyParent myParent = asMyParent();
        String json = gson.toJson(myParent);

        assertThat(Splitter.on("aString").splitToList(json).size() - 1, is(1));
    }

    private static JsonObject asJsonObject() {

        JsonReader reader = Json.createReader(
                new StringReader(("{'aString':'foostring', 'anInt':101}").replace('\'', '"')));

        JsonObject childAsJsonObject = reader.readObject();
        reader.close();

        // See: http://docs.oracle.com/javaee/7/api/javax/json/JsonObject.html
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        return factory.createObjectBuilder()
                .add("aString", "")
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

package com.github.leifoolsen.jerseyguicepersist.sampledata;

import com.github.leifoolsen.jerseyguicepersist.domain.User;

import java.util.HashMap;
import java.util.Map;

public class SampleDomain {

    public static final String SCOTT = "SCOTT";  // Prepopulated via import.sql
    public static final String HOMER = "HOMER";  // Prepopulated via import.sql
    public static final String ELVIS = "ELVIS";  // Prepopulated via import.sql
    public static final String SHREK = "SHREK";  // Prepopulated via import.sql
    public static final String ALICE = "ALICE";
    public static final String PLUTO = "PLUTO";
    public static final String LEIFO = "LEIFO";

    private static final Map<String, User> USERS =  new HashMap<String, User>() {{
        put(ALICE, new User(ALICE, "wonderland", true));
        put(PLUTO, new User(PLUTO, "thedog", true));
        put(LEIFO, new User(LEIFO, "lollol", true));
    }};

    private SampleDomain() {}

    public static User user(final String key) { return USERS.get(key); }
    public static Map<String, User> users() { return USERS; }
}

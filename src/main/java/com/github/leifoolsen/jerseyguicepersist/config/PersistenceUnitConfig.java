package com.github.leifoolsen.jerseyguicepersist.config;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Properties;

public class PersistenceUnitConfig {
    @NotBlank
    private String name;

    @NotNull
    @NotEmpty
    private Properties properties;

    public PersistenceUnitConfig() {}

    public PersistenceUnitConfig name(final String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public PersistenceUnitConfig properties(final Properties properties) {
        this.properties = properties;
        return this;
    }

    public Properties properties() {
        return properties;
    }
}

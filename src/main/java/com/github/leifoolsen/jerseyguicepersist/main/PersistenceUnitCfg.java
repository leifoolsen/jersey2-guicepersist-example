package com.github.leifoolsen.jerseyguicepersist.main;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Properties;

public class PersistenceUnitCfg {
    @NotBlank
    private String name;

    @NotNull
    @NotEmpty
    private Properties properties;

    public PersistenceUnitCfg() {}

    public PersistenceUnitCfg name(final String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public PersistenceUnitCfg properties(final Properties properties) {
        this.properties = properties;
        return this;
    }

    public Properties properties() {
        return properties;
    }
}

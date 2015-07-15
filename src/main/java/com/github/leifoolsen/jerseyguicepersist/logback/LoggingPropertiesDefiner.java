package com.github.leifoolsen.jerseyguicepersist.logback;

import ch.qos.logback.core.PropertyDefinerBase;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;

public class LoggingPropertiesDefiner extends PropertyDefinerBase {

    private String key;
    private String value;

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public String getPropertyValue() {
        // Load config
        ApplicationConfig.load();
        return ApplicationConfig.logPath();
    }
}

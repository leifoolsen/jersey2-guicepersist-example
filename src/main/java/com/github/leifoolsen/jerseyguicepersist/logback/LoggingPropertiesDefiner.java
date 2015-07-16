package com.github.leifoolsen.jerseyguicepersist.logback;

import ch.qos.logback.core.PropertyDefinerBase;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;

public class LoggingPropertiesDefiner extends PropertyDefinerBase {

    private String key;

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public String getPropertyValue() {
        // Load config
        ApplicationConfig.load();
        if("logPath".equals(key)) {
            return ApplicationConfig.logPath();
        }
        else if("logLevel".equals(key)) {
            return ApplicationConfig.Stage.PROD.equals(ApplicationConfig.stage()) ? "info" : "debug";
        }
        return "Error: unknown key!";
    }
}

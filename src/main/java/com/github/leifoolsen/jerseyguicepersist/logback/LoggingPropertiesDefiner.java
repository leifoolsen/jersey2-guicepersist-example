package com.github.leifoolsen.jerseyguicepersist.logback;

import ch.qos.logback.core.PropertyDefinerBase;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;

public class LoggingPropertiesDefiner extends PropertyDefinerBase {

    private String key;

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public String getPropertyValue() {
        // Load config
        ApplicationConfigFactory.load();
        if("logPath".equals(key)) {
            //System.out.println("[INFO] logPath set to: " + ApplicationConfigFactory.applicationConfig().logPath());

            return ApplicationConfigFactory.applicationConfig().logPath();
        }
        else if("logLevel".equals(key)) {
            return ApplicationConfig.Stage.PROD.equals(ApplicationConfigFactory.applicationConfig().stage())
                    ? "info"
                    : "debug";
        }
        return "Error: unknown key!";
    }
}

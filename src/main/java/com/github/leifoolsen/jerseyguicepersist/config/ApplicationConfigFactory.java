package com.github.leifoolsen.jerseyguicepersist.config;

import com.github.leifoolsen.jerseyguicepersist.util.FileUtil;
import com.github.leifoolsen.jerseyguicepersist.util.GsonTypeAdapters;
import com.github.leifoolsen.jerseyguicepersist.util.SneakyThrow;
import com.github.leifoolsen.jerseyguicepersist.util.StringUtil;
import com.github.leifoolsen.jerseyguicepersist.util.ValidatorHelper;
import com.google.common.reflect.ClassPath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class ApplicationConfigFactory {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfigFactory.class);


    private static final String ROOT_PATH = "application";
    private static final String APPASSEMBLER_APP_HOME = "app.home";

    private static Config config = null;
    private static ApplicationConfig applicationConfig;

    private ApplicationConfigFactory() {}

    public static String load() {

        // Logger may not be available yet. Returning a log message which can be logged elsewhere
        final String logMessage;

        if(config == null) {
            final String resourceBaseName = resourceBaseName();
            if (StringUtil.blankToNull(resourceBaseName) == null) {
                logMessage = "Loading default config, application.conf. No callback defined";
                config = ConfigFactory.load();
            } else {
                logMessage = "Loading config from: '" + resourceBaseName + ".conf' with fallback: 'application.conf'";
                config = ConfigFactory.load(resourceBaseName).withFallback(ConfigFactory.load());
            }
            applicationConfig = unMarshalConfig(config.getConfig(ROOT_PATH).root());
            applicationConfig.appHome(calculateAppHome());
            ValidatorHelper.validate(applicationConfig);
        }
        else {
            logMessage = "Config file was already loaded";
        }

        return logMessage;
    }

    public static Config config() { return config; }

    public static ApplicationConfig applicationConfig() { return applicationConfig; }

    private static ApplicationConfig unMarshalConfig(final ConfigObject configObject) {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, GsonTypeAdapters.stringDeserializerEmptyToNull())
                .create();

        String json = applicationConfigToJSON(configObject, true);
        return gson.fromJson(json, ApplicationConfig.class);
    }

    public static String applicationConfigToJSON(final ConfigObject configObject, final boolean formatted) {
        return config.getConfig(ROOT_PATH).root()
                .render(ConfigRenderOptions.concise().setJson(true).setFormatted(formatted));
    }

    private static String calculateAppHome() {
        // If the application is launched from appassembler, then 'app.home' property is set from appassembler's startapp script
        // else, the location of this jar is used as appHome
        String appHome = config.hasPath(APPASSEMBLER_APP_HOME)
                ? config.getString(APPASSEMBLER_APP_HOME)
                : FileUtil.jarPath(ApplicationConfigFactory.class).toString();

        return appHome.endsWith("classes")
                ? Paths.get(appHome).getParent().toString()
                : appHome;
    }

    public static String resourceBaseName() {
        if(FileUtil.testClassesPath() != null) {
            return "application-test";
        }
        else {
            return isStartedWithAppassembler()
                    ? "application-prod"
                    : "application-dev";
        }
    }

    public static boolean isJunitInClassPath() {
        try {
            ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());

            // Assumes org.junt only defined in scope test
            return cp.getTopLevelClasses("org.junit").size() > 0;
        }
        catch (Exception e) {
            SneakyThrow.propagate(e);
            return false; // Should never happen
        }

    }

    /**
     * The properties "app.home", "app.name", "app.repo", "app.pid" is set in "./appassembler/bin/startapp" script
     * @return true if the current process has been started with appassembler.
     */
    public static boolean isStartedWithAppassembler() {
        final String[] appAssemblerProperties = {
                "app.home",
                "app.name",
                "app.repo",
        };
        for (String property : appAssemblerProperties) {
            if (System.getProperty(property) != null) {
                return true;
            }
        }
        return false;
    }
}

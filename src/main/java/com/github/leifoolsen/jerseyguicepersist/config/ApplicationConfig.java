package com.github.leifoolsen.jerseyguicepersist.config;

import com.github.leifoolsen.jerseyguicepersist.util.FileUtil;
import com.github.leifoolsen.jerseyguicepersist.util.SneakyThrow;
import com.github.leifoolsen.jerseyguicepersist.util.StringUtil;
import com.github.leifoolsen.jerseyguicepersist.util.ValidatorHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.reflect.ClassPath;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);


    public enum Stage {
        DEV("dev"),
        PROD("prod"),
        TEST("test");

        private static final Map<String, Stage> LOOKUP = new HashMap<>();
        static {
            for (Stage s : Stage.values()) {
                LOOKUP.put(s.stage, s);
            }
        }

        private final String stage;

        Stage(String stage) { this.stage = stage; }

        public String stage() { return stage; }

        public static Stage get(final String stage) {
            Preconditions.checkNotNull(stage, "Stage to get may not be null");
            return Preconditions.checkNotNull(LOOKUP.get(stage), "Stage '%s' not found", stage);
        }
    }

    private static final String ROOT_PATH = "application";
    private static final String JETTY_PATH = ROOT_PATH + ".jettyConfig";
    private static final String PU_PATH = ROOT_PATH + ".persistenceUnitConfig";
    private static final String APPASSEMBLER_APP_HOME = "app.home";

    private static Config config = null;

    private ApplicationConfig() {}

    public static String load() {
        // Logger may not be available yet. Returning a log message that can be logged elsewhere
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
        }
        else {
            logMessage = "Config file was already loaded";
        }
        return logMessage;
    }

    public static Config config() {
        return config;
    }

    public static Stage stage() {
        return Stage.get(config.getString(ROOT_PATH + ".stage"));
    }

    public static String appHome() {
        // If the application is launched from appassembler, then 'app.home' property is set from appassembler's startapp script
        // else, the location of this jar is used as appHome
        String appHome = config.hasPath(APPASSEMBLER_APP_HOME)
                ? config.getString(APPASSEMBLER_APP_HOME)
                : FileUtil.jarPath(ApplicationConfig.class).toString();

        return appHome.endsWith("classes")
                ? Paths.get(appHome).getParent().toString()
                : appHome;
    }

    public static String workPath() {
        String workPath = config.hasPath(ROOT_PATH + ".workPath")
                ? config.getString(ROOT_PATH + ".workPath")
                : null;

        // .get(appHome).resolve(workPath) will generate an absolute path from 'workPath' if workPath starts with '/'
        // else, workPath will be joined with appHome()
        workPath = StringUtil.blankToNull(workPath) == null
                ? appHome()
                : Paths.get(appHome()).resolve(workPath).normalize().toAbsolutePath().toString();

        return workPath;
    }

    public static String logPath() {
        String logPath = config.hasPath(ROOT_PATH + ".logPath")
                ? config.getString(ROOT_PATH + ".logPath")
                : null;

        // .get(appHome).resolve(logPath) will generate an absolute path from 'logPath' if logPath starts with '/'
        // else, logPath will be joined with appHome()
        logPath = StringUtil.blankToNull(logPath) == null
                ? workPath()
                : Paths.get(appHome()).resolve(logPath).normalize().toAbsolutePath().toString();

        return logPath;
    }

    public static JettyConfig jettyConfig() {

        Config c = config.getConfig(JETTY_PATH + ".server");
        JettyConfig.ServerConfig serverConfig = new JettyConfig.ServerConfig()
                .accessLogPath(logPath())
                .useAccessLog(c.getBoolean("useAccessLog"));

        c = config.getConfig(JETTY_PATH + ".threadPool");
        JettyConfig.ThreadPoolConfig threadPoolConfig = new JettyConfig.ThreadPoolConfig()
                .minThreads(c.getInt("minThreads"))
                .maxThreads(c.getInt("maxThreads"))
                .daemon(c.getBoolean("daemon"))
                .name(c.getString("name"));

        c = config.getConfig(JETTY_PATH + ".serverConnector");
        JettyConfig.ServerConnectorConfig serverConnectorConfig = new JettyConfig.ServerConnectorConfig()
                .scheme(c.getString("scheme"))
                .host(c.getString("host"))
                .port(c.getInt("port"))
                .idleTimeout(c.getInt("idleTimeout"))
                .shutdownToken(c.getString("shutDownToken"));

        c = config.getConfig(JETTY_PATH + ".webAppContext");
        JettyConfig.WebAppContextConfig webAppContextConfig = new JettyConfig.WebAppContextConfig()
                .contextPath(c.getString("contextPath"))
                .resourceBase(c.getString("resourceBase"))
                .enableDirectoryListing(c.getBoolean("enableDirectoryListing"));

        JettyConfig jettyConfig = new JettyConfig()
                .serverConfig(serverConfig)
                .threadPoolConfig(threadPoolConfig)
                .serverConnectorConfig(serverConnectorConfig)
                .webAppContextConfig(webAppContextConfig);

        return ValidatorHelper.validate(jettyConfig);
    }

    public static PersistenceUnitConfig persistenceUnitConfig() {
        Config c = config.getConfig(PU_PATH);
        Properties p = new Properties();
        for (String s : c.getStringList("properties")) {
            List<String> nameValue = Splitter.on('=').trimResults().splitToList(s);
            if(nameValue.size() > 0) {
                p.put(nameValue.get(0), nameValue.size() > 1 ? nameValue.get(1) : "");
            }
        }

        PersistenceUnitConfig persistenceUnitConfig = new PersistenceUnitConfig()
                .name(c.getString("name"))
                .properties(p);

        return ValidatorHelper.validate(persistenceUnitConfig);
    }


    public static String resourceBaseName() {
        if(isJunitInClassPath()) {
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

    public static void logConfig() {
        // TODO: Improve this
        logger.debug("applicationStartupPath: {}", FileUtil.applicationStartupPath());
        logger.debug("appHome               : {}", appHome());
        logger.debug("workPath              : {}", workPath());
        logger.debug("logPath               : {}", logPath());
    }
}

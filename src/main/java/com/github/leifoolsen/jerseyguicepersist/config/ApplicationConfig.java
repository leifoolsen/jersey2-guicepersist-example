package com.github.leifoolsen.jerseyguicepersist.config;

import com.github.leifoolsen.jerseyguicepersist.util.StringUtil;
import com.github.leifoolsen.jerseyguicepersist.util.ValidatorHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ApplicationConfig {

    enum Stage {
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

    private static Config config;

    private ApplicationConfig() {}

    public static void load(final String resourceBaseName) {
        config = StringUtil.blankToNull(resourceBaseName) == null
                ? ConfigFactory.load()
                : ConfigFactory.load(resourceBaseName).withFallback(ConfigFactory.load());
    }

    public static Config config() {
        return config;
    }

    public static Stage stage() {
        return Stage.get(config.getString(ROOT_PATH + ".stage"));
    }

    public static JettyConfig jettyConfig() {

        Config c = config.getConfig(JETTY_PATH + ".server");
        JettyConfig.ServerConfig serverConfig = new JettyConfig.ServerConfig()
                .accessLogPath(c.getString("accessLogPath"))
                .shutdownToken(c.getString("shutDownToken"));

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
                .idleTimeout(c.getInt("idleTimeout"));

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
}

package com.github.leifoolsen.jerseyguicepersist.main;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.embeddedjetty.JettyFactory;
import com.github.leifoolsen.jerseyguicepersist.util.FileUtil;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private Main() {}

    public static void main(String[] args) throws Exception {

        // Load config
        ApplicationConfig.load(isStartedWithAppassembler() ? "application-prod" : "application-dev");

        logger.debug("applicationStartupPath: {}", FileUtil.applicationStartupPath());
        logger.debug("appHome               : {}", ApplicationConfig.appHome());
        logger.debug("workPath              : {}", ApplicationConfig.workPath());
        logger.debug("logPath               : {}", ApplicationConfig.logPath());

        Server server = JettyFactory.createServer(ApplicationConfig.jettyConfig());
        JettyFactory.startAndWait(server);
    }

    /**
     * The properties "app.home", "app.name", "app.repo", "app.pid" is set in "./appassembler/bin/startapp" script
     * @return true if the current process has been started with appassembler.
     */
    private static boolean isStartedWithAppassembler() {
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

package com.github.leifoolsen.jerseyguicepersist.main;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;
import com.github.leifoolsen.jerseyguicepersist.embeddedjetty.JettyFactory;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private Main() {}

    public static void main(String[] args) throws Exception {

        // Load config
        ApplicationConfigFactory.load();

        // Redirect System.out and System.err to slf4j
        //SysStreamsLogger.bindSystemStreams();

        logger.info("Starting Application with config stage: {}", ApplicationConfigFactory.applicationConfig().stage());
        if (ApplicationConfig.Stage.TEST.equals(ApplicationConfigFactory.applicationConfig().stage())) {
            logger.error("ApplicationConfig.stage() reports Stage.TEST! " +
                    "This could be a config error or the 'org.junit' dependency is defined without scope test in pom.xml");
        }

        Server server = JettyFactory.createServer(ApplicationConfigFactory.applicationConfig().jettyConfig());
        JettyFactory.startAndWait(server);
    }
}

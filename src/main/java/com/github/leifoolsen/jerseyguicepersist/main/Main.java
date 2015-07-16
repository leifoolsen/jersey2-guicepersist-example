package com.github.leifoolsen.jerseyguicepersist.main;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.embeddedjetty.JettyFactory;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private Main() {}

    public static void main(String[] args) throws Exception {

        // Load config
        ApplicationConfig.load();
        //SysStreamsLogger.bindSystemStreams();;

        logger.info("Starting Application with config stage: {}", ApplicationConfig.stage());
        if (ApplicationConfig.Stage.TEST.equals(ApplicationConfig.stage())) {
            logger.error("ApplicationConfig.stage() reports Stage.TEST! " +
                    "This could be a config error or the 'org.junit' dependency is not defined with scope test in pom.xml");
        }

        Server server = JettyFactory.createServer(ApplicationConfig.jettyConfig());
        JettyFactory.startAndWait(server);
    }
}

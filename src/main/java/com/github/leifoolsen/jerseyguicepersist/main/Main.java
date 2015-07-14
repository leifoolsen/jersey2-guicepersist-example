package com.github.leifoolsen.jerseyguicepersist.main;

import com.github.leifoolsen.jerseyguicepersist.embeddedjetty.JettyFactory;
import org.eclipse.jetty.server.Server;

public class Main {

    private Main() {}

    public static void main(String[] args) throws Exception {

        // Load config
        ApplicationConfig.load(isStartedWithAppassembler() ? "application-prod" : "application-dev");
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

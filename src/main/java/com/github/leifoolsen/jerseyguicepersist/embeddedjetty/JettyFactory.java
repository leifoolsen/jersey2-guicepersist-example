package com.github.leifoolsen.jerseyguicepersist.embeddedjetty;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;
import com.github.leifoolsen.jerseyguicepersist.config.JettyConfig;
import com.github.leifoolsen.jerseyguicepersist.util.FileUtil;
import com.github.leifoolsen.jerseyguicepersist.util.SneakyThrow;
import com.github.leifoolsen.jerseyguicepersist.util.ValidatorHelper;
import com.google.common.base.Joiner;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JettyFactory {
    private static final Logger logger = LoggerFactory.getLogger(JettyFactory.class);

    private JettyFactory() {}

    public static Server createServer(final JettyConfig jettyConfig) {
        ValidatorHelper.validate(jettyConfig);

        JettyConfig.ServerConfig serverConfig = jettyConfig.serverConfig();
        JettyConfig.ThreadPoolConfig threadPoolConfig = jettyConfig.threadPoolConfig();
        JettyConfig.ServerConnectorConfig serverConnectorConfig = jettyConfig.serverConnectorConfig();
        JettyConfig.WebAppContextConfig webAppContextConfig = jettyConfig.webAppContextConfig();

        // Create server
        QueuedThreadPool threadPool = new QueuedThreadPool(threadPoolConfig.minThreads(), threadPoolConfig.maxThreads());
        threadPool.setDaemon(threadPoolConfig.daemon());
        if(threadPoolConfig.name() != null) threadPool.setName(threadPoolConfig.name());

        Server server = new Server(threadPool);

        // Configuration classes. This gives support for multiple features.
        // The annotationConfiguration is required to support annotations like @WebServlet
        // See: http://www.eclipse.org/jetty/documentation/current/configuring-webapps.html
        // See: http://www.eclipse.org/jetty/documentation/current/using-annotations-embedded.html
        try {
            Class.forName("org.eclipse.jetty.annotations.AnnotationConfiguration");
            Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
            classlist.addBefore(
                    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",    // Processes a WEB-INF/web.xml file
                    "org.eclipse.jetty.webapp.WebInfConfiguration",         // Extracts war, orders jars and defines classpath
                    "org.eclipse.jetty.annotations.AnnotationConfiguration" // Scan container and web app jars looking for @WebServlet, @WebFilter, @WebListener etc
            );
            logger.info("Annotation processing is enabled.");
        }
        catch (ClassNotFoundException e) {
            logger.info("Annotation processing is not enabled, missing dependency on jetty-annotations.");
        }

        // Connector
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(serverConnectorConfig.host());
        connector.setPort(serverConnectorConfig.port());
        connector.setIdleTimeout(serverConnectorConfig.idleTimeout());
        server.addConnector(connector);


        // Access log
        if(serverConfig.useAccessLog()) {
            Path logPath = Paths.get(serverConfig.accessLogPath());
            if (!Files.isDirectory(logPath)) {
                try {
                    Files.createDirectory(logPath);
                } catch (IOException e) {
                    SneakyThrow.propagate(e);
                }
            }
            NCSARequestLog requestLog = new NCSARequestLog(logPath.resolve(JettyConfig.ServerConfig.ACCESS_LOG_FILE).normalize().toString());
            requestLog.setAppend(true);
            requestLog.setExtended(false);
            requestLog.setLogTimeZone("GMT");
            server.setRequestLog(requestLog);

            logger.info("Access log @ {}", requestLog.getFilename());
        }
        else {
            logger.info("Access logging not configured.");
        }

        // Handlers
        HandlerCollection handlers = new HandlerCollection();

        // WebAppContext
        handlers.addHandler(createWebApp(webAppContextConfig));

        // Shutdown handler
        if(serverConnectorConfig.shutdownToken() != null) {
            handlers.addHandler(new ShutdownHandler(serverConnectorConfig.shutdownToken(), true, false));
            logger.info("Shutdown handler @ " +
                    UriBuilder.fromUri(server.getURI())
                            .port(serverConnectorConfig.port())
                            .path("shutdown")
                            .queryParam("token", "******").build());
        }

        server.setHandler(handlers);
        server.setStopAtShutdown(true);
        server.setStopTimeout(5000);

        return server;
    }


    private static WebAppContext createWebApp(final JettyConfig.WebAppContextConfig webAppContextConfig) {
        // The WebAppContext is the entity that controls the environment in
        // which a web application lives and breathes.
        WebAppContext webapp = new WebAppContext();

        webapp.setContextPath(webAppContextConfig.contextPath());

        webapp.setBaseResource(Resource.newClassPathResource(webAppContextConfig.resourceBase()));


        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        webapp.setParentLoaderPriority(true);

        // fail if the web app does not deploy correctly
        webapp.setThrowUnavailableOnStartupException(true);

        // Directory listing
        webapp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed",
                Boolean.valueOf(webAppContextConfig.enableDirectoryListing()).toString());


        // AnntationConfiguration class scans annotations via its scanForAnnotations(WebAppContext) method.
        // In the method AnnotationConfiguration class scans following path.
        //   container jars
        //   WEB-INF/classes
        //   WEB-INF/libs
        //
        // In exploded mode we also need Jetty to scan the "target/classes" and "target/test-classes" directory for annotations
        final Path classesPath = FileUtil.classesPath();
        final Path testClassesPath = FileUtil.testClassesPath();

        if(classesPath != null) {
            final String path = Joiner.on(";").skipNulls()
                    .join(classesPath.toAbsolutePath(), testClassesPath != null ? testClassesPath.toAbsolutePath() : null);
            logger.info("Running Jetty i exploded mode with extra class path @ {}", path);
            webapp.setExtraClasspath(path);
        }

        return webapp;
    }

    /**
     * Start embedded Jetty server.
     */
    public static void start(final Server server) {
        try {
            server.start();
            //server.dump(System.err);
        }
        catch (Exception e) {
            SneakyThrow.propagate(e);
        }
        logger.info("Jetty started at: " + server.getURI());
    }

    /**
     * Stop embedded Jetty server.
     */
    public static void stop(final Server server) {
        logger.info("Stopping Jetty at: " + server.getURI());
        try {
            server.stop();
            server.join();
        }
        catch (Exception e) {
            SneakyThrow.propagate(e);
        }
        logger.debug("Jetty stopped!");
    }

    /**
     * Start embedded Jetty server and wait until the server is done executing.
     */
    public static void startAndWait(final Server server) {
        start(server);

        JettyConfig.ServerConnectorConfig serverConnectorConfig = ApplicationConfigFactory.applicationConfig().jettyConfig().serverConnectorConfig();

        String s = "\n" +
                ">>>\n" +
                ">>> PRESS ENTER TO STOP JETTY\n";

        if(serverConnectorConfig.shutdownToken() != null) {
            s += ">>> OR POST @ " +
                    UriBuilder.fromUri(server.getURI())
                            .port(serverConnectorConfig.port())
                            .path("shutdown")
                            .queryParam("token", "******").build().toString() + "\n";
        }
        s += ">>>";

        System.out.println(s);

        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        }
        catch (IOException e) {
            // Do nothing
        }
        finally {
            if(server.isRunning()) {
                stop(server);
            }
        }
    }
}

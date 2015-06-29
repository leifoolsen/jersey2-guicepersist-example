package com.github.leifoolsen.jerseyguicepersist.embeddedjetty;

import com.github.leifoolsen.jerseyguicepersist.constraint.AssertMethodAsTrue;
import com.github.leifoolsen.jerseyguicepersist.util.ValidatorHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EmbeddedJettyBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedJettyBuilder.class);

    @NotNull
    @Valid
    private ServerBuilder serverBuilder;

    @NotEmpty
    @Valid
    private List<WebAppContextBuilder> contextBuilders = new ArrayList<>();

    public EmbeddedJettyBuilder() {}

    public EmbeddedJettyBuilder server(final ServerBuilder serverBuilder) {
        this.serverBuilder = serverBuilder;
        return this;
    }

    public EmbeddedJettyBuilder webAppContext(final WebAppContextBuilder webAppContextBuilder) {
        contextBuilders.add(webAppContextBuilder);
        return this;
    }

    public EmbeddedJettyBuilder defaultServer() {
        server(new ServerBuilder());
        webAppContext(new WebAppContextBuilder());
        return this;
    }

    public Server build() {
        ValidatorHelper.validate(this);

        Server server = serverBuilder.build();

        WebAppContextBuilder contextBuilder = contextBuilders.get(0);
        server.setHandler(contextBuilder.build(server));

        //SysStreamsLogger.bindSystemStreams();

        return server;
    }

    @AssertMethodAsTrue(value="validateThreadPoolSize", message="maxThreads must be greater or equal to minThreads")
    public static class ServerBuilder {
        @Min(8)
        private int minThreads = 8;

        @Min(10)
        private int maxThreads = 200;

        public ServerBuilder() {}

        public boolean validateThreadPoolSize() {
            return maxThreads >= minThreads;
        }

        public ServerBuilder minThreads(final int minThreads) {
            this.minThreads = minThreads;
            return this;
        }

        public ServerBuilder maxThreads(final int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        private Server build() {
            // Setup Threadpool
            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setMinThreads(minThreads);
            threadPool.setMaxThreads(maxThreads);

            // Create server
            return new Server(threadPool);
        }

    }

    public static class WebAppContextBuilder {

        @NotBlank
        private String name = "Public";

        @NotBlank
        private String host = "localhost";

        @Min(80)
        @Max(65535)
        private int port = 8080;

        @Min(0)
        private int idleTimeout = 30000;

        private String contextPath = "/";

        private boolean enableDirectoryListing = false;

        public WebAppContextBuilder() {}

        public WebAppContextBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public WebAppContextBuilder host(final String host) {
            this.host = host;
            return this;
        }

        public WebAppContextBuilder port(final int port) {
            this.port = port;
            return this;
        }

        public WebAppContextBuilder idleTimeout(final int idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        private WebAppContext build(final Server server) {

            // Configuration classes. This gives support for multiple features.
            // The annotationConfiguration is required to support annotations like @WebServlet
            // See: http://www.eclipse.org/jetty/documentation/current/configuring-webapps.html
            // See: http://www.eclipse.org/jetty/documentation/current/using-annotations-embedded.html
            Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
            try {
                Class.forName("org.eclipse.jetty.annotations.AnnotationConfiguration");
                classlist.addBefore(
                        "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",    // Processes a WEB-INF/web.xml file
                        "org.eclipse.jetty.webapp.WebInfConfiguration",         // Extracts war, orders jars and defines classpath
                        "org.eclipse.jetty.annotations.AnnotationConfiguration" // Scan container and web app jars looking for @WebServlet, @WebFilter, @WebListener etc
                );
                logger.debug("Annotation processing is enabled.");
            } catch (ClassNotFoundException e) {
                logger.debug("Annotation processing is not enabled, missing dependency on jetty-annotations.");
            }

            // HTTP connector
            ServerConnector http = new ServerConnector(server);
            http.setName(name);
            http.setHost(host);
            http.setPort(port);
            http.setIdleTimeout(idleTimeout);
            server.addConnector(http);

            // The WebAppContext is the entity that controls the environment in
            // which a web application lives and breathes. In this example the
            // context path is being set to "/" so it is suitable for serving root
            // context requests and then we see it setting the location of the war.
            // A whole host of other configurations are available, ranging from
            // configuring to support annotation scanning in the webapp (through
            // PlusConfiguration) to choosing where the webapp will unpack itself.
            WebAppContext webapp = new WebAppContext();

            webapp.setResourceBase("/webapp");

            webapp.setContextPath(contextPath);

            // Parent loader priority is a class loader setting that Jetty accepts.
            // By default Jetty will behave like most web containers in that it will
            // allow your application to replace non-server libraries that are part of the
            // container. Setting parent loader priority to true changes this behavior.
            // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
            webapp.setParentLoaderPriority(true);

            // fail if the web app does not deploy correctly
            webapp.setThrowUnavailableOnStartupException(true);

            // Directory listing
            //webapp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", Boolean.valueOf(enableDirectoryListing).toString());


            // AnntationConfiguration class scans annotations via its scanForAnnotations(WebAppContext) method.
            // In the method AnnotationConfiguration class scans following path.
            //   container jars
            //   WEB-INF/classes
            //   WEB-INF/libs
            //
            // In exploded mode we also need Jetty to scan the "target/classes" directory for annotations
            URL classes = EmbeddedJettyBuilder.class.getProtectionDomain().getCodeSource().getLocation();
            if(classes != null) {
                webapp.setExtraClasspath(classes.getPath());  // TODO: Set path to test-classes if needed
            }

            return webapp;
        }
    }



    /**
     * Start embedded Jetty server.
     * @throws Exception
     */
    public static void start(final Server server) throws Exception {
        logger.debug("Starting Jetty ...");

        server.start();
        //server.dump(System.err);

        logger.info("Jetty started at: " + server.getURI());

    }

    /**
     * Start embedded Jetty server and wait until the server is done executing.
     * @throws Exception
     */
    public static void startAndWait(final Server server) throws Exception {
        start(server);
        try {
            // The use of server.join() will make the current thread join and
            // wait until the server is done executing.
            // See: http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
            server.join();
        }
        finally {
            if(server.isRunning()) {
                stop(server);
            }
        }
    }

    /**
     * Stops embedded Jetty server.
     * @throws Exception
     */
    public static void stop(final Server server) throws Exception {
        logger.info("Stopping Jetty at: " + server.getURI());
        server.stop();
        logger.debug("Jetty stopped!");
    }
}

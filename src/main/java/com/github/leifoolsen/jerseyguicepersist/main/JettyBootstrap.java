package com.github.leifoolsen.jerseyguicepersist.main;

import com.google.common.base.Throwables;
import eu.nets.oss.jetty.ContextPathConfig;
import eu.nets.oss.jetty.EmbeddedJettyBuilder;
import eu.nets.oss.jetty.StaticConfig;
import eu.nets.oss.jetty.StdoutRedirect;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;

public class JettyBootstrap {

    private JettyBootstrap() {}

    public static Server start(final String contextPath, final int port) {
        return JettyBootstrap.start(new StaticConfig(contextPath, port));
    }

    public static Server start(final ContextPathConfig config) {

        // Properties "app.home", "app.name", "app.repo" from "./appassembler/bin/startapp"
        boolean onServer = EmbeddedJettyBuilder.isStartedWithAppassembler();

        final EmbeddedJettyBuilder builder = new EmbeddedJettyBuilder(config, !onServer);

        if (onServer) {
            builder.addHttpAccessLogAtRoot();
        }

        // TODO: Get token from config or as a param
        // TODO: How to do this with eu.nets.oss.jetty.EmbeddedJettyBuilder???
        //builder.addHandlerAtRoot(new HandlerBuilder<Handler>(new ShutdownHandler("foo")));


        EmbeddedJettyBuilder.ServletContextHandlerBuilder<WebAppContext> ctx =
                builder.createRootWebAppContext("", Resource.newClassPathResource("/webapp"));

        WebAppContext handler = ctx.getHandler();

        // AnntationConfiguration class scans annotations via its scanForAnnotations(WebAppContext) method.
        // In the method AnnotationConfiguration class scans following path.
        //   container jars
        //   WEB-INF/classes
        //   WEB-INF/libs
        //
        // In exploded mode we also need Jetty to scan the "target/classes" directory for annotations
        URL classes = JettyBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        if(classes != null) {
            handler.setExtraClasspath(classes.getPath());  // TODO: Set path to test-classes if needed
        }

        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        handler.setParentLoaderPriority(true);

        // fail if the web app does not deploy correctly
        handler.setThrowUnavailableOnStartupException(true);

        // disable directory listing
        handler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        StdoutRedirect.tieSystemOutAndErrToLog();
        System.out.println(">>> Starting Jetty");

        try {
            builder.createServer();
            builder.startJetty();
        }
        catch (Exception e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            Throwables.propagate(e);
        }

        return builder.getServer();
    }

    public static void stop(final Server server) {
        System.out.println(">>> Stopping Jetty");
        try {
            server.stop();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

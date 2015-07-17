package com.github.leifoolsen.jerseyguicepersist.rest.application;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;
import com.github.leifoolsen.jerseyguicepersist.guice.GuiceModule;
import com.github.leifoolsen.jerseyguicepersist.guice.PersistenceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/*")
public class ApplicationModel extends ResourceConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String APPLICATION_PATH;

    static {
        String appPath = "";
        if(ApplicationModel.class.isAnnotationPresent(ApplicationPath.class)) {
            // Remove '/*' from @ApplicationPath, e.g:  "/api/*" -> /api
            appPath = ApplicationModel.class.getAnnotation(ApplicationPath.class).value();
            appPath = appPath.substring(0, appPath.endsWith("/*") ? appPath.lastIndexOf("/*") : appPath.length()-1);
        }
        APPLICATION_PATH = appPath;
    }

    @Inject
    public ApplicationModel(ServiceLocator serviceLocator) {

        logger.debug("Initializing ...");

        bridgeJulToSlf4J();

        guiceHK2Integration(serviceLocator);

        if(ApplicationConfigFactory.applicationConfig().jerseyTraceLogging()) {
            // Enable LoggingFilter & output entity.
            registerInstances(new LoggingFilter(java.util.logging.Logger.getLogger(this.getClass().getName()), true));

            // Enable Tracing support.
            property(ServerProperties.TRACING, "ALL");
        }

        // Invoke startup and shutdown of app
        register(ApplicationLifecycleListener.class);

        // Scans during deployment for JAX-RS components in packages
        packages("com.github.leifoolsen.jerseyguicepersist.rest");

        // ... or register resource classes
        //registerClasses(UserResource.class);
        //registerClasses(UnitOfWorkFilter.class);
        //registerClasses(GenericExceptionMapper.class);
    }

    // make Jersey log through SLF4J
    private static void bridgeJulToSlf4J() {
        // Jersey uses java.util.logging. Bridge jul to slf4j
        java.util.logging.LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getGlobal().setLevel(java.util.logging.Level.FINEST);
        java.util.logging.Logger.getLogger("org.glassfish.jersey").setLevel(java.util.logging.Level.INFO);
    }

    // Guice
    private static void guiceHK2Integration(ServiceLocator serviceLocator) {

        Injector injector = Guice.createInjector(new PersistenceModule(), new GuiceModule());

        // Guice HK2 bridge
        // See e.g. https://github.com/t-tang/jetty-jersey-HK2-Guice-boilerplate
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge bridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        bridge.bridgeGuiceInjector(injector);
    }


    private static class ApplicationLifecycleListener extends AbstractContainerLifecycleListener {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Inject
        PersistService service;

        @Override
        public void onStartup(Container container) {
            logger.info(">>> Application startup");
        }

        @Override
        public void onShutdown(Container container) {
            logger.info(">>> Application shutdown");

            // Stop persistence service
            service.stop();
        }
    }
}

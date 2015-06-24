package com.github.leifoolsen.jerseyguicepersist.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.persist.jpa.JpaPersistModule;

import java.util.Properties;

public class PersistenceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.install(new JpaPersistModule("jpa-example").properties(getPersistenceProperties()));
        binder.bind(PersistenceInitializer.class).asEagerSingleton();
    }

    private static Properties getPersistenceProperties() {
        // TODO: Properties should be injected via @Named
        Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("javax.persistence.jdbc.url", "jdbc:h2:mem:mymemdb");
        properties.put("javax.persistence.jdbc.user", "sa");
        properties.put("javax.persistence.jdbc.password", "");

        // eclipselink.ddl-generation: "create-tables", "create-or-extend-tables", "drop-and-create-tables", "none"
        //                        See: http://eclipse.org/eclipselink/documentation/2.5/jpa/extensions/p_ddl_generation.htm
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables"); //
        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.logging.level", "OFF");  // OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
        properties.put("eclipselink.logging.level.sql", "INFO");
        properties.put("eclipselink.logging.parameters", "true");
        properties.put("eclipselink.logging.timestamp", "true");
        properties.put("eclipselink.logging.session", "true");
        properties.put("eclipselink.logging.thread", "true");
        properties.put("eclipselink.logging.exceptions", "true");

        // EL optimization, see: http://java-persistence-performance.blogspot.no/2011/06/how-to-improve-jpa-performance-by-1825.html
        properties.put("eclipselink.jdbc.cache-statements", "true");
        properties.put("eclipselink.jdbc.batch-writing", "JDBC");
        properties.put("eclipselink.jdbc.batch-writing.size", "1000");
        properties.put("eclipselink.persistence-context.flush-mode", "commit");
        properties.put("eclipselink.persistence-context.close-on-commit", "true");
        properties.put("eclipselink.persistence-context.persist-on-commit", "false");
        properties.put("eclipselink.flush-clear.cache", "drop");
        //properties.put("eclipselink.logging.logger", "JavaLogger");

        // Eclipselink can not load entity classes dynamically.
        // Classes must be added to META-INF/eclipselink-orm.xml by hand :-(
        properties.put("eclipselink.metadata-source", "XML");
        properties.put("eclipselink.metadata-source.xml.file", "META-INF/eclipselink-orm.xml");

        return properties;
    }
}

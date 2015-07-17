package com.github.leifoolsen.jerseyguicepersist.guice;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;
import com.github.leifoolsen.jerseyguicepersist.config.PersistenceUnitConfig;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.persist.jpa.JpaPersistModule;

public class PersistenceModule implements Module {

    @Override
    public void configure(Binder binder) {
        PersistenceUnitConfig puc = ApplicationConfigFactory.applicationConfig().persistenceUnitConfig();
        binder.install(
                new JpaPersistModule(puc.name())
                        .properties(puc.properties())
        );
        binder.bind(PersistenceInitializer.class).asEagerSingleton();
    }
}

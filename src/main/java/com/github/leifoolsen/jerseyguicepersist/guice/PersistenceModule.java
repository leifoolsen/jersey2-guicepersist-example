package com.github.leifoolsen.jerseyguicepersist.guice;

import com.github.leifoolsen.jerseyguicepersist.main.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.main.PersistenceUnitCfg;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.persist.jpa.JpaPersistModule;

public class PersistenceModule implements Module {

    @Override
    public void configure(Binder binder) {
        PersistenceUnitCfg persistenceUnitCfg = ApplicationConfig.persistenceUnitConfig();
        binder.install(
                new JpaPersistModule(persistenceUnitCfg.name())
                        .properties(persistenceUnitCfg.properties())
        );
        binder.bind(PersistenceInitializer.class).asEagerSingleton();
    }
}

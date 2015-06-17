package com.github.leifoolsen.jerseyguicepersist.guice;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.persist.PersistService;

@Singleton
public class PersistenceInitializer {

    @Inject
    public PersistenceInitializer(PersistService service) {
        service.start();
    }
}

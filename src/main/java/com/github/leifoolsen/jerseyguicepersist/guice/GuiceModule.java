package com.github.leifoolsen.jerseyguicepersist.guice;

import com.github.leifoolsen.jerseyguicepersist.repository.UserRepository;
import com.google.inject.Binder;
import com.google.inject.Module;

public class GuiceModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(UserRepository.class);
    }
}

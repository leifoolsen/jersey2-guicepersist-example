package com.github.leifoolsen.jerseyguicepersist.repository;

import com.github.leifoolsen.jerseyguicepersist.domain.User;
import com.github.leifoolsen.jerseyguicepersist.util.StringUtil;
import com.google.common.base.MoreObjects;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class UserRepository {

    private Provider<EntityManager> emProvider;

    @Inject
    public UserRepository(Provider<EntityManager> emProvider) {
        this.emProvider = emProvider;
    }

    @Transactional
    public void persist(final User user) {
        getEntityManager().persist(user);
    }

    public User findById(final String id) {
        return getEntityManager().find(User.class, id);
    }

    public List<User> findByName(final String username) {
        TypedQuery<User> q = getEntityManager()
                .createQuery("select u from User u where u.username like :username", User.class)
                .setParameter("username", MoreObjects.firstNonNull(StringUtil.blankToNull(username), "%"));

        return q.getResultList();
    }

    public EntityManager getEntityManager() {
        return emProvider.get();
    }
}

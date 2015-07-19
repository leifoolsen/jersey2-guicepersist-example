package com.github.leifoolsen.jerseyguicepersist.repository;


import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;
import com.github.leifoolsen.jerseyguicepersist.domain.User;
import com.github.leifoolsen.jerseyguicepersist.guice.GuiceModule;
import com.github.leifoolsen.jerseyguicepersist.guice.PersistenceModule;
import com.github.leifoolsen.jerseyguicepersist.sampledata.SampleDomain;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class UserRepositoryTest {

    private static Injector injector;

    @Inject
    private UnitOfWork unitOfWork;

    @Inject
    private Provider<EntityManager> emProvider;

    private static UserRepository userRepository = null;

    @BeforeClass
    public static void setUp() {
        ApplicationConfigFactory.load();

        assertThat("ApplicationConfig.stage() does NOT report Stage.TEST! " +
                        "This could be a config error, or 'test-classes' is not on class path",
                ApplicationConfigFactory.applicationConfig().stage(), is(ApplicationConfig.Stage.TEST));

        injector = Guice.createInjector(new PersistenceModule(), new GuiceModule());
        userRepository = injector.getInstance(UserRepository.class);
    }

    @AfterClass
    public static void tearDown() {
        PersistService service = injector.getInstance(PersistService.class);
        service.stop();
    }

    @Before
    public void before() {
        if(unitOfWork == null) {
            injector.injectMembers(this);
            assertThat(emProvider, is(notNullValue()));
            assertThat(unitOfWork, is(notNullValue()));
        }
        unitOfWork.begin();
    }
    @After
    public void after() {
        unitOfWork.end();
    }

    @Test
    public void persistUser() {
        User user = SampleDomain.users().get("LEIFO");
        userRepository.persist(user);
        assertThat(userRepository.findById(user.getId()), is(notNullValue()));
    }

    @Test
    public void findUserByName() {
        List<User> users = userRepository.findByName(SampleDomain.SCOTT);
        assertThat(users, hasSize(greaterThan(0)));
    }

    @Test
    public void findAllUsersByName() {
        List<User> users = userRepository.findByName(null);
        assertThat(users, hasSize(greaterThan(2)));
    }

    @Test
    public void testNestedTransactions() {
        EntityManager em = emProvider.get();
        em.getTransaction().begin();

        User u1 = new User("U1", "u1u1", true);
        userRepository.persist(u1);

        User u2 = new User("U2", "u2u2", true);
        userRepository.persist(u2);

        assertThat(userRepository.findById(u1.getId()), is(notNullValue()));
        assertThat(userRepository.findById(u2.getId()), is(notNullValue()));

        assertThat(em.isJoinedToTransaction(), is(true));
        em.getTransaction().rollback();

        assertThat(userRepository.findById(u1.getId()), is(nullValue()));
        assertThat(userRepository.findById(u2.getId()), is(nullValue()));
    }

}

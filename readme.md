#Jersey-2, Guice Persist, Embedded Jetty
A project demonstrating how to configure Google Guice with JPA in a Jersey2 (JAX-RS) container. This project does not 
use the Guice servlet module or the Guice persist filter - which anyway should be regarded as redundant components in a 
stateless JAX-RS container.

Typically, in a servlet environment, Guice is bootstrapped trough a ServletModule, and the HTTP request 
[Unit of Work](https://github.com/google/guice/wiki/Transactions) lifecycle is managed trough a PersistFilter.
Since a servlet filter is unnecessary in JAX-RS, one can use a combined JAX-RS ContainerRequest Response Filter to 
handle Unit of Work. Beyond that, to ensure that each request get it's own thread safe entity manager, 
an ```Provider<EntityManager>``` should be injected rather than injecting the entity manager directly.
 
With the Guice HK2 bridge i place, bootstrapping Guice in pure Java and or a JAX-RS container is no different.
 
## Set up Guice persist with integration tests

### JPA Domain
```java
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class User implements Serializable {
    private static final long serialVersionUID = 3665349089500867570L;

    @Id
    @Column(length=36)
    private String id  = UUID.randomUUID().toString();

    @Version
    private Long version;

    @Column(unique = true)
    private String username;

    private String password;
    private boolean active;

    protected User() {}

    public User(final String username, final String password, final boolean active) {
        this.username = username;
        this.password = password;
        this.active = active;
    }
    public String getId() { 
        return id; 
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() { 
        return password; 
    }
    public boolean isActive() { 
        return active; 
    }
}
```

### Repository
```java
@Singleton
public class UserRepository {
    private Provider<EntityManager> provider;

    @Inject
    public UserRepository(Provider<EntityManager> provider) { 
        this.provider = provider; 
    }
    @Transactional
    public void persist(final User user) { 
        getEntityManager().persist(user); 
    }
    public User find(final String id) { 
        return getEntityManager().find(User.class, id);  
    }
    public List<User> findUserByName(final String username) {
        TypedQuery<User> q = getEntityManager()
                .createQuery("select u from User u where u.username like :username", User.class)
                .setParameter("username", username);
        return q.getResultList();
    }
    public EntityManager getEntityManager() { 
        return provider.get(); 
    }
}
```

### persistence.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.1"
     xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">

    <persistence-unit name="jpa-example" transaction-type="RESOURCE_LOCAL">
        <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
        <exclude-unlisted-classes />
        <properties>
        </properties>
    </persistence-unit>

</persistence>
```

### eclipselink-orm.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-mappings xmlns="http://www.eclipse.org/eclipselink/xsds/persistence/orm"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    version="2.5"
    xsi:schemaLocation="http://www.eclipse.org/eclipselink/xsds/persistence/orm http://www.eclipse.org/eclipselink/xsds/eclipselink_orm_2_5.xsd">

    <entity class="com.github.leifoolsen.jerseyguicepersist.domain.User" />
</entity-mappings>
```
### GuiceModule
```java
public class GuiceModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(UserRepository.class);
    }
}
```

### PersistenceModule
```java
public class PersistenceModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder
          .install(new JpaPersistModule("jpa-example")
          .properties(getPersistenceProperties()));
          
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
```

### PersistenceInitializer
```java
@Singleton
public class PersistenceInitializer {
    @Inject
    public PersistenceInitializer(PersistService service) {
        service.start();
    }
}
```

### Repository Integration Tests
```java
public class UserRepositoryTest {
    private static Injector injector;

    @Inject
    private Provider<EntityManager> provider;

    @Inject
    private UnitOfWork unitOfWork;

    private static UserRepository userRepository = null;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(new PersistenceModule(), new GuiceModule());
        userRepository = injector.getInstance(UserRepository.class);
    }

    @Before
    public void before() {
        if(provider == null) {
            injector.injectMembers(this);
            assertThat(provider, is(notNullValue()));
            assertThat(unitOfWork, is(notNullValue()));
        }
        unitOfWork.begin();
    }
    @After
    public void after() {
        unitOfWork.end();
    }

    @Test
    public void addUser() {
        User user = new User("UserLOL", "lollol", true);
        userRepository.persist(user);
        assertThat(userRepository.find(user.getId()), is(notNullValue()));
    }

    @Test
    public void findUserByName() {
        User user = new User("User#2", "useruser", true);
        userRepository.persist(user);
        List<User> users = userRepository.findUserByName("User%");
        assertThat(users, hasSize(greaterThan(0)));
    }

    @Test
    public void testNestedTransactions() {
        EntityManager em = provider.get();
        em.getTransaction().begin();

        User u1 = new User("U1", "u1u1", true);
        userRepository.persist(u1);

        User u2 = new User("U2", "u2u2", true);
        userRepository.persist(u2);

        assertThat(userRepository.find(u1.getId()), is(notNullValue()));
        assertThat(userRepository.find(u2.getId()), is(notNullValue()));

        assertThat(em.isJoinedToTransaction(), is(true));
        em.getTransaction().rollback();

        assertThat(userRepository.find(u1.getId()), is(nullValue()));
        assertThat(userRepository.find(u2.getId()), is(nullValue()));
    }
}
```

## Set up JAX-RS resource with client api integration tests
TBD...

##Steps to run this project
* Fork, Clone or Download ZIP
* Build project: *mvn clean install -U*
* Start Jetty from project folder: *mvn exec:java*
* Application.wadl: *http://localhost:8080/api/application.wadl*
* Example usage: *http://localhost:8080/api/users*
* Import project into your favourite IDE
* Open `UserResourceTest.java` to start exploring code

###Note
The project can be packaged with the [appassembler-maven-plugin](http://mojo.codehaus.org/appassembler/appassembler-maven-plugin/)

* Build the project with the *appassembler* profile: *mvn install -Pappassembler* 
* ... then run the app from the project folder with the following command:<br/>sh _target/appassembler/bin/startapp_
* Open a browser and hit *http://localhost:8087/api/users*

package com.github.leifoolsen.jerseyguicepersist.rest.api;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;
import com.github.leifoolsen.jerseyguicepersist.domain.User;
import com.github.leifoolsen.jerseyguicepersist.embeddedjetty.JettyFactory;
import com.github.leifoolsen.jerseyguicepersist.rest.application.ApplicationModel;
import com.github.leifoolsen.jerseyguicepersist.sampledata.SampleDomain;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class UserResourceTest {
    private static Server server;
    private static WebTarget target;

    @BeforeClass
    public static void setUp() throws Exception {
        // Config
        ApplicationConfigFactory.load();

        assertThat("ApplicationConfig.stage() does NOT report Stage.TEST! " +
                        "This could be a config error, or 'test-classes' is not on class path",
                ApplicationConfigFactory.applicationConfig().stage(), is(ApplicationConfig.Stage.TEST));

        // Start the server
        server = JettyFactory.createServer(ApplicationConfigFactory.applicationConfig().jettyConfig());
        JettyFactory.start(server);
        assertThat(server.isRunning(), is(true));

        // create the client
        Client c = ClientBuilder.newClient();
        target = c.target(server.getURI()).path(ApplicationModel.APPLICATION_PATH);

        // Client interceptor to deflate GZIP'ed content on client side
        //c.register(GZIPReaderInterceptor.class);

        User alice = SampleDomain.users().get(SampleDomain.ALICE);
        target.path(UserResource.RESOURCE_PATH)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(alice, MediaType.APPLICATION_JSON_TYPE));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        JettyFactory.stop(server);
    }

    @Test
    public void shouldCreateNewUser() {
        User pluto = SampleDomain.users().get(SampleDomain.PLUTO);
        final Response response = target.path(UserResource.RESOURCE_PATH)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(pluto, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
    }

    @Test
    public void shouldFindUserByGivenId() {
        String id = SampleDomain.users().get(SampleDomain.ALICE).getId();
        final Response response = target
                .path("users")
                .path(id)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));

        User u = response.readEntity(User.class);
        assertNotNull(u);
        assertThat(u.getId(), equalTo(id));
    }

    @Test
    public void shouldFindUsersByName() {
        final Response response = target
                .path("users")
                .queryParam("user", "S%")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));

        List<User> users = response.readEntity(new GenericType<List<User>>() {});
        assertThat(users, hasSize(greaterThan(1)));
    }

    @Test
    public void unhandeledExceptionShouldReturn_INTERNAL_SERVER_ERROR() {
        final Response response = target
                .path("users")
                .path("test-unsupported-exception")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    public void getApplicationWadl() throws Exception {
        final Response response = target
                .path("application.wadl")
                .request(MediaType.APPLICATION_XML)
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        String wadl = response.readEntity(String.class);
        assertThat(wadl.length(), greaterThan(0));
    }
}

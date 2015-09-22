package com.github.leifoolsen.jerseyguicepersist.rest.api;

import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfig;
import com.github.leifoolsen.jerseyguicepersist.config.ApplicationConfigFactory;
import com.github.leifoolsen.jerseyguicepersist.embeddedjetty.JettyFactory;
import com.github.leifoolsen.jerseyguicepersist.rest.application.ApplicationModel;
import com.github.leifoolsen.jerseyguicepersist.rest.interceptor.GZIPReaderInterceptor;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ExceptionMapperTest {

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

        // Client interceptor to deflate GZIP'ed content on client side
        c.register(GZIPReaderInterceptor.class);

        target = c.target(server.getURI()).path(ApplicationModel.APPLICATION_PATH);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        JettyFactory.stop(server);
    }

    @Test
    public void unhandeledExceptionShouldReturn_INTERNAL_SERVER_ERROR() {
        final Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("unhandled-exception-internal-server-error")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));

        final String errorMessage = response.readEntity(String.class);
        assertThat(errorMessage, containsString("UnsupportedOperationException"));
    }

    @Test
    public void nullResultShuldReturn_BAD_REQUEST() {
        final Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("null-result-bad-request")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));

        final String errorMessage = response.readEntity(String.class);
        assertThat(errorMessage, containsString("constraintViolationMessages"));
    }

    @Test
    public void pathParamValidationExceptionShuldReturn_BAD_REQUEST() {
        Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("path-param-validation-bad-request")
                .path("xxxx")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));

        String errorMessage = response.readEntity(String.class);
        assertThat(errorMessage, containsString("constraintViolationMessages"));


        response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("path-param-validation-bad-request")
                .path("123")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));

        errorMessage = response.readEntity(String.class);
        assertThat(errorMessage, containsString("constraintViolationMessages"));
    }

    @Test
    public void webApplicationExceptionShouldReturnAppdefinedResponseCode() {
        final Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("web-application-exception-with-appdefined-response-code")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.EXPECTATION_FAILED.getStatusCode()));

        final String errorMessage = response.readEntity(String.class);
        assertThat(errorMessage, containsString("WebApplicationException"));
    }

    @Test
    public void beanValidationExceptionShouldReturn_BAD_REQUEST() {
        final Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("bean-validation-bad-request")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));

        final String errorMessage = response.readEntity(String.class);
        assertThat(errorMessage, containsString("constraintViolationMessages"));
    }

    @Test
    public void beanValidationHelperExceptionShouldReturn_BAD_REQUEST() {
        final Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("bean-validation-helper-bad-request")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));

        final String errorMessage = response.readEntity(String.class);
        assertThat(errorMessage, containsString("constraintViolationMessages"));
    }

    @Test
    public void entityNotFoundExceptionShouldReturn_NOT_FOUND() {
        final Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("entity-not-found-exception")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void entityExistsExceptionShouldReturn_CONFLICT() {
        final Response response = target
                .path(ExceptionMapperTestResource.RESOURCE_PATH)
                .path("entity-exists-exception")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.getStatus(), equalTo(Response.Status.CONFLICT.getStatusCode()));
    }
}

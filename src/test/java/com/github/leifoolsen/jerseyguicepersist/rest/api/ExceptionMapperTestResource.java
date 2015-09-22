package com.github.leifoolsen.jerseyguicepersist.rest.api;

import com.github.leifoolsen.jerseyguicepersist.util.ValidatorHelper;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Singleton
@Path("exceptionmapper-test-resource")
@Produces(MediaType.APPLICATION_JSON)
public class ExceptionMapperTestResource {

    public static final String RESOURCE_PATH;
    static {
        RESOURCE_PATH = ExceptionMapperTestResource.class.isAnnotationPresent(Path.class)
                ? ExceptionMapperTestResource.class.getAnnotation(Path.class).value() : "";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private UriInfo uriInfo;

    public ExceptionMapperTestResource(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        logger.debug(this.getClass().getSimpleName() + " created");
    }

    @GET
    @Path("unhandled-exception-internal-server-error")
    public Object unhandledExceptionWillReturn_INTERNAL_SERVER_ERROR() {
        // The GenericExceptionMapper class should catch this exception and return
        // a Response.Status.INTERNAL_SERVER_ERROR status to the client
        throw new UnsupportedOperationException("UNSUPPORTED!!!");
    }

    @GET
    @Path("null-result-bad-request")
    @NotNull(message="Return value NULL not allowed")
    public Object nullResultWillReturn_BAD_REQUEST() {
        return null;
    }

    @GET
    @Path("path-param-validation-bad-request/{foo}")
    public Object pathParamValidationExceptionWillReturn_BAD_REQUEST(
            @PathParam("foo")
            @NotNull
            @Size(min = 4, max = 4)
            @Pattern(regexp = "\\d+", message = "The foo param must be a valid number")
            final String foo) {

        return null;
    }

    @GET
    @Path("web-application-exception-with-appdefined-response-code")
    public Object willReturnWebApplicationExcception() {
        throw new WebApplicationException(
                Response.status(Response.Status.EXPECTATION_FAILED)
                        .location(uriInfo.getAbsolutePath())
                        .build()
        );
    }

    @GET
    @Path("bean-validation-bad-request")
    @Valid // Trigger bean validation on return
    public ABeanValidatedBean beanValidationExceptionWillReturn_BAD_REQUEST() {
        return new ABeanValidatedBean(null, "ABC");
    }

    @GET
    @Path("bean-validation-helper-bad-request")
    public ABeanValidatedBean beanValidationHelperExceptionWillReturn_BAD_REQUEST() {
        ABeanValidatedBean bean = new ABeanValidatedBean(null, "ABC");
        ValidatorHelper.validate(bean);
        return bean;
    }


    @GET
    @Path("entity-not-found-exception")
    public Object entityNotFoundExceptionWillReturn_NOT_FOUND() {
        // The GenericExceptionMapper class should catch this exception and return
        // a Response.Status.NOT_FOUND status to the client
        throw new EntityNotFoundException("ENTITY NOT FOUND!!");
    }

    @GET
    @Path("entity-exists-exception")
    public Object entityExistsExceptionWillReturn_CONFLICT() {
        // The GenericExceptionMapper class should catch this exception and return
        // a Response.Status.CONFLICT status to the client
        throw new EntityExistsException("ENTITY EXISTS!!");
    }


    public static class ABeanValidatedBean {

        @NotNull
        Integer anInt;

        @NotBlank
        @Size(min = 4, max = 4)
        String aString;

        public ABeanValidatedBean(final Integer i, final String s) {
            anInt = i;
            aString = s;
        }
    }

}

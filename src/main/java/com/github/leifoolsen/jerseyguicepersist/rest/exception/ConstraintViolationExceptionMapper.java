package com.github.leifoolsen.jerseyguicepersist.rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
//@Priority(Priorities.USER)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UriInfo uriInfo; // actual uri info provided by parent resource (threadsafe)

    public ConstraintViolationExceptionMapper(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        ErrorMessage errorMessage = ErrorMessage.with(exception, uriInfo).build();

        logger.debug(errorMessage.toString());

        return Response
                .status(errorMessage.getResponseStatusCode())
                .entity(errorMessage)
                .location(uriInfo.getRequestUri()) // uriInfo.getAbsolutePath()
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

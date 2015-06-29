package com.github.leifoolsen.jerseyguicepersist.rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
//@Priority(Priorities.USER)
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UriInfo uriInfo; // actual uri info provided by parent resource (threadsafe)

    public GenericExceptionMapper(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        logger.debug(this.getClass().getSimpleName() + " provider created");
    }

    @Override
    public Response toResponse(Throwable t) {
        logger.error("Unhandeled exception: {}", t.toString());

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unhandeled exception: " + t.toString())
                .location(uriInfo.getRequestUri()) // uriInfo.getAbsolutePath()
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}



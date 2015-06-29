package com.github.leifoolsen.jerseyguicepersist.rest.filter;

import com.google.inject.persist.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class UnitOfWorkFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UnitOfWork unitOfWork;

    @Inject
    public UnitOfWorkFilter(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        logger.debug(this.getClass().getSimpleName() + " provider created");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        //logger.debug(">>> Request  filter");
        unitOfWork.begin();
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        //logger.debug("<<< Response filter");
        unitOfWork.end();
    }
}
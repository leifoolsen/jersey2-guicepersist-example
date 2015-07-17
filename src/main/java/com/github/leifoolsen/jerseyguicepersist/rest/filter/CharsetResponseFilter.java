package com.github.leifoolsen.jerseyguicepersist.rest.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

// See: https://jersey.java.net/documentation/latest/user-guide.html#filters-and-interceptors
// See: http://stackoverflow.com/questions/5514087/jersey-rest-default-character-encoding/20569571

@Provider
public class CharsetResponseFilter implements ContainerResponseFilter {

    //private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        //logger.debug("CharsetResponseFilter.filter");
        MediaType type = response.getMediaType();
        if (type != null) {
            if (!type.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
                MediaType typeWithCharset = type.withCharset("utf-8");
                response.getHeaders().putSingle("Content-Type", typeWithCharset);
            }
        }
    }
}
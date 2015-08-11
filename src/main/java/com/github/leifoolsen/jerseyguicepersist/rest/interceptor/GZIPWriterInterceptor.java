package com.github.leifoolsen.jerseyguicepersist.rest.interceptor;


import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * A writer interceptor that enables GZIP compression of the whole entity body.
 * See: https://jersey.java.net/documentation/latest/user-guide.html#filters-and-interceptors
 * See: http://www.codingpedia.org/ama/how-to-compress-responses-in-java-rest-api-with-gzip-and-jersey/
 */

@Provider  // => Automatically discovered by the JAX-RS runtime during a provider scanning phase.

//@Compress
public class GZIPWriterInterceptor implements WriterInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HttpHeaders httpHeaders;

    public GZIPWriterInterceptor(@Context @NotNull HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

        MultivaluedMap<String,String> requestHeaders =  httpHeaders.getRequestHeaders();
        List<String> acceptEncoding = MoreObjects.firstNonNull(
                requestHeaders.get(HttpHeaders.ACCEPT_ENCODING), new ArrayList<String>());

        // Compress if client accepts gzip encoding
        for (String s : acceptEncoding) {
            if(s.contains("gzip")) {
                logger.debug("GZIP'ing response");

                MultivaluedMap<String, Object> headers = context.getHeaders();
                headers.add(HttpHeaders.CONTENT_ENCODING, "gzip"); //com.google.common.net.MediaType.GZIP);

                final OutputStream outputStream = context.getOutputStream();
                context.setOutputStream(new GZIPOutputStream(outputStream));

                break;
            }
        }
        context.proceed();
    }
}

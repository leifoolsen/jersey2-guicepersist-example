package com.github.leifoolsen.jerseyguicepersist.rest.interceptor;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Client interceptor to deflate GZIP'ed content
 * See: https://jersey.java.net/documentation/latest/user-guide.html#filters-and-interceptors
 * See: http://www.codingpedia.org/ama/how-to-compress-responses-in-java-rest-api-with-gzip-and-jersey/
 */

public class GZIPReaderInterceptor implements ReaderInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {

        MultivaluedMap<String,String> headers = context.getHeaders();

        List<String> contentEncoding = MoreObjects.firstNonNull(
                headers.get(HttpHeaders.CONTENT_ENCODING), new ArrayList<String>());

        for (String s : contentEncoding) {
            if(s.contains("gzip")) {
                logger.debug("Decompressing GZIP");

                final InputStream originalInputStream = context.getInputStream();
                context.setInputStream(new GZIPInputStream(originalInputStream));

                break;
            }
        }
        return context.proceed();
    }
}

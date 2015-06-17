package com.github.leifoolsen.jerseyguicepersist.rest.api;

import com.github.leifoolsen.jerseyguicepersist.domain.User;
import com.github.leifoolsen.jerseyguicepersist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    public static final String RESOURCE_PATH;

    static {
        RESOURCE_PATH = UserResource.class.isAnnotationPresent(Path.class)
                ? UserResource.class.getAnnotation(Path.class).value() : "";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UserRepository userRepository;

    @Inject
    public UserResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(final User user) {
        userRepository.persist(user);
    }

    @GET
    @Path("{id}")
    public User find(@PathParam("id") final String id) {

        //throw new UnsupportedOperationException("UNSUPPORTED!!!");
        return userRepository.find(id);
    }

    @GET
    @Path("unsupported-exception")
    public Object unsupportedException() {
        // The GenericExceptionMapper class should catch this exception and return
        // a Response.Status.INTERNAL_SERVER_ERROR status to the client
        throw new UnsupportedOperationException("UNSUPPORTED!!!");
    }
}

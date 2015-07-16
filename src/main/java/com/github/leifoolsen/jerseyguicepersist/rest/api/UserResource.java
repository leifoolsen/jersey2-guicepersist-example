package com.github.leifoolsen.jerseyguicepersist.rest.api;

import com.github.leifoolsen.jerseyguicepersist.domain.User;
import com.github.leifoolsen.jerseyguicepersist.repository.UserRepository;
import com.google.common.base.MoreObjects;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

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

    private UriInfo uriInfo;
    private UserRepository userRepository;

    @Inject // @Inject injects UserRepository from Guice container. @Context injects from REST container
    public UserResource(UserRepository userRepository, @Context UriInfo uriInfo) {
        this.userRepository = userRepository;
        this.uriInfo = uriInfo;
        logger.debug(this.getClass().getSimpleName() + " created");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(final User user) {
        userRepository.persist(user);
    }

    @GET
    public Response allUsers(@QueryParam("user") final String user) {

        List<User> users = userRepository.findByName(MoreObjects.firstNonNull(user, "") + "%");
        if(users.size()< 1) {
            return Response
                    .noContent()
                    .location(uriInfo.getRequestUri())
                    .build();
        }

        GenericEntity<List<User>> entities = new GenericEntity<List<User>>(users){};
        return Response
                .ok(entities)
                .location(uriInfo.getRequestUri())
                .build();
    }

    @GET
    @Path("{id}")
    public User findById(@PathParam("id") final String id) {
        return userRepository.findById(id);
    }

    @GET
    @Path("test-unsupported-exception")
    public Object unsupportedException() {
        // The GenericExceptionMapper class should catch this exception and return
        // a Response.Status.INTERNAL_SERVER_ERROR status to the client
        throw new UnsupportedOperationException("UNSUPPORTED!!!");
    }
}

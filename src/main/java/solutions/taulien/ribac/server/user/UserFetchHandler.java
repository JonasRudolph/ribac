package solutions.taulien.ribac.server.user;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.apache.http.HttpStatus;
import solutions.taulien.ribac.server.ReadOrCreateRequestIdHandler;
import solutions.taulien.ribac.server.Responder;
import solutions.taulien.ribac.server.error.ResourceNotFoundError;

public class UserFetchHandler implements Handler<RoutingContext> {

    private final UserRepository userRepository;

    private final Responder responder;



    @Inject
    public UserFetchHandler(UserRepository userRepository, Responder responder) {
        this.userRepository = userRepository;
        this.responder = responder;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var externalUserId = ctx.pathParam("userId");

        final String requestId = ctx.get(ReadOrCreateRequestIdHandler.REQUEST_ID_KEY);
        this.userRepository
            .getUser(externalUserId, requestId)
            .subscribe(
                requestedUser -> this.responder
                                     .ok(
                                         ctx,
                                         new JsonObject()
                                             .put(
                                                 "requestedUser", new JsonObject().put(
                                                     "id", requestedUser.getExternalId()
                                                 )
                                             )
                                     ),
                failure -> ctx.fail(
                    (failure instanceof ResourceNotFoundError)
                        ? ((ResourceNotFoundError) failure).toHttpError(HttpStatus.SC_NOT_FOUND)
                        : failure
                )
            );
    }
}

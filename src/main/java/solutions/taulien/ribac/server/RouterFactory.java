package solutions.taulien.ribac.server;

import solutions.taulien.ribac.server.error.HttpErrorHandler;
import solutions.taulien.ribac.server.error.InternalServerErrorFailureHandler;
import solutions.taulien.ribac.server.error.OpenApiValidationFailureHandler;
import solutions.taulien.ribac.server.user.UserCreateHandler;
import solutions.taulien.ribac.server.user.UserFetchHandler;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.reactivex.ext.web.handler.CorsHandler;

import javax.inject.Inject;

public class RouterFactory {

    private final OpenAPI3RouterFactory openAPI3RouterFactory;

    private final InternalServerErrorFailureHandler internalServerErrorFailureHandler;

    private final OpenApiValidationFailureHandler validationFailureHandler;

    private final HttpErrorHandler httpErrorHandler;

    private final CorsHandler corsHandler;

    private final UserCreateHandler userCreateHandler;

    private final UserFetchHandler userFetchHandler;

    private final ReadOrCreateRequestIdHandler readOrCreateRequestIdHandler;

    private final LogRequestStartHandler logRequestStartHandler;



    @Inject
    public RouterFactory(
        OpenAPI3RouterFactory openAPI3RouterFactory,
        UserCreateHandler userCreateHandler,
        InternalServerErrorFailureHandler internalServerErrorFailureHandler,
        OpenApiValidationFailureHandler validationFailureHandler,
        HttpErrorHandler httpErrorHandler,
        CorsHandler corsHandler,
        UserFetchHandler userFetchHandler,
        ReadOrCreateRequestIdHandler readOrCreateRequestIdHandler,
        LogRequestStartHandler logRequestStartHandler
    ) {
        this.openAPI3RouterFactory = openAPI3RouterFactory;
        this.userCreateHandler = userCreateHandler;
        this.internalServerErrorFailureHandler = internalServerErrorFailureHandler;
        this.validationFailureHandler = validationFailureHandler;
        this.httpErrorHandler = httpErrorHandler;
        this.corsHandler = corsHandler;
        this.userFetchHandler = userFetchHandler;
        this.readOrCreateRequestIdHandler = readOrCreateRequestIdHandler;
        this.logRequestStartHandler = logRequestStartHandler;
    }



    public Router create() {
        this.openAPI3RouterFactory.addGlobalHandler(this.corsHandler)
                                  .addGlobalHandler(this.readOrCreateRequestIdHandler)
                                  .addGlobalHandler(this.logRequestStartHandler);

        this.addHandler("userCreate", this.userCreateHandler);
        this.addHandler("userFetch", this.userFetchHandler);

        return this.openAPI3RouterFactory.getRouter()
                                         .errorHandler(400, this.validationFailureHandler)
                                         .errorHandler(500, this.internalServerErrorFailureHandler);
    }



    private void addHandler(String operationId, Handler<RoutingContext> handler) {
        this.openAPI3RouterFactory.addHandlerByOperationId(operationId, handler);
        this.openAPI3RouterFactory.addFailureHandlerByOperationId(operationId, this.httpErrorHandler);
    }
}
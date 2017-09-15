package io.abner.vertx.postgres.handlers;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava.ext.sql.SQLConnection;
import rx.Single;
import rx.SingleSubscriber;

public class SetupDbConnectionHandler implements Handler<AsyncResult<SQLConnection>> {

    private SQLConnection connection;
    private boolean handleHasOccurred = false;
    private Throwable errorOnGetConnection = null;
    private SingleSubscriber whenReadyEmitter;
    private Single<SQLConnection> whenReady;

    public SetupDbConnectionHandler() {
        this.whenReady = Single.<SQLConnection>create(e -> {
            this.whenReadyEmitter = e;
        });
    }

    @Override
    public void handle(AsyncResult<SQLConnection> event) {
        handleHasOccurred = true;
        if (event.succeeded()) {
            this.connection = event.result();
            this.whenReadyEmitter.onSuccess(this.connection);
        } else {
            this.whenReadyEmitter.onError(event.cause());
        }
    }

    public Single<SQLConnection> whenReady() {
        return this.whenReady;
    }
}

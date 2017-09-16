package io.abner.vertx.postgres.services;

import io.abner.vertx.postgres.handlers.SetupDbConnectionHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.ext.sql.SQLClient;
import io.vertx.rxjava.ext.sql.SQLConnection;
import rx.Single;

public abstract class DatabaseOperationHandler<T> {

    SetupDbConnectionHandler connectionHandler = new SetupDbConnectionHandler();
    private SQLClient sqlClient;
    private SQLConnection connection;

    public DatabaseOperationHandler(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    public Single<DatabaseOperationHandler> init() {
        this.sqlClient.getConnection(connectionHandler);
        return this.connectionHandler.whenReady()
                /*.doOnUnsubscribe(() -> {
                    this.connection.rxClose().subscribe();
                })*/
                .map(conn -> {
                    this.connection = conn;
                    return this;
                });
    }


    public Single<Object> execute() {
        return this.connection.rxQuery(this.getSql()).map(this::mapResult);
    }

    public Single<ResultSet> executeSql(String sql, JsonArray params) {
        return this.connection.rxQueryWithParams(sql, params);
    }

    protected abstract void doOnNext(T r);

    protected abstract void doOnError(Throwable e);

    protected abstract Object mapResult(ResultSet rs);

    protected abstract String getSql();
}

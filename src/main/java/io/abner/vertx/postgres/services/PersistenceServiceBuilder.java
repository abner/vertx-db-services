package io.abner.vertx.postgres.services;

import io.abner.vertx.postgres.services.interfaces.ExecuteDatabaseOperation;
import io.vertx.rxjava.ext.sql.SQLClient;
import io.vertx.rxjava.ext.sql.SQLConnection;
import rx.Single;

public class PersistenceServiceBuilder {

    private static PersistenceServiceBuilder INSTANCE;
    SQLClient dbClient;

    private PersistenceServiceBuilder(SQLClient dbClient) {
        this.dbClient = dbClient;
    }

    static PersistenceServiceBuilder setup(SQLClient client) {
        if (INSTANCE == null) {
            INSTANCE = new PersistenceServiceBuilder(client);
        } else {
            INSTANCE.dbClient = client;
        }
        return INSTANCE;
    }

    public static <T extends PersistenceService> void build(Class<T> type, ExecuteDatabaseOperation<T> execute) {
        INSTANCE._prepare(true, type, execute);
    }

    public static <T extends PersistenceService> void build(boolean autoCommit, Class<T> type, ExecuteDatabaseOperation<T> execute) {
        INSTANCE._prepare(autoCommit, type, execute);
    }


    public <T extends PersistenceService> void _prepare(Class<T> type, ExecuteDatabaseOperation<T> execute) {
        this._prepare(true, type, execute);
    }

    public <T extends PersistenceService> void _prepare(boolean autoCommit, Class<T> type, ExecuteDatabaseOperation<T> executeDbOperation) {
        this.dbClient.rxGetConnection().flatMap(conn ->
                conn.rxSetAutoCommit(autoCommit)
                        .flatMap(Void -> {
                            try {
                                T service = this.buildService(conn, type);
                                return executeDbOperation.execute(service).doOnError(e -> service.rollbackAndDispose()).doAfterTerminate(() -> {
                                    service.commitAndDispose().subscribe();
                                }).doOnError(e -> {
                                    service.rollbackAndDispose().subscribe();
                                });
                            } catch(Exception e) {
                               return Single.error(e);
                            }
                        })
        ).subscribe();
    }

    protected  <T extends PersistenceService> T buildService(SQLConnection connection, Class<T> type) throws Exception {
        return type.getConstructor(SQLConnection.class).newInstance(connection);
    }

}

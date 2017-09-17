package io.abner.vertx.postgres.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.abner.vertx.postgres.services.interfaces.ExecuteDatabaseErrorHandler;
import io.abner.vertx.postgres.services.interfaces.ExecuteDatabaseOperation;
import io.vertx.rxjava.ext.sql.SQLClient;
import io.vertx.rxjava.ext.sql.SQLConnection;
import rx.Single;

public class PersistenceServiceBuilder {
	
	private static Logger LOGGER = LoggerFactory.getLogger(PersistenceServiceBuilder.class);

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

    public <T extends PersistenceService<?>> void build(Class<T> type, ExecuteDatabaseOperation<T> execute, ExecuteDatabaseErrorHandler errorHandler) {
        this._prepare(true, type, execute, errorHandler);
    }

    public <T extends PersistenceService<?>> void build(boolean autoCommit, Class<T> type, ExecuteDatabaseOperation<T> execute, ExecuteDatabaseErrorHandler errorHandler) {
    	this._prepare(autoCommit, type, execute, errorHandler);
    }


    public <T extends PersistenceService<?>> void _prepare(Class<T> type, ExecuteDatabaseOperation<T> execute, ExecuteDatabaseErrorHandler errorHandler) {
        this._prepare(true, type, execute, errorHandler);
    }

    public <T extends PersistenceService<?>> void _prepare(boolean autoCommit, Class<T> type, ExecuteDatabaseOperation<T> executeDbOperation, ExecuteDatabaseErrorHandler errorHandler) {
    	LOGGER.info("AUTOCOMMIT: " + autoCommit);
        this.dbClient.rxGetConnection()
        	.doOnError(e -> {
        		if (errorHandler != null) {
        			errorHandler.handle(new Exception("Failed to get the connection!", e));
        		}
        	})
        	.flatMap(conn ->
                conn.rxSetAutoCommit(autoCommit)
                        .flatMap(Void -> {
                            try {
                                T service = this.buildService(conn, autoCommit, type);
                                return executeDbOperation.execute(service).doOnError(e -> 
                                { 
                                	System.err.println("ERROR 1 -> " + e.getMessage());
                                	service.rollbackAndDispose().subscribe((r) -> {}, e2 -> {
                                		System.err.println("ERROR 2 -> " + e2.getMessage());
                                		Single.error(e2);
                                	});
                                }).doOnSuccess((res) -> {
                                    service.commitAndDispose().subscribe();
                                }).doOnError(e -> {
                                    service.rollbackAndDispose().subscribe();
                                });
                            } catch(Exception e) {
                              if (errorHandler != null) {
                            	  errorHandler.handle(e);
                              }
                              return Single.error(e);
                            }
                        })
        ).subscribe((r) -> {}, e -> {
        	LOGGER.error("Failed to execute persistence service execute block", e);
        });
    }

    protected  <T extends PersistenceService<?>> T buildService(SQLConnection connection, boolean autoCommit, Class<T> type) throws Exception {
        return type.getConstructor(SQLConnection.class, Boolean.class).newInstance(connection, autoCommit);
    }

}

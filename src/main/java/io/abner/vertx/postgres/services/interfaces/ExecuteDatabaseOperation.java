package io.abner.vertx.postgres.services.interfaces;

import io.abner.vertx.postgres.services.PersistenceService;
import rx.Single;

public interface ExecuteDatabaseOperation<T extends PersistenceService> {
    Single execute(T service);
}

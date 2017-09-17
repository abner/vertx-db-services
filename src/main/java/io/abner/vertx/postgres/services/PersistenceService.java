package io.abner.vertx.postgres.services;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;
import io.abner.vertx.postgres.services.interfaces.ExecuteDatabaseOperation;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.rxjava.ext.sql.SQLConnection;
import rx.Single;

import java.util.List;
import java.util.stream.Collectors;

public abstract class PersistenceService<T> {

    public abstract String getTableName();
    public abstract Class<T> getModelType();

    private SQLConnection conn;
    private boolean autoCommit = true;
    protected UUIDGenerator uuidGenerator = new UUIDGenerator();

    public PersistenceService(SQLConnection conn) {
        this(conn, true);
    }

    public PersistenceService(SQLConnection conn, boolean autoCommit) {
        this.conn = conn;
    }

    public Single<List<JsonObject> > queryAndGetAllRowsAndColumns(String sql) {
        return this.query(sql).map(this::mapAllRowsAndColumns);
    }

    public Single<List<JsonObject> > queryAndGetAllRowsAndColumns(String sql, JsonArray params) {
        return this.queryWithParams(sql, params).map(this::mapAllRowsAndColumns);
    }

    public Single<List<T>> getAll() {
        return this.query("Select * from " + this.getTableName()).map(resultSet -> {
           return resultSet.getRows().stream().map(obj -> {
               return obj.mapTo(this.getModelType());
           }).collect(Collectors.toList());
        });
    }


    protected Single<ResultSet> query(String sql) {
        return this.conn.rxQuery(sql);
    }

    protected Single<ResultSet> queryWithParams(String sql, JsonArray params) {
        return this.conn.rxQueryWithParams(sql, params);
    }

    protected Single<UpdateResult> executeUpdate(String sql) {
        return this.conn.rxUpdate(sql);
    }

    protected Single<UpdateResult> executeUpdateWithParams(String sql, JsonArray params) {
        return this.conn.rxUpdateWithParams(sql, params);
    }

    protected <K> K mapResultSingleResponse(ResultSet resultSet) {
        return (K)resultSet.getResults().get(0).getValue(0);
    }


    protected JsonObject mapResultJsonObject(ResultSet resultSet) {
        return this.mapResultJsonObject(resultSet, 0);
    }

    protected JsonObject mapResultJsonObject(ResultSet resultSet, int fieldPosition) {
        return resultSet.getResults().get(0).getJsonObject(fieldPosition);
    }

    protected List<JsonObject> mapAllRowsAndColumns(ResultSet resultSet) {
        return resultSet.getRows();
    }

    public Single<Void> commitAndDispose() {
        return this.autoCommit
                ? this.conn.rxClose()
                : this.conn.rxCommit().flatMap((Void) -> this.conn.rxClose());
    }

    public Single<Void> rollbackAndDispose() {
        return this.autoCommit
                ? Single.error(new Exception("AutoCommit is on. Cannot rollback operations!"))
                : this.conn.rxRollback().flatMap((Void) -> this.conn.rxClose());
    }

    protected String getUUIDFor(Object obj) {
        return this.uuidGenerator.generateId(obj).toString();
    }

}

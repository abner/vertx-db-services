package io.abner.vertx.postgres.services;

import io.vertx.rxjava.ext.sql.SQLClient;



public class SomeDatabaseOperationHandler extends DatabaseOperationHandler<Integer> {

    public SomeDatabaseOperationHandler(SQLClient sqlClient) {
        super(sqlClient);
    }

    @Override
    protected void doOnNext(Integer r) {
        System.out.println(r.toString());
    }

    @Override
    protected void doOnError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    protected Object mapResult(io.vertx.ext.sql.ResultSet rs) {
        return (Object)Integer.valueOf(rs.getNumRows());
    }

    @Override
    protected String getSql() {
        return "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
    }


}

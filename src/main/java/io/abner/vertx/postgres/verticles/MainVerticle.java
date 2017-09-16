package io.abner.vertx.postgres.verticles;

import io.abner.vertx.postgres.services.SomeDatabaseOperationHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import rx.Single;

public class MainVerticle extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        vertx.rxDeployVerticle(MainVerticle.class.getName()).subscribe();
    }

    @Override
    public void start() throws Exception {

        /*JsonObject config = new JsonObject().put("url", "jdbc:hsqldb:mem:test?shutdown=true")
                .put("driver_class", "org.hsqldb.jdbcDriver");
        */

        JsonObject config = new JsonObject()
                .put("initial_pool_size", 1)
                .put("min_pool_size", 1)
                .put("max_pool_size", 1)
                .put("max_idle_time", 10)
                .put("url", "jdbc:postgresql://localhost:5432/postgres?user=postgres&loggerLevel=TRACE"); //
        JDBCClient jdbc = JDBCClient.createShared(vertx, config);
/*
        // Connect to the database
        jdbc.rxGetConnection().flatMap(conn -> {

            // Now chain some statements using flatmap composition
            Single<ResultSet> resa = conn.rxUpdate("CREATE TABLE test(col VARCHAR(20))")
                    .flatMap(result -> conn.rxUpdate("INSERT INTO test (col) VALUES ('val1')"))
                    // .flatMap(result -> conn.rxUpdate("INSERT INTO test (col) VALUES ('val2')"))
                    .flatMap(result -> conn.rxQuery("SELECT * FROM test"));

            return resa.doAfterTerminate(conn::close);

        }).subscribe(resultSet -> {
            // Subscribe to the final result
            System.out.println("Results : " + resultSet.getRows());
        }, err -> {
            System.out.println("Database problem");
            err.printStackTrace();
        });*/

        jdbc.rxGetConnection().subscribe(conn -> {
            conn.rxQuery("SELECT sum(numbackends) FROM pg_stat_database;")
                    .subscribe(rsSessions -> {
                        System.out.println("CONNECTIONS COUNT: " + rsSessions.getResults().get(0).getInteger(0).toString());
                    });
        });

        vertx.setTimer(2000, (timer) -> {

            SomeDatabaseOperationHandler op = new SomeDatabaseOperationHandler(jdbc);
            op.init().flatMap((Void) -> {
                return op.execute().flatMap(r ->
                        op.executeSql("SELECT 1 + ?", new JsonArray().add(r)).map(rs -> rs.getResults().get(0).getInteger(0))
                );
            })
                    .doAfterTerminate(() -> {
                        jdbc.rxGetConnection().subscribe(conn -> {
                            conn.rxQuery("SELECT sum(numbackends) FROM pg_stat_database;")
                                    .subscribe(rsSessions -> {
                                        System.out.println("CONNECTIONS COUNT: " + rsSessions.getResults().get(0).getInteger(0).toString());
                                    });
                        });
                    })
                    .subscribe(rs -> {
                        System.out.println("OK -> " + rs.toString());
                    }, e -> {
                        ((Exception) e).printStackTrace();
                    });
        });
    }
}
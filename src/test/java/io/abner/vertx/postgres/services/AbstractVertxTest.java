package io.abner.vertx.postgres.services;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import io.vertx.rxjava.ext.sql.SQLClient;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractVertxTest {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected SQLClient dbClient;
    protected Scheduler scheduler;
    protected Async async;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    private Properties testProperties = new Properties();

    public JsonObject getDbConfig() throws Exception {
        this.loadTestProperties();
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", testProperties.getProperty("db_host"),
                Integer.parseInt(testProperties.getProperty("db_port")), testProperties.getProperty("db_database"));
        JsonObject config = new JsonObject().put("url", jdbcUrl).put("driver_class", "org.postgresql.Driver")
                .put("user", testProperties.getProperty("db_username"))
                .put("password", testProperties.getProperty("db_password"))
                .put("max_iddle_time", 120)
                .put("max_pool_size", 5);
        LOGGER.trace("DB CONFIG: \n" + config.encodePrettily());
        return config;
    }

    protected Vertx getVertxRx() {
        return new io.vertx.rxjava.core.Vertx(rule.vertx());
    }

    protected void loadTestProperties() throws Exception{
        InputStream input = null;
        try {
            input = this.getClass().getClassLoader().getResourceAsStream("test.properties");
            // load a properties file
            testProperties.load(input);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Before
    public void setup(TestContext context) throws Exception {
        async = context.async();
        dbClient = JDBCClient.createShared(getVertxRx(), getDbConfig());
        onSetup().subscribe((Void) -> async.complete(), e -> context.fail(e));
    }

    public Single<Void> onSetup() {
        return Single.just(null);
    }
}

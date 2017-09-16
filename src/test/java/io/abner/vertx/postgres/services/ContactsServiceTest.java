package io.abner.vertx.postgres.services;

import io.abner.vertx.postgres.models.Contact;
import io.abner.vertx.postgres.services.interfaces.ExecuteDatabaseOperation;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Observable;
import rx.Single;

import java.util.ArrayList;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class ContactsServiceTest extends AbstractVertxTest {


    @Test
    public void testGetAll(TestContext context) {
        Async async = context.async();


        PersistenceServiceBuilder
                .setup(dbClient)
                .build(false, ContactsService.class, service ->
                service.getAll().doOnSuccess(rs -> {
                    context.assertEquals("Abner Oliveira", rs.get(0).getName());
                    async.complete();
                })
        );

    }
}

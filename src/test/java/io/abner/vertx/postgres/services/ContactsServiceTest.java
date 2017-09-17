package io.abner.vertx.postgres.services;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ContactsServiceTest extends AbstractVertxTest {


    @Test
    public void testGetAll(TestContext context) {
        Async async = context.async();


        PersistenceServiceBuilder
                .setup(dbClient)
                .build(false, ContactsService.class, service ->
                        service.getAll().doOnSuccess(rs -> {
                            context.assertEquals(4, rs.size());
                            context.assertEquals("John Doe", rs.get(0).getName());
                            async.complete();
                        })
                );

    }
}

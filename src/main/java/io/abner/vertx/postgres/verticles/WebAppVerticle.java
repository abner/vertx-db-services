package io.abner.vertx.postgres.verticles;

import java.awt.FlowLayout;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.abner.vertx.postgres.handlers.SQLConnectionHandler;
import io.abner.vertx.postgres.routes.ContactsRoutesHandler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import io.vertx.rxjava.ext.sql.SQLClient;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;

public class WebAppVerticle extends AbstractVerticle {

	private static Logger LOGGER = LoggerFactory.getLogger(WebAppVerticle.class);
			
	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {

		Vertx vertx = Vertx.vertx();

		DeploymentOptions opt = new DeploymentOptions().setConfig(new JsonObject().put("contacts_jdbc_url", "jdbc:postgresql://localhost:5432/contacts?user=contacts&password=contacts"));
		vertx.rxDeployVerticle(WebAppVerticle.class.getName(), opt).subscribe(r -> {
			LOGGER.info("WebAppVerticle deployed. " + r);
		});
		
		
	}

	@Override
	public void start() throws Exception {
		
		
		
		if(System.getenv("IN_DOCKER") != null) {
			this.config().put("contacts_jdbc_url", "jdbc:postgresql://postgresql:5432/postgres?user=postgres");
		}

		System.out.println("CONFIG => " + this.config().encodePrettily());

		System.out.println(
				"Application just started...\nIt's so cool! :D ----------------------------------------------------");

		JsonObject config = new JsonObject().put("url",  this.config().getString("contacts_jdbc_url"));
		SQLClient sqlClient = JDBCClient.createShared(vertx, config);
		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());
		
		router.route().handler(SQLConnectionHandler.create(sqlClient));
		
		vertx.rxExecuteBlocking(f -> {
			try {
				Flyway flyway = new Flyway();
				flyway.setDataSource(this.config().getString("contacts_jdbc_url"), "postgres", "");
				flyway.migrate();;
				f.complete();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}).subscribe();
		
		ContactsRoutesHandler contactsHandler = new ContactsRoutesHandler();
		router.get("/contacts").handler(contactsHandler);
		router.post("/contacts").handler(contactsHandler);
		router.put("/contacts/:id").handler(contactsHandler);
		router.delete("/contacts/:id").handler(contactsHandler);
		
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}

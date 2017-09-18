package io.abner.vertx.postgres.routes;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.abner.vertx.postgres.handlers.SQLConnectionHandler;
import io.abner.vertx.postgres.services.ContactsService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.rxjava.ext.sql.SQLConnection;
import io.vertx.rxjava.ext.web.RoutingContext;

public class ContactsRoutesHandler implements Handler<RoutingContext>{
	
	private static Logger LOGGER = LoggerFactory.getLogger(ContactsRoutesHandler.class);
	
	@Override
	public void handle(RoutingContext context) {
		SQLConnection conn = SQLConnectionHandler.getCurrentConnection(context);
		
		switch (context.request().method()) {
		case GET:
			handleGET(context, conn);
			break;
		case POST:
			handlePOST(context, conn);
			break;
		case PUT:
			handlePUT(context, conn);
			break;
		case DELETE:
			handleDELETE(context, conn);
		default:
			break;
		}
		
	}
	
	private void handleGET(RoutingContext context, SQLConnection conn) {
		ContactsService service = new ContactsService(conn);
		service.getAll().subscribe(list -> {
			Buffer json = Json.encodeToBuffer(list);
			context.response().end(json.toString());
		});
	}
	
	private void handlePUT(RoutingContext context, SQLConnection conn) {
		
	}
	
	private void handleDELETE(RoutingContext context, SQLConnection conn) {
		
	}
	
	private void handlePOST(RoutingContext context, SQLConnection conn) {
		ContactsService service = new ContactsService(conn);
		service.insert(context.getBodyAsJson()).subscribe(ok -> {
			if (ok) {
				context.response().setStatusCode(201).end();
			} else {
				context.response().setStatusCode(400).end();
				LOGGER.error("It is weird. No error executing select, but no record was updated.");
			}
		}, context::fail);
	}

}

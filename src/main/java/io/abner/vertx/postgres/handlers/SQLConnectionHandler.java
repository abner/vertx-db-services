package io.abner.vertx.postgres.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.rxjava.ext.sql.SQLConnection;
import io.vertx.rxjava.ext.sql.SQLClient;
import io.vertx.rxjava.ext.web.RoutingContext;
import rx.Single;

/***
 * This Handler starts a connection with transaction set as auto commit as false,
 * and if the request is OK, it commits the transaction, otherwise it cancels the transaction 
 * 
 * @author Abner
 *
 */
public class SQLConnectionHandler implements Handler<RoutingContext>{

	private static Logger LOGGER = LoggerFactory.getLogger(SQLConnectionHandler.class);
	private static String handlerCacheKey = "__SQL_CONNECTION_HANDLER_CONN";
	
	private SQLClient sqlClient;
	
	@Override
	public void handle(RoutingContext context) {
		this.sqlClient.rxGetConnection()
		.flatMap(this::configureTransaction)
		.doOnError(e -> {
			context.fail(e);	
		})
		.doOnSuccess(conn -> {
			context.put(handlerCacheKey, conn);
			context.addBodyEndHandler(v -> closeConnection(context));
			context.next();
		}).subscribe();
	}
	
	private void closeConnection(RoutingContext context) {
		SQLConnection conn = context.<SQLConnection>get(handlerCacheKey);
		if (context.response().getStatusCode() < 400) {
			conn.rxCommit().flatMap(Void -> conn.rxClose())
			.doOnError(context::fail)
			.subscribe();
		} else {
			conn.rxRollback().flatMap(Void -> conn.rxClose())
				.doOnError(e -> LOGGER.error("Failed to close the connection", e))
				.subscribe();
		}
	}
	
	private Single<SQLConnection> configureTransaction(SQLConnection conn) {
		return conn.rxSetAutoCommit(false).flatMap(Void -> Single.just(conn));
	}
	
	private SQLConnectionHandler(SQLClient sqlClient) {
		this.sqlClient = sqlClient;
	}
	
	
	public static SQLConnectionHandler create(SQLClient sqlClient) {
		return new SQLConnectionHandler(sqlClient);
	}
	
	public static SQLConnection getCurrentConnection(RoutingContext context) {
		return context.<SQLConnection>get(handlerCacheKey);
	}

}

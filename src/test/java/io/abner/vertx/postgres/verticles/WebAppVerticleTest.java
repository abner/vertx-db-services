package io.abner.vertx.postgres.verticles;

import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.abner.vertx.postgres.services.AbstractVertxTest;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class WebAppVerticleTest extends AbstractVertxTest {

	Logger LOGGER = LoggerFactory.getLogger(WebAppVerticleTest.class);
	Integer port;

	@Before
	public void setup(TestContext context) throws Exception {
		ServerSocket socket = new ServerSocket(0);
		port = socket.getLocalPort();
		socket.close();
		Async async = context.async();
		DeploymentOptions opt = new DeploymentOptions()
				.setConfig(
						new JsonObject()
							.put("contacts_jdbc_url", getDbConfig().getString("url"))
							.put("http.port", port)
				);
		getVertxRx().rxDeployVerticle(WebAppVerticle.class.getName(), opt).doOnEach(v -> {
			async.complete();
		}).doOnError(context::fail).subscribe();
	}

	@After
	public void afterTest(TestContext context) {
		getVertxRx().getDelegate().close(context.asyncAssertSuccess());
	}

	@Test
	public void testarQualquerCoisa(TestContext context) {
		LOGGER.info("TESTE RODOU");
		final Async async = context.async();
		getVertxRx().getDelegate().createHttpClient().get(port, "localhost", "/contacts", response -> {
			response.handler(body -> {
				context.assertTrue(body.toString().contains("John Doe"));
				async.complete();
			});
		}).exceptionHandler(e -> {
			context.fail(e);
		}).end();
	}
}

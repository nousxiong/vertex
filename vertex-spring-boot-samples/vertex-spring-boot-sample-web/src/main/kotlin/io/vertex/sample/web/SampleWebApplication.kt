package io.vertex.sample.web

import io.vertex.autoconfigure.web.server.GracefulShutdown
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticleFactory
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.await
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class SampleWebApplication(private val vertx: Vertx) {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SampleWebApplication::class.java)
	}

	private suspend fun getUrlSize(host: String, uri: String = "/"): Int {
		val client = WebClient.create(vertx)
		try {
			val req = client.get(host, uri)
			val rsp = req.send().await()
			return rsp.bodyAsString().length
		} finally {
			client.close()
		}
	}

	@Bean
	fun serverVerticleFactory(): VertexServerVerticleFactory {
		return object : VertexServerVerticleFactory {
			override fun create(
				httpServerOptions: HttpServerOptions,
				handler: Handler<RoutingContext>,
				gracefulShutdown: GracefulShutdown?
			): VertexServerVerticle {
				return MyServerVerticle(httpServerOptions, handler, gracefulShutdown)
			}
		}
	}

	@GetMapping("/hello")
	suspend fun hello(): String {
		logger.info("vertex before vid=${VertexServerVerticle.getIdOrNull()}")
//		delay(100L)
		logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
		logger.info("vertex after vid=${VertexServerVerticle.getIdOrNull()}")
		return "Hello, World!"
	}
}

class MyServerVerticle(
	httpServerOptions: HttpServerOptions,
	requestHandler: Handler<RoutingContext>,
	gracefulShutdown: GracefulShutdown?,
) : VertexServerVerticle(httpServerOptions, requestHandler, gracefulShutdown)

fun main(args: Array<String>) {
	runApplication<SampleWebApplication>(*args)
}

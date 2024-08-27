package io.vertex.sample.web

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexCloser
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticleFactory
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.coAwait
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
			val rsp = req.send().coAwait()
			return rsp.bodyAsString().length
		} finally {
			client.close()
		}
	}

	@Bean
	fun serverVerticleFactory(): VertexServerVerticleFactory {
		return object : VertexServerVerticleFactory {
			override fun create(
				instances: Int,
				index: Int,
				httpServerOptions: HttpServerOptions,
				handler: Handler<RoutingContext>,
				gracefulShutdown: GracefulShutdown?
			): VertexServerVerticle {
				return MyServerVerticle(instances, index, httpServerOptions, handler, gracefulShutdown)
			}
		}
	}

	@GetMapping("/hello")
	suspend fun hello(): String {
		logger.info("vertex before vid=${VertexVerticle.idOrNull()}")
//		delay(100L)
		logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
		logger.info("vertex after vid=${VertexVerticle.idOrNull()}")
		return "Hello, World!"
	}

	@Bean
	fun vertexCloser(vertx: Vertx): VertexCloser {
		return object : VertexCloser {
			override fun close() {
				logger.info("close vertex")
				vertx.close()
			}
		}
	}
}

class MyServerVerticle(
	instances: Int,
    index: Int,
    httpServerOptions: HttpServerOptions,
    requestHandler: Handler<RoutingContext>,
    gracefulShutdown: GracefulShutdown?,
) : VertexServerVerticle(instances, index, httpServerOptions, requestHandler, gracefulShutdown)

fun main(args: Array<String>) {
	runApplication<SampleWebApplication>(*args)
}

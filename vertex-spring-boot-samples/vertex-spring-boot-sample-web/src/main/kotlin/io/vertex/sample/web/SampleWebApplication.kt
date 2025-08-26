package io.vertex.sample.web

//import io.vertx.ext.web.client.WebClient
//import io.vertx.kotlin.coroutines.coAwait
import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexCloser
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticleFactory
import io.vertex.util.verticleScope
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@SpringBootApplication
@RestController
class SampleWebApplication(private val vertx: Vertx) {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SampleWebApplication::class.java)
	}
	@Autowired
	private lateinit var clientHttpConnector: ClientHttpConnector

	private suspend fun getUrlSize(host: String, uri: String = "/"): Int {
		val webClient = WebClient.builder()
			.baseUrl("https://$host")
			.clientConnector(clientHttpConnector)
			.build()
		return webClient.get()
			.uri(uri)
			.retrieve()
			.awaitBody<String>().length
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
		delay(100L)
		logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
		logger.info("vertex after vid=${VertexVerticle.idOrNull()}")
		return "Hello, World!"
	}

	@GetMapping("/hellov")
	fun hellov() = verticleScope {
		logger.info("vertex before vid=${VertexVerticle.idOrNull()}")
		delay(100L)
		logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
		logger.info("vertex after vid=${VertexVerticle.idOrNull()}")
		"Hello, Verticle!"
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

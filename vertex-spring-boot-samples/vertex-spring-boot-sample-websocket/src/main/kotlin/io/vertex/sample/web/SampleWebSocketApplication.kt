package io.vertex.sample.web

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticleFactory
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.*

@SpringBootApplication
@RestController
class SampleWebSocketApplication(private val vertx: Vertx) {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SampleWebSocketApplication::class.java)
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

	@Bean
	fun handlerMapping(): HandlerMapping {
		// Define URL mapping for the socket handlers
		val handlerMapping = SimpleUrlHandlerMapping(mapOf("/echo-upper" to WebSocketHandler {
			session -> toUppercaseHandler(session)
		}))
		// Set a higher precedence than annotated controllers (smaller value means higher precedence)
		handlerMapping.order = -1

		return handlerMapping
	}

	private fun toUppercaseHandler(session: WebSocketSession): Mono<Void> {
		val messages = session.receive()
			.filter { it.type == WebSocketMessage.Type.TEXT }
			.asFlow()
			.map {
				val payload = handleMessage(it)
				session.textMessage(payload)
			}
			.asFlux(Vertx.currentContext().dispatcher())
		return session.send(messages) // Send response messages
	}

	private suspend fun handleMessage(message: WebSocketMessage): String {
		logger.info("vertex before vid=${VertexVerticle.idOrNull()}")
		logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
		logger.info("vertex after vid=${VertexVerticle.idOrNull()}")
		return message.payloadAsText.uppercase(Locale.getDefault())
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
}

class MyServerVerticle(
	instances: Int,
    index: Int,
    httpServerOptions: HttpServerOptions,
    requestHandler: Handler<RoutingContext>,
    gracefulShutdown: GracefulShutdown?,
) : VertexServerVerticle(instances, index, httpServerOptions, requestHandler, gracefulShutdown)

fun main(args: Array<String>) {
	runApplication<SampleWebSocketApplication>(*args)
}

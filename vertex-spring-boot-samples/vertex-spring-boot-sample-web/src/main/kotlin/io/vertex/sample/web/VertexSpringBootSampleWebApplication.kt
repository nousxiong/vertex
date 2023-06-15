package io.vertex.sample.web

import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertx.core.Vertx
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class VertexSpringBootSampleWebApplication {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(VertexSpringBootSampleWebApplication::class.java)
	}
	@Autowired
	private var vertx: Vertx? = null

	@GetMapping("/hello")
	suspend fun hello(): String {
		logger.info("vertex ctx=${Vertx.currentContext().get<Int>(VertexServerVerticle.CONTEXT_ID)}")
		delay(100L)
		logger.info("vertex ctx=${Vertx.currentContext().get<Int>(VertexServerVerticle.CONTEXT_ID)}")
		return "Hello, World!"
	}
}

fun main(args: Array<String>) {
	runApplication<VertexSpringBootSampleWebApplication>(*args)
}

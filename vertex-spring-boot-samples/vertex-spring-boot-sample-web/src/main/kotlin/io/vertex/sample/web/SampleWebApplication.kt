package io.vertex.sample.web

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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.config.BlockingExecutionConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@SpringBootApplication
@RestController
class SampleWebApplication(private val vertx: Vertx) : WebFluxConfigurer {
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

	private fun getUrlSizeBlocked(host: String, uri: String = "/"): Int {
		val webClient = WebClient.builder()
			.baseUrl("https://$host")
			.clientConnector(clientHttpConnector)
			.build()
		return webClient.get()
			.uri(uri)
			.retrieve()
			.bodyToMono<String>()
			.block()!!.length
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

	override fun configureBlockingExecution(configurer: BlockingExecutionConfigurer) {
		logger.info("configureBlockingExecution")
		configurer.setControllerMethodPredicate {
			val beanTypeCheck = it.beanType != SampleWebApplication::class.java
			logger.info("ControllerMethodPredicate beanType check $beanTypeCheck")
			beanTypeCheck
		}
	}

	/**
	 * 使用“姿势”
	 * 1、vertex.http.server.deployments.threadingModel=EVENT_LOOP
	 * 	+ vertex.http.client.enabled=true
	 *  + 禁用delay等非vertx支持的suspend方式
	 *  + 禁用阻塞方法
	 */
	@GetMapping("/hello")
	suspend fun hello(): String {
		logger.info("hello vertex before vid=${VertexVerticle.idOrNull()}")
//		delay(100L)
		logger.info("hello baidu size: ${getUrlSize("www.baidu.com")}")
		logger.info("hello vertex after vid=${VertexVerticle.idOrNull()}")
		return "Hello, Suspend!"
	}

	/**
	 * 使用“姿势”
	 * 1、vertex.http.server.deployments.threadingModel=EVENT_LOOP
	 * 	+ vertex.http.client.enabled=false
	 *  + 重载WebFluxConfigurer.configureBlockingExecution 判定此方法为非阻塞调用
	 */
	@GetMapping("/hellob")
	fun hellob(): String {
		logger.info("hellob vertex before vid=${VertexVerticle.idOrNull()}")
		logger.info("hellob baidu size: ${getUrlSizeBlocked("www.baidu.com")}")
		logger.info("hellob vertex after vid=${VertexVerticle.idOrNull()}")
		return "Hello, Block!"
	}

	/**
	 * 使用“姿势”
	 * 1、vertex.http.server.deployments.threadingModel=EVENT_LOOP
	 * 	+ vertex.http.client.enabled=true
	 */
	@GetMapping("/him")
	fun him(): Mono<String> {
		logger.info("him vertex before vid=${VertexVerticle.idOrNull()}")
		logger.info("him vertex after vid=${VertexVerticle.idOrNull()}")
		return Mono.just("Hi, Mono!")
	}

	/**
	 * 使用“姿势”
	 * 1、vertex.http.server.deployments.threadingModel=EVENT_LOOP
	 * 	+ vertex.http.client.enabled=true
	 *  + 禁用阻塞方法
	 */
	@RequestMapping("/hellov")
	fun hellov() = verticleScope {
		logger.info("hellov vertex before vid=${VertexVerticle.idOrNull()}")
		delay(100L)
		logger.info("hellov baidu size: ${getUrlSize("www.baidu.com")}")
		logger.info("hellov vertex after vid=${VertexVerticle.idOrNull()}")
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

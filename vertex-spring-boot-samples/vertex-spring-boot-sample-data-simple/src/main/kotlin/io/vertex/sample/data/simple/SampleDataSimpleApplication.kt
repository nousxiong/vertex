package io.vertex.sample.data.simple

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexCloser
import io.vertex.autoconfigure.core.VertexVerticle
//import io.vertex.autoconfigure.data.rtwb.RtwbSave
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticleFactory
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@SpringBootApplication
@EnableRedisRepositories(basePackages = ["io.vertex.sample.data.simple"])
class SampleDataSimpleApplication(private val vertx: Vertx) {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SampleDataSimpleApplication::class.java)
	}

	@Bean
	fun serverVerticleFactory(): VertexServerVerticleFactory {
		return object : VertexServerVerticleFactory {
			override fun create(
				index: Int,
				httpServerOptions: HttpServerOptions,
				handler: Handler<RoutingContext>,
				gracefulShutdown: GracefulShutdown?
			): VertexServerVerticle {
				return MyServerVerticle(index, httpServerOptions, handler, gracefulShutdown)
			}
		}
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

	@Bean
	fun redisConnectionFactory(): RedisConnectionFactory {
		val redisCfg = RedisStandaloneConfiguration("bj-crs-hbcc149a.sql.tencentcdb.com", 23110).apply {
			password = RedisPassword.of("APsM5NqeIWU")
		}
		return LettuceConnectionFactory(redisCfg)
	}

	@Bean
	fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<*, *> {
		val template = RedisTemplate<ByteArray, ByteArray>()
		template.connectionFactory = redisConnectionFactory
		return template
	}

	@Bean
	fun <T> sampleSave(): SampleSave<T> {
		return KSampleSaveImpl()
	}

	@Bean
	fun <T : Any, ID> sampleCrud(): SampleCrud<T, ID> {
		return KSampleCrudImpl()
	}
}

@RedisHash("person")
class Person(
	@Id val id: String,
	val firstname: String,
	val lastname: String,
)

//interface RtwbSave<T> {
//	fun <S : T> save(entity: S): S
//}
//
//open class SampleRtwbSaveImpl<T> : RtwbSave<T> {
//	companion object {
//		val logger: Logger = LoggerFactory.getLogger(SampleRtwbSaveImpl::class.java)
//	}
//	override fun <S : T> save(entity: S): S {
//		logger.info("RtwbSaveImpl.save")
//		return entity
//	}
//}

class KSampleSaveImpl<T> : SampleSave<T> {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(KSampleSaveImpl::class.java)
	}
	override fun <S : T> save(entity: S): S {
		logger.info("KSampleSaveImpl.save")
		return entity
	}
}

class KSampleCrudImpl<T : Any, ID> : SampleCrud<T, ID> {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(KSampleCrudImpl::class.java)
	}

	override fun <S : T> save(entity: S): S {
		logger.info("KSampleCrudImpl.save")
		return entity
	}

	override fun findById(id: ID): Optional<T> {
		logger.info("KSampleCrudImpl.findById")
		return Optional.empty()
	}

	override fun existsById(id: ID): Boolean {
		logger.info("KSampleCrudImpl.existsById")
		return false
	}

	override fun count(): Long {
		logger.info("KSampleCrudImpl.count")
		return 0
	}

}

//interface PersonCrudRepository : CrudRepository<Person, Long>
interface PersonCrudRepository : CrudRepository<Person, Long>, SampleCrud<Person, Long>

@RestController
class SampleDataSimpleController(
	private val vertx: Vertx,
	private val personCrudRepository: PersonCrudRepository
) {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SampleDataSimpleController::class.java)
	}

	@GetMapping("/hello")
	fun hello(): String {
		logger.info("vertex ctx=${VertexVerticle.id()}")
		personCrudRepository.save(Person("1", "xiong", "xiaolong2"))
		val persionCount = personCrudRepository.count()
		logger.info("persionCount: $persionCount")
		val persion = personCrudRepository.findByIdOrNull(1L)
		logger.info("persion: ${persion?.firstname}")
		logger.info("vertex ctx=${VertexVerticle.id()}")
		return "hello world!"
	}
}

class MyServerVerticle(
    index: Int,
    httpServerOptions: HttpServerOptions,
    requestHandler: Handler<RoutingContext>,
    gracefulShutdown: GracefulShutdown?,
) : VertexServerVerticle(index, httpServerOptions, requestHandler, gracefulShutdown)

fun main(args: Array<String>) {
	runApplication<SampleDataSimpleApplication>(*args)
}

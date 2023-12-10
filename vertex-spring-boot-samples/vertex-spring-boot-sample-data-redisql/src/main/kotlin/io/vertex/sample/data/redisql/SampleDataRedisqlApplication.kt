package io.vertex.sample.data.redisql

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexCloser
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertex.autoconfigure.data.rtwb.service.BehindDataService
import io.vertex.autoconfigure.data.rtwb.service.PrimaryDataService
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticleFactory
import io.vertex.data.redisql.EnableRedisqlRepositories
import io.vertex.data.redisql.RedisqlKeyValueMap
import io.vertex.data.redisql.VertexRedisqlAutoConfiguration
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.keyvalue.annotation.KeySpace
import org.springframework.data.keyvalue.core.KeyValueAdapter
import org.springframework.data.keyvalue.core.KeyValueOperations
import org.springframework.data.keyvalue.core.KeyValueTemplate
import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.data.map.MapKeyValueAdapter
import org.springframework.data.map.repository.config.EnableMapRepositories
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@SpringBootApplication
@EnableRedisqlRepositories
//@EnableRedisRepositories(basePackages = ["io.vertex.sample.data.redis"])
//@EnableMapRepositories(keyValueTemplateRef = "mapKeyValueTemplate2")
//@EnableMapRepositories(
//	basePackages = ["io.vertex.sample.data.redisql"],
//	keyValueTemplateRef = "mapKeyValueTemplate2"
//)
class SampleDataRedisqlApplication(private val vertx: Vertx) {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SampleDataRedisqlApplication::class.java)
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

//	@Bean
//	fun mapKeyValueTemplate2(keyValueAdapter: KeyValueAdapter): KeyValueOperations {
//		logger.info("mapKeyValueTemplate2")
//		return KeyValueTemplate(keyValueAdapter)
//	}
//
//	@Bean
//	fun keyValueAdapter(): KeyValueAdapter {
//		logger.info("keyValueAdapter2")
//		return MapKeyValueAdapter(hashMapOf<String?, MutableMap<Any, Any>?>().apply {
//			put("persons", RedisqlKeyValueMap())
//		})
//	}
//
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

//@RedisHash("person")
@KeySpace("persons")
class Person(
	@Id val id: Long,
	val firstname: String,
	val lastname: String,
)

@KeySpace("car")
class Car(
	@Id val id: Long,
	val name: String,
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
		logger.info("KSampleCrudImpl.save $entity")
		return entity
	}

	override fun findById(id: ID): Optional<T> {
		logger.info("KSampleCrudImpl.findById $id")
		return Optional.empty()
	}

	override fun existsById(id: ID): Boolean {
		logger.info("KSampleCrudImpl.existsById $id")
		return false
	}

	override fun count(): Long {
		logger.info("KSampleCrudImpl.count")
		return 0
	}

}

interface PersonCrudRepository : KeyValueRepository<Person, Long>
//interface PersonCrudRepository2 : CrudRepository<Person, Long>
//interface PersonCrudRepository : CrudRepository<Person, Long>, SampleCrud<Person, Long>
//interface CarCrudRepository : CrudRepository<Car, Long>, SampleCrud<Car, Long>

@RestController
class SampleDataRedisqlController(
	private val vertx: Vertx,
//	private val primaryDataService: PrimaryDataService<Person, Long>,
//	private val carDataService: PrimaryDataService<Car, Long>,
//	private val behindDataService: BehindDataService<Person, Long>,
	private val personCrudRepository: PersonCrudRepository,
//	private val personCrudRepository2: PersonCrudRepository2,
//	private val carCrudRepository: CarCrudRepository,
) {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SampleDataRedisqlController::class.java)
	}

	@GetMapping("/hello")
	fun hello(): String {
		logger.info("vertex ctx=${VertexVerticle.id()}")
		personCrudRepository.save(Person(2L, "xiong2", "xiaolong2"))
//		personCrudRepository.save(Person(10L, "xiong", "xiaolong"))
//		carCrudRepository.save(Car(20L, "benz"))
//		val persionCount = personCrudRepository.count()
//		val carCount = carCrudRepository.count()
//		logger.info("persionCount: $persionCount carCount: $carCount")
//		val persion = personCrudRepository.findByIdOrNull(1L)
//		val car = carCrudRepository.findByIdOrNull(1L)
//		logger.info("persion: ${persion?.firstname} car: ${car?.name}")
		logger.info("vertex ctx=${VertexVerticle.id()}")
		return "hello world!"
	}

	@GetMapping("/hi")
	fun hi(): String {
//		logger.info("vertex ctx=${VertexVerticle.id()}, primaryDataService=$primaryDataService")
//		logger.info("vertex ctx=${VertexVerticle.id()}, carDataService=$carDataService")
////		primaryDataService.status(Person(10L, "xiong", "xiaolong"))
////		carDataService.status(Car(20L, "benz"))
//		logger.info("vertex ctx=${VertexVerticle.id()}, behindDataService=$behindDataService")
		return "hi, world!"
	}
}

class MyServerVerticle(
    index: Int,
    httpServerOptions: HttpServerOptions,
    requestHandler: Handler<RoutingContext>,
    gracefulShutdown: GracefulShutdown?,
) : VertexServerVerticle(index, httpServerOptions, requestHandler, gracefulShutdown)

fun main(args: Array<String>) {
	runApplication<SampleDataRedisqlApplication>(*args)
}

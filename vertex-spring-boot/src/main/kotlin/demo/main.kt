package demo

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexCloser
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertex.autoconfigure.core.VerticleLifecycle
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.autoconfigure.web.server.VertexServerVerticleFactory
import io.vertex.util.verticleScope
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.measureTimedValue


/**
 * Created by xiongxl in 2023/6/16
 */
@SpringBootApplication
@RestController
class VertexApplication(
    private val vertx: Vertx
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(VertexApplication::class.java)
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
    fun serverVerticleFactory(): VertexServerVerticleFactory {
        return object : VertexServerVerticleFactory {
            override fun create(
                index: Int,
                httpServerOptions: HttpServerOptions,
                handler: Handler<RoutingContext>,
                gracefulShutdown: GracefulShutdown?
            ): VertexServerVerticle {
                return VtServerVerticle(index, httpServerOptions, handler, gracefulShutdown)
            }
        }
    }
}

@RestController
class DemoController(
    private val vertx: Vertx
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(DemoController::class.java)
    }

    private val webClient = VerticleLifecycle<WebClient>("demo.WebClient").factory {
        logger.info("create WebClient ctx=${VertexVerticle.id()}")
        WebClient.create(vertx)
    }.closer {
        logger.info("close WebClient ctx=${VertexVerticle.id()}")
        it.close()
    }

    private val webClient2 = VerticleLifecycle<WebClient>("demo.WebClient2").awaitFactory {
        logger.info("create WebClient2 ctx=${VertexVerticle.id()}")
        WebClient.create(vertx)
    }.closer {
        logger.info("close WebClient2 ctx=${VertexVerticle.id()}")
        it.close()
    }

    private suspend fun getUrlSizeAwait(host: String, uri: String = "/"): Int {
        val client = webClient2.getAwait()
        val req = client.get(host, uri)
        val rsp = req.send().coAwait()
        return rsp.bodyAsString().length
    }

    private fun getUrlSize(host: String, uri: String = "/"): Int {
        val client = webClient.get()
        val req = client.get(host, uri)
        val rsp = Future.await(req.send())
        return rsp.bodyAsString().length
    }

    @GetMapping("/hi")
    fun hi(): String {
        logger.info("vertex ctx=${VertexVerticle.id()}")
        logger.info("vertex ctx=${VertexVerticle.id()}")
        return "Hello, World!"
    }

    @GetMapping("/hello")
    suspend fun hello(): String {
        logger.info("vertex ctx=${VertexVerticle.id()}")
//        delay(100L)
        logger.info("baidu size: ${getUrlSizeAwait("www.baidu.com")}")
        logger.info("vertex ctx=${VertexVerticle.id()}")
        return "Hello, World!"
    }

    @GetMapping("/verticle")
    fun verticle() = verticleScope {
        logger.info("vertex before ctx=${VertexVerticle.id()}")
        logger.info("baidu size: ${getUrlSizeAwait("www.baidu.com")}")
        logger.info("vertex after ctx=${VertexVerticle.id()}")
        delay(1 * 300L)
        logger.info("vertex delayed ctx=${VertexVerticle.id()}")
        "Hello, World!"
    }

}

/**
 * 2024/2/19 测试Java21的虚拟线程运行Kt的协程情况
 * 1、两者可以同时存在（协程运行在vt上）
 * 2、默认情况下（Vertx.CoroutineVerticle），用vt运行协程时，协程挂起恢复后的vt可能和之前不是一个
 * ├── 2.1、尽量直接使用Java21的虚拟线程运行io代码，而非协程
 * └── 2.2、协程仅用于兼容带有协程的Kotlin代码用
 */
class VtServerVerticle(
    index: Int,
    httpServerOptions: HttpServerOptions,
    requestHandler: Handler<RoutingContext>,
    gracefulShutdown: GracefulShutdown?,
) : VertexServerVerticle(index, httpServerOptions, requestHandler, gracefulShutdown) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(VtServerVerticle::class.java)
    }
    override suspend fun start() {
        super.start()

        getBaiduUrlSize()
        getBaiduUrlSizeAwait()
    }

    private fun getBaiduUrlSize() {
        val tvRsp = measureTimedValue {
            logger.info("start await get baidu url size ctx=${id()}")
            val client = WebClient.create(vertx)
            val req = client.get("www.baidu.com", "/")
            Future.await(req.send())
        }
        logger.info("et<${tvRsp.duration}> await baidu size: ${tvRsp.value.bodyAsString().length} ctx=${id()}")
    }

    private suspend fun getBaiduUrlSizeAwait() {
        val tvRsp = measureTimedValue {
            logger.info("start co await get baidu url size ctx=${id()}")
            val client = WebClient.create(vertx)
            val req = client.get("www.baidu.com", "/")
            req.send().coAwait()
        }
        logger.info("et<${tvRsp.duration}> co await baidu size: ${tvRsp.value.bodyAsString().length} ctx=${id()}")
    }
}

fun main(args: Array<String>) {
    runApplication<VertexApplication>(*args)
}
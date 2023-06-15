package demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by xiongxl in 2023/6/15
 */
@SpringBootApplication
@RestController
class VertexApplication {
    @GetMapping("/hello")
    suspend fun hello(): String {
        return "Hello, World!"
    }
}

fun main(args: Array<String>) {
    runApplication<VertexApplication>(*args)
}

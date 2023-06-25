package io.vertex.autoconfigure.web.test

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by xiongxl in 2023/6/25
 */
@RestController
@SpringBootApplication
class TestApplication {

    @GetMapping("/")
    suspend fun test(): String {
        return "test"
    }
}
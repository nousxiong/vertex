package io.vertex.autoconfigure

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VertexSpringBootApplication

fun main(args: Array<String>) {
	runApplication<VertexSpringBootApplication>(*args)
}

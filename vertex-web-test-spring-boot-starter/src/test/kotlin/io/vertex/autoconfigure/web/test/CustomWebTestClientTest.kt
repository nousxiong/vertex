package io.vertex.autoconfigure.web.test

import io.mockk.mockk
import io.vertex.autoconfigure.web.client.VertexClientHttpConnector
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClientConfigurer
import kotlin.test.assertIs

@SpringBootTest(
    classes = [
        TestApplication::class,
        CustomWebTestClientTest.CustomWebTestClientConfiguration::class
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CustomWebTestClientTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun shouldInjectCustomClient() {
        assertIs<CustomWebTestClient>(webTestClient)
    }

    @Configuration
    class CustomWebTestClientConfiguration {
        @Bean
        fun customWebTestClient(connector: VertexClientHttpConnector): WebTestClient {
            return CustomWebTestClient()
        }

    }

    class CustomWebTestClient : WebTestClient {
        override fun get(): WebTestClient.RequestHeadersUriSpec<*> {
            return mockk()
        }

        override fun head(): WebTestClient.RequestHeadersUriSpec<*> {
            return mockk()
        }

        override fun post(): WebTestClient.RequestBodyUriSpec {
            return mockk()
        }

        override fun put(): WebTestClient.RequestBodyUriSpec {
            return mockk()
        }

        override fun patch(): WebTestClient.RequestBodyUriSpec {
            return mockk()
        }

        override fun delete(): WebTestClient.RequestHeadersUriSpec<*> {
            return mockk()
        }

        override fun options(): WebTestClient.RequestHeadersUriSpec<*> {
            return mockk()
        }

        override fun method(method: HttpMethod): WebTestClient.RequestBodyUriSpec {
            return mockk()
        }

        override fun mutate(): WebTestClient.Builder {
            return mockk()
        }

        override fun mutateWith(configurer: WebTestClientConfigurer): WebTestClient {
            return mockk()
        }
    }
}
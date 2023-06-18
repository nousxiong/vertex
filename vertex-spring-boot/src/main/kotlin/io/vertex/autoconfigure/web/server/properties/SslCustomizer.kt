package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.http.ClientAuth
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.net.JksOptions
import io.vertx.core.net.KeyCertOptions
import io.vertx.core.net.PfxOptions
import io.vertx.core.net.TrustOptions
import org.springframework.boot.context.properties.PropertyMapper
import org.springframework.boot.web.server.AbstractConfigurableWebServerFactory
import org.springframework.boot.web.server.Ssl
import java.util.*
import java.util.stream.Stream

/**
 * Created by xiongxl in 2023/6/8
 */
class SslCustomizer(private val factory: AbstractConfigurableWebServerFactory) : HttpServerOptionsCustomizer {
    private val propertyMapper = PropertyMapper.get()
    override fun apply(options: HttpServerOptions): HttpServerOptions {
        val ssl = factory.ssl ?: return options

        options.setSsl(ssl.isEnabled)
        options.setKeyCertOptions(keyCertOptionsAdapter(ssl))
        options.setTrustOptions(trustOptionsAdapter(ssl))

        propertyMapper.from(ssl.clientAuth)
            .whenNonNull()
            .`as` { clientAuth: Ssl.ClientAuth? ->
                clientAuthAdapter(
                    clientAuth!!
                )
            }
            .to { clientAuth: ClientAuth? ->
                options.setClientAuth(
                    clientAuth
                )
            }

        propertyMapper.from(ssl.enabledProtocols)
            .whenNonNull()
            .`as` { a: Array<String>? ->
                a?.toList()
            }
            .`as` { c: List<String>? ->
                c?.toMutableList()?.let {
                    LinkedHashSet(
                        it
                    )
                }
            }
            .to { enabledSecureTransportProtocols: LinkedHashSet<String>? ->
                options.setEnabledSecureTransportProtocols(
                    enabledSecureTransportProtocols
                )
            }

        propertyMapper.from(ssl.ciphers)
            .whenNonNull()
            .`as` { array: Array<String>? ->
                Arrays.stream(
                    array
                )
            }
            .to { stream: Stream<String> ->
                stream.forEach { suite: String? ->
                    options.addEnabledCipherSuite(
                        suite
                    )
                }
            }

        return options
    }

    private fun clientAuthAdapter(clientAuth: Ssl.ClientAuth): ClientAuth {
        return when (clientAuth) {
            Ssl.ClientAuth.WANT -> ClientAuth.REQUEST
            Ssl.ClientAuth.NEED -> ClientAuth.REQUIRED
            else -> ClientAuth.NONE
        }
    }

    private fun keyCertOptionsAdapter(ssl: Ssl): KeyCertOptions? {
        if ("JKS".equals(ssl.keyStoreType, ignoreCase = true)) {
            return getJksOptions(ssl.keyStore, ssl.keyStorePassword)
        } else if ("PKCS12".equals(ssl.keyStoreType, ignoreCase = true)) {
            return getPfxOptions(ssl.keyStore, ssl.keyStorePassword)
        }
        return null
    }

    private fun trustOptionsAdapter(ssl: Ssl): TrustOptions? {
        if ("JKS".equals(ssl.trustStoreType, ignoreCase = true)) {
            return getJksOptions(ssl.trustStore, ssl.trustStorePassword)
        } else if ("PKCS12".equals(ssl.trustStoreType, ignoreCase = true)) {
            return getPfxOptions(ssl.trustStore, ssl.trustStorePassword)
        }
        return null
    }

    private fun getJksOptions(path: String, password: String): JksOptions {
        val options = JksOptions()
        options.setPath(path)
        options.setPassword(password)
        return options
    }

    private fun getPfxOptions(path: String, password: String): PfxOptions {
        val options = PfxOptions()
        options.setPath(path)
        options.setPassword(password)
        return options
    }
}
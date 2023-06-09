package io.vertex.autoconfigure.web.server

import org.springframework.http.server.reactive.SslInfo
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSession

/**
 * Created by xiongxl in 2023/6/9
 */
class VertexSslInfo(private val session: SSLSession) : SslInfo {
    override fun getSessionId(): String? {
        val bytes = session.id ?: return null

        val sb = StringBuilder()
        for (b in bytes) {
            var digit = Integer.toHexString(b.toInt())
            if (digit.length < 2) {
                sb.append('0')
            }
            if (digit.length > 2) {
                digit = digit.substring(digit.length - 2)
            }
            sb.append(digit)
        }
        return sb.toString()
    }

    override fun getPeerCertificates(): Array<X509Certificate>? {
        val certificates: Array<Certificate> = try {
            session.peerCertificates
        } catch (ex: Throwable) {
            return null
        }

        val result: MutableList<X509Certificate> = ArrayList(certificates.size)
        for (certificate in certificates) {
            if (certificate is X509Certificate) {
                result.add(certificate)
            }
        }
        return if (result.isNotEmpty()) result.toTypedArray<X509Certificate>() else null
    }
}
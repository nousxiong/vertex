package io.vertex.utils

import io.vertx.core.http.Cookie
import org.springframework.http.HttpCookie
import org.springframework.http.ResponseCookie
import java.util.stream.Collectors

/**
 * Created by xiongxl in 2023/6/9
 */
object CookieConverter {
    fun toCookie(responseCookie: ResponseCookie): Cookie {
        val cookie = Cookie.cookie(responseCookie.name, responseCookie.value)
            .setDomain(responseCookie.domain)
            .setPath(responseCookie.path)
            .setHttpOnly(responseCookie.isHttpOnly)
            .setSecure(responseCookie.isSecure)
        if (!responseCookie.maxAge.isNegative) {
            cookie.setMaxAge(responseCookie.maxAge.seconds)
        }
        return cookie
    }

    fun toHttpCookie(cookie: Cookie): HttpCookie {
        return HttpCookie(cookie.name, cookie.value)
    }

    fun toResponseCookies(cookieHeader: String): List<ResponseCookie> {
        return java.net.HttpCookie.parse(cookieHeader)
            .stream()
            .map { cookie: java.net.HttpCookie -> toResponseCookie(cookie) }
            .collect(Collectors.toList())
    }

    private fun toResponseCookie(cookie: java.net.HttpCookie): ResponseCookie {
        return ResponseCookie.from(cookie.name, cookie.value)
            .domain(cookie.domain)
            .httpOnly(cookie.isHttpOnly)
            .maxAge(cookie.maxAge)
            .path(cookie.path)
            .secure(cookie.secure)
            .build()
    }
}
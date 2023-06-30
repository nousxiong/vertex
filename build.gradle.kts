import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version

plugins {
    id("org.springframework.boot") version "3.1.1" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("org.jetbrains.kotlin.kapt") version "1.8.22" apply false
    kotlin("jvm") version "1.8.22" apply false
    kotlin("plugin.spring") version "1.8.22" apply false
}
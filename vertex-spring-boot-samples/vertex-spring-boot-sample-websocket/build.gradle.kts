import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

val vertexSpringBootVersion: String by project
val vertxVersion: String by project

group = "io.vertex"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

val localProperties = Properties().apply {
	load(FileInputStream(File(rootProject.rootDir, "local.properties")))
}
val codingArtifactsRepoUrl = localProperties["codingArtifactsRepoUrl"] as String
val codingArtifactsUsername = localProperties["codingArtifactsUsername"] as String
val codingArtifactsPassword = localProperties["codingArtifactsPassword"] as String

repositories {
	maven {
		url = uri(codingArtifactsRepoUrl)
		credentials {
			username = codingArtifactsUsername
			password = codingArtifactsPassword
		}
	}
	mavenCentral()
}

dependencies {
	implementation(project(":vertex-web-spring-boot-starter"))
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
//	implementation("jakarta.websocket:jakarta.websocket-api:2.0.0")
//	implementation("io.vertex:vertex-web-spring-boot-starter:$vertexSpringBootVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

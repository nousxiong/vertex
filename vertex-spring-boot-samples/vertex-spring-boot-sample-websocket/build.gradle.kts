import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("jvm")
	kotlin("plugin.spring")
}

val javaVersion: String by project
val kotlinJvmTarget: String by project
val vertexSpringBootVersion: String by project
val vertxVersion: String by project

group = "io.vertex"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.valueOf(javaVersion)

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
	implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = kotlinJvmTarget
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

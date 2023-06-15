import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.21"
	kotlin("plugin.spring") version "1.8.21"
	id("maven-publish")
}

val vertexSpringBootStarterVersion: String by project
val vertexWebSpringBootStarterVersion: String by project
val vertxVersion: String by project

group = "io.vertex"
version = vertexWebSpringBootStarterVersion
java.sourceCompatibility = JavaVersion.VERSION_17

val localProperties = Properties().apply {
	load(FileInputStream(File(rootProject.rootDir, "local.properties")))
}
val codingArtifactsRepoUrl = localProperties["codingArtifactsRepoUrl"] as String
val codingArtifactsUsername = localProperties["codingArtifactsUsername"] as String
val codingArtifactsPassword = localProperties["codingArtifactsPassword"] as String

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

// 发布项目jar到coding仓库
publishing {
	repositories {
		maven {
			url = uri(codingArtifactsRepoUrl)
			credentials {
				username = codingArtifactsUsername
				password = codingArtifactsPassword
			}
		}
	}
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}
}

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
	api(project(":vertex-spring-boot-starter"))
//	api("io.vertex:vertex-spring-boot-starter:$vertexSpringBootStarterVersion")
	api("io.vertx:vertx-web:$vertxVersion")
	api("io.vertx:vertx-web-client:$vertxVersion")
}

tasks.getByName<Jar>("jar") {
	archiveClassifier.set("")
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

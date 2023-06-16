import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	id("maven-publish")
}

val vertexSpringBootVersion: String by project
val vertexSpringBootStarterVersion: String by project

group = "io.vertex"
version = vertexSpringBootStarterVersion
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
	api("org.springframework.boot:spring-boot-starter")
	api(project(":vertex-spring-boot"))
//	api("io.vertex:vertex-spring-boot:$vertexSpringBootVersion")
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

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
val jakartaWebsocketVersion: String by project

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
	implementation(project(":vertex-actuator-spring-boot-starter"))
	implementation("jakarta.websocket:jakarta.websocket-api:$jakartaWebsocketVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation(project(":vertex-web-test-spring-boot-starter"))
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

tasks.withType<Test> {
	useJUnitPlatform()

	// 为了去掉仅执行部分测试而报的警告：tests were Method or class mismatch
	// 方案见：https://stackoverflow.com/questions/66586272/running-a-single-junit5-test-on-gradle-exits-with-standard-error
	systemProperty("java.util.logging.config.file", "${project.layout.buildDirectory}/resources/test/logging-test.properties")
	setForkEvery(1)

	testLogging {
		showStandardStreams = true
	}
}

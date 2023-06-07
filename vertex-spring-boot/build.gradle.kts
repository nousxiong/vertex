import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.jetbrains.kotlin.kapt") version "1.8.21"
	kotlin("jvm") version "1.8.21"
	kotlin("plugin.spring") version "1.8.21"
	id("maven-publish")
}

group = "io.vertex"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

val vertxVersion: String by project

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

// 设置源代码jar任务，为之后的发布jar做准备
val sourcesJar by tasks.registering(Jar::class) {
	dependsOn(tasks.getByName("classes"))
	from(sourceSets["main"].allSource)
	archiveClassifier.set("sources")
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
			artifact(sourcesJar)
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
	implementation("org.springframework.boot:spring-boot-autoconfigure")
	api("org.springframework.boot:spring-boot-starter-webflux") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-reactor-netty")
	}
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	api("io.vertx:vertx-core:${vertxVersion}")
	api("io.vertx:vertx-lang-kotlin:${vertxVersion}")
	api("io.vertx:vertx-lang-kotlin-coroutines:${vertxVersion}")
	compileOnly("io.vertx:vertx-web:$vertxVersion")
	compileOnly("io.vertx:vertx-web-client:$vertxVersion")
	kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation(kotlin("test"))
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

tasks.withType<Test> {
	useJUnitPlatform()

	// 为了去掉仅执行部分测试而报的警告：tests were Method or class mismatch
	// 方案见：https://stackoverflow.com/questions/66586272/running-a-single-junit5-test-on-gradle-exits-with-standard-error
	systemProperty("java.util.logging.config.file", "${project.buildDir}/resources/test/logging-test.properties")
	setForkEvery(1)

	testLogging {
		showStandardStreams = true
	}
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	id("org.jetbrains.kotlin.kapt")
	kotlin("jvm")
	kotlin("plugin.spring")
	id("maven-publish")
}

val javaVersion: String by project
val kotlinJvmTarget: String by project
val vertexSpringBootVersion: String by project
val vertxVersion: String by project
val springmockkVersion: String by project

group = "io.vertex"
version = vertexSpringBootVersion
java.sourceCompatibility = JavaVersion.valueOf(javaVersion)

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
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor:reactor-core")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	//#region vertex.core
	implementation("io.vertx:vertx-core:${vertxVersion}")
	implementation("io.vertx:vertx-lang-kotlin:${vertxVersion}")
	implementation("io.vertx:vertx-lang-kotlin-coroutines:${vertxVersion}")
	//#endregion vertex.core
	//#region vertex.web
	implementation("org.springframework.boot:spring-boot-starter-webflux") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-reactor-netty")
	}
	implementation("io.vertx:vertx-web:$vertxVersion")
	implementation("io.vertx:vertx-web-client:$vertxVersion")
	//#endregion vertex.web
	annotationProcessor("io.vertx:vertx-codegen:$vertxVersion")
	kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation(kotlin("test"))
}

tasks.getByName<Jar>("jar") {
	archiveClassifier.set("")
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

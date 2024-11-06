val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val h2_version: String by project
val postgres_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    idea
}

group = "com.farmstandtracker"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.51.1")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")

    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.2")
    testImplementation("org.testcontainers:testcontainers:1.20.2")
    testImplementation("org.testcontainers:postgresql:1.20.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

sourceSets {
    create("integrationTest") {
        kotlin.srcDir("$projectDir/src/integrationTest/kotlin")
        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
}

val integrationTestImplementation by configurations.getting { extendsFrom(configurations.testImplementation.get()) }

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTest =
    task<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter("test")
        useJUnitPlatform()
        testLogging { events("passed") }
    }

idea {
    module {
        testSources.setFrom(sourceSets["integrationTest"].java.srcDirs)
        testResources.setFrom(sourceSets["integrationTest"].resources.srcDirs)
    }
}
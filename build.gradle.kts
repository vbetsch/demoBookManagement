import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    id("jacoco")
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.8.22"
    id("info.solidsoft.pitest") version "1.15.0"
}

group = "com.jicay"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

jacoco {
    toolVersion = "0.8.7"
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}


sourceSets {
    create("testIntegration") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

repositories {
    mavenCentral()
}

val testIntegrationImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.liquibase:liquibase-core")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.1.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
    testImplementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
    testImplementation("net.jqwik:jqwik-kotlin:1.8.1")

    testIntegrationImplementation("io.mockk:mockk:1.13.8")
    testIntegrationImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
    testIntegrationImplementation("com.ninja-squad:springmockk:4.0.2")
    testIntegrationImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testIntegrationImplementation("org.testcontainers:postgresql:1.19.1")
    testIntegrationImplementation("org.testcontainers:junit-jupiter:1.19.1")
    testIntegrationImplementation("org.testcontainers:jdbc-test:1.12.0")
    testIntegrationImplementation("org.testcontainers:testcontainers:1.19.1")
    testIntegrationImplementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

task<Test>("testIntegration") {
    useJUnitPlatform()
    testClassesDirs = sourceSets["testIntegration"].output.classesDirs
    classpath = sourceSets["testIntegration"].runtimeClasspath
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JacocoReport>("jacocoFullReport") {
    executionData(tasks.named("test").get())
    sourceSets(sourceSets["main"])

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

pitest {
    targetClasses.add("com.jicay.bookmanagement.*")
    junit5PluginVersion = "1.0.0"
    avoidCallsTo.set(setOf("kotlin.jvm.internal"))
    mutators.set(setOf("STRONGER"))
    threads.set(Runtime.getRuntime().availableProcessors())
    testSourceSets.addAll(sourceSets["test"])
    mainSourceSets.addAll(sourceSets["main"])
    outputFormats.addAll("XML", "HTML")
    excludedClasses.add("**BookManagementApplication")
}

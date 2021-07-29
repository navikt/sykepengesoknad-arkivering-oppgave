import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.spring") version "1.5.21"
}

group = "no.nav.helse.flex"
version = "0.0.1-SNAPSHOT"
description = "syfogsak"
java.sourceCompatibility = JavaVersion.VERSION_14

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.1.0")
    }
}

ext["okhttp3.version"] = "4.9.0" // For at token support testen kjører (tror jeg)

val githubUser: String by project
val githubPassword: String by project

apply(plugin = "org.jlleitschuh.gradle.ktlint")

repositories {
    mavenCentral()

    maven {
        url = uri("https://maven.pkg.github.com/navikt/maven-release")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

val syfoKafkaVersion = "2021.07.20-09.39-6be2c52c"
val mockitoKotlinVersion = "2.2.0"
val ojdbc8Version = "19.3.0.0"
val logstashLogbackEncoderVersion = "6.6"
val mockitoCoreVersion = "3.10.0"
val assertjVersion = "3.20.2"
val tjenestespesifikasjonerVersion = "1.2019.08.16-13.46-35cbdfd492d4"
val cxfVersion = "3.4.3"
val tokenSupportVersion = "1.3.8"
val testContainersVersion = "1.16.0"
val kluentVersion = "1.68"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("javax.jws:javax.jws-api:1.1")
    implementation("javax.inject:javax.inject:1")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("no.nav.syfo.kafka:kafkautils:$syfoKafkaVersion")
    implementation("no.nav.syfo.kafka:legacy-serialisering:$syfoKafkaVersion")
    implementation("no.nav.syfo.kafka:felles:$syfoKafkaVersion")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.sun.xml.messaging.saaj:saaj-impl:1.5.1")
    implementation("javax.xml.soap:saaj-api:1.3.5")
    implementation("javax.xml.ws:jaxws-api:2.3.1")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jta-atomikos")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.oracle.ojdbc:ojdbc8:$ojdbc8Version")
    implementation("org.flywaydb:flyway-core")
    implementation("org.slf4j:slf4j-api")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("no.nav.tjenestespesifikasjoner:nav-fim-behandleJournal-v2-tjenestespesifikasjon:$tjenestespesifikasjonerVersion")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    implementation("com.h2database:h2")

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-core:$mockitoCoreVersion")
    testImplementation("org.awaitility:awaitility")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "14"
        if (System.getenv("CI") == "true") {
            kotlinOptions.allWarningsAsErrors = true
        }
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}

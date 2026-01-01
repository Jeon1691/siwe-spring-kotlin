import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Jackson Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Ethereum signature recovery (personal_sign / eth_sign)
    // Web3j 4.10.3 depends on older libs with CVEs, forcing upgrades
    implementation("org.web3j:core:4.10.3") {
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okio:okio:3.9.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    
    // Force upgrade commons-lang3 to fix CVE-2025-48924 if possible, though 3.17.0 is quite new.
    // Checking if a newer version exists or just explicitly stating it to override transitive deps.
    implementation("org.apache.commons:commons-lang3:3.18.0")

    // Swagger / OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")
    implementation("io.swagger.core.v3:swagger-models:2.2.27")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.27")
    implementation("io.swagger.core.v3:swagger-core:2.2.27")
    
    // JAXB API
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    // JWT (JJWT)
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

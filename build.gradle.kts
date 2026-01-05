plugins {
    java
    id("io.quarkus")
    alias(libs.plugins.spotless)
    jacoco
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-mutiny")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    implementation("io.quarkus:quarkus-cache")

    // Vert.x WebClient for reactive HTTP
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client")

    // MapStruct
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    // Error Prone (static analysis)
    annotationProcessor(libs.errorprone.core)
    compileOnly(libs.errorprone.annotations)

    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.rest.assured)
}

group = "com.aggregator"
version = "1.0-SNAPSHOT"

extra.apply {
    set("jacocoVersion", libs.versions.jacoco.get())
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

spotless {
    java {
        palantirJavaFormat()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.withType<Test>())
    reports {
        xml.required.set(false)
        html.required.set(true)
        csv.required.set(false)
    }
}

jacoco {
    toolVersion = property("jacocoVersion").toString()
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

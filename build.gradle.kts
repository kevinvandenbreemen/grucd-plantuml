plugins {
    kotlin("jvm") version "1.5.10"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    val javaParserVersion = "3.22.1"
    implementation("com.github.javaparser:javaparser-symbol-solver-core:$javaParserVersion")

    val kluentVersion = "1.68"
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")

    val plantUmlVersion = "1.0.39"
    implementation("com.credibledoc:plantuml-core:$plantUmlVersion")

    val log4jVersion = "1.2.14"
    implementation("log4j:log4j:$log4jVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
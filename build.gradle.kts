plugins {
    kotlin("jvm") version "1.5.10"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
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

    val kevinCommonVersion = "1.0.4"
    implementation("com.github.kevinvandenbreemen:kevin-common:$kevinCommonVersion")

    val kotlinParserVersion = "bf1da05656"
    implementation("com.github.kotlinx.ast:common:$kotlinParserVersion")
    implementation("com.github.kotlinx.ast:grammar-kotlin-parser-antlr-kotlin:$kotlinParserVersion")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
plugins {
    kotlin("jvm") version "1.5.10"
    java
    `maven-publish`
    `java-library`
}

val versionRaw = File("src/main/resources/version.properties")
val versionNumber = versionRaw.readText()

group = "com.vandenbreemen"
version = versionNumber

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    val kluentVersion = "1.68"
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")

    val plantUmlLibVersion = "1.2023.12"
    implementation("net.sourceforge.plantuml:plantuml-mit:$plantUmlLibVersion")


    val log4jVersion = "1.2.14"
    implementation("log4j:log4j:$log4jVersion")

    val kevinCommonVersion = "1.0.6.1000"
    implementation("com.github.kevinvandenbreemen:kevin-common:$kevinCommonVersion")

    val grucdVersion = "1.0.5.1000"
    implementation("com.github.kevinvandenbreemen:grucd:$grucdVersion")
    testImplementation("com.github.kevinvandenbreemen:grucd:$grucdVersion")

}

val fatJar = task("FatJar", type = Jar::class) {

    val jarName = "grucd.jar"

    archiveFileName.set(jarName)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["Main-Class"] = "com.vandenbreemen.grucd.main.Main"

    }
    from(configurations.runtimeClasspath.get().map {
        if(it.isDirectory) it else zipTree(it)
    })
    with(tasks.jar.get() as CopySpec)

    copy {
        from("build/libs/$jarName")
        into("./")
    }
    println("Built and copied $jarName")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

//  Based on https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/maven-publish/build.gradle.kts
val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
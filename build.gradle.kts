plugins {
    kotlin("jvm") version "1.9.20"
    java
    application
    id("me.champeau.jmh") version "0.7.2"
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.openjdk.jcstress:jcstress-core:0.16")
    testAnnotationProcessor("org.openjdk.jcstress:jcstress-core:0.16")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}

// JCStress runner task: runs JCStress tests located on the test runtime classpath
// Use: ./gradlew jcstress [-PjcstressArgs="-v -m quick"]
tasks.register<JavaExec>("jcstress") {
    group = "verification"
    description = "Run JCStress stress tests"
    mainClass.set("org.openjdk.jcstress.Main")
    classpath = sourceSets.test.get().runtimeClasspath
    dependsOn("testClasses")

    val argsProp = project.findProperty("jcstressArgs") as String?
    if (!argsProp.isNullOrBlank()) {
        args = argsProp.split("\\s+".toRegex())
    }
}

// JMH configuration for benchmarks
// Use: ./gradlew jmh -Pjmh.include=org.itmo.BFSBenchmark
jmh {
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(1)
    timeOnIteration.set("1s")
}


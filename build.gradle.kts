plugins {
    `java-library`
    id("me.champeau.jmh") version("0.6.7")
}

group = "org.rgbhsv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-modules=jdk.incubator.vector", "--enable-preview")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("--add-modules=jdk.incubator.vector", "--enable-preview"))
}

java {
    // todo: modularity.inferModulePath = true
    toolchain {
        // to get this to work, please download the jdk16 pre-release binaries
        // and set the JDK16 env var (e.g. `export JDK16=~/Downloads/jdk-16/Contents/Home && ./gradlew test`)
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

dependencies {
    jmh(libs.jmh.core)
    jmh(libs.jmh.generator.annprocess)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

jmh {
    warmupIterations.set(2)
    iterations.set(2)
    fork.set(2)
    jvmArgs.addAll(listOf("--add-modules=jdk.incubator.vector", "--enable-preview"))
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(19))
    })
}

tasks.jmhRunBytecodeGenerator {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(19))
    })
    jvmArgs.addAll("--add-modules=jdk.incubator.vector", "--enable-preview")
}

tasks.jmhCompileGeneratedClasses {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(19))
    })
}

tasks.jmh {
    jvmArgs.addAll(listOf("--add-modules=jdk.incubator.vector", "--enable-preview"))
}

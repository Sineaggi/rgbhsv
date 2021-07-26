plugins {
    `java-library`
}

group = "org.rgbhsv"
version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
    //jvmArgs?.add("--add-modules=jdk.incubator.vector")
    //jvmArgs?.add("--enable-preview")
    jvmArgs("--add-modules=jdk.incubator.vector", "--enable-preview")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--add-modules=jdk.incubator.vector", "--enable-preview"))
}

java {
    // todo: modularity.inferModulePath = true
    toolchain {
        // to get this to work, please download the jdk16 pre-release binaries
        // and set the JDK16 env var (e.g. `export JDK16=~/Downloads/jdk-16/Contents/Home && ./gradlew test`)
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

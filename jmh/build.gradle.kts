plugins {
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.32")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.32")
    implementation(projects.rgbhsv)
}

tasks.test {
    useJUnitPlatform()
    //jvmArgs?.add("--add-modules=jdk.incubator.vector")
    //jvmArgs?.add("--enable-preview")
    jvmArgs("--add-modules=jdk.incubator.vector", "--enable-preview")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--add-modules=jdk.incubator.vector", "--enable-preview"))
}

application {
    mainClass.set("org.rgbhsv.JmhMain")
    applicationDefaultJvmArgs = listOf("--add-modules=jdk.incubator.vector", "--enable-preview")
}

plugins {
    `java-library`
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

}

tasks.test {
    //jvmArgs?.add("--add-modules=jdk.incubator.vector")
    //jvmArgs?.add("--enable-preview")
    jvmArgs("--add-modules=jdk.incubator.vector", "--enable-preview")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--add-modules=jdk.incubator.vector", "--enable-preview"))
}

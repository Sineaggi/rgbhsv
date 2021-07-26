plugins {
    id("rgbhsv.java-conventions")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.32")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.32")
    implementation(projects.rgbhsv)
}

application {
    mainClass.set("org.rgbhsv.JmhMain")
    applicationDefaultJvmArgs = listOf("--add-modules=jdk.incubator.vector", "--enable-preview")
}

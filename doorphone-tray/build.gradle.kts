plugins {
    java
    application
}

group = "kg.musabaev"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":doorphone-core"))

    implementation("ch.qos.logback:logback-classic:1.5.32")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

application {
    mainClass = "kg.musabaev.doorphone.tray.Main"
}

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
    implementation(project(":core"))
}

application {
    mainClass = "kg.musabaev.Main"
}

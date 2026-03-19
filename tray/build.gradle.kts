plugins {
    java
    application
}

group = "kg.musabaev"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass = "kg.musabaev.Main"
}

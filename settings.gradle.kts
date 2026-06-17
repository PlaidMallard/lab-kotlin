pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

rootProject.name = "lab-kotlin"
include("app")

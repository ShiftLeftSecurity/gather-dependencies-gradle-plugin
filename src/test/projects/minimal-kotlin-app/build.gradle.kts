buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

plugins {
   kotlin("jvm") version "1.6.21"
   java
   application
   id("io.shiftleft.gather-dependencies") version "0.5"
}

application {
  mainClass.set("com.example.MinKt")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")
    implementation("com.google.guava:guava:31.1-jre")
}

gatherDependencies {
  configurationName.set("default")
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    kotlin("jvm") version "1.5.31"
    id("com.gradle.plugin-publish") version "0.18.0"
}

group = "io.shiftleft"
version = "0.5"

pluginBundle {
    website = "https://github.com/ShiftLeftSecurity/gather-dependencies-gradle-plugin"
    vcsUrl = "https://github.com/ShiftLeftSecurity/gather-dependencies-gradle-plugin"
    tags = listOf("dependencies")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation("com.android.tools.build:gradle:4.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("gather-dependencies") {
            id = "io.shiftleft.gather-dependencies"
            displayName = "Gather Dependencies"
            description = "Copy all the dependency jars of a project into a destination directory"
            implementationClass = "io.shiftleft.GatherDependenciesPlugin"
        }
    }
}

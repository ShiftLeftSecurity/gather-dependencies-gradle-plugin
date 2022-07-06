plugins {
  id("com.android.application") version "7.2.0"
  id("io.shiftleft.gather-dependencies") version "0.3"
}

android {
    compileSdkVersion(31)
    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
    }
}

gatherDependencies {
  outputDirectory.set("$buildDir/customOutputDirectory")
}

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

    buildTypes {
      // this is how you define a new custom Android variant
      // for more info check https://developer.android.com/studio/releases/gradle-plugin
       create("myCustomVariant") {
          initWith(getByName("release"))
       }
    }
}

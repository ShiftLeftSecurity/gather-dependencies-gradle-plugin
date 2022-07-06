# gather-dependencies-gradle-plugin

A Gradle Plugin which resolves all the dependencies of a project and places them
into a directory.


### Development

Generate Gradle wrapper:
```
$ gradle wrapper
```

Publish to Maven Local:
```
$ ./gradlew publishToMavenLocal
```

Run tests:
```
$ ./gradlew test
```

### Usage

#### Applying the plugin

```
# build.gradle
plugins {
    id 'io.shiftleft.gather-dependencies' version '<version>'
 }
```

```
# build.gradle.kts
plugins {
    id("io.shiftleft.gather-dependencies") version "<version>"
}
```

#### Plugin configuration

The plugin accepts three configuration values: `outputDirectory`, `androidVariantName`, and `configurationName`.

`outputDirectory` allows you to specify the path to a directory in which the dependencies will be placed.
Pointing to a new path will create the directory.

`androidVariantName` allows you to specify the name of a variant defined for projects configured with the Android Gradle Plugin.
It defaults to _release_.

`configurationName` allows you to specify a Gradle configuration from which to copy the dependencies.
If set, the value specified in `androidVariantName` will be ignored.

For Android projects:

```
#...build.gradle
gatherDependencies {
    outputDirectory = "$buildDir/gatheredDependencies"
    androidVariantName = 'release'
}
```

```
#...build.gradle.kts
gatherDependencies {
    outputDirectory.set("$buildDir/gatheredDependencies")
    androidVariantName.set("release")
}
```

For a specific configuration:

```
#...build.gradle
gatherDependencies {
    outputDirectory = "$buildDir/gatheredDependencies"
    configurationName = 'default'
}
```

```
#...build.gradle.kts
gatherDependencies {
    outputDirectory.set("$buildDir/gatheredDependencies")
    configurationName.set("default")
}
```

### Try locally

1. Publish plugin to local maven repository

2. Run `gatherDependencies` task for the _SlimAndroid_ test project:

```
$ cd SlimAndroid
$ gradle wrapper
$ ./gradlew gatherDependencies
$ find build/gatheredDependencies -name "*jar" | wc -l
39
```

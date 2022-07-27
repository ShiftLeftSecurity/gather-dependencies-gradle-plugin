## AppWithLibModule

This Android application is an example for how to apply ShiftLeft's
`gather-dependencies` Gradle Plugin in a project with a library module.

### How to execute the task defined by the Gradle Plugin

```
$ gradle wrapper
$ ./gradlew ':app:gatherDependencies'
$ ls build/gatheredDependencies | grep 'jar$' | wc -l
45
```

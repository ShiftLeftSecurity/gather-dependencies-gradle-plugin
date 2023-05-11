# springboot-kotlin-webgoat

Simple vulnerable Spring Boot Application written in Kotlin

## Run

```
$ ./gradlew bootRun
```

## Vulnerabilities

```
$ grep -R -A 1 vulnerability src
src/main/kotlin/ai/qwiet/springbootkotlinwebgoat/HelloController.kt:    // vulnerability: XSS
src/main/kotlin/ai/qwiet/springbootkotlinwebgoat/HelloController.kt-    return "Greetings ${username}!"
```

## Examples of HTTP endpoints querying

```
$ curl localhost:8080
Greetings from Spring Boot
```

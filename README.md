# Kotlin starter for Wodby

A production-oriented HTTP application for the [Wodby Kotlin service](https://github.com/wodby/service-kotlin) and
[Kotlin stack](https://github.com/wodby/stack-kotlin).

It uses [Ktor](https://ktor.io) with the Netty engine and demonstrates:

- routing with JSON responses through kotlinx.serialization
- packaged HTML and static assets
- health, not-found, method-not-allowed and error responses
- request logging with Logback
- graceful shutdown on SIGTERM
- tests with Ktor's test host, and Wodby CI

## Local development

Java 21 or newer is required. The Gradle wrapper downloads Gradle itself.

```shell
./gradlew test
./gradlew run
```

Open <http://localhost:8080>. Useful endpoints are:

- `/` — the landing page
- `/assets/styles.css` — a packaged static resource
- `/api/status` — a JSON response
- `/healthz` — the deployment health endpoint

Set `PORT` to use a different local port.

## Project structure

`src/main/kotlin/com/example/Application.kt` contains the server and its routes. Files under
`src/main/resources` are packaged into the application's JAR: the page template, static assets and the Logback
configuration.

`./gradlew assemble` writes one self-contained jar, `build/libs/kotlin-boilerplate.jar`, with the application and
its dependencies (the Shadow plugin). That is what the Wodby Java image runs with `java -jar`. The `Dockerfile` builds
it and copies only the jar into the image.

The code is compiled for Java 21, so the application runs on both JDKs the Wodby Kotlin service offers.
Dependency versions are in `gradle/libs.versions.toml`.

## In a Wodby development workspace

The workspace builds the jar with the Gradle wrapper and runs it. When a file changes, it builds again and restarts
the application; nothing needs to be deployed for a code change.

The health endpoint intentionally avoids optional services. PostgreSQL, Valkey, and SMTP links can be enabled later
through Wodby's documented environment variables.

ARG WODBY_BASE_IMAGE
FROM ${WODBY_BASE_IMAGE} AS builder

# Download Gradle and the dependencies in their own layer, so a source change reuses them.
ARG COPY_FROM
COPY --chown=wodby:wodby ${COPY_FROM}/gradlew ${COPY_FROM}/settings.gradle.kts ${COPY_FROM}/build.gradle.kts /usr/src/app/
COPY --chown=wodby:wodby ${COPY_FROM}/gradle /usr/src/app/gradle
RUN sh ./gradlew --no-daemon -q dependencies > /dev/null

COPY --chown=wodby:wodby ${COPY_FROM} /usr/src/app
RUN java-build /home/wodby/app.jar

FROM ${WODBY_BASE_IMAGE}
COPY --from=builder --chown=wodby:wodby /home/wodby/app.jar /home/wodby/app.jar

CMD ["java-app"]

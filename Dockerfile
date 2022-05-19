FROM navikt/java:17-appdynamics

COPY build/libs/eessi-pensjon-saksbehandling-api.jar /app/app.jar

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER saksbehandling-api
ENV APPD_ENABLED true

FROM navikt/java:11-appdynamics

COPY build/libs/eessi-pensjon-frontend-api-fss.jar /app/app.jar

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER saksbehandling-api
ENV APPD_ENABLED true

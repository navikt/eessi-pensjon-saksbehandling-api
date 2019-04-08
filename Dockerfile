FROM navikt/java:8-appdynamics

COPY build/libs/eessi-pensjon-frontend-api*.jar /app/app.jar

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER api-fss
ENV APPD_ENABLED true

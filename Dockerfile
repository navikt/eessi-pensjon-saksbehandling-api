FROM navikt/java:8-appdynamics

COPY build/libs/eessi-pensjon-frontend-api-fss*.jar /app/app.jar

COPY nais/export-vault-secrets.sh /init-scripts/
RUN chmod +x /init-scripts/*

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER saksbehandling-api
ENV APPD_ENABLED true

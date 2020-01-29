FROM navikt/java:8-appdynamics

COPY build/libs/eessi-pensjon-saksbehandling-api*.jar /app/app.jar

COPY nais/export-vault-secrets.sh /init-scripts/
RUN chmod +x /init-scripts/*

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER api-fss
ENV APPD_ENABLED true

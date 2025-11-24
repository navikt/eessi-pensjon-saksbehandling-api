FROM ghcr.io/navikt/baseimages/temurin:21

COPY init-scripts/ep-jvm-tuning.sh /init-scripts/

COPY build/libs/eessi-pensjon-saksbehandling-api.jar /app/app.jar
COPY nais/export-vault-secrets.sh /init-scripts/

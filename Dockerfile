FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

COPY init-scripts/ep-jvm-tuning.sh /init-scripts/

COPY build/libs/eessi-pensjon-saksbehandling-api.jar /app/app.jar
COPY nais/export-vault-secrets.sh /init-scripts/
CMD ["-jar","app.jar"]
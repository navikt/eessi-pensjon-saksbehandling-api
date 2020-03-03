![](https://github.com/navikt/eessi-pensjon-saksbehandling-api/workflows/Bygg%20og%20deploy%20Q2/badge.svg)
![](https://github.com/navikt/eessi-pensjon-saksbehandling-api/workflows/Manuell%20deploy/badge.svg)


EESSI Pensjon Frontend API
===============================

This is the web application's API module for the EESSI Pensjon, dveloped in Kotlin.

## INSTALL

run './gradlew.bat assemble' (Windows) or './gradlew assemble' (Mac/Linux) if it's the first time you run this app, or everytime there is some code changes.

## TEST

#### Kjøre tester
run `ApplicationTests.kt`

#### S3

Miljøvariabler for S3 må settes for å kjøre testene uten feil. Disse finnes i fasit.
Søk opp *eessi_pensjon_frontend_api_s3_creds* (t eller q) for å finne username og passord. 

Legg inn følgende variabelnavn med brukernavn og passord fra fasit:
* varibelnavn: eessi_pensjon_frontend_api_s3_creds_username 
* variabelnavn: eessi_pensjon_frontend_api_s3_creds_password


## RUN 

### Development

Make sure you have ADEO_MAVEN_URL, ADEO_MAVEN_USERNAME and ADEO_MAVEN_PASSWORD in your env.
Run in your IDE the `Application.kt` file with VM options `-Dspring.profiles.active=local`.

It will start the service in port 8080.

### Aurthentication as localhost

In order to get a local authenticated token that allows your local instances able to interact with other
services, you need to point your browser to:

      http://localhost:{port}/locallogin?redirect={redirectUrl}

The common case where you have the frontend-ui running on localhost:3000 and the frontend-api
running on localhost:8080, then visit:

       http://localhost:8080/locallogin?redirect=http://localhost:3000

And the frontend will load with an authentication cookie for subject 12345678910

### Production

Same as development

## Troubleshoot

### When backend complains about certificate access to other 3rd party https urls...

On any page of https://(.*).preprod.local, click on the certificate, download it as a file (say C:\Temp\preprod.cer)

Launch the kse explorer, the JAR executable file (kse-51), load cacerts from your JAVA_HOME\lib\security.

Import the downloaded certificate into cacerts, save it (if you don't have permissions, save it in Temp, then copy over).

# Utvikling

## Oppdatere avhengigheter

Sjekke om man har utdaterte avhengigheter (forsøker å unngå milestones og beta-versjoner):

```
./gradlew dependencyUpdates
```

Dersom du er supertrygg på testene kan du forsøke en oppdatering av alle avhengighetene:

```
./gradlew useLatestVersions && ./gradlew useLatestVersionsCheck
```

Enkelte interne artifakter har ikke komplette metadata i repo.adeo.no - noe som gir en advarsel:

```
 Failed to determine the latest version for the following dependencies (use --info for details):
 - no.nav.meldinger.virksomhet:nav-virksomhet-varsel-v1-meldingsdefinisjon
 - no.nav.tjenester:nav-person-v3-tjenestespesifikasjon
 ```

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #eessi-pensjonpub.

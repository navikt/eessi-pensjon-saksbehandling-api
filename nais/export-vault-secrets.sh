#!/usr/bin/env bash

echo "Sjekker eessi-pensjon-saksbehandling-api srvPassord"
if test -f /var/run/secrets/nais.io/srveessipensjon/password;
then
  echo "Setter eessi-pensjon-saksbehandling-api srvPassord"
    export srvpassword=$(cat /var/run/secrets/nais.io/srveessipensjon/password)
fi

echo "Sjekker eessi-pensjon-saksbehandling-api srvUsername"
if test -f /var/run/secrets/nais.io/srveessipensjon/username;
then
    echo "Setter eessi-pensjon-saksbehandling-api srvUsername"
    export srvusername=$(cat /var/run/secrets/nais.io/srveessipensjon/username)
fi

#Vault path
#/kv/preprod/fss/eessi-pensjon-frontend-api-fss/q2

echo "Sjekker eessi_pensjon_s3_crypto_password"
if test -f /var/run/secrets/nais.io/appsecrets/eessi_pensjon_s3_crypto_password;
then
  echo "Setter eessi_pensjon_s3_crypto_password"
    export eessi_pensjon_s3_crypto_password=$(cat /var/run/secrets/nais.io/appsecrets/eessi_pensjon_s3_crypto_password)
fi

#Vault path
#/secrets/credential/dev/eessi-pensjon-fss

echo "Sjekker eessi_pensjon_frontend_api_s3_creds_password"
if test -f /var/run/secrets/nais.io/appcredentials/password;
then
  echo "Setter eessi_pensjon_frontend_api_s3_creds_password"
    export eessi_pensjon_frontend_api_s3_creds_password=$(cat /var/run/secrets/nais.io/appcredentials/password)
fi
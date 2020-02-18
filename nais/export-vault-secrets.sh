#!/usr/bin/env bash

echo "Sjekker eessi-pensjon-frontend-api-fss srvPassord"
if test -f /var/run/secrets/nais.io/srveessipensjon/password;
then
  echo "Setter eessi-pensjon-frontend-api-fss srvPassord"
    export srvpassword=$(cat /var/run/secrets/nais.io/srveessipensjon/password)
fi

echo "Sjekker eessi-pensjon-frontend-api-fss srvUsername"
if test -f /var/run/secrets/nais.io/srveessipensjon/username;
then
    echo "Setter eessi-pensjon-frontend-api-fss srvUsername"
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

echo "Sjekker isso_agent_password"
if test -f /var/run/secrets/nais.io/appsecrets/isso_agent_password;
then
  echo "Setter isso_agent_password"
    export isso_agent_password=$(cat /var/run/secrets/nais.io/appsecrets/isso_agent_password)
fi

echo "Sjekker aad_b2c_clientid_password"
if test -f /var/run/secrets/nais.io/appsecrets/aad_b2c_clientid_password;
then
  echo "Setter aad_b2c_clientid_password"
    export aad_b2c_clientid_password=$(cat /var/run/secrets/nais.io/appsecrets/aad_b2c_clientid_password)
fi

echo "Sjekker aad_b2c_clientid_username"
if test -f /var/run/secrets/nais.io/appsecrets/aad_b2c_clientid_username;
then
  echo "Setter aad_b2c_clientid_username"
    export aad_b2c_clientid_username=$(cat /var/run/secrets/nais.io/appsecrets/aad_b2c_clientid_username)
fi

echo "Sjekker OpenIdConnectAgent_password"
if test -f /var/run/secrets/nais.io/appsecrets/OpenIdConnectAgent_password;
then
  echo "Setter OpenIdConnectAgent_password"
    export OpenIdConnectAgent_password=$(cat /var/run/secrets/nais.io/appsecrets/OpenIdConnectAgent_password)
fi

echo "Sjekker whitelist_users"
if test -f /var/run/secrets/nais.io/appsecrets/whitelist_users;
then
  echo "Setter whitelist_users"
    export whitelist_users=$(cat /var/run/secrets/nais.io/appsecrets/whitelist_users)
fi

#Vault path
#/secrets/credential/dev/eessi-pensjon-fss

echo "Sjekker eessi_pensjon_frontend_api_s3_creds_password"
if test -f /var/run/secrets/nais.io/appcredentials/password;
then
  echo "Setter eessi_pensjon_frontend_api_s3_creds_password"
    export eessi_pensjon_frontend_api_s3_creds_password=$(cat /var/run/secrets/nais.io/appcredentials/password)
fi
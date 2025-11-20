#!/usr/bin/env bash

#echo "Sjekker eessi-pensjon-journalforing srvPassord"
#if test -f /var/run/secrets/nais.io/srveessi-pensjon-journalforing/password;
#then
#  echo "Setter eessi-pensjon-journalforing srvPassord"
#    export srvpassword=$(cat /var/run/secrets/nais.io/srveessi-pensjon-journalforing/password)
#fi
#
#echo "Sjekker eessi-pensjon-journalforing srvUsername"
#if test -f /var/run/secrets/nais.io/srveessi-pensjon-journalforing/username;
#then
#    echo "Setter eessi-pensjon-journalforing srvUsername"
#    export srvusername=$(cat /var/run/secrets/nais.io/srveessi-pensjon-journalforing/username)
#fi


# Team namespace Q2
#echo "Sjekker srvpassword eessi-pensjon-journalforing q2 i team namespace"
#if test -f /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q2/password;
#then
#  echo "Setter srvpassword eessi-pensjon-journalforing q2 i team namespace"
#    export srvpassword=$(cat /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q2/password)
#fi
#
#echo "Sjekker srvusername i eessi-pensjon-journalforing q2 i team namespace"
#if test -f /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q2/username;
#then
#    echo "Setter srvusername i eessi-pensjon-journalforing q2 i team namespace"
#    export srvusername=$(cat /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q2/username)
#fi

# Team namespace Q2/Q1
echo "Sjekker srvfagmodulpassword eessi-pensjon-fagmodul"
if test -f /var/run/secrets/nais.io/srveessipensjon/password;
then
  echo "Setter srvfagmodulpassword eessi-pensjon-fagmodul"
    export srvfagmodulpassword=$(cat /var/run/secrets/nais.io/srveessipensjon/password)
fi

echo "Sjekker srvfagmodulusername i eessi-pensjon-fagmodul"
if test -f /var/run/secrets/nais.io/srveessipensjon/username;
then
    echo "Setter srvfagmodulusername i eessi-pensjon-fagmodul"
    export srvfagmodulusername=$(cat /var/run/secrets/nais.io/srveessipensjon/username)
fi

#echo "Sjekker srvpdlppassword for eessi-pensjon-pdl-produsent"
#if test -f /var/run/secrets/nais.io/srveessipensjonpdlp/password;
#then
#  echo "Setter srvpdlppassword for eessi-pensjon-pdl-produsent"
#    export srvpdlppassword=$(cat /var/run/secrets/nais.io/srveessipensjonpdlp/password)
#fi
#
#echo "Sjekker srvpdlppusername for i eessi-pensjon-pdl-produsent"
#if test -f /var/run/secrets/nais.io/srveessipensjonpdlp/username;
#then
#    echo "Setter srvpdlppusername i eessi-pensjon-pdl-produsent"
#    export srvpdlpusername=$(cat /var/run/secrets/nais.io/srveessipensjonpdlp/username)
#fi

# Team namespace Q1
#echo "Sjekker srvpassword eessi-pensjon-journalforing q1 i team namespace"
#if test -f /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q1/password;
#then
#  echo "Setter srvpassword eessi-pensjon-journalforing q1 i team namespace"
#    export srvpassword=$(cat /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q1/password)
#fi
#
#echo "Sjekker srvusername i eessi-pensjon-journalforing q1 i team namespace"
#if test -f /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q1/username;
#then
#    echo "Setter srvusername i eessi-pensjon-journalforing q1 i team namespace"
#    export srvusername=$(cat /var/run/secrets/nais.io/srveessi-pensjon-journalforing-q1/username)
#fi
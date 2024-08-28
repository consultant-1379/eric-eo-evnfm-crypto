#!/bin/bash
#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

echo "Adding kms-ca-cert to java keystore"
keytool -storepass 'changeit' -noprompt -trustcacerts -importcert -file "/run/secrets/kms-ca-cert/ca.crt" -alias kms-ca-cert -keystore /var/lib/ca-certificates/java-cacerts 2>&1
echo "Cert has been added to keystore"
exec java -Djava.security.egd=file:/dev/./urandom -Xmx400m -jar /eric-eo-evnfm-crypto-migration.jar

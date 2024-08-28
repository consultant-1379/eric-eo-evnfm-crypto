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

ARG BASE_IMAGE_VERSION
FROM armdocker.rnd.ericsson.se/proj-am/sles/sles-corretto-openjdk17:${BASE_IMAGE_VERSION}

ARG CRYPTO_DATA_DIR="/crypto"
# User Id generated based on ADP rule DR-D1123-122 (eric-eo-evnfm-crypto : 144294)
ARG CRYPTO_GID=144294
ARG CRYPTO_UID=144294

ARG COMMIT=""
ARG APP_VERSION=""
ARG CURRENT_DATE=""
ARG PRODUCT_REVISION=""
LABEL \
    com.ericsson.product-number="unknown"\
    com.ericsson.product-revision=$PRODUCT_REVISION\
    org.opencontainers.image.title="eric-eo-evnfm-crypto"\
    org.opencontainers.image.created=$CURRENT_DATE\
    org.opencontainers.image.revision=$COMMIT\
    org.opencontainers.image.vendor="Ericsson"\
    org.opencontainers.image.version=$APP_VERSION

ADD target/eric-eo-evnfm-crypto-migration.jar eric-eo-evnfm-crypto-migration.jar

COPY entryPoint.sh /entryPoint.sh

RUN echo "${CRYPTO_UID}:x:${CRYPTO_UID}:${CRYPTO_GID}:crypto-user:/:/bin/false" >> /etc/passwd \
    && sed -i '/root/s/bash/false/g' /etc/passwd

RUN sh -c 'touch /eric-eo-evnfm-crypto-migration.jar' \
    && chmod 777 /entryPoint.sh \
    && zypper install -l -y shadow util-linux unzip \
    && zypper clean --all \
    && mkdir -p "$CRYPTO_DATA_DIR" \
    && chown -fR ${CRYPTO_UID}:0 "$CRYPTO_DATA_DIR" \
    && chmod -R g=u "$CRYPTO_DATA_DIR" \
    && chmod 777 "$CRYPTO_DATA_DIR" /tmp

RUN chown ${CRYPTO_UID}:0 /var/lib/ca-certificates/java-cacerts \
    && chmod -R g=u /var/lib/ca-certificates/java-cacerts \
    && chmod 755 /var/lib/ca-certificates/java-cacerts

RUN chown ${CRYPTO_UID}:0 /var/lib/ca-certificates/ca-bundle.pem \
    && chmod -R g=u /var/lib/ca-certificates/ca-bundle.pem \
    && chmod 755 /var/lib/ca-certificates/ca-bundle.pem

RUN chown ${CRYPTO_UID}:0 /etc/ssl/ca-bundle.pem \
    && chmod -R g=u /etc/ssl/ca-bundle.pem \
    && chmod 755 /etc/ssl/ca-bundle.pem

USER $CRYPTO_UID:$CRYPTO_GID
WORKDIR $CRYPTO_DATA_DIR
ENTRYPOINT ["sh", "-c", "/entryPoint.sh"]

EXPOSE 8888

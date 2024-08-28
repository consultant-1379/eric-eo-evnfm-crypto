/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.eo.evnfm.crypto.migration.util;

import java.util.UUID;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsEndPointEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class KmsUtil {
    private KmsUtil() {

    }

    public static ObjectMapper getObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    public static String getCustomPath(KmsEndPointEnum endPoint, UUID secretId) {
        String customPath = endPoint.getCustomSecretPath();
        if (KmsEndPointEnum.KMS_API_V1 == endPoint) {
            customPath = customPath + secretId.toString();
        }
        return customPath;
    }

}

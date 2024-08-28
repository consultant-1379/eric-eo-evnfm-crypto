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
package com.ericsson.eo.evnfm.crypto.presentation.model;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.crypto.SecretKey;

public class CipherKey {
    UUID alias;
    SecretKey key;
    LocalDateTime created;
    String creator;

    public CipherKey() {
    }

    public CipherKey(final UUID alias, final SecretKey key, final LocalDateTime created, final String creator) {
        this.alias = alias;
        this.key = key;
        this.created = created;
        this.creator = creator;
    }

    public CipherKey(final UUID alias, final SecretKey key, final LocalDateTime created) {
        this.alias = alias;
        this.key = key;
        this.created = created;
    }

    public UUID getAlias() {
        return alias;
    }

    public void setAlias(final UUID alias) {
        this.alias = alias;
    }

    public SecretKey getKey() {
        return key;
    }

    public void setKey(final SecretKey key) {
        this.key = key;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(final String creator) {
        this.creator = creator;
    }
}

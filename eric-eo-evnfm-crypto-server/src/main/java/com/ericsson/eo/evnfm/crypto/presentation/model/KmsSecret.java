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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;
import java.util.UUID;
public class KmsSecret {
    private UUID alias;
    private String key;
    private LocalDateTime created;

    public KmsSecret() {
    }

    public KmsSecret(UUID alias, String key, LocalDateTime created) {
        this.alias = alias;
        this.key = key;
        this.created = created;
    }

    public UUID getAlias() {
        return alias;
    }

    public void setAlias(UUID alias) {
        this.alias = alias;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        KmsSecret rhs = (KmsSecret) obj;
        return new EqualsBuilder()
                .append(this.alias, rhs.alias)
                .append(this.key, rhs.key)
                .append(this.created, rhs.created)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(alias)
                .append(key)
                .append(created)
                .toHashCode();
    }
}

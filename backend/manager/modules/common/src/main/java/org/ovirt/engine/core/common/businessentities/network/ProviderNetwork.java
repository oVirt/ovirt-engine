package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ProviderNetwork implements Serializable {

    private static final long serialVersionUID = -1080092693244154625L;

    private Guid providerId;

    private String externalId;

    public ProviderNetwork() {
    }

    public ProviderNetwork(Guid providerId, String externalId) {
        this.providerId = providerId;
        this.externalId = externalId;
    }

    public Guid getProviderId() {
        return providerId;
    }

    public void setProviderId(Guid providerId) {
        this.providerId = providerId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                externalId,
                providerId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProviderNetwork)) {
            return false;
        }
        ProviderNetwork other = (ProviderNetwork) obj;
        return Objects.equals(externalId, other.externalId)
                && Objects.equals(providerId, other.providerId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("providerId", getProviderId())
                .append("externalId", getExternalId())
                .build();
    }
}

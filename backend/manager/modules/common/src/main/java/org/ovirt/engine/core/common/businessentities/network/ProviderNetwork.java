package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getExternalId() == null) ? 0 : getExternalId().hashCode());
        result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProviderNetwork other = (ProviderNetwork) obj;
        if (getExternalId() == null) {
            if (other.getExternalId() != null)
                return false;
        } else if (!getExternalId().equals(other.getExternalId()))
            return false;
        if (getProviderId() == null) {
            if (other.getProviderId() != null)
                return false;
        } else if (!getProviderId().equals(other.getProviderId()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("providerId", getProviderId())
                .append("externalId", getExternalId())
                .build();
    }
}

package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ExternalLease implements BusinessEntity<Guid>, Serializable {

    private Guid leaseId;
    private Guid storageDomainId;

    public ExternalLease() {
    }

    public ExternalLease(Guid leaseId, Guid storageDomainId) {
        this.leaseId = leaseId;
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Guid getId() {
        return leaseId;
    }

    @Override
    public void setId(Guid id) {
        this.leaseId = id;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalLease)) {
            return false;
        }

        ExternalLease diskLease = (ExternalLease) o;
        return Objects.equals(leaseId, diskLease.leaseId) &&
                Objects.equals(storageDomainId, diskLease.storageDomainId);
    }


    public int hashCode() {
        return Objects.hash(leaseId, storageDomainId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ExternalLease.class.getSimpleName() + "[", "]")
                .add("leaseId=" + leaseId)
                .add("storageDomainId=" + storageDomainId)
                .toString();
    }
}

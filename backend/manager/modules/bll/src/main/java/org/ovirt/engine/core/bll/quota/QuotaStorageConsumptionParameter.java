package org.ovirt.engine.core.bll.quota;

import org.ovirt.engine.core.compat.Guid;

public class QuotaStorageConsumptionParameter extends QuotaConsumptionParameter {

    private Guid storageDomainId;
    private double requestedStorageGB;

    public QuotaStorageConsumptionParameter(Guid quotaGuid,
            QuotaAction quotaAction,
            Guid storageDomainId,
            Double requestedStorageGB) {
        super(quotaGuid, quotaAction);
        this.storageDomainId = storageDomainId;
        this.requestedStorageGB = requestedStorageGB;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Double getRequestedStorageGB() {
        return requestedStorageGB;
    }

    public void setRequestedStorageGB(Double requestedStorageGB) {
        this.requestedStorageGB = requestedStorageGB;
    }

    @Override
    public QuotaStorageConsumptionParameter clone() throws CloneNotSupportedException {
        return new QuotaStorageConsumptionParameter(
                getQuotaGuid(),
                getQuotaAction(),
                storageDomainId,
                requestedStorageGB);
    }

    @Override
    public ParameterType getParameterType() {
        return ParameterType.STORAGE;
    }
}

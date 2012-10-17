package org.ovirt.engine.core.bll.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.Guid;

public class QuotaVdsGroupConsumptionParameter extends QuotaConsumptionParameter {

    private Guid vdsGroupId;
    private int requestedCpu;
    private long requestedMemory;

    public QuotaVdsGroupConsumptionParameter(Guid quotaGuid,
            Quota quota,
            QuotaAction quotaAction,
            Guid vdsGroupId,
            int requestedCpu,
            long requestedMemory) {
        super(quotaGuid, quota, quotaAction);
        this.vdsGroupId = vdsGroupId;
        this.requestedCpu = requestedCpu;
        this.requestedMemory = requestedMemory;
    }

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public void setVdsGroupId(Guid vdsGroupId) {
        this.vdsGroupId = vdsGroupId;
    }

    public int getRequestedCpu() {
        return requestedCpu;
    }

    public void setRequestedCpu(int requestedCpu) {
        this.requestedCpu = requestedCpu;
    }

    public long getRequestedMemory() {
        return requestedMemory;
    }

    public void setRequestedMemory(long requestedMemory) {
        this.requestedMemory = requestedMemory;
    }

    @Override
    public QuotaVdsGroupConsumptionParameter clone() throws CloneNotSupportedException {
        return new QuotaVdsGroupConsumptionParameter(
                getQuotaGuid(),
                getQuota(),
                getQuotaAction(),
                vdsGroupId,
                requestedCpu,
                requestedMemory);
    }

    @Override
    public ParameterType getParameterType() {
        return ParameterType.VDS_GROUP;
    }
}

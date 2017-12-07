package org.ovirt.engine.core.bll.quota;

import org.ovirt.engine.core.compat.Guid;

public class QuotaClusterConsumptionParameter extends QuotaConsumptionParameter {

    private Guid clusterId;
    private int requestedCpu;
    private long requestedMemory;

    public QuotaClusterConsumptionParameter(Guid quotaGuid,
            QuotaAction quotaAction,
            Guid clusterId,
            int requestedCpu,
            long requestedMemory) {
        super(quotaGuid, quotaAction);
        this.clusterId = clusterId;
        this.requestedCpu = requestedCpu;
        this.requestedMemory = requestedMemory;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
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
    public QuotaClusterConsumptionParameter clone() throws CloneNotSupportedException {
        return new QuotaClusterConsumptionParameter(
                getQuotaGuid(),
                getQuotaAction(),
                clusterId,
                requestedCpu,
                requestedMemory);
    }

    @Override
    public ParameterType getParameterType() {
        return ParameterType.CLUSTER;
    }
}

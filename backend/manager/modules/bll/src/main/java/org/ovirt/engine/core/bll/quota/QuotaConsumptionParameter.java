package org.ovirt.engine.core.bll.quota;

import org.ovirt.engine.core.compat.Guid;

public abstract class QuotaConsumptionParameter implements Cloneable{
    private Guid quotaGuid;
    private QuotaAction quotaAction;

    protected QuotaConsumptionParameter(Guid quotaGuid, QuotaAction quotaAction) {
        this.quotaGuid = quotaGuid;
        this.quotaAction = quotaAction;
    }

    public Guid getQuotaGuid() {
        return quotaGuid;
    }

    public void setQuotaGuid(Guid quotaGuid) {
        this.quotaGuid = quotaGuid;
    }

    public QuotaAction getQuotaAction() {
        return quotaAction;
    }

    public void setQuotaAction(QuotaAction quotaAction) {
        this.quotaAction = quotaAction;
    }

    public abstract ParameterType getParameterType();

    public static enum QuotaAction {
        CONSUME, RELEASE
    }

    @Override
    public abstract QuotaConsumptionParameter clone() throws CloneNotSupportedException;

    public enum ParameterType {
        STORAGE, CLUSTER, SANITY
    }
}

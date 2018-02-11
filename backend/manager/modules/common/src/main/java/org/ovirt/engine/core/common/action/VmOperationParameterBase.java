package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmOperationParameterBase extends ActionParametersBase implements Serializable {
    private static final long serialVersionUID = -6248335374537898949L;
    private Guid quotaId;
    private boolean rerun;

    public VmOperationParameterBase() {
        vmId = Guid.Empty;
    }

    private Guid vmId;

    public VmOperationParameterBase(Guid vmId) {
        this.vmId = vmId;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid value) {
        vmId = value;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid value) {
        quotaId = value;
    }

    public boolean isRerun() {
        return rerun;
    }

    public void setRerun(boolean rerun) {
        this.rerun = rerun;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("vmId", getVmId());
    }
}

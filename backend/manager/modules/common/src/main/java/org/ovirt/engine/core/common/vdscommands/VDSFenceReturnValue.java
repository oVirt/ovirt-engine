package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;

public class VDSFenceReturnValue extends VDSReturnValue {

    public VDSFenceReturnValue() {
        super();
    }

    private VDS proxyHostUsed;
    private FenceAgent fenceAgentUsed;

    public VDSFenceReturnValue(VDSReturnValue result) {
        super();
        setCreationInfo(result.getCreationInfo());
        setExceptionObject(result.getExceptionObject());
        setExceptionString(result.getExceptionString());
        setReturnValue(result.getReturnValue());
        setSucceeded(result.getSucceeded());
        setVdsError(result.getVdsError());
    }

    public VDS getProxyHostUsed() {
        return proxyHostUsed;
    }

    public void setProxyHostUsed(VDS proxyHostUsed) {
        this.proxyHostUsed = proxyHostUsed;
    }

    public FenceAgent getFenceAgentUsed() {
        return fenceAgentUsed;
    }

    public void setFenceAgentUsed(FenceAgent fenceAgentUsed) {
        this.fenceAgentUsed = fenceAgentUsed;
    }

    public boolean isProxyHostFound() {
        return proxyHostUsed != null;
    }

    /**
     * Determines according to the return status from fence invocation whether the fence-operation has been skipped.
     */
    public boolean isSkipped() {
        if (getReturnValue() != null && getReturnValue() instanceof FenceStatusReturnValue) {
            FenceStatusReturnValue fenceStatus =
                    (FenceStatusReturnValue) getReturnValue();
            return fenceStatus.getIsSkipped();
        } else {
            return false;
        }
    }
}

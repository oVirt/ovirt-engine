package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class FenceVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid targetVdsId;
    private FenceAgent fenceAgent;
    private FenceActionType action;
    private Map<String, Object> fencingPolicyParams;

    private FenceVdsVDSCommandParameters() {
    }

    public FenceVdsVDSCommandParameters(
            Guid proxyVdsId,
            Guid targetVdsId,
            FenceAgent fenceAgent,
            FenceActionType action,
            Map<String, Object> fencingPolicyParams) {
        super(proxyVdsId);
        this.targetVdsId = targetVdsId;
        this.fenceAgent = fenceAgent;
        this.action = action;
        this.fencingPolicyParams = fencingPolicyParams;
    }

    public Guid getTargetVdsID() {
        return targetVdsId;
    }

    public FenceAgent getFenceAgent() {
        return fenceAgent;
    }

    public FenceActionType getAction() {
        return action;
    }

    public Map<String, Object> getFencingPolicyParams() {
        return fencingPolicyParams;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("targetVdsId", getTargetVdsID())
                .append("action", getAction())
                .append("agent", getFenceAgent())
                .append("policy", getFencingPolicyParams());
    }
}

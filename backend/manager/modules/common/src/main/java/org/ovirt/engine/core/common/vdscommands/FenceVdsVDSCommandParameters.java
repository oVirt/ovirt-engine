package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.compat.Guid;

public class FenceVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid targetVdsId;
    private FenceAgent fenceAgent;
    private FenceActionType action;
    private FencingPolicy fencingPolicy;

    private FenceVdsVDSCommandParameters() {
        action = FenceActionType.RESTART;
        fenceAgent = new FenceAgent();
    }

    public FenceVdsVDSCommandParameters(
            Guid proxyVdsId,
            Guid targetVdsId,
            FenceAgent fenceAgent,
            FenceActionType action,
            FencingPolicy fencingPolicy) {
        super(proxyVdsId);
        this.targetVdsId = targetVdsId;
        this.fenceAgent = fenceAgent;
        this.action = action;
        this.fencingPolicy = fencingPolicy;
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

    public FencingPolicy getFencingPolicy() {
        return fencingPolicy;
    }

    @Override
    public String toString() {
        return String.format(
                "%s, targetVdsId = %s, action = %s, agent = '%s', policy = '%s'",
                super.toString(),
                getTargetVdsID(),
                getAction(),
                getFenceAgent(),
                getFencingPolicy());
    }
}

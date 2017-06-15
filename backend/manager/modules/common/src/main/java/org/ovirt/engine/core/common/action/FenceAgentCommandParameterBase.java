package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.compat.Guid;

public class FenceAgentCommandParameterBase extends ActionParametersBase implements Serializable {

    private static final long serialVersionUID = -8383185727830349139L;

    @Valid
    private FenceAgent agent;
    private Guid vdsId;

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public FenceAgent getAgent() {
        return agent;
    }

    public void setAgent(FenceAgent agent) {
        this.agent = agent;
    }
}

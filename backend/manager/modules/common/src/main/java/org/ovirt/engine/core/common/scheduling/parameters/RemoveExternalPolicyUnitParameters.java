package org.ovirt.engine.core.common.scheduling.parameters;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class RemoveExternalPolicyUnitParameters extends ActionParametersBase {

    private static final long serialVersionUID = -2354147436466353253L;

    private Guid policyUnitId;

    public RemoveExternalPolicyUnitParameters() {
    }

    public RemoveExternalPolicyUnitParameters(Guid policyUnitId) {
        this.setPolicyUnitId(policyUnitId);
    }

    public Guid getPolicyUnitId() {
        return policyUnitId;
    }

    public void setPolicyUnitId(Guid policyUnitId) {
        this.policyUnitId = policyUnitId;
    }

}

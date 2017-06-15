package org.ovirt.engine.core.common.scheduling.parameters;

import javax.validation.Valid;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

public class AffinityGroupCRUDParameters extends ActionParametersBase {
    private static final long serialVersionUID = 8148828386101354522L;

    private Guid affinityGroupId;
    @Valid
    private AffinityGroup affinityGroup;

    public AffinityGroupCRUDParameters() {
    }

    public AffinityGroupCRUDParameters(Guid affinityGroupId, AffinityGroup affinityGroup) {
        this.affinityGroupId = affinityGroupId;
        this.affinityGroup = affinityGroup;
    }

    public Guid getAffinityGroupId() {
        return affinityGroupId;
    }

    public void setAffinityGroupId(Guid affinityGroupId) {
        this.affinityGroupId = affinityGroupId;
    }

    public AffinityGroup getAffinityGroup() {
        return affinityGroup;
    }

    public void setAffinityGroup(AffinityGroup affinityGroup) {
        this.affinityGroup = affinityGroup;
    }

}

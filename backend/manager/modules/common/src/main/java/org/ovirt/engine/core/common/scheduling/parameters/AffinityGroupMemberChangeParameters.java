package org.ovirt.engine.core.common.scheduling.parameters;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

public class AffinityGroupMemberChangeParameters extends AffinityGroupCRUDParameters {
    private static final long serialVersionUID = 2845914953169785366L;

    public AffinityGroupMemberChangeParameters() {
    }

    public AffinityGroupMemberChangeParameters(Guid affinityGroupId, AffinityGroup affinityGroup) {
        super(affinityGroupId, affinityGroup);
    }

    public AffinityGroupMemberChangeParameters(Guid affinityGroupId) {
        setAffinityGroupId(affinityGroupId);
    }

    public AffinityGroupMemberChangeParameters(Guid affinityGroupId, Guid id) {
        this(affinityGroupId);
        setEntityId(id);
    }

    @NotNull
    private Guid entityId;

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }
}

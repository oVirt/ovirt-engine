package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;

public class ExternalPolicyUnit extends PolicyUnitImpl {
    public ExternalPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    protected String getName() {
        return getPolicyUnit().getName();
    }

    @Override
    protected String getDescription() {
        return getPolicyUnit().getDescription();
    }

    @Override
    protected Guid getGuid() {
        return getPolicyUnit().getId();
    }

    @Override
    protected PolicyUnitType getType() {
        return getPolicyUnit().getPolicyUnitType();
    }
}

package org.ovirt.engine.core.bll.scheduling;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class InternalPolicyUnitsTest {

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    public class DummyUnit extends PolicyUnitImpl {
        public DummyUnit(PolicyUnit policyUnit,
                PendingResourceManager pendingResourceManager) {
            super(policyUnit, pendingResourceManager);
        }
    }

    @SchedulingUnit(
            guid = "438b0000-90ab-0000-9be0-a22560200000",
            name = "Not-enabled",
            type = PolicyUnitType.FILTER,
            description = "Not enabled unit"
    )
    public class NotEnabledDummyUnit extends PolicyUnitImpl {
        public NotEnabledDummyUnit(PolicyUnit policyUnit,
                PendingResourceManager pendingResourceManager) {
            super(policyUnit, pendingResourceManager);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void instantiateNonUnit() throws Exception {
        InternalPolicyUnits.instantiate(DummyUnit.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void instantiateDisabledUnit() throws Exception {
        InternalPolicyUnits.instantiate(NotEnabledDummyUnit.class, null);
    }

    @Test
    public void instantiateProper() throws Exception {
        for (Class<? extends PolicyUnitImpl> unit: InternalPolicyUnits.getList()) {
            InternalPolicyUnits.instantiate(unit, null);
        }
    }
}

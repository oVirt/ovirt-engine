package org.ovirt.engine.core.bll.scheduling;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class InternalPolicyUnitsTest {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.SpmVmGraceForEvenGuestDistribute, 5),
                MockConfigDescriptor.of(ConfigValues.MigrationThresholdForEvenGuestDistribute, 5),
                MockConfigDescriptor.of(ConfigValues.HighVmCountForEvenGuestDistribute, 10)
        );
    }

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

    @Test
    public void instantiateNonUnit() {
        assertThrows(IllegalArgumentException.class, () -> InternalPolicyUnits.instantiate(DummyUnit.class, null));
    }

    @Test
    public void instantiateDisabledUnit() {
        assertThrows(IllegalArgumentException.class,
                () -> InternalPolicyUnits.instantiate(NotEnabledDummyUnit.class, null));
    }

    @Test
    public void instantiateProper() throws Exception {
        for (Class<? extends PolicyUnitImpl> unit: InternalPolicyUnits.getList()) {
            InternalPolicyUnits.instantiate(unit, null);
        }
    }
}

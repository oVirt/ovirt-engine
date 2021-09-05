package org.ovirt.engine.core.bll.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
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
                MockConfigDescriptor.of(ConfigValues.HighVmCountForEvenGuestDistribute, 10),
                MockConfigDescriptor.of(ConfigValues.HostedEngineMaximumHighAvailabilityScore, 3400)
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

        for (Class<? extends PolicyUnitImpl> unit: InternalPolicyUnits.getMandatoryUnits()) {
            InternalPolicyUnits.instantiate(unit, null);
        }
    }

    @Test
    public void optionalAndMandatoryUnitsAreDisjoint() {
        assertTrue(CollectionUtils.intersection(
                InternalPolicyUnits.getList(),
                InternalPolicyUnits.getMandatoryUnits()
        ).isEmpty());
    }

    @Test
    public void mandatoryPoliciesAreFilters() {
        for (Class<? extends PolicyUnitImpl> unitClass : InternalPolicyUnits.getMandatoryUnits()) {
            SchedulingUnit annotation = unitClass.getAnnotation(SchedulingUnit.class);
            assertSame(PolicyUnitType.FILTER, annotation.type(),
                    String.format("Mandatory policy unit %s is not a filter.", unitClass.getName()));
        }
    }

    @Test
    public void mandatoryUnitsWithoutParameters() {
        // Mandatory policy units currently does not support parameters.
        // If they are needed, implement it.
        for (Class<? extends PolicyUnitImpl> unitClass : InternalPolicyUnits.getMandatoryUnits()) {
            SchedulingUnit annotation = unitClass.getAnnotation(SchedulingUnit.class);
            assertEquals(0, annotation.parameters().length,
                    String.format("Mandatory policy unit %s has parameters.", unitClass.getName()));
        }
    }
}

package org.ovirt.engine.core.bll.scheduling;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.policyunits.CPUPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CompatibilityVersionFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuLevelFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuPinningPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EmulatedMachineFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionCPUWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionMemoryWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HaReservationWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostDeviceFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.LabelFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MemoryPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MigrationPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NetworkPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NoneBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PinToHostPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingCPUWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingMemoryWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityWeightPolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class InternalPolicyUnits {
    private static final Set<Class<? extends PolicyUnitImpl>> enabledUnits = new HashSet<>();

    static {
        enabledUnits.add(CpuLevelFilterPolicyUnit.class);
        enabledUnits.add(CPUPolicyUnit.class);
        enabledUnits.add(EmulatedMachineFilterPolicyUnit.class);
        enabledUnits.add(EvenDistributionBalancePolicyUnit.class);
        enabledUnits.add(EvenDistributionCPUWeightPolicyUnit.class);
        enabledUnits.add(EvenDistributionMemoryWeightPolicyUnit.class);
        enabledUnits.add(EvenGuestDistributionBalancePolicyUnit.class);
        enabledUnits.add(EvenGuestDistributionWeightPolicyUnit.class);
        //enabledUnits.add(HaReservationBalancePolicyUnit.class); /* TODO not used? */
        enabledUnits.add(HaReservationWeightPolicyUnit.class);
        enabledUnits.add(HostDeviceFilterPolicyUnit.class);
        enabledUnits.add(HostedEngineHAClusterFilterPolicyUnit.class);
        enabledUnits.add(HostedEngineHAClusterWeightPolicyUnit.class);
        enabledUnits.add(MemoryPolicyUnit.class);
        enabledUnits.add(MigrationPolicyUnit.class);
        enabledUnits.add(NetworkPolicyUnit.class);
        enabledUnits.add(NoneBalancePolicyUnit.class);
        enabledUnits.add(PinToHostPolicyUnit.class);
        enabledUnits.add(PowerSavingBalancePolicyUnit.class);
        enabledUnits.add(PowerSavingCPUWeightPolicyUnit.class);
        enabledUnits.add(PowerSavingMemoryWeightPolicyUnit.class);
        enabledUnits.add(VmAffinityFilterPolicyUnit.class);
        enabledUnits.add(VmAffinityWeightPolicyUnit.class);
        enabledUnits.add(CpuPinningPolicyUnit.class);
        enabledUnits.add(CompatibilityVersionFilterPolicyUnit.class);
        enabledUnits.add(InClusterUpgradeFilterPolicyUnit.class);
        enabledUnits.add(InClusterUpgradeWeightPolicyUnit.class);
        enabledUnits.add(LabelFilterPolicyUnit.class);
    }

    public static Collection<Class<? extends PolicyUnitImpl>> getList() {
        return Collections.unmodifiableSet(enabledUnits);
    }

    public static PolicyUnitImpl instantiate(Class<? extends PolicyUnitImpl> unitType, PendingResourceManager pendingResourceManager) {
        try {
            // This check is only performed once during the static initializer run
            // and serves as a sanity check
            if (unitType.getAnnotation(SchedulingUnit.class) == null) {
                throw new IllegalArgumentException(unitType.getName()
                        + " is missing the required SchedulingUnit annotation metadata.");
            }

            if (!enabledUnits.contains(unitType)) {
                throw new IllegalArgumentException("Policy unit " + unitType.getName() + " is not present"
                        + " in the list of enabled internal policy units.");
            }

            PolicyUnitImpl unit = unitType
                    .getConstructor(PolicyUnit.class, PendingResourceManager.class)
                    .newInstance(null, pendingResourceManager);
            return unit;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}

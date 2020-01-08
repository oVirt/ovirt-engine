package org.ovirt.engine.core.bll.scheduling;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.policyunits.BasicWeightSelectorPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CPUPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.ClusterInMaintenanceFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CompatibilityVersionFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuAndNumaPinningWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuLevelFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuOverloadPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuPinningPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EmulatedMachineFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionCPUWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionMemoryWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HaReservationWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HighPerformanceCpuPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostDeviceFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineMemoryReservationFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HugePagesFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MDevicePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MemoryPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MigrationPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MigrationTscFrequencyPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NetworkPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NoneBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NumaPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NumaWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PinToHostPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingCPUWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingMemoryWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PreferredHostsWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.RankSelectorPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.SwapFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmLeasesReadyFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmToHostAffinityFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmToHostAffinityWeightPolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;

public class InternalPolicyUnits {

    private static final Set<Class<? extends PolicyUnitImpl>> enabledUnits = new HashSet<>();
    private static final Set<Class<? extends PolicyUnitImpl>> mandatoryUnits = new HashSet<>();

    static {
        enabledUnits.add(ClusterInMaintenanceFilterPolicyUnit.class);
        enabledUnits.add(CpuOverloadPolicyUnit.class);
        enabledUnits.add(CPUPolicyUnit.class);
        enabledUnits.add(EmulatedMachineFilterPolicyUnit.class);
        enabledUnits.add(EvenDistributionBalancePolicyUnit.class);
        enabledUnits.add(EvenDistributionCPUWeightPolicyUnit.class);
        enabledUnits.add(EvenDistributionMemoryWeightPolicyUnit.class);
        enabledUnits.add(EvenGuestDistributionBalancePolicyUnit.class);
        enabledUnits.add(EvenGuestDistributionWeightPolicyUnit.class);
        //enabledUnits.add(HaReservationBalancePolicyUnit.class); /* TODO not used? */
        enabledUnits.add(HaReservationWeightPolicyUnit.class);
        enabledUnits.add(HostedEngineHAClusterFilterPolicyUnit.class);
        enabledUnits.add(HostedEngineHAClusterWeightPolicyUnit.class);
        enabledUnits.add(HostedEngineMemoryReservationFilterPolicyUnit.class);
        enabledUnits.add(HugePagesFilterPolicyUnit.class);
        enabledUnits.add(MemoryPolicyUnit.class);
        enabledUnits.add(NumaPolicyUnit.class);
        enabledUnits.add(NumaWeightPolicyUnit.class);
        enabledUnits.add(MigrationPolicyUnit.class);
        enabledUnits.add(NetworkPolicyUnit.class);
        enabledUnits.add(NoneBalancePolicyUnit.class);
        enabledUnits.add(PowerSavingBalancePolicyUnit.class);
        enabledUnits.add(PowerSavingCPUWeightPolicyUnit.class);
        enabledUnits.add(PowerSavingMemoryWeightPolicyUnit.class);
        enabledUnits.add(PreferredHostsWeightPolicyUnit.class);
        enabledUnits.add(SwapFilterPolicyUnit.class);
        enabledUnits.add(VmAffinityFilterPolicyUnit.class);
        enabledUnits.add(VmAffinityWeightPolicyUnit.class);
        enabledUnits.add(VmToHostAffinityFilterPolicyUnit.class);
        enabledUnits.add(VmToHostAffinityWeightPolicyUnit.class);
        enabledUnits.add(InClusterUpgradeFilterPolicyUnit.class);
        enabledUnits.add(InClusterUpgradeWeightPolicyUnit.class);
        enabledUnits.add(BasicWeightSelectorPolicyUnit.class);
        enabledUnits.add(RankSelectorPolicyUnit.class);
        enabledUnits.add(HighPerformanceCpuPolicyUnit.class);
        enabledUnits.add(CpuAndNumaPinningWeightPolicyUnit.class);
        enabledUnits.add(MigrationTscFrequencyPolicyUnit.class);

        mandatoryUnits.add(CompatibilityVersionFilterPolicyUnit.class);
        mandatoryUnits.add(CpuLevelFilterPolicyUnit.class);
        mandatoryUnits.add(CpuPinningPolicyUnit.class);
        mandatoryUnits.add(HostDeviceFilterPolicyUnit.class);
        mandatoryUnits.add(PinToHostPolicyUnit.class);
        mandatoryUnits.add(VmLeasesReadyFilterPolicyUnit.class);
        mandatoryUnits.add(MDevicePolicyUnit.class);
    }

    public static Collection<Class<? extends PolicyUnitImpl>> getList() {
        return Collections.unmodifiableSet(enabledUnits);
    }

    public static Set<Class<? extends PolicyUnitImpl>> getMandatoryUnits() {
        return Collections.unmodifiableSet(mandatoryUnits);
    }

    public static PolicyUnitImpl instantiate(Class<? extends PolicyUnitImpl> unitType, PendingResourceManager pendingResourceManager)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // This check is only performed once during the static initializer run
        // and serves as a sanity check
        if (unitType.getAnnotation(SchedulingUnit.class) == null) {
            throw new IllegalArgumentException(unitType.getName()
                    + " is missing the required SchedulingUnit annotation metadata.");
        }

        if (!enabledUnits.contains(unitType) && !mandatoryUnits.contains(unitType)) {
            throw new IllegalArgumentException("Policy unit " + unitType.getName() + " is not present"
                    + " in the list of enabled internal policy units.");
        }

        return unitType
                .getConstructor(PolicyUnit.class, PendingResourceManager.class)
                .newInstance(null, pendingResourceManager);
    }

    public static Guid getGuid(Class<? extends PolicyUnitImpl> unitType) {
        // This check is only performed once during the static initializer run
        // and serves as a sanity check
        if (unitType.getAnnotation(SchedulingUnit.class) == null) {
            throw new IllegalArgumentException(unitType.getName()
                    + " is missing the required SchedulingUnit annotation metadata.");
        }

        return Guid.createGuidFromString(unitType.getAnnotation(SchedulingUnit.class).guid());
    }
}

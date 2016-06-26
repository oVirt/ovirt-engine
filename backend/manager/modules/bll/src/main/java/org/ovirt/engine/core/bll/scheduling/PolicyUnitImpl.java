package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.policyunits.CPUPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuLevelFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuPinningPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EmulatedMachineFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionCPUWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionMemoryWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HaReservationBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HaReservationWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostDeviceFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit;
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
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(PolicyUnitImpl.class);

    public static final int MaxSchedulerWeight = Config.<Integer> getValue(ConfigValues.MaxSchedulerWeight);;

    private VdsDynamicDao vdsDynamicDao;

    public static PolicyUnitImpl getPolicyUnitImpl(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        switch (policyUnit.getName()) {
        case "Migration":
            return new MigrationPolicyUnit(policyUnit, pendingResourceManager);
        case "PinToHost":
            return new PinToHostPolicyUnit(policyUnit, pendingResourceManager);
        case "CPU":
            return new CPUPolicyUnit(policyUnit, pendingResourceManager);
        case "Memory":
            return new MemoryPolicyUnit(policyUnit, pendingResourceManager);
        case "Network":
            return new NetworkPolicyUnit(policyUnit, pendingResourceManager);
        case "HA":
            if (policyUnit.getPolicyUnitType() == PolicyUnitType.WEIGHT) {
                return new HostedEngineHAClusterWeightPolicyUnit(policyUnit, pendingResourceManager);
            } else if (policyUnit.getPolicyUnitType() == PolicyUnitType.FILTER) {
                return new HostedEngineHAClusterFilterPolicyUnit(policyUnit, pendingResourceManager);
            }
            break;
        case "OptimalForHaReservation":
            if (policyUnit.getPolicyUnitType() == PolicyUnitType.WEIGHT) {
                return new HaReservationWeightPolicyUnit(policyUnit, pendingResourceManager);
            }
            else if (policyUnit.getPolicyUnitType() == PolicyUnitType.LOAD_BALANCING) {
                return new HaReservationBalancePolicyUnit(policyUnit, pendingResourceManager);
            }
            break;
        case "CPU-Level":
            return new CpuLevelFilterPolicyUnit(policyUnit, pendingResourceManager);
        case "None":
            return new NoneBalancePolicyUnit(policyUnit, pendingResourceManager);
        case "OptimalForPowerSaving":
            return new PowerSavingBalancePolicyUnit(policyUnit, pendingResourceManager);
        case "OptimalForEvenDistribution":
            return new EvenDistributionBalancePolicyUnit(policyUnit, pendingResourceManager);
        case "OptimalForPowerSavingCPU":
            return new PowerSavingCPUWeightPolicyUnit(policyUnit, pendingResourceManager);
        case "OptimalForEvenDistributionCPU":
            return new EvenDistributionCPUWeightPolicyUnit(policyUnit, pendingResourceManager);
        case "OptimalForPowerSavingMemory":
            return new PowerSavingMemoryWeightPolicyUnit(policyUnit, pendingResourceManager);
        case "OptimalForEvenDistributionMemory":
            return new EvenDistributionMemoryWeightPolicyUnit(policyUnit, pendingResourceManager);
        case "OptimalForEvenGuestDistribution":
                if (policyUnit.getPolicyUnitType() == PolicyUnitType.WEIGHT) {
                    return new EvenGuestDistributionWeightPolicyUnit(policyUnit, pendingResourceManager);
                }
                else if (policyUnit.getPolicyUnitType() == PolicyUnitType.LOAD_BALANCING) {
                    return new EvenGuestDistributionBalancePolicyUnit(policyUnit, pendingResourceManager);
                }
                break;
        case "VmAffinityGroups":
            if (policyUnit.getPolicyUnitType() == PolicyUnitType.FILTER) {
                return new VmAffinityFilterPolicyUnit(policyUnit, pendingResourceManager);
            } else if (policyUnit.getPolicyUnitType() == PolicyUnitType.WEIGHT) {
                return new VmAffinityWeightPolicyUnit(policyUnit, pendingResourceManager);
            }
        case "Emulated-Machine":
            return new EmulatedMachineFilterPolicyUnit(policyUnit, pendingResourceManager);
        case "HostDevice":
            return new HostDeviceFilterPolicyUnit(policyUnit, pendingResourceManager);
        case "CpuPinning":
            return new CpuPinningPolicyUnit(policyUnit, pendingResourceManager);
        case "InClusterUpgrade":
            if (policyUnit.getPolicyUnitType() == PolicyUnitType.FILTER) {
                return new InClusterUpgradeFilterPolicyUnit(policyUnit, pendingResourceManager);
            } else if (policyUnit.getPolicyUnitType() == PolicyUnitType.WEIGHT) {
                return new InClusterUpgradeWeightPolicyUnit(policyUnit, pendingResourceManager);
            }
        default:
            break;
        }
        throw new NotImplementedException("policyUnit: " + policyUnit.getName());
    }

    private final PolicyUnit policyUnit;
    protected VdsFreeMemoryChecker memoryChecker;
    protected PendingResourceManager pendingResourceManager;

    public PolicyUnitImpl(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        this.policyUnit = policyUnit;
        this.pendingResourceManager = pendingResourceManager;
        this.vdsDynamicDao = Injector.get(VdsDynamicDao.class);
    }

    public List<VDS> filter(@NotNull VDSGroup cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        log.error("Policy unit '{}' filter is not implemented", getPolicyUnit().getName());
        return hosts;
    }

    public List<Pair<Guid, Integer>> score(@NotNull  VDSGroup cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        log.error("Policy unit '{}' function is not implemented", getPolicyUnit().getName());
        List<Pair<Guid, Integer>> pairs = new ArrayList<>();
        for (VDS vds : hosts) {
            pairs.add(new Pair<>(vds.getId(), 1));
        }
        return pairs;
    }

    public Pair<List<Guid>, Guid> balance(@NotNull VDSGroup cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {
        log.error("Policy unit '{}' balance is not implemented", getPolicyUnit().getName());
        return null;
    }

    public final PolicyUnit getPolicyUnit() {
        return policyUnit;
    }

    public void setMemoryChecker(VdsFreeMemoryChecker memoryChecker) {
        this.memoryChecker = memoryChecker;

    }

    public PendingResourceManager getPendingResourceManager() {
        return pendingResourceManager;
    }

    protected VdsDynamic getLastHost(final VM vm) {
        if (vm.getRunOnVds() == null) {
            return null;
        }
        return vdsDynamicDao.get(vm.getRunOnVds());
    }

}

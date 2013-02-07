package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.DefaultMapper;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class VmCountVdsLoadBalancingAlgorithm extends VdsLoadBalancingAlgorithm {

    public VmCountVdsLoadBalancingAlgorithm(VDSGroup group) {
        super(group);
    }

    @Override
    protected void InitOverUtilizedList() {
        int vmCount = 0;

        switch (getDefaultSelectionAlgorithm()) {
        case EvenlyDistribute: {
            vmCount = Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute);
            break;
        }
        case PowerSave: {
            vmCount = Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave);
            break;
        }
        }

        final int vmCountTemp = vmCount;
        List<VDS> vdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getVmCount() > vmCountTemp * VdsSelector.getEffectiveCpuCores(p);
            }
        });
        Collections.sort(vdses, new Comparator<VDS>() {
            @Override
            public int compare(VDS o1, VDS o2) {
                return o2.getVmCount() - o1.getVmCount();
            }
        });
        setOverUtilizedServers(LinqUtils.toMap(vdses, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS vds) {
                return vds.getId();
            }
        }));
    }

    private VdsSelectionAlgorithm getDefaultSelectionAlgorithm() {
        VdsSelectionAlgorithm defaultSelectionAlgorithm;
        try {
            defaultSelectionAlgorithm =
                    VdsSelectionAlgorithm.valueOf(Config.<String> GetValue(ConfigValues.VdsSelectionAlgorithm));
        } catch (Exception e) {
            defaultSelectionAlgorithm = VdsSelectionAlgorithm.EvenlyDistribute;
        }
        return defaultSelectionAlgorithm;
    }

    @Override
    protected void InitUnderUtilizedList() {
        int vmCount = 0;
        switch (getDefaultSelectionAlgorithm()) {
        case EvenlyDistribute: {
            vmCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForEvenlyDistribute);
            break;
        }
        case PowerSave: {
            vmCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForPowerSave);
            break;
        }
        }

        final int vmCountTemp = vmCount;
        List<VDS> vdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getVmCount() < vmCountTemp * VdsSelector.getEffectiveCpuCores(p);
            }
        });
        Collections.sort(vdses, new Comparator<VDS>() {
            @Override
            public int compare(VDS o1, VDS o2) {
                return o1.getVmCount() - o2.getVmCount();
            }
        });
        setUnderUtilizedServers(LinqUtils.toMap(vdses, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS vds) {
                return vds.getId();
            }
        }));
    }

    @Override
    protected void InitReadyToMigrationList() {
        int highVdsCount = 0;
        int lowVdsCount = 0;
        int afterThreasholdInPercent = Config.<Integer> GetValue(ConfigValues.UtilizationThresholdInPercent);

        switch (getDefaultSelectionAlgorithm()) {
        case EvenlyDistribute: {
            highVdsCount = Math.min(
                    afterThreasholdInPercent
                            * Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute) / 100,
                    Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute) - 1);
            lowVdsCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForEvenlyDistribute);
            break;
        }
        case PowerSave: {
            highVdsCount = Math.min(
                    afterThreasholdInPercent * Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave)
                            / 100, Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave) - 1);
            lowVdsCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForPowerSave);
            break;
        }
        }

        final int highVdsCountTemp = highVdsCount;
        final int lowVdsCountTemp = lowVdsCount;
        List<VDS> vdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getVmCount() < highVdsCountTemp * VdsSelector.getEffectiveCpuCores(p)
                        && p.getVmCount() >= lowVdsCountTemp * VdsSelector.getEffectiveCpuCores(p);
            }
        });
        setReadyToMigrationServers(LinqUtils.toMap(vdses, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS vds) {
                return vds.getId();
            }
        }));
    }

    @Override
    protected VM getBestVmToMigrate(List<VM> vms, Guid vdsId) {
        return vms.get(0);
    }

}

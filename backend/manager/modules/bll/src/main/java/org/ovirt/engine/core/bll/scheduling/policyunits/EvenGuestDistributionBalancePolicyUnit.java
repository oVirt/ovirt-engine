package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvenGuestDistributionBalancePolicyUnit extends EvenDistributionBalancePolicyUnit {

    private final int spmVmGraceDefault;
    private final int migrationThresholdDefault;
    private final int highVmCountDefault;
    protected static final Logger log = LoggerFactory.getLogger(EvenGuestDistributionBalancePolicyUnit.class);

    public EvenGuestDistributionBalancePolicyUnit (PolicyUnit policyUnit) {
        super(policyUnit);
        spmVmGraceDefault = Config.<Integer> getValue(ConfigValues.SpmVmGraceForEvenGuestDistribute);
        migrationThresholdDefault = Config.<Integer> getValue(ConfigValues.MigrationThresholdForEvenGuestDistribute);
        highVmCountDefault = Config.<Integer> getValue(ConfigValues.HighVmCountForEvenGuestDistribute);
    }

    /* returns the number of running VMS on given VDS
       if the VDS is SPM the return value is the number of running VMS + SPMVMCountGrace
     */
    private int getOccupiedVmSlots(VDS vds, Map<String, String> parameters) {
        int occupiedSlots = vds.getVmActive();
        final int spmVmCountGrace = NumberUtils.toInt(parameters.get("SpmVmGrace"),
                spmVmGraceDefault);
        if (vds.isSpm()) {
            occupiedSlots += spmVmCountGrace;
        }

        return occupiedSlots;
    }

    private VDS getWorstVDS(List<VDS> relevantHosts, Map<String, String> parameters) {
        VDS worstVds = relevantHosts.get(0);
        int worstVdsSlots = 0;
        for (VDS vds: relevantHosts) {
            if (getOccupiedVmSlots(vds, parameters) > worstVdsSlots) {
                worstVds = vds;
                worstVdsSlots = getOccupiedVmSlots(worstVds, parameters);
            }
        }

        return worstVds;
    }

    @Override
    protected List<VDS> getOverUtilizedHosts(List<VDS> relevantHosts,
            final Map<String, String> parameters) {

        final int highVmCountUtilization = NumberUtils.toInt(parameters.get("HighVmCount"),
                highVmCountDefault);

        final VDS worstVDS = getWorstVDS(relevantHosts, parameters);
        final int worstVdsOccupiedVmSlots = getOccupiedVmSlots(worstVDS, parameters);
        if (worstVdsOccupiedVmSlots < highVmCountUtilization) {
            log.info("There is no host with more than {} running guests, no balancing is needed",
                    highVmCountUtilization);
            return null;
        }

        return LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return getOccupiedVmSlots(p, parameters) >= worstVdsOccupiedVmSlots;
            }
        });

    }

    @Override
    protected List<VDS> getUnderUtilizedHosts(VDSGroup cluster,
            List<VDS> relevantHosts,
            final Map<String, String> parameters) {

        final int migrationThreshold = NumberUtils.toInt(parameters.get("MigrationThreshold"),
                migrationThresholdDefault);

        final VDS worstVDS = getWorstVDS(relevantHosts, parameters);
        final int worstVdsOccupiedVmSlots = getOccupiedVmSlots(worstVDS, parameters);

        List<VDS> underUtilizedHosts = LinqUtils.filter(relevantHosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                int distance = worstVdsOccupiedVmSlots - getOccupiedVmSlots(p, parameters);
                return distance >= migrationThreshold;
            }
        });

        if (underUtilizedHosts.size() == 0) {
            log.warn("There is no host with less than {} running guests",
                    worstVdsOccupiedVmSlots - migrationThreshold);
        }

        return underUtilizedHosts;
    }

}

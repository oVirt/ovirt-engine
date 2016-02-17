package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(PolicyUnitImpl.class);

    public static final int MaxSchedulerWeight = Config.<Integer> getValue(ConfigValues.MaxSchedulerWeight);;

    private final PolicyUnit policyUnit;
    protected VdsFreeMemoryChecker memoryChecker;
    protected PendingResourceManager pendingResourceManager;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    public PolicyUnitImpl(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        if (policyUnit != null) {
            // External policy unit provided
            this.policyUnit = policyUnit;
        } else {
            // Internal policy unit, prepare the dummy db entity
            this.policyUnit = new PolicyUnit();
            this.policyUnit.setEnabled(true);
            this.policyUnit.setName(getName());
            this.policyUnit.setDescription(getDescription());
            this.policyUnit.setId(getGuid());
            this.policyUnit.setInternal(true);
            this.policyUnit.setPolicyUnitType(getType());
            this.policyUnit.setParameterRegExMap(new HashMap<>());

            // Add all supported config values to the saved policy unit configuration
            for (PolicyUnitParameter parameter: getParameters()) {
                this.policyUnit.getParameterRegExMap().put(parameter.getDbName(), parameter.getRegex());
            }
        }

        this.pendingResourceManager = pendingResourceManager;
    }

    public List<VDS> filter(@NotNull Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        log.error("Policy unit '{}' filter is not implemented", getPolicyUnit().getName());
        return hosts;
    }

    public List<Pair<Guid, Integer>> score(@NotNull  Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        log.error("Policy unit '{}' function is not implemented", getPolicyUnit().getName());
        List<Pair<Guid, Integer>> pairs = new ArrayList<>();
        for (VDS vds : hosts) {
            pairs.add(new Pair<>(vds.getId(), 1));
        }
        return pairs;
    }

    public Pair<List<Guid>, Guid> balance(@NotNull Cluster cluster,
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

    // The following methods are only used when instantiating an internal policy unit

    protected String getName() {
        SchedulingUnit unit = getClass().getAnnotation(SchedulingUnit.class);
        return unit.name();
    }

    protected String getDescription() {
        SchedulingUnit unit = getClass().getAnnotation(SchedulingUnit.class);
        return unit.description();
    }

    protected PolicyUnitType getType() {
        SchedulingUnit unit = getClass().getAnnotation(SchedulingUnit.class);
        return unit.type();
    }

    protected Guid getGuid() {
        SchedulingUnit unit = getClass().getAnnotation(SchedulingUnit.class);
        return Guid.createGuidFromString(unit.guid());
    }

    protected Set<PolicyUnitParameter> getParameters() {
        SchedulingUnit unit = getClass().getAnnotation(SchedulingUnit.class);
        if (unit.parameters().length == 0) {
            return EnumSet.noneOf(PolicyUnitParameter.class);
        } else {
            return EnumSet.copyOf(Arrays.asList(unit.parameters()));
        }
    }

    protected VdsDynamic getLastHost(final VM vm) {
        if (vm.getRunOnVds() == null) {
            return null;
        }
        return vdsDynamicDao.get(vm.getRunOnVds());
    }
}

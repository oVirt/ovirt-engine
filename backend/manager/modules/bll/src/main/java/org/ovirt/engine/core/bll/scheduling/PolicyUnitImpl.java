package org.ovirt.engine.core.bll.scheduling;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.selector.SelectorInstance;
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

    public static int getMaxSchedulerWeight() {
        return Config.<Integer> getValue(ConfigValues.MaxSchedulerWeight);
    }

    private final PolicyUnit policyUnit;
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

    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup, PerHostMessages messages) {
        List<VDS> availableHosts = hosts;
        for (VM vm : vmGroup) {
            availableHosts = filter(context, availableHosts, vm, messages);
        }
        return availableHosts;
    }

    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        log.error("Policy unit '{}' filter is not implemented", getPolicyUnit().getName());
        return hosts;
    }

    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        return vmGroup.stream()
                .flatMap(vm -> score(context, hosts, vm).stream())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, Integer::sum))
                .entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        log.error("Policy unit '{}' function is not implemented", getPolicyUnit().getName());

        return hosts.stream().map(host -> new Pair<>(host.getId(), 1)).collect(Collectors.toList());
    }

    public List<BalanceResult> balance(Cluster cluster,
            List<VDS> hosts,
            Map<String, String> parameters) {
        log.error("Policy unit '{}' balance is not implemented", getPolicyUnit().getName());
        return Collections.emptyList();
    }

    public SelectorInstance selector(Map<String, String> parameters) {
        log.error("Policy unit '{}' selector is not implemented", getPolicyUnit().getName());
        return null;
    }

    public final PolicyUnit getPolicyUnit() {
        return policyUnit;
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

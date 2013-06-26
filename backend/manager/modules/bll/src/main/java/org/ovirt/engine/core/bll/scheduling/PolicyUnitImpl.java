package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.bll.scheduling.policyunits.CPUPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MemoryPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MigrationDomainPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MigrationPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NetworkPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NonePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PinToHostPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingPolicyUnit;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class PolicyUnitImpl extends PolicyUnit {
    public static final String VM = "vm";

    public static PolicyUnitImpl getPolicyUnitImpl(PolicyUnit policyUnit) {
        switch (policyUnit.getName()) {
        case "Migration":
            return new MigrationPolicyUnit(policyUnit);
        case "MigrationDomain":
            return new MigrationDomainPolicyUnit(policyUnit);
        case "PinToHost":
            return new PinToHostPolicyUnit(policyUnit);
        case "CPU":
            return new CPUPolicyUnit(policyUnit);
        case "Memory":
            return new MemoryPolicyUnit(policyUnit);
        case "Network":
            return new NetworkPolicyUnit(policyUnit);
        case "None":
            return new NonePolicyUnit(policyUnit);
        case "PowerSaving":
            return new PowerSavingPolicyUnit(policyUnit);
        case "EvenDistribution":
            return new EvenDistributionPolicyUnit(policyUnit);
        default:
            throw new NotImplementedException("policyUnit: " + policyUnit.getName());
        }
    }

    protected static final Log log = LogFactory.getLog(PolicyUnitImpl.class);
    private final PolicyUnit policyUnit;
    protected VdsFreeMemoryChecker memoryChecker;

    public PolicyUnitImpl(PolicyUnit policyUnit) {
        this.policyUnit = policyUnit;
    }

    public List<VDS> filter(List<VDS> hosts, Map<String, Object> parameters, List<String> messages) {
        log.error("policy unit:" + getName() + "filter is not implemented");
        return hosts;
    }

    public List<Pair<Guid, Integer>> score(List<VDS> hosts, Map<String, Object> parameters) {
        log.error("policy unit:" + getPolicyUnit().getName() + "function is not implemented");
        List<Pair<Guid, Integer>> pairs = new ArrayList<Pair<Guid, Integer>>();
        for (VDS vds : hosts) {
            pairs.add(new Pair<Guid, Integer>(vds.getId(), 1));
        }
        return pairs;
    }

    public Pair<List<Guid>, Guid> balance(VDSGroup cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {
        log.error("policy unit:" + getName() + "balance is not implemented");
        return null;
    }

    @Override
    public final Guid getId() {
        return policyUnit.getId();
    }

    @Override
    public final void setId(Guid id) {
        policyUnit.setId(id);
    }

    @Override
    public final String getName() {
        return policyUnit.getName();
    }

    @Override
    public final void setName(String name) {
        policyUnit.setName(name);
    }

    @Override
    public final boolean isInternal() {
        return policyUnit.isInternal();
    }

    @Override
    public final void setInternal(boolean internal) {
        policyUnit.setInternal(internal);
    }

    @Override
    public final boolean isFilterImplemeted() {
        return policyUnit.isFilterImplemeted();
    }

    @Override
    public final void setFilterImplemeted(boolean filterImplemeted) {
        policyUnit.setFilterImplemeted(filterImplemeted);
    }

    @Override
    public final boolean isFunctionImplemeted() {
        return policyUnit.isFunctionImplemeted();
    }

    @Override
    public final void setFunctionImplemeted(boolean functionImplemeted) {
        policyUnit.setFunctionImplemeted(functionImplemeted);
    }

    @Override
    public final boolean isBalanceImplemeted() {
        return policyUnit.isBalanceImplemeted();
    }

    @Override
    public final void setBalanceImplemeted(boolean balanceImplemeted) {
        policyUnit.setBalanceImplemeted(balanceImplemeted);
    }

    @Override
    public final Map<String, String> getParameterRegExMap() {
        return policyUnit.getParameterRegExMap();
    }

    @Override
    public final void setParameterRegExMap(Map<String, String> parameterRegExMap) {
        policyUnit.setParameterRegExMap(parameterRegExMap);
    }

    public final PolicyUnit getPolicyUnit() {
        return policyUnit;
    }

    public void setMemoryChecker(VdsFreeMemoryChecker memoryChecker) {
        this.memoryChecker = memoryChecker;

    }
}

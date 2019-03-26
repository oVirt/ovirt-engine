package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.utils.Pair;

public class SchedulingContext {
    private final Cluster cluster;
    private final List<PolicyUnitImpl> internalFilters = new ArrayList<>();
    private final List<PolicyUnitImpl> externalFilters = new ArrayList<>();
    private final List<Pair<PolicyUnitImpl, Integer>> internalScoreFunctions = new ArrayList<>();
    private final List<Pair<PolicyUnitImpl, Integer>> externalScoreFunctions = new ArrayList<>();

    private final Map<String, String> policyParameters;
    private final boolean ignoreHardVmToVmAffinity;
    private final boolean doNotGroupVms;
    private boolean shouldWeighClusterHosts;

    /**
     * This field is set, if it is possible to delay the scheduling.
     * It can be checked by a policy unit, if it wants to delay.
     *
     * Initially it is set to true and after delaying it is set to false.
     */
    private boolean canDelay = true;

    /**
     * This field is set by a policy unit that wants to delay the scheduling.
     *
     * If canDelay and shouldDelay are set, the scheduling will be delayed.
     */
    private boolean shouldDelay = false;

    private final List<String> messages = new ArrayList<>();

    public SchedulingContext(Cluster cluster, Map<String, String> policyParameters, boolean ignoreHardVmToVmAffinity, boolean doNotGroupVms) {
        this.cluster = cluster;
        this.policyParameters = policyParameters;
        this.ignoreHardVmToVmAffinity = ignoreHardVmToVmAffinity;
        this.doNotGroupVms = doNotGroupVms;
    }

    public SchedulingContext(Cluster cluster, Map<String, String> policyParameters) {
        this(cluster, policyParameters, false, false);
    }

    public Cluster  getCluster() {
        return cluster;
    }

    public List<PolicyUnitImpl> getInternalFilters() {
        return internalFilters;
    }

    public List<PolicyUnitImpl> getExternalFilters() {
        return externalFilters;
    }

    public List<Pair<PolicyUnitImpl, Integer>> getInternalScoreFunctions() {
        return internalScoreFunctions;
    }

    public List<Pair<PolicyUnitImpl, Integer>> getExternalScoreFunctions() {
        return externalScoreFunctions;
    }

    public Map<String, String> getPolicyParameters() {
        return policyParameters;
    }

    public boolean isIgnoreHardVmToVmAffinity() {
        return ignoreHardVmToVmAffinity;
    }

    public boolean isDoNotGroupVms() {
        return doNotGroupVms;
    }

    public boolean isShouldWeighClusterHosts() {
        return shouldWeighClusterHosts;
    }

    public void setShouldWeighClusterHosts(boolean shouldWeighClusterHosts) {
        this.shouldWeighClusterHosts = shouldWeighClusterHosts;
    }

    public boolean isCanDelay() {
        return canDelay;
    }

    public void setCanDelay(boolean canDelay) {
        this.canDelay = canDelay;
    }

    public boolean isShouldDelay() {
        return shouldDelay;
    }

    public void setShouldDelay(boolean shouldDelay) {
        this.shouldDelay = shouldDelay;
    }

    public List<String> getMessages() {
        return messages;
    }
}

package org.ovirt.engine.core.bll.scheduling;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;

public class SchedulingContext {
    private final Cluster cluster;
    private final Map<String, String> policyParameters;
    private final SchedulingParameters schedulingParameters;

    public SchedulingContext(Cluster cluster, Map<String, String> policyParameters, SchedulingParameters schedulingParameters) {
        this.cluster = cluster;
        this.policyParameters = policyParameters;
        this.schedulingParameters = schedulingParameters;
    }

    public Cluster  getCluster() {
        return cluster;
    }

    public Map<String, String> getPolicyParameters() {
        return policyParameters;
    }

    public SchedulingParameters getSchedulingParameters() {
        return schedulingParameters;
    }
}

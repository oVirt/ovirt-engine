package org.ovirt.engine.core.bll.scheduling;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;

public class SchedulingContext {
    private final Cluster cluster;
    private final Map<String, String> policyParameters;

    public SchedulingContext(Cluster cluster, Map<String, String> policyParameters) {
        this.cluster = cluster;
        this.policyParameters = policyParameters;
    }

    public Cluster  getCluster() {
        return cluster;
    }

    public Map<String, String> getPolicyParameters() {
        return policyParameters;
    }
}

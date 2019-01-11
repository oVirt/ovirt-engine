package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;

public class SchedulingContext {
    private final Cluster cluster;
    private final Map<String, String> policyParameters;
    private final SchedulingParameters schedulingParameters;

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

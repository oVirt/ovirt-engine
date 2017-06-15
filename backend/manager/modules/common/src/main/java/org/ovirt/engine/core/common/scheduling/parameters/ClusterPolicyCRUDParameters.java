package org.ovirt.engine.core.common.scheduling.parameters;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public class ClusterPolicyCRUDParameters extends ActionParametersBase {
    private static final long serialVersionUID = 8144928386101354544L;
    private Guid clusterPolicyId;
    private ClusterPolicy clusterPolicy;

    public ClusterPolicyCRUDParameters() {
    }

    public ClusterPolicyCRUDParameters(Guid clusterPolicyId, ClusterPolicy clusterPolicy) {
        this.clusterPolicyId = clusterPolicyId;
        this.clusterPolicy = clusterPolicy;
    }

    public ClusterPolicy getClusterPolicy() {
        return clusterPolicy;
    }

    public void setClusterPolicy(ClusterPolicy clusterPolicy) {
        this.clusterPolicy = clusterPolicy;
    }

    public Guid getClusterPolicyId() {
        return clusterPolicyId;
    }

    public void setClusterPolicyId(Guid clusterPolicyId) {
        this.clusterPolicyId = clusterPolicyId;
    }

}

package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.utils.customprop.ValidationError;

public abstract class VdsGroupOperationCommandBase<T extends VdsGroupOperationParameters> extends
        VdsGroupCommandBase<T> {

    // If the CPU thresholds are set to -1 then we should get the value from the configuration
    public static final int GET_CPU_THRESHOLDS_FROM_CONFIGURATION = -1;

    public VdsGroupOperationCommandBase(T parameters) {
        super(parameters);
    }

    protected VdsGroupOperationCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    public VDSGroup getVdsGroup() {
        return getParameters().getVdsGroup();
    }

    protected void CheckMaxMemoryOverCommitValue() {
        if (getVdsGroup().getmax_vds_memory_over_commit() <= 0) {
            getVdsGroup().setmax_vds_memory_over_commit(
                    Config.<Integer> GetValue(ConfigValues.MaxVdsMemOverCommit));
        }
    }

    protected boolean isAllowClusterWithVirtGluster() {
        Boolean allowVirGluster = Config.<Boolean> GetValue(ConfigValues.AllowClusterWithVirtGlusterEnabled);
        return allowVirGluster;
    }

    protected boolean validateClusterPolicy() {
        ClusterPolicy clusterPolicy = null;
        if (getVdsGroup().getClusterPolicyId() != null) {
            clusterPolicy =
                SchedulingManager.getInstance().getClusterPolicy(getVdsGroup().getClusterPolicyId());
        }
        if (clusterPolicy == null) {
            clusterPolicy = SchedulingManager.getInstance().getClusterPolicy(getVdsGroup().getClusterPolicyName());
            if (clusterPolicy == null) {
                return false;
            }
            getVdsGroup().setClusterPolicyId(clusterPolicy.getId());
        }
        updateClusterPolicyProperties(getVdsGroup(), clusterPolicy);
        List<ValidationError> validationErrors =
                SimpleCustomPropertiesUtil.getInstance().validateProperties(SchedulingManager.getInstance()
                        .getCustomPropertiesRegexMap(clusterPolicy),
                        getVdsGroup().getClusterPolicyProperties());
        if (!validationErrors.isEmpty()) {
            SimpleCustomPropertiesUtil.getInstance().handleCustomPropertiesError(validationErrors,
                    getReturnValue().getCanDoActionMessages());
            return false;
        }
        return true;
    }

    /**
     * Updates cluster policy parameters map to contain all default cluster properties and remove properties that
     * doesn't exist in the cluster policy.
     *
     * @param cluster
     * @param clusterPolicy
     */
    private void updateClusterPolicyProperties(VDSGroup cluster,
            ClusterPolicy clusterPolicy) {
        if (cluster.getClusterPolicyProperties() == null) {
            cluster.setClusterPolicyProperties(new LinkedHashMap<String, String>());
        }
        Map<String, String> clusterPolicyProperties = cluster.getClusterPolicyProperties();
        List<String> toRemoveKeysList = new ArrayList<String>();
        if (clusterPolicy.getParameterMap() != null) {
            for (Entry<String, String> entry : clusterPolicy.getParameterMap().entrySet()) {
                if (!clusterPolicyProperties.containsKey(entry.getKey())) {
                    clusterPolicyProperties.put(entry.getKey(), entry.getValue());
                }
            }
            for (String key : clusterPolicyProperties.keySet()) {
                if (!clusterPolicy.getParameterMap().containsKey(key)) {
                    toRemoveKeysList.add(key);
                }
            }
            for (String key : toRemoveKeysList) {
                clusterPolicyProperties.remove(key);
            }
        }
    }
}

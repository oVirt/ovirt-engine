package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.ClusterDao;

public abstract class ClusterOperationCommandBase<T extends ClusterOperationParameters> extends
        ClusterCommandBase<T> {

    // If the CPU thresholds are set to -1 then we should get the value from the configuration
    public static final int GET_CPU_THRESHOLDS_FROM_CONFIGURATION = -1;

    @Inject
    private SchedulingManager schedulingManager;

    public ClusterOperationCommandBase(T parameters) {
        this(parameters, null);
    }

    protected ClusterOperationCommandBase(Guid commandId) {
        super(commandId);
    }

    public ClusterOperationCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public Cluster getCluster() {
        return getParameters().getCluster();
    }

    /**
     * Get the cluster object as it is in database before update
     *
     * @return Current cluster object before database update, or null if not existing
     */
    public Cluster getPrevCluster() {
        return super.getCluster();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__CLUSTER);
    }

    protected ArchitectureType getArchitecture() {
        if (StringUtils.isNotEmpty(getCluster().getCpuName())) {
            return getCpuFlagsManagerHandler().getArchitectureByCpuName(getCluster().getCpuName(),
                    getCluster().getCompatibilityVersion());
        } else if (getCluster().getArchitecture() == null) {
            return ArchitectureType.undefined;
        }

        return getCluster().getArchitecture();
    }

    protected void updateMigrateOnError() {
        if (getCluster() != null && getCluster().getMigrateOnError() == null) {
            boolean isMigrationSupported =
                    FeatureSupported.isMigrationSupported(getArchitecture(),
                            getCluster().getCompatibilityVersion());

            MigrateOnErrorOptions migrateOnError =
                    isMigrationSupported ? MigrateOnErrorOptions.YES : MigrateOnErrorOptions.NO;

            getCluster().setMigrateOnError(migrateOnError);
        }
    }

    protected void checkMaxMemoryOverCommitValue() {
        if (getCluster().getMaxVdsMemoryOverCommit() <= 0) {
            getCluster().setMaxVdsMemoryOverCommit(
                    Config.<Integer>getValue(ConfigValues.MaxVdsMemOverCommit));
        }
    }

    protected boolean isAllowClusterWithVirtGluster() {
        Boolean allowVirGluster = Config.<Boolean> getValue(ConfigValues.AllowClusterWithVirtGlusterEnabled);
        return allowVirGluster;
    }

    protected boolean validateClusterPolicy() {
        ClusterPolicy clusterPolicy = null;
        if (getCluster().getClusterPolicyId() != null) {
            clusterPolicy =
                schedulingManager.getClusterPolicy(getCluster().getClusterPolicyId());
        }
        if (clusterPolicy == null) {
            clusterPolicy = schedulingManager.getClusterPolicy(getCluster().getClusterPolicyName());
            if (clusterPolicy == null) {
                return false;
            }
            getCluster().setClusterPolicyId(clusterPolicy.getId());
        }
        Map<String, String> customPropertiesRegexMap =
                schedulingManager.getCustomPropertiesRegexMap(clusterPolicy);
        updateClusterPolicyProperties(getCluster(), clusterPolicy, customPropertiesRegexMap);
        List<ValidationError> validationErrors =
                SimpleCustomPropertiesUtil.getInstance().validateProperties(customPropertiesRegexMap,
                        getCluster().getClusterPolicyProperties());
        if (!validationErrors.isEmpty()) {
            SimpleCustomPropertiesUtil.getInstance().handleCustomPropertiesError(validationErrors,
                    getReturnValue().getValidationMessages());
            return false;
        }
        return true;
    }

    /**
     * Updates cluster policy parameters map to contain all default cluster properties and remove properties that
     * doesn't exist in the valid custom properties.
     *
     * @param customPropertiesRegexMap
     *            - custom properties for all policy unit in cluster policy
     */
    private void updateClusterPolicyProperties(Cluster cluster,
            ClusterPolicy clusterPolicy, Map<String, String> customPropertiesRegexMap) {
        if (cluster.getClusterPolicyProperties() == null) {
            cluster.setClusterPolicyProperties(new LinkedHashMap<>());
        }
        Map<String, String> clusterPolicyProperties = cluster.getClusterPolicyProperties();
        List<String> toRemoveKeysList = new ArrayList<>();
        if (clusterPolicy.getParameterMap() != null) {
            for (Entry<String, String> entry : clusterPolicy.getParameterMap().entrySet()) {
                if (!clusterPolicyProperties.containsKey(entry.getKey())) {
                    clusterPolicyProperties.put(entry.getKey(), entry.getValue());
                }
            }
            for (String key : clusterPolicyProperties.keySet()) {
                if (!customPropertiesRegexMap.containsKey(key)) {
                    toRemoveKeysList.add(key);
                }
            }
            for (String key : toRemoveKeysList) {
                clusterPolicyProperties.remove(key);
            }
        }
    }

    protected boolean isClusterUnique(String clusterName) {
        ClusterDao clusterDao = getClusterDao();
        List<Cluster> clusters = clusterDao.getByName(clusterName, true);
        return (clusters == null || clusters.isEmpty());
    }

    protected void alertIfFencingDisabled() {
        if (!getCluster().getFencingPolicy().isFencingEnabled()) {
            AuditLogableBase alb = new AuditLogableBase();
            alb.setClusterId(getCluster().getId());
            alb.setRepeatable(true);
            auditLogDirector.log(alb, AuditLogType.FENCE_DISABLED_IN_CLUSTER_POLICY);
        }
    }
}

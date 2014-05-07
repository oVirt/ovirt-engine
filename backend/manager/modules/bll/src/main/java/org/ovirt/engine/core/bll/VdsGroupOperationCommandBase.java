package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsGroupDAO;

public abstract class VdsGroupOperationCommandBase<T extends VdsGroupOperationParameters> extends
        VdsGroupCommandBase<T> {

    // If the CPU thresholds are set to -1 then we should get the value from the configuration
    public static final int GET_CPU_THRESHOLDS_FROM_CONFIGURATION = -1;

    public VdsGroupOperationCommandBase(T parameters) {
        this(parameters, null);
    }

    protected VdsGroupOperationCommandBase(Guid commandId) {
        super(commandId);
    }

    public VdsGroupOperationCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public VDSGroup getVdsGroup() {
        return getParameters().getVdsGroup();
    }

    protected ArchitectureType getArchitecture() {
        if (StringUtils.isNotEmpty(getVdsGroup().getcpu_name())) {
            return CpuFlagsManagerHandler.getArchitectureByCpuName(getVdsGroup().getcpu_name(),
                    getVdsGroup().getcompatibility_version());
        } else if (getVdsGroup().getArchitecture() == null) {
            return ArchitectureType.undefined;
        }

        return getVdsGroup().getArchitecture();
    }

    protected void updateMigrateOnError() {
        if (getVdsGroup() != null && getVdsGroup().getMigrateOnError() == null) {
            boolean isMigrationSupported =
                    FeatureSupported.isMigrationSupported(getArchitecture(),
                            getVdsGroup().getcompatibility_version());

            MigrateOnErrorOptions migrateOnError =
                    isMigrationSupported ? MigrateOnErrorOptions.YES : MigrateOnErrorOptions.NO;

            getVdsGroup().setMigrateOnError(migrateOnError);
        }
    }

    protected void checkMaxMemoryOverCommitValue() {
        if (getVdsGroup().getmax_vds_memory_over_commit() <= 0) {
            getVdsGroup().setmax_vds_memory_over_commit(
                    Config.<Integer> getValue(ConfigValues.MaxVdsMemOverCommit));
        }
    }

    protected boolean isAllowClusterWithVirtGluster() {
        Boolean allowVirGluster = Config.<Boolean> getValue(ConfigValues.AllowClusterWithVirtGlusterEnabled);
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
        Map<String, String> customPropertiesRegexMap = SchedulingManager.getInstance()
                .getCustomPropertiesRegexMap(clusterPolicy);
        updateClusterPolicyProperties(getVdsGroup(), clusterPolicy, customPropertiesRegexMap);
        List<ValidationError> validationErrors =
                SimpleCustomPropertiesUtil.getInstance().validateProperties(customPropertiesRegexMap,
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
     * doesn't exist in the valid custom properties.
     *
     * @param cluster
     * @param clusterPolicy
     * @param customPropertiesRegexMap
     *            - custom properties for all policy unit in cluster policy
     */
    private void updateClusterPolicyProperties(VDSGroup cluster,
            ClusterPolicy clusterPolicy, Map<String, String> customPropertiesRegexMap) {
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
                if (!customPropertiesRegexMap.containsKey(key)) {
                    toRemoveKeysList.add(key);
                }
            }
            for (String key : toRemoveKeysList) {
                clusterPolicyProperties.remove(key);
            }
        }
    }

    protected boolean isVdsGroupUnique(String vdsGroupName) {
        VdsGroupDAO vdsGroupDao = getVdsGroupDAO();
        List<VDSGroup> vdsGroups = vdsGroupDao.getByName(vdsGroupName, false);
        return (vdsGroups == null || vdsGroups.isEmpty());
    }

    protected void alertIfFencingDisabled() {
        if (!getVdsGroup().getFencingPolicy().isFencingEnabled()) {
            AuditLogableBase alb = new AuditLogableBase();
            alb.setVdsGroupId(getVdsGroup().getId());
            alb.setRepeatable(true);
            AuditLogDirector.log(alb, AuditLogType.FENCE_DISABLED_IN_CLUSTER_POLICY);
        }
    }
}

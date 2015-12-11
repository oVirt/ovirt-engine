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
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public abstract class VdsGroupOperationCommandBase<T extends VdsGroupOperationParameters> extends
        VdsGroupCommandBase<T> {

    // If the CPU thresholds are set to -1 then we should get the value from the configuration
    public static final int GET_CPU_THRESHOLDS_FROM_CONFIGURATION = -1;

    @Inject
    private SchedulingManager schedulingManager;

    @Inject
    private InClusterUpgradeValidator upgradeValidator;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

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

    /**
     * Get the cluster object as it is in database before update
     *
     * @return Current cluster object before database update, or null if not existing
     */
    public VDSGroup getPrevVdsGroup() {
        return super.getVdsGroup();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__TYPE__CLUSTER);
    }

    protected ArchitectureType getArchitecture() {
        if (StringUtils.isNotEmpty(getVdsGroup().getCpuName())) {
            return CpuFlagsManagerHandler.getArchitectureByCpuName(getVdsGroup().getCpuName(),
                    getVdsGroup().getCompatibilityVersion());
        } else if (getVdsGroup().getArchitecture() == null) {
            return ArchitectureType.undefined;
        }

        return getVdsGroup().getArchitecture();
    }

    protected void updateMigrateOnError() {
        if (getVdsGroup() != null && getVdsGroup().getMigrateOnError() == null) {
            boolean isMigrationSupported =
                    FeatureSupported.isMigrationSupported(getArchitecture(),
                            getVdsGroup().getCompatibilityVersion());

            MigrateOnErrorOptions migrateOnError =
                    isMigrationSupported ? MigrateOnErrorOptions.YES : MigrateOnErrorOptions.NO;

            getVdsGroup().setMigrateOnError(migrateOnError);
        }
    }

    protected void checkMaxMemoryOverCommitValue() {
        if (getVdsGroup().getMaxVdsMemoryOverCommit() <= 0) {
            getVdsGroup().setMaxVdsMemoryOverCommit(
                    Config.<Integer>getValue(ConfigValues.MaxVdsMemOverCommit));
        }
    }

    protected boolean isAllowClusterWithVirtGluster() {
        Boolean allowVirGluster = Config.<Boolean> getValue(ConfigValues.AllowClusterWithVirtGlusterEnabled);
        return allowVirGluster;
    }

    protected boolean validateClusterPolicy(VDSGroup oldVdsGroup) {
        VDSGroup newVdsGroup = getVdsGroup();
        boolean alreadyInUpgradeMode = oldVdsGroup != null && oldVdsGroup.isInUpgradeMode();
        ClusterPolicy clusterPolicy = getClusterPolicy(newVdsGroup);
        if (clusterPolicy == null) {
            return false;
        }
        newVdsGroup.setClusterPolicyId(clusterPolicy.getId());

        if (alreadyInUpgradeMode && !newVdsGroup.isInUpgradeMode()) {
            // Check if we can safely stop the cluster upgrade
            final List<VDS> hosts = getVdsDao().getAllForVdsGroup(getVdsGroupId());
            if (!validate(getUpgradeValidator().isUpgradeDone(hosts))) {
                return false;
            }
        } else if (!alreadyInUpgradeMode && newVdsGroup.isInUpgradeMode()) {
            // Check if we can safely start the cluster upgrade
            if (!validate(getUpgradeValidator().checkClusterUpgradeIsEnabled(getVdsGroup()))) {
                return false;
            }
            final List<VDS> hosts = getVdsDao().getAllForVdsGroup(getVdsGroupId());
            final List<VM> vms = getVmDao().getAllForVdsGroup(getVdsGroupId());
            populateVMNUMAInfo(vms);

            if (!validate(getUpgradeValidator().isUpgradePossible(hosts, vms))) {
                return false;
            }
        }

        Map<String, String> customPropertiesRegexMap =
                getSchedulingManager().getCustomPropertiesRegexMap(clusterPolicy);
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

    private ClusterPolicy getClusterPolicy(final VDSGroup vdsGroup) {
        ClusterPolicy clusterPolicy = null;
        if (vdsGroup == null){
            return null;
        }
        if (vdsGroup.getClusterPolicyId() != null) {
            clusterPolicy = getSchedulingManager().getClusterPolicy(vdsGroup.getClusterPolicyId());
        }
        if (clusterPolicy == null) {
            clusterPolicy = getSchedulingManager().getClusterPolicy(vdsGroup.getClusterPolicyName());
        }
        return clusterPolicy;
    }

    private void populateVMNUMAInfo(final List<VM> vms) {
        // Populate numa nodes with a mass update
        final Map<Guid, List<VmNumaNode>> numaNodes =
                getVmNumaNodeDao().getVmNumaNodeInfoByVdsGroupIdAsMap(getVdsGroupId());
        for (final VM vm : vms) {
            if (numaNodes.containsKey(vm.getId())) {
                vm.setvNumaNodeList(numaNodes.get(vm.getId()));
            }
        }
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

    protected boolean isVdsGroupUnique(String vdsGroupName) {
        VdsGroupDao vdsGroupDao = getVdsGroupDao();
        List<VDSGroup> vdsGroups = vdsGroupDao.getByName(vdsGroupName, true);
        return (vdsGroups == null || vdsGroups.isEmpty());
    }

    protected void alertIfFencingDisabled() {
        if (!getVdsGroup().getFencingPolicy().isFencingEnabled()) {
            AuditLogableBase alb = new AuditLogableBase();
            alb.setVdsGroupId(getVdsGroup().getId());
            alb.setRepeatable(true);
            auditLogDirector.log(alb, AuditLogType.FENCE_DISABLED_IN_CLUSTER_POLICY);
        }
    }

    protected VmNumaNodeDao getVmNumaNodeDao() {
        return vmNumaNodeDao;
    }

    protected SchedulingManager getSchedulingManager() {
        return schedulingManager;
    }

    protected InClusterUpgradeValidator getUpgradeValidator() {
        return upgradeValidator;
    }
}

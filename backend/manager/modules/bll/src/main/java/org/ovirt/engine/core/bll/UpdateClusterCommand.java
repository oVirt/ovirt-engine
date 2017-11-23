package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterValidatorBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.RngDeviceUtils;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HasRngDevice;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.qualifiers.MomPolicyUpdate;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

public class UpdateClusterCommand<T extends ManagementNetworkOnClusterOperationParameters> extends
        ClusterOperationCommandBase<T> implements RenamedEntityInfoProvider{

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private SupportedHostFeatureDao hostFeatureDao;
    @Inject
    private ClusterFeatureDao clusterFeatureDao;
    @Inject
    @MomPolicyUpdate
    private Event<Cluster> momPolicyUpdatedEvent;
    @Inject
    private MoveMacs moveMacs;
    @Inject
    private InitGlusterCommandHelper glusterCommandHelper;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private RngDeviceUtils rngDeviceUtils;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private GlusterVolumeDao glusterVolumeDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private DbFacade dbFacade;

    private List<VDS> allForCluster;

    private Cluster oldCluster;

    private boolean isAddedToStoragePool = false;

    private List<VmStatic> vmsLockedForUpdate = Collections.emptyList();
    private List<VmTemplate> templatesLockedForUpdate = Collections.emptyList();

    private Map<String, String> failedUpgradeEntities = new HashMap<>();
    public static final String MESSAGE_REG_EX = "^(?<message>\\$message) (?<error>.*)";
    public static final Pattern msgRegEx = Pattern.compile(MESSAGE_REG_EX);

    @Override
    protected void init() {
        updateMigrateOnError();
        oldCluster = clusterDao.get(getCluster().getId());
        if (oldCluster != null
                && !Objects.equals(oldCluster.getCompatibilityVersion(), getCluster().getCompatibilityVersion())) {
            vmsLockedForUpdate = filterVmsInClusterNeedUpdate();
            templatesLockedForUpdate = filterTemplatesInClusterNeedUpdate();
        }
    }

    /**
     * Returns list of VMs that requires to be updated provided cluster compatibility version has changed.
     */
    protected List<VmStatic> filterVmsInClusterNeedUpdate() {
        return vmStaticDao.getAllByCluster(getCluster().getId()).stream()
                .filter(vm -> vm.getOrigin() != OriginType.EXTERNAL && !vm.isHostedEngine())
                .filter(vm -> vm.getCustomCompatibilityVersion() == null)
                .sorted()
                .collect(Collectors.toList());
    }

    protected List<VmTemplate> filterTemplatesInClusterNeedUpdate() {
        return vmTemplateDao.getAllForCluster(getCluster().getId()).stream()
                .filter(template -> template.getCustomCompatibilityVersion() == null)
                .sorted()
                .collect(Collectors.toList());
    }

    public UpdateClusterCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected UpdateClusterCommand(Guid commandId) {
        super(commandId);
    }

    private Guid getOldMacPoolId() {
        return oldCluster.getMacPoolId();
    }

    private Guid getNewMacPoolId() {
        final Cluster cluster = getCluster();
        return cluster == null ? null : cluster.getMacPoolId();
    }

    @Override
    protected void executeCommand() {
        Guid newMacPoolId = getNewMacPoolId();
        moveMacs.migrateMacsToAnotherMacPool(
                oldCluster,
                newMacPoolId,
                getContext());

        getCluster().setArchitecture(getArchitecture());

        setDefaultSwitchTypeIfNeeded();
        setDefaultFirewallTypeIfNeeded();

        // TODO: This code should be revisited and proper compensation logic should be introduced here
        checkMaxMemoryOverCommitValue();
        if (!Objects.equals(oldCluster.getCompatibilityVersion(), getParameters().getCluster().getCompatibilityVersion())) {
            String emulatedMachine = null;
            // pick an UP host randomly - all should have latest compat version already if we passed validate.
            for (VDS vds : allForCluster) {
                if (vds.getStatus() == VDSStatus.Up) {
                    emulatedMachine = getEmulatedMachineOfHostInCluster(vds);
                    break;
                }
            }
            if (emulatedMachine == null) {
                getParameters().getCluster().setDetectEmulatedMachine(true);
            } else {
                getParameters().getCluster().setEmulatedMachine(emulatedMachine);
            }
        }
        else if (oldCluster.getArchitecture() != getCluster().getArchitecture()) {
            // if architecture was changed, emulated machines must be updated when adding new host.
            // At this point the cluster is empty and have changed CPU name
            getParameters().getCluster().setDetectEmulatedMachine(true);
            getParameters().getCluster().setEmulatedMachine(null);
        }

        if (getParameters().isForceResetEmulatedMachine()) {
            getParameters().getCluster().setDetectEmulatedMachine(true);
        }

        boolean isKsmPolicyChanged = (getCluster().isKsmMergeAcrossNumaNodes() != getPrevCluster().isKsmMergeAcrossNumaNodes()) ||
                (getCluster().isEnableKsm() != getPrevCluster().isEnableKsm());

        clusterDao.update(getParameters().getCluster());
        addOrUpdateAddtionalClusterFeatures();
        if (!oldCluster.supportsGlusterService() && getCluster().supportsGlusterService()) {
            //update gluster parameters on all hosts
           updateGlusterHosts();
        }

        if (isAddedToStoragePool) {
            for (VDS vds : allForCluster) {
                VdsActionParameters parameters = new VdsActionParameters();
                parameters.setVdsId(vds.getId());
                ActionReturnValue addVdsSpmIdReturn = runInternalAction(ActionType.AddVdsSpmId, parameters, cloneContextAndDetachFromParent());
                if (!addVdsSpmIdReturn.getSucceeded()) {
                    setSucceeded(false);
                    getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                    return;
                }
            }

            final NetworkCluster managementNetworkCluster = createManagementNetworkCluster();
            networkClusterDao.save(managementNetworkCluster);
        }

        alertIfFencingDisabled();

        if (isKsmPolicyChanged) {
            momPolicyUpdatedEvent.fire(getCluster());
        }

        updateDefaultNetworkProvider();

        // Call UpdateVmCommand on all VMs in the cluster to update defaults (i.e. DisplayType)
        updateVms();
        updateTemplates();

        if (getCluster().getFirewallType() != getPrevCluster().getFirewallType()) {
            markHostsForReinstall();
        }

        if (!failedUpgradeEntities.isEmpty()) {
            logFailedUpgrades();
            failValidation(Arrays.asList(EngineMessage.CLUSTER_CANNOT_UPDATE_CLUSTER_FAILED_TO_UPDATE_VMS),
                    "$VmList " + StringUtils.join(failedUpgradeEntities.keySet(), ", "));
            getReturnValue().setValid(false);
            setSucceeded(false);
            return;
        }

        if (!Objects.equals(oldCluster.getCompatibilityVersion(), getCluster().getCompatibilityVersion())) {
            vmStaticDao.getAllByCluster(getCluster().getId()).forEach(this::updateClusterVersionInManager);
        }

        setSucceeded(true);
    }

    private void updateDefaultNetworkProvider() {
        if (Objects.equals(getCluster().getDefaultNetworkProviderId(),
                getPrevCluster().getDefaultNetworkProviderId())) {
            return;
        }

        allForCluster.stream()
                .filter(vds -> !Objects.equals(vds.getOpenstackNetworkProviderId(),
                        getCluster().getDefaultNetworkProviderId()))
                .forEach(vds -> {
                    VdsStatic vdsStatic = vds.getStaticData();
                    vdsStatic.setOpenstackNetworkProviderId(getCluster().getDefaultNetworkProviderId());
                    vdsStatic.setReinstallRequired(true);
                    vdsStaticDao.update(vdsStatic);
                });
    }

    private void updateClusterVersionInManager(VmStatic vm) {
        VmManager vmManager = resourceManager.getVmManager(vm.getId(), false);
        if (vmManager != null) {
            vmManager.setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
        }
    }

    private void markHostsForReinstall() {
        for (VDS vds : allForCluster) {
            vdsStaticDao.updateReinstallRequired(vds.getId(), true);
        }
    }

    private boolean updateVms() {
        for (VmStatic vm : vmsLockedForUpdate) {
            VmManagementParametersBase updateParams = new VmManagementParametersBase(vm);
            /*
            Locking by UpdateVmCommand is disabled since VMs are already locked in #getExclusiveLocks method.
            This logic relies on assumption that UpdateVmCommand locks exactly only updated VM.
             */
            updateParams.setLockProperties(LockProperties.create(LockProperties.Scope.None));
            updateParams.setClusterLevelChangeFromVersion(oldCluster.getCompatibilityVersion());

            upgradeGraphicsDevices(vm, updateParams);
            updateResumeBehavior(vm);
            updateRngDeviceIfNecessary(vm.getId(), vm.getCustomCompatibilityVersion(), updateParams);

            ActionReturnValue result = runInternalAction(
                    ActionType.UpdateVm,
                    updateParams,
                    cloneContextAndDetachFromParent());

            if (!result.getSucceeded()) {
                List<String> params = new ArrayList<>();
                params.add("$action Update");
                params.add("$type VM");
                params.add(parseErrorMessage(result.getValidationMessages()));
                List<String> messages = Backend.getInstance().getErrorsTranslator().translateErrorText(params);

                failedUpgradeEntities.put(vm.getName(), getFailedMessage(messages));
            }
        }
        return true;
    }

    private void logFailedUpgrades() {
        for (Map.Entry<String, String> entry : failedUpgradeEntities.entrySet()) {
            addCustomValue("VmName", entry.getKey());
            addCustomValue("Message", entry.getValue());
            auditLogDirector.log(this, AuditLogType.CLUSTER_CANNOT_UPDATE_VM_COMPATIBILITY_VERSION);
        }
    }

    /**
     * This can be dropped together with support of 4.0 compatibility level.
     */
    private void updateRngDeviceIfNecessary(
            Guid vmBaseId,
            Version customCompatibilityLevel,
            HasRngDevice updateParameters) {
        final Version oldEffectiveVersion = CompatibilityVersionUtils.getEffective(
                customCompatibilityLevel,
                () -> oldCluster.getCompatibilityVersion());
        final Version newEffectiveVersion = CompatibilityVersionUtils.getEffective(
                customCompatibilityLevel,
                () -> getCluster().getCompatibilityVersion());
        final Optional<VmRngDevice> updatedDeviceOptional = rngDeviceUtils.createUpdatedRngDeviceIfNecessary(
                oldEffectiveVersion, newEffectiveVersion, vmBaseId, cloneContext());
        if (updatedDeviceOptional.isPresent()) {
            updateParameters.setUpdateRngDevice(true);
            updateParameters.setRngDevice(updatedDeviceOptional.get());
        }
    }

    private boolean updateTemplates() {
        for (VmTemplate template : templatesLockedForUpdate) {
            // the object was loaded in before command execution started and thus the value may be outdated
            template.setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            UpdateVmTemplateParameters parameters = new UpdateVmTemplateParameters(template);
            // Locking by UpdateVmTemplate is disabled since templates are already locked in #getExclusiveLocks method.
            parameters.setLockProperties(LockProperties.create(LockProperties.Scope.None));
            parameters.setClusterLevelChangeFromVersion(oldCluster.getCompatibilityVersion());

            updateRngDeviceIfNecessary(template.getId(), template.getCustomCompatibilityVersion(), parameters);
            updateResumeBehavior(template);

            final ActionReturnValue result = runInternalAction(
                    ActionType.UpdateVmTemplate,
                    parameters,
                    cloneContextAndDetachFromParent());

            if (!result.getSucceeded()) {
                List<String> params = new ArrayList<>();
                params.add("$action Update");
                params.add("$type Template");
                params.add(parseErrorMessage(result.getValidationMessages()));
                List<String> messages = Backend.getInstance().getErrorsTranslator().translateErrorText(params);

                failedUpgradeEntities.put(template.getName(), getFailedMessage(messages));
            }
        }
        return true;
    }

    private String getFailedMessage(List<String> messages) {
        String msg = "[No Message]";
        if (messages.size() > 0 && !StringUtils.isEmpty(messages.get(0))) {
            msg = messages.get(0);
        }
        return msg;
    }

    private String parseErrorMessage(List<String> messages) {
        // method gets command Validation Messages and return the message
        for(String message: messages) {
            Matcher matcher = msgRegEx.matcher(message);
            if (matcher.matches()) {
                return matcher.group("error");
            }
        }
        return "";
    }

    /**
     * If upgrading cluster from 3.6 to 4.0 then switch from VNC/cirrus to VNC/vga
     */
    private void upgradeGraphicsDevices(VmStatic dbVm, VmManagementParametersBase updateParams) {
        Version oldVersion = updateParams.getClusterLevelChangeFromVersion();
        if (Version.v4_0.greater(oldVersion)) {
            VmStatic paramVm = updateParams.getVmStaticData();

            if (dbVm.getDefaultDisplayType() == DisplayType.cirrus) {
                paramVm.setDefaultDisplayType(DisplayType.vga);
            }
        }
    }

    private void updateResumeBehavior(VmBase vmBase) {
        vmHandler.autoSelectResumeBehavior(vmBase, getCluster());
    }

    private String getEmulatedMachineOfHostInCluster(VDS vds) {
        Set<String> emulatedMachinesLookup =
                new HashSet<>(Arrays.asList(vds.getSupportedEmulatedMachines().split(",")));
        return Config.<List<String>>getValue(ConfigValues.ClusterEmulatedMachines,
                        getParameters().getCluster().getCompatibilityVersion().getValue())
                .stream().filter(emulatedMachinesLookup::contains).findFirst().orElse(null);
    }

    private void addOrUpdateAddtionalClusterFeatures() {
        Set<SupportedAdditionalClusterFeature> featuresInDb =
                clusterFeatureDao.getSupportedFeaturesByClusterId(getCluster().getId());
        Map<Guid, SupportedAdditionalClusterFeature> featuresEnabled = new HashMap<>();

        for (SupportedAdditionalClusterFeature feature : getCluster().getAddtionalFeaturesSupported()) {
            featuresEnabled.put(feature.getFeature().getId(), feature);
        }

        for (SupportedAdditionalClusterFeature featureInDb : featuresInDb) {
            if (featureInDb.isEnabled() && !featuresEnabled.containsKey(featureInDb.getFeature().getId())) {
                // Disable the features which are not selected in update cluster
                featureInDb.setEnabled(false);
                clusterFeatureDao.updateSupportedClusterFeature(featureInDb);
            } else if (!featureInDb.isEnabled() && featuresEnabled.containsKey(featureInDb.getFeature().getId())) {
                // Enable the features which are selected in update cluster
                featureInDb.setEnabled(true);
                clusterFeatureDao.updateSupportedClusterFeature(featureInDb);
            }
            featuresEnabled.remove(featureInDb.getFeature().getId());
        }
        // Add the newly add cluster features
        if (CollectionUtils.isNotEmpty(featuresEnabled.values())) {
            clusterFeatureDao.addAllSupportedClusterFeature(featuresEnabled.values());
        }

    }

    private void updateGlusterHosts() {
        allForCluster.forEach(glusterCommandHelper::initGlusterHost);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().getIsInternalCommand()) {
            return getSucceeded() ? AuditLogType.SYSTEM_UPDATE_CLUSTER
                    : AuditLogType.SYSTEM_UPDATE_CLUSTER_FAILED;
        }

        return getSucceeded() ? AuditLogType.USER_UPDATE_CLUSTER
                : AuditLogType.USER_UPDATE_CLUSTER_FAILED;
    }

    @Override
    protected boolean validate() {
        boolean result = true;
        boolean hasVms = false;
        boolean hasVmOrHost = false;
        boolean sameCpuNames = false;
        boolean allVdssInMaintenance = false;

        List<VM> vmList = null;

        if (oldCluster == null) {
            addValidationMessage(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
            result = false;
        }
        // if the name was changed then make sure the new name is unique
        if (result && !StringUtils.equals(oldCluster.getName(), getCluster().getName())) {
            if (!isClusterUnique(getCluster().getName())) {
                addValidationMessage(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE);
                result = false;
            }
        }
        if (result && !VersionSupport.checkVersionSupported(getCluster()
                .getCompatibilityVersion())) {
            addValidationMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        }

        if (result) {
            allForCluster = vdsDao.getAllForCluster(oldCluster.getId());
        }
        // decreasing of compatibility version is only allowed when no hosts exists, and not beneath the DC version
        if (result && getCluster().getCompatibilityVersion().compareTo(oldCluster.getCompatibilityVersion()) < 0) {
            if (!allForCluster.isEmpty()) {
                result = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_CLUSTER_WITH_HOSTS_COMPATIBILITY_VERSION);
            }

            if (oldCluster.getStoragePoolId() != null) {
                ClusterValidator validator = new ClusterValidator(
                        dbFacade, oldCluster, getCpuFlagsManagerHandler());
                if (!validate(validator.dataCenterVersionMismatch())) {
                    result = false;
                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION_UNDER_DC);
                }
            }

        }
        if (result && oldCluster.getStoragePoolId() != null
                && !oldCluster.getStoragePoolId().equals(getCluster().getStoragePoolId())) {
            addValidationMessage(EngineMessage.CLUSTER_CANNOT_CHANGE_STORAGE_POOL);
            result = false;
        }
        // If both original Cpu and new Cpu are null, don't check Cpu validity
        if (result) {
            allVdssInMaintenance = areAllVdssInMaintenance(allForCluster);
        }
        // Validate the cpu only if the cluster supports Virt
        if (result && getCluster().supportsVirtService()
                && (oldCluster.getCpuName() != null || getCluster().getCpuName() != null)) {
            // Check that cpu exist
            if (!checkIfCpusExist()) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
                addValidationMessage(EngineMessage.VAR__TYPE__CLUSTER);
                result = false;
            } else {
                // if cpu changed from intel to amd (or backwards) and there are
                // vds in this cluster, cannot update
                if (!StringUtils.isEmpty(oldCluster.getCpuName())
                        && !checkIfCpusSameManufacture(oldCluster)
                        && !allVdssInMaintenance) {
                    addValidationMessage(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ILLEGAL);
                    result = false;
                }
            }
        }

        if (result) {
            vmList = vmDao.getAllForCluster(oldCluster.getId());
            hasVmOrHost = !vmList.isEmpty() || !allForCluster.isEmpty();
        }

        // cannot change the processor architecture while there are attached hosts or VMs to the cluster
        if (result  && getCluster().supportsVirtService()
                && !isArchitectureUpdatable()
                && hasVmOrHost) {
            addValidationMessage(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_ARCHITECTURE_ILLEGAL);
            result = false;
        }

        if (result) {
            sameCpuNames = StringUtils.equals(oldCluster.getCpuName(), getCluster().getCpuName());
        }

        if (result) {
            boolean isOldCPUEmpty = StringUtils.isEmpty(oldCluster.getCpuName());

            if (!isOldCPUEmpty && !sameCpuNames && !isCpuUpdatable(oldCluster) && hasVmOrHost) {
                addValidationMessage(EngineMessage.CLUSTER_CPU_IS_NOT_UPDATABLE);
                result = false;
            }
        }

        if (result && !oldCluster.getCompatibilityVersion().equals(getCluster().getCompatibilityVersion())) {
            for (VM vm : vmList) {
                if (vm.isPreviewSnapshot()) {// can't change cluster version when a VM is in preview
                    if (result) {
                        addValidationMessage(EngineMessage.CLUSTER_VERSION_CHANGE_VM_PREVIEW);
                        result = false; // and continue with adding validation messages
                    }
                    addValidationMessage(vm.getName());
                }
            }
        }

        if (result) {
            List<VDS> vdss = new ArrayList<>();
            isAddedToStoragePool = oldCluster.getStoragePoolId() == null
                    && getCluster().getStoragePoolId() != null;

            if (isAddedToStoragePool && !validateManagementNetwork()) {
                return false;
            }

            for (VDS vds : allForCluster) {
                if (vds.getStatus() == VDSStatus.Up) {
                    if (isAddedToStoragePool) {
                        addValidationMessage(EngineMessage.CLUSTER_CANNOT_UPDATE_VDS_UP);
                        return false;
                    } else {
                        vdss.add(vds);
                    }
                }
            }
            for (VDS vds : vdss) {
                if (!VersionSupport.checkClusterVersionSupported(
                        getCluster().getCompatibilityVersion(), vds)) {
                    result = false;
                    addValidationMessage(EngineMessage.CLUSTER_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS);
                    break;
                } else if (getCluster().supportsVirtService() && missingServerCpuFlags(vds) != null) {
                    addValidationMessage(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS);
                    result = false;
                    break;
                }
                if (!isSupportedEmulatedMachinesMatchClusterLevel(vds)) {
                    return failValidation(EngineMessage.CLUSTER_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_INCOMPATIBLE_EMULATED_MACHINE);
                }
            }

            if (result) {
                Set<SupportedAdditionalClusterFeature> additionalClusterFeaturesAdded =
                        getAdditionalClusterFeaturesAdded();
                // New Features cannot be enabled if all up hosts are not supporting the selected feature
                if (CollectionUtils.isNotEmpty(additionalClusterFeaturesAdded)
                        && !checkClusterFeaturesSupported(vdss, additionalClusterFeaturesAdded)) {
                    addValidationMessage(EngineMessage.CLUSTER_CANNOT_UPDATE_SUPPORTED_FEATURES_WITH_LOWER_HOSTS);
                    result = false;
                }
            }

            if (result) {
                boolean notDownVms = false;
                boolean suspendedVms = false;
                hasVms = vmList.size() > 0;

                if (!sameCpuNames) {
                    for (VM vm : vmList) {
                        if (vm.getStatus() == VMStatus.Suspended) {
                            suspendedVms = true;
                            break;
                        } else if (vm.getStatus() != VMStatus.Down) {
                            notDownVms = true;
                            break;
                        }
                    }
                    if (suspendedVms) {
                        addValidationMessage(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_WITH_SUSPENDED_VMS);
                        result = false;
                    } else if (notDownVms) {
                        int compareResult = compareCpuLevels(oldCluster);
                        if (compareResult > 0) {// Upgrade of CPU in same compability level is allowed if
                                                       // there
                            // are running VMs - but we should warn they
                            // cannot not be hibernated
                            addCustomValue("Cluster", getParameters().getCluster().getName());
                            auditLogDirector.log(this,
                                    AuditLogType.CANNOT_HIBERNATE_RUNNING_VMS_AFTER_CLUSTER_CPU_UPGRADE);
                        }
                    }
                }
            }
        }
        if (result && getCluster().getStoragePoolId() != null) {
            StoragePool storagePool = storagePoolDao.get(getCluster().getStoragePoolId());
            if (oldCluster.getStoragePoolId() == null && storagePool.isLocal()) {
                // we allow only one cluster in localfs data center
                if (!clusterDao.getAllForStoragePool(getCluster().getStoragePoolId()).isEmpty()) {
                    getReturnValue()
                            .getValidationMessages()
                            .add(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE
                                    .toString());
                    result = false;
                }
                else if (Config.getValue(ConfigValues.AutoRegistrationDefaultClusterID).equals(getCluster().getId())) {
                    addValidationMessage(EngineMessage.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
                    result = false;
                }
            }
        }

        if (result) {
            if (!(getCluster().supportsGlusterService() || getCluster().supportsVirtService())) {
                addValidationMessage(EngineMessage.CLUSTER_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED);
                result = false;
            }
            else if (getCluster().supportsGlusterService() && getCluster().supportsVirtService()
                    && !isAllowClusterWithVirtGluster()) {
                addValidationMessage(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED);
                result = false;
            }
        }
        if (result && hasVms && !getCluster().supportsVirtService()) {
            addValidationMessage(EngineMessage.CLUSTER_CANNOT_DISABLE_VIRT_WHEN_CLUSTER_CONTAINS_VMS);
            result = false;
        }
        if (result && !getCluster().supportsGlusterService()) {
            List<GlusterVolumeEntity> volumes = glusterVolumeDao.getByClusterId(getCluster().getId());
            if (volumes != null && volumes.size() > 0) {
                addValidationMessage(EngineMessage.CLUSTER_CANNOT_DISABLE_GLUSTER_WHEN_CLUSTER_CONTAINS_VOLUMES);
                result = false;
            }
        }
        if (result && getCluster().supportsTrustedService() && Config.<String> getValue(ConfigValues.AttestationServer).equals("")) {
            addValidationMessage(EngineMessage.CLUSTER_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED);
            result = false;
        }

        if (result
                && !FeatureSupported.isMigrationSupported(getArchitecture(), getCluster().getCompatibilityVersion())
                && getCluster().getMigrateOnError() != MigrateOnErrorOptions.NO) {
            return failValidation(EngineMessage.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED);
        }

        if (result) {
            result = validateClusterPolicy(oldCluster);
        }
        if (result && getParameters().isForceResetEmulatedMachine()) {
            for (VDS vds : allForCluster) {
                if (vds.getStatus() == VDSStatus.Up) {
                    addValidationMessage(EngineMessage.CLUSTER_HOSTS_MUST_BE_DOWN);
                    result = false;
                    break;
                }
            }
        }

        ClusterValidator clusterValidator = new ClusterValidator(
                dbFacade,
                getCluster(),
                cpuFlagsManagerHandler);

        result = result
                && validate(clusterValidator.rngSourcesAllowed())
                && validate(clusterValidator.memoryOptimizationConfiguration())
                && validate(moveMacs.canMigrateMacsToAnotherMacPool(oldCluster, getNewMacPoolId()))
                && validateDefaultNetworkProvider()
                && validate(clusterValidator.supportedFirewallTypeForClusterVersion());

        return result;
    }

    protected boolean isSupportedEmulatedMachinesMatchClusterLevel(VDS vds) {
        return getEmulatedMachineOfHostInCluster(vds) != null;
    }

    private Set<SupportedAdditionalClusterFeature> getAdditionalClusterFeaturesAdded() {
        // Lets not modify the existing collection. Hence creating a new hashset.
        Set<SupportedAdditionalClusterFeature> featuresSupported =
                new HashSet<>(getCluster().getAddtionalFeaturesSupported());
        featuresSupported.removeAll(clusterFeatureDao.getSupportedFeaturesByClusterId(getCluster().getId()));
        return featuresSupported;
    }

    private boolean checkClusterFeaturesSupported(List<VDS> vdss,
            Set<SupportedAdditionalClusterFeature> newFeaturesEnabled) {
        Set<String> featuresNamesEnabled = new HashSet<>();
        for (SupportedAdditionalClusterFeature feature : newFeaturesEnabled) {
            featuresNamesEnabled.add(feature.getFeature().getName());
        }

        for (VDS vds : vdss) {
            Set<String> featuresSupportedByVds = hostFeatureDao.getSupportedHostFeaturesByHostId(vds.getId());
            if (!featuresSupportedByVds.containsAll(featuresNamesEnabled)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    protected boolean isArchitectureUpdatable() {
        return oldCluster.getArchitecture() == ArchitectureType.undefined ? true
                : getArchitecture() == oldCluster.getArchitecture();
    }

    protected boolean checkIfCpusSameManufacture(Cluster group) {
        return getCpuFlagsManagerHandler().checkIfCpusSameManufacture(group.getCpuName(),
                getCluster().getCpuName(),
                getCluster().getCompatibilityVersion());
    }

    protected boolean checkIfCpusExist() {
        return getCpuFlagsManagerHandler().checkIfCpusExist(getCluster().getCpuName(),
                getCluster().getCompatibilityVersion());
    }

    protected List<String> missingServerCpuFlags(VDS vds) {
        return getCpuFlagsManagerHandler().missingServerCpuFlags(
                getCluster().getCpuName(),
                vds.getCpuFlags(),
                getCluster().getCompatibilityVersion());
    }

    protected boolean isCpuUpdatable(Cluster cluster) {
        return getCpuFlagsManagerHandler().isCpuUpdatable(cluster.getCpuName(), cluster.getCompatibilityVersion());
    }

    private boolean areAllVdssInMaintenance(List<VDS> vdss) {
        boolean allInMaintenance = true;
        for (VDS vds : vdss) {
            if (vds.getStatus() != VDSStatus.Maintenance) {
                allInMaintenance = false;
                break;
            }
        }
        return allInMaintenance;
    }

    protected int compareCpuLevels(Cluster otherGroup) {
        return getCpuFlagsManagerHandler().compareCpuLevels(getCluster().getCpuName(),
                otherGroup.getCpuName(),
                otherGroup.getCompatibilityVersion());
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.Cluster.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldCluster.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getCluster().getName();
    }

    @Override
    public void setEntityId(AuditLogable logable) {
        logable.setClusterId(oldCluster.getId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> result = new ArrayList<>(super.getPermissionCheckSubjects());

        final Guid macPoolId = getNewMacPoolId();
        final boolean changingPoolDefinition = macPoolId != null && !macPoolId.equals(getOldMacPoolId());
        if (changingPoolDefinition) {
            result.add(new PermissionSubject(macPoolId, VdcObjectType.MacPool, ActionGroup.CONFIGURE_MAC_POOL));
        }

        return result;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        final LockMessage lockMessage = createLockMessage();
        return templatesLockedForUpdate.stream()
                .collect(Collectors.toMap(
                        template -> template.getId().toString(),
                        template -> LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, lockMessage)));
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        final LockMessage lockMessage = createLockMessage();
        return vmsLockedForUpdate.stream()
                .collect(Collectors.toMap(
                        vm -> vm.getId().toString(),
                        vm -> LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, lockMessage)));
    }

    private LockMessage createLockMessage() {
        return new LockMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_BEING_UPDATED)
                .with("clusterName", oldCluster.getName());
    }

    @Override
    protected LockProperties getLockProperties() {
        return LockProperties.create(LockProperties.Scope.Command).withWait(false);
    }

    @Override
    protected boolean validateInputManagementNetwork(NetworkClusterValidatorBase networkClusterValidator) {
        return validate(networkClusterValidator.networkBelongsToClusterDataCenter(getCluster(), getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkNotExternal(getManagementNetwork()));
    }
}

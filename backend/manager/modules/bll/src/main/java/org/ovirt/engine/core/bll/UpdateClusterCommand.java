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
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.RngDeviceUtils;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.AuditLogType;
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
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.Snapshot;
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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

@NonTransactiveCommandAttribute(forceCompensation = true)
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
    private SnapshotDao snapshotDao;
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
    private VmInitDao vmInitDao;

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

    protected void setVmInitToVms() {
        for (VmStatic vm : vmsLockedForUpdate) {
            vm.setVmInit(vmInitDao.get(vm.getId()));
        }
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

    private void reDetectDefaultsForDeprecatedCPUs() {
        boolean oldCpuExists = cpuFlagsManagerHandler.checkIfCpusExist(oldCluster.getCpuName(),
                getParameters().getCluster().getCompatibilityVersion());
        boolean newCpuExists = cpuFlagsManagerHandler.checkIfCpusExist(getParameters().getCluster().getCpuName(),
                getParameters().getCluster().getCompatibilityVersion());
        boolean oldCpuExisted = cpuFlagsManagerHandler.checkIfCpusExist(oldCluster.getCpuName(),
                oldCluster.getCompatibilityVersion());
        String oldCpuManufacturer = "";
        ServerCpu scMin = null;

        if (oldCpuExisted) {
            oldCpuManufacturer = cpuFlagsManagerHandler.getVendorByCpuName(oldCluster.getCpuName(),
                    oldCluster.getCompatibilityVersion());
            oldCpuExisted = !oldCpuManufacturer.isEmpty();
        }

        for (VDS vds : allForCluster) {
            ServerCpu sc = cpuFlagsManagerHandler.findMaxServerCpuByFlags(vds.getCpuFlags(),
                    getParameters().getCluster().getCompatibilityVersion());

            if (vds.getStatus() == VDSStatus.Up && sc != null  &&
                    (scMin == null || scMin.getLevel() > sc.getLevel()) &&
                    (!oldCpuExisted || oldCpuManufacturer.equals(cpuFlagsManagerHandler.
                            getVendorByCpuName(sc.getCpuName(), getParameters().getCluster().getCompatibilityVersion())))) {
                scMin = sc;
            }
        }

        // Update the Current CPU if either the previous CPU was Deprecated or if the New CPU is not
        // Valid to compensate for validation fall through during upgrading.
        if (!oldCpuExists || !newCpuExists) {
            // If the minimum was not found and the current cpu does not exist, fetch the lowest CPU
            // in the list as the default per Manufacturer.
            if (scMin == null && !newCpuExists) {
                if (oldCpuExisted) {
                    List<ServerCpu> serverList = cpuFlagsManagerHandler.allServerCpuList(getParameters().
                            getCluster().getCompatibilityVersion());

                    for (ServerCpu serverCpu : serverList) {
                        if (oldCpuManufacturer.equals(cpuFlagsManagerHandler.
                                getVendorByCpuName(serverCpu.getCpuName(),
                                        getParameters().getCluster().getCompatibilityVersion()))) {
                            scMin = serverCpu;
                            break;
                        }
                    }
                }

                // If still not found fetch the lowest CPU in the list.
                if (scMin == null) {
                    scMin = cpuFlagsManagerHandler.allServerCpuList(getParameters().getCluster().
                            getCompatibilityVersion()).get(0);
                }
            }

            if (scMin != null) {
                getCluster().setCpuName(scMin.getCpuName());
                addCustomValue("CPU", scMin.getCpuName());
                addCustomValue("Cluster", getParameters().getCluster().getName());
                auditLogDirector.log(this, AuditLogType.CLUSTER_UPDATE_CPU_WHEN_DEPRECATED);
            }
        }
    }

    @Override
    protected void executeCommand() {
        Guid newMacPoolId = getNewMacPoolId();
        moveMacs.migrateMacsToAnotherMacPool(
                oldCluster,
                newMacPoolId,
                getContext());

        getCluster().setArchitecture(getArchitecture());

        setVmInitToVms();
        setDefaultSwitchTypeIfNeeded();
        setDefaultFirewallTypeIfNeeded();
        setDefaultLogMaxMemoryUsedThresholdIfNeeded();

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

            reDetectDefaultsForDeprecatedCPUs();

        } else if (oldCluster.getArchitecture() != getCluster().getArchitecture()) {
            // if architecture was changed, emulated machines must be updated when adding new host.
            // At this point the cluster is empty and have changed CPU name
            getParameters().getCluster().setDetectEmulatedMachine(true);
            getParameters().getCluster().setEmulatedMachine(null);
        }

        if (getParameters().isForceResetEmulatedMachine()) {
            getParameters().getCluster().setDetectEmulatedMachine(true);
        }

        TransactionSupport.executeInNewTransaction(() -> {
            CompensationUtils.updateEntity(getParameters().getCluster(), oldCluster, clusterDao, getCompensationContext());
            addOrUpdateAddtionalClusterFeatures();

            getCompensationContext().stateChanged();
            return null;
        });

        if (isAddedToStoragePool) {
            for (VDS vds : allForCluster) {
                VdsActionParameters parameters = new VdsActionParameters();
                parameters.setVdsId(vds.getId());
                parameters.setCompensationEnabled(true);

                ActionReturnValue addVdsSpmIdReturn = runInternalAction(ActionType.AddVdsSpmId, parameters, cloneContextWithNoCleanupCompensation());
                if (!addVdsSpmIdReturn.getSucceeded()) {
                    setSucceeded(false);
                    getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                    return;
                }
            }

            TransactionSupport.executeInNewTransaction(() -> {
                final NetworkCluster managementNetworkCluster = createManagementNetworkCluster();
                CompensationUtils.saveEntity(managementNetworkCluster, networkClusterDao, getCompensationContext());

                getCompensationContext().stateChanged();
                return null;
            });
        }

        // Call UpdateVmCommand on all VMs in the cluster to update defaults (i.e. DisplayType)
        updateVms();
        updateTemplates();

        if (!failedUpgradeEntities.isEmpty()) {
            logFailedUpgrades();
            failValidation(Arrays.asList(EngineMessage.CLUSTER_CANNOT_UPDATE_CLUSTER_FAILED_TO_UPDATE_VMS),
                    "$VmList " + StringUtils.join(failedUpgradeEntities.keySet(), ", "));
            getReturnValue().setValid(false);
            setSucceeded(false);
            return;
        }

        // Executing the rest of the command in one transaction.
        // This avoids using compensation context inside functions
        // called here, which would be tricky.
        //
        // If anything fails, this transaction is not committed and
        // changes committed before this block are reverted using compensation.
        TransactionSupport.executeInNewTransaction(() -> {
            updateDefaultNetworkProvider();

            if (getCluster().getFirewallType() != oldCluster.getFirewallType()
                    || getCluster().isVncEncryptionEnabled() != oldCluster.isVncEncryptionEnabled()) {
                markHostsForReinstall();
            }

            if (!oldCluster.supportsGlusterService() && getCluster().supportsGlusterService()) {
                //update gluster parameters on all hosts
                updateGlusterHosts();
            }

            alertIfFencingDisabled();

            boolean isKsmPolicyChanged = (getCluster().isKsmMergeAcrossNumaNodes() != oldCluster.isKsmMergeAcrossNumaNodes()) ||
                    (getCluster().isEnableKsm() != oldCluster.isEnableKsm());

            if (isKsmPolicyChanged) {
                momPolicyUpdatedEvent.fire(getCluster());
            }

            if (!Objects.equals(oldCluster.getCompatibilityVersion(), getCluster().getCompatibilityVersion())) {
                vmStaticDao.getAllByCluster(getCluster().getId()).forEach(this::updateClusterVersionInManager);
            }

            return null;
        });

        setSucceeded(true);
    }

    private void updateDefaultNetworkProvider() {
        if (getCluster().hasDefaultNetworkProviderId(oldCluster.getDefaultNetworkProviderId())) {
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

    private void updateVms() {
        for (VmStatic vm : vmsLockedForUpdate) {
            VmManagementParametersBase updateParams = new VmManagementParametersBase(vm);
            /*
            Locking by UpdateVmCommand is disabled since VMs are already locked in #getExclusiveLocks method.
            This logic relies on assumption that UpdateVmCommand locks exactly only updated VM.
             */
            updateParams.setLockProperties(LockProperties.create(LockProperties.Scope.None));
            updateParams.setClusterLevelChangeFromVersion(oldCluster.getCompatibilityVersion());
            updateParams.setCompensationEnabled(true);

            ActionReturnValue result = runInternalAction(
                    ActionType.UpdateVm,
                    updateParams,
                    cloneContextWithNoCleanupCompensation());

            if (!result.getSucceeded()) {
                List<String> params = new ArrayList<>();
                params.add("$action Update");
                params.add("$type VM");
                params.add(parseErrorMessage(result.getValidationMessages()));
                List<String> messages = backend.getErrorsTranslator().translateErrorText(params);

                failedUpgradeEntities.put(vm.getName(), getFailedMessage(messages));
            }
        }
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

    private void updateTemplates() {
        for (VmTemplate template : templatesLockedForUpdate) {
            // the object was loaded in before command execution started and thus the value may be outdated
            template.setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            UpdateVmTemplateParameters parameters = new UpdateVmTemplateParameters(template);
            // Locking by UpdateVmTemplate is disabled since templates are already locked in #getExclusiveLocks method.
            parameters.setLockProperties(LockProperties.create(LockProperties.Scope.None));
            parameters.setClusterLevelChangeFromVersion(oldCluster.getCompatibilityVersion());
            parameters.setCompensationEnabled(true);

            updateRngDeviceIfNecessary(template.getId(), template.getCustomCompatibilityVersion(), parameters);
            updateResumeBehavior(template);

            final ActionReturnValue result = runInternalAction(
                    ActionType.UpdateVmTemplate,
                    parameters,
                    cloneContextWithNoCleanupCompensation());

            if (!result.getSucceeded()) {
                List<String> params = new ArrayList<>();
                params.add("$action Update");
                params.add("$type Template");
                params.add(parseErrorMessage(result.getValidationMessages()));
                List<String> messages = backend.getErrorsTranslator().translateErrorText(params);

                failedUpgradeEntities.put(template.getName(), getFailedMessage(messages));
            }
        }
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
                clusterFeatureDao.getAllByClusterId(getCluster().getId());
        Map<Guid, SupportedAdditionalClusterFeature> featuresEnabled = new HashMap<>();

        for (SupportedAdditionalClusterFeature feature : getCluster().getAddtionalFeaturesSupported()) {
            featuresEnabled.put(feature.getFeature().getId(), feature);
        }

        for (SupportedAdditionalClusterFeature featureInDb : featuresInDb) {
            if (featureInDb.isEnabled() && !featuresEnabled.containsKey(featureInDb.getFeature().getId())) {
                // Disable the features which are not selected in update cluster
                getCompensationContext().snapshotEntityUpdated(featureInDb);
                featureInDb.setEnabled(false);
                clusterFeatureDao.update(featureInDb);
            } else if (!featureInDb.isEnabled() && featuresEnabled.containsKey(featureInDb.getFeature().getId())) {
                // Enable the features which are selected in update cluster
                getCompensationContext().snapshotEntityUpdated(featureInDb);
                featureInDb.setEnabled(true);
                clusterFeatureDao.update(featureInDb);
            }
            featuresEnabled.remove(featureInDb.getFeature().getId());
        }
        // Add the newly add cluster features
        if (CollectionUtils.isNotEmpty(featuresEnabled.values())) {
            clusterFeatureDao.saveAll(featuresEnabled.values());
            getCompensationContext().snapshotNewEntities(featuresEnabled.values());
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
        ClusterValidator clusterValidator = getClusterValidator(oldCluster, getCluster());
        boolean returnValue = validate(clusterValidator.oldClusterIsValid());
        if (returnValue) {
            returnValue = validate(clusterValidator.newNameUnique())
                    && validate(clusterValidator.newClusterVersionSupported())
                    && validate(clusterValidator.decreaseClusterWithHosts())
                    && validate(clusterValidator.decreaseClusterBeneathDc(getClusterValidator(oldCluster)))
                    && validate(clusterValidator.canChangeStoragePool())
                    && validate(clusterValidator.cpuNotFound(checkIfCpusExist()))
                    && validate(clusterValidator.updateCpuIllegal(checkIfCpusExist(),
                            checkIfCpusSameManufacture(oldCluster)))
                    && validate(clusterValidator.architectureIsLegal(isArchitectureUpdatable()))
                    && validate(clusterValidator.cpuUpdatable())
                    && validate(clusterValidator.vmInPrev())
                    && validateManagementNetworkAndAdditionToStoragePool()
                    && validate(clusterValidator.vdsUp())
                    && validate(clusterValidator.hostsDown(getParameters().isForceResetEmulatedMachine()))
                    && canUpdateCompatibilityVersionOrCpu()
                    && validate(clusterValidator.updateSupportedFeatures())
                    && hasSuspendedVms()
                    && validate(clusterValidator.addMoreThanOneHost())
                    && validate(clusterValidator.defaultClusterOnLocalfs())
                    && validate(clusterValidator.oneServiceEnabled())
                    && validate(clusterValidator.mixedClusterServicesSupportedForNewCluster())
                    && validate(clusterValidator.disableVirt())
                    && validate(clusterValidator.disableGluster())
                    && validate(clusterValidator.setTrustedAttestation())
                    && validate(clusterValidator.migrationOnError(getArchitecture()))
                    && validateClusterPolicy(oldCluster)
                    && validateConfiguration();
        }
        return returnValue;
    }

    private void addValidationVarAndMessage(String varName, Object varValue, EngineMessage message) {
        addValidationMessageVariable(varName, varValue);
        addValidationMessage(message);
    }

    private boolean validateConfiguration() {
        ClusterValidator newClusterValidator = getClusterValidator(getCluster());
        if (!validate(newClusterValidator.rngSourcesAllowed())
                || !validate(newClusterValidator.memoryOptimizationConfiguration())
                || !validate(moveMacs.canMigrateMacsToAnotherMacPool(oldCluster, getNewMacPoolId()))
                || !validateDefaultNetworkProvider()) {
            return false;
        }
        return true;
    }

    private boolean validateManagementNetworkAndAdditionToStoragePool() {
        isAddedToStoragePool = oldCluster.getStoragePoolId() == null
                && getCluster().getStoragePoolId() != null;
        if (isAddedToStoragePool && !validateManagementNetwork()) {
            return false;
        }
        return true;
    }

    private boolean hasSuspendedVms() {
        boolean notDownVms = false;
        List<VM> vmList = vmDao.getAllForCluster(oldCluster.getId());
        boolean sameCpuNames = Objects.equals(oldCluster.getCpuName(), getCluster().getCpuName());
        if (!sameCpuNames) {
            for (VM vm : vmList) {
                VMStatus vmStatus = vm.getStatus();
                if (vmStatus == VMStatus.Suspended) {
                    return failValidation(EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_WITH_SUSPENDED_VMS);
                }
                if (vmStatus != VMStatus.Down) {
                    notDownVms = true;
                    break;
                }
            }
            /**
             * Upgrade of CPU in same compatibility level is allowed if
             * there are running VMs - but we should warn they cannot not be hibernated
             */
            if (notDownVms) {
                if (compareCpuLevels(oldCluster) > 0) {
                    addCustomValue("Cluster", getParameters().getCluster().getName());
                    auditLogDirector.log(this,
                            AuditLogType.CANNOT_HIBERNATE_RUNNING_VMS_AFTER_CLUSTER_CPU_UPGRADE);
                }
            }
        }
        return true;
    }

    private boolean canUpdateCompatibilityVersionOrCpu() {
        allForCluster = vdsDao.getAllForCluster(oldCluster.getId());
        List<VDS> upVdss = allForCluster.stream()
                .filter(v -> v.getStatus() == VDSStatus.Up)
                .collect(Collectors.toList());
        boolean valid = true;
        List<String> lowerVersionHosts = new ArrayList<>();
        List<String> lowCpuHosts = new ArrayList<>();
        List<String> incompatibleEmulatedMachineHosts = new ArrayList<>();
        for (VDS vds : upVdss) {
            if (!VersionSupport.checkClusterVersionSupported(
                    getCluster().getCompatibilityVersion(),
                    vds)) {
                valid = false;
                lowerVersionHosts.add(vds.getName());
            }
            if (getCluster().supportsVirtService() && missingServerCpuFlags(vds) != null) {
                valid = false;
                lowCpuHosts.add(vds.getName());
            }
            if (!isSupportedEmulatedMachinesMatchClusterLevel(vds)) {
                valid = false;
                incompatibleEmulatedMachineHosts.add(vds.getName());
            }
        }
        if (!lowerVersionHosts.isEmpty()) {
            addValidationVarAndMessage("host",
                    String.join(", ", lowerVersionHosts),
                    EngineMessage.CLUSTER_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS);
        }
        if (!lowCpuHosts.isEmpty()) {
            addValidationVarAndMessage("host",
                    String.join(", ", lowCpuHosts),
                    EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS);
        }
        if (!incompatibleEmulatedMachineHosts.isEmpty()) {
            addValidationVarAndMessage("host",
                    String.join(", ", incompatibleEmulatedMachineHosts),
                    EngineMessage.CLUSTER_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_INCOMPATIBLE_EMULATED_MACHINE);
        }
        return valid;
    }

    protected boolean isSupportedEmulatedMachinesMatchClusterLevel(VDS vds) {
        return getEmulatedMachineOfHostInCluster(vds) != null;
    }

    private Set<SupportedAdditionalClusterFeature> getAdditionalClusterFeaturesAdded() {
        // Lets not modify the existing collection. Hence creating a new hashset.
        Set<SupportedAdditionalClusterFeature> featuresSupported =
                new HashSet<>(getCluster().getAddtionalFeaturesSupported());
        featuresSupported.removeAll(clusterFeatureDao.getAllByClusterId(getCluster().getId()));
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

    private boolean hasNextRunConfiguration(VM vm) {
        return snapshotDao.exists(vm.getId(), Snapshot.SnapshotType.NEXT_RUN);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    protected boolean isCpuDeprecated() {
        // If the version is being upgrading and the previous or current CPU was deprecated,
        // then skip the validation so that we can adjust the cpu type in the executeCommand() function.
        return !oldCluster.getCompatibilityVersion().equals(getParameters().getCluster().getCompatibilityVersion()) &&
                ((cpuFlagsManagerHandler.checkIfCpusExist(oldCluster.getCpuName(),
                        oldCluster.getCompatibilityVersion()) &&
                        !cpuFlagsManagerHandler.checkIfCpusExist(oldCluster.getCpuName(),
                                getParameters().getCluster().getCompatibilityVersion())) ||
                        !cpuFlagsManagerHandler.checkIfCpusExist(getCluster().getCpuName(),
                                getParameters().getCluster().getCompatibilityVersion()));
    }

    protected boolean isArchitectureUpdatable() {
        if (isCpuDeprecated()) {
            return true;
        }

        return oldCluster.getArchitecture() == ArchitectureType.undefined ? true
                : getArchitecture() == oldCluster.getArchitecture();
    }

    protected boolean checkIfCpusSameManufacture(Cluster group) {
        if (!cpuFlagsManagerHandler.checkIfCpusExist(group.getCpuName(),
                    getParameters().getCluster().getCompatibilityVersion()) ||
            !cpuFlagsManagerHandler.checkIfCpusExist(getCluster().getCpuName(),
                    getParameters().getCluster().getCompatibilityVersion())) {
            return true;
        }

        return cpuFlagsManagerHandler.checkIfCpusSameManufacture(group.getCpuName(),
                getCluster().getCpuName(),
                getCluster().getCompatibilityVersion());
    }

    protected boolean checkIfCpusExist() {
        // If the version is being upgrading and the previous or current CPU was deprecated,
        // then skip the validation so that we can adjust the cpu type in the executeCommand() function.
        if (isCpuDeprecated()) {
            return true;
        }

        return cpuFlagsManagerHandler.checkIfCpusExist(getCluster().getCpuName(),
                getCluster().getCompatibilityVersion());
    }

    protected List<String> missingServerCpuFlags(VDS vds) {
        return cpuFlagsManagerHandler.missingServerCpuFlags(
                getCluster().getCpuName(),
                vds.getCpuFlags(),
                getCluster().getCompatibilityVersion());
    }

    protected boolean isCpuUpdatable(Cluster cluster) {
        return cpuFlagsManagerHandler.isCpuUpdatable(cluster.getCpuName(), cluster.getCompatibilityVersion());
    }

    private boolean areAllVdssInMaintenance(List<VDS> vdss) {
        return vdss.stream().allMatch(vds -> vds.getStatus() == VDSStatus.Maintenance);
    }

    protected int compareCpuLevels(Cluster otherGroup) {
        return cpuFlagsManagerHandler.compareCpuLevels(getCluster().getCpuName(),
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

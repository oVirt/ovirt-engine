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
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterValidatorBase;
import org.ovirt.engine.core.bll.utils.CompatibilityVersionUpdater;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.RngDeviceUtils;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.action.HasRngDevice;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.qualifiers.MomPolicyUpdate;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.EmulatedMachineCommonUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateClusterCommand<T extends ClusterOperationParameters> extends
        ClusterOperationCommandBase<T> implements RenamedEntityInfoProvider{

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
    private ClusterCpuFlagsManager clusterCpuFlagsManager;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private VmInitDao vmInitDao;
    @Inject
    private OsRepository osRepository;

    private List<VDS> allHostsForCluster;

    private Cluster oldCluster;

    private boolean isAddedToStoragePool = false;

    private List<VmStatic> allVmsInCluster;
    private List<VmStatic> vmsLockedForUpdate = Collections.emptyList();
    private List<VmTemplate> templatesLockedForUpdate = Collections.emptyList();

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^(?<message>\\$message) (?<error>.*)");

    @Override
    protected void init() {
        updateMigrateOnError();
        oldCluster = clusterDao.get(getCluster().getId());
        if (oldCluster != null && shouldUpdateVmsAndTemplates()) {
            vmsLockedForUpdate = filterVmsInClusterNeedUpdate();
            templatesLockedForUpdate = filterTemplatesInClusterNeedUpdate();
        }

        if (oldCluster == null || isCpuNameChanged() || isVersionChanged()) {
            log.info("Updating cluster CPU flags and verb according to the configuration of the " + getCluster().getCpuName() + " cpu");
            clusterCpuFlagsManager.updateCpuFlags(getCluster());
        } else {
            getCluster().setCpuFlags(oldCluster.getCpuFlags());
            getCluster().setCpuVerb(oldCluster.getCpuVerb());
        }
    }

    protected List<VmStatic> filterVmsInClusterNeedUpdate() {
        return getAllVmsInCluster().stream()
                .filter(vm -> vm.getOrigin() != OriginType.EXTERNAL)
                .sorted()
                .collect(Collectors.toList());
    }

    private List<VmStatic> getAllVmsInCluster() {
        if (allVmsInCluster == null) {
            allVmsInCluster = vmStaticDao.getAllByCluster(getCluster().getId());
        }
        return allVmsInCluster;
    }

    private boolean shouldUpdateVmsAndTemplates() {
        return isVersionChanged()
                || isCpuNameChanged()
                || oldCluster.getArchitecture() != getArchitecture()
                || getParameters().isChangeVmsChipsetToQ35();
    }

    private boolean shouldUpdateVmBase(VmBase vmBase) {
        return isVersionChanged()
                || isCpuNameChanged()
                || oldCluster.getArchitecture() != getArchitecture()
                || shouldUpdateVmsChipset(vmBase);
    }

    private boolean shouldUpdateVmsChipset(VmBase vmBase) {
        return getParameters().isChangeVmsChipsetToQ35()
                        && !vmBase.isHostedEngine()
                        && vmBase.getBiosType() != null
                        && vmBase.getBiosType().getChipsetType() == ChipsetType.I440FX;
    }

    private boolean isVersionChanged() {
        return !Objects.equals(oldCluster.getCompatibilityVersion(), getCluster().getCompatibilityVersion());
    }

    private boolean isCpuNameChanged() {
        return !Objects.equals(oldCluster.getCpuName(), getCluster().getCpuName());
    }

    private void setVmInitToVms() {
        List<Guid> vmIds = vmsLockedForUpdate.stream()
                .map(VmBase::getId)
                .collect(Collectors.toList());

        Map<Guid, VmInit> vmInitMap = vmInitDao.getVmInitByIds(vmIds).stream()
                .collect(Collectors.toMap(VmInit::getId, init -> init));

        for (VmStatic vm : vmsLockedForUpdate) {
            vm.setVmInit(vmInitMap.get(vm.getId()));
        }
    }

    private List<VmTemplate> filterTemplatesInClusterNeedUpdate() {
        return vmTemplateDao.getAllForCluster(getCluster().getId()).stream()
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
        // if the CPU has set Auto detect, do not change it
        if (StringUtils.isEmpty(getCluster().getCpuName())) {
            return;
        }

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

        for (VDS vds : allHostsForCluster) {
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
                getCluster().setArchitecture(scMin.getArchitecture());
                clusterCpuFlagsManager.updateCpuFlags(getCluster());
                addCustomValue("CPU", scMin.getCpuName());
                addCustomValue("Cluster", getParameters().getCluster().getName());
                auditLog(this, AuditLogType.CLUSTER_UPDATE_CPU_WHEN_DEPRECATED);
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
        if (isVersionChanged()) {
            String emulatedMachine = getEmulatedMachineFromHost();
            if (emulatedMachine == null) {
                getParameters().getCluster().setDetectEmulatedMachine(true);
            } else {
                getParameters().getCluster().setEmulatedMachine(emulatedMachine);
            }

            reDetectDefaultsForDeprecatedCPUs();

        } else if (oldCluster.getArchitecture() != getCluster().getArchitecture()) {
            // if architecture was changed, emulated machines must be updated when adding a new host.
            // At this point the cluster is empty and have a changed CPU name
            // Also, along with the emulation machines, the Bios Type may need to be updated as well.
            getParameters().getCluster().setDetectEmulatedMachine(true);
            getParameters().getCluster().setEmulatedMachine(null);
        }

        if (getCluster().getArchitecture() != ArchitectureType.undefined &&
                getCluster().getBiosType() == null) {
            setDefaultBiosType();
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
            if (!addVdsSpmIdForAllHosts()) {
                return;
            }

            TransactionSupport.executeInNewTransaction(() -> {
                final NetworkCluster managementNetworkCluster = createManagementNetworkCluster();
                CompensationUtils.saveEntity(managementNetworkCluster, networkClusterDao, getCompensationContext());

                getCompensationContext().stateChanged();
                return null;
            });
        }

        if (!updateVmsAndTemplates()) {
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

            if (isVersionChanged()) {
                updateClusterVersionInVmManagers();
            }

            return null;
        });

        setSucceeded(true);
    }

    private String getEmulatedMachineFromHost() {
        // pick an UP host randomly - all should have latest compat version already if we passed validate.
        for (VDS vds : allHostsForCluster) {
            if (vds.getStatus() == VDSStatus.Up) {
                return getEmulatedMachineOfHostInCluster(vds);
            }
        }
        return null;
    }

    private boolean addVdsSpmIdForAllHosts() {
        for (VDS vds : allHostsForCluster) {
            VdsActionParameters parameters = new VdsActionParameters();
            parameters.setVdsId(vds.getId());
            parameters.setCompensationEnabled(true);

            ActionReturnValue addVdsSpmIdReturn = runInternalAction(ActionType.AddVdsSpmId, parameters, cloneContextWithNoCleanupCompensation());
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return false;
            }
        }
        return true;
    }

    private boolean updateVmsAndTemplates() {
        List<Pair<String, String>> failedUpgradeEntities = new ArrayList<>();

        updateVms(failedUpgradeEntities);
        updateTemplates(failedUpgradeEntities);

        if (failedUpgradeEntities.isEmpty()) {
            return true;
        }

        StringJoiner nameJoiner = new StringJoiner(", ");
        for (Pair<String, String> pair : failedUpgradeEntities) {
            String entityName = pair.getFirst();
            nameJoiner.add(entityName);

            addCustomValue("VmName", entityName);
            addCustomValue("Message", pair.getSecond());
            auditLog(this, AuditLogType.CLUSTER_CANNOT_UPDATE_VM_COMPATIBILITY_VERSION);
        }

        failValidation(List.of(EngineMessage.CLUSTER_CANNOT_UPDATE_CLUSTER_FAILED_TO_UPDATE_VMS),
                "$VmList " + nameJoiner.toString());
        getReturnValue().setValid(false);
        setSucceeded(false);
        return false;
    }

    private void updateDefaultNetworkProvider() {
        if (getCluster().hasDefaultNetworkProviderId(oldCluster.getDefaultNetworkProviderId())) {
            return;
        }

        allHostsForCluster.stream()
                .forEach(vds -> {
                    VdsStatic vdsStatic = vds.getStaticData();
                    vdsStatic.setReinstallRequired(true);
                    vdsStaticDao.update(vdsStatic);
                });
    }

    private void updateClusterVersionInVmManagers() {
        for (VmStatic vmStatic : getAllVmsInCluster()) {
            VmManager vmManager = resourceManager.getVmManager(vmStatic.getId(), false);
            if (vmManager != null) {
                vmManager.setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            }
        }
    }

    private void markHostsForReinstall() {
        for (VDS vds : allHostsForCluster) {
            vdsStaticDao.updateReinstallRequired(vds.getId(), true);
        }
    }

    private void updateVms(List<Pair<String, String>> failedUpgradeEntities) {
        for (VmStatic vm : vmsLockedForUpdate) {
            if (!shouldUpdateVmBase(vm)) {
                continue;
            }
            ActionReturnValue result = runInternalAction(
                    ActionType.UpdateVm,
                    createUpdateVmParameters(vm),
                    cloneContextWithNoCleanupCompensation());

            if (!result.getSucceeded()) {
                List<String> params = new ArrayList<>();
                params.add("$action Update");
                params.add("$type VM");
                params.add(parseErrorMessage(result.getValidationMessages()));
                List<String> messages = backend.getErrorsTranslator().translateErrorText(params);

                failedUpgradeEntities.add(new Pair<>(vm.getName(), getFailedMessage(messages)));
            }
        }
    }

    private VmManagementParametersBase createUpdateVmParameters(VmStatic originalVmStatic) {
        VmManagementParametersBase updateParams;

        VM nextRunVM = vmHandler.getNextRunVmConfiguration(originalVmStatic.getId(), null, false, true);
        if (nextRunVM == null) {
            updateParams = new VmManagementParametersBase(originalVmStatic);
        } else {
            updateParams = vmHandler.createVmManagementParametersBase(nextRunVM);
        }
        /*
        Locking by UpdateVmCommand is disabled since VMs are already locked in #getExclusiveLocks method.
        This logic relies on assumption that UpdateVmCommand locks exactly only updated VM.
         */
        updateParams.setLockProperties(LockProperties.create(LockProperties.Scope.None));
        if (nextRunVM == null || originalVmStatic.getCustomCompatibilityVersion() == null) {
            updateParams.setClusterLevelChangeFromVersion(oldCluster.getCompatibilityVersion());
        }
        updateParams.setCompensationEnabled(true);

        if (updateParams.getVmStaticData().getCustomCompatibilityVersion() == null) {
            new CompatibilityVersionUpdater().updateVmBaseCompatibilityVersion(
                    updateParams.getVmStaticData(),
                    getCluster().getCompatibilityVersion(),
                    getCluster()
            );
        }
        if (originalVmStatic.getDefaultDisplayType() == DisplayType.bochs && !isBochsDisplaySupported(originalVmStatic)) {
            updateParams.getVmStaticData().setDefaultDisplayType(DisplayType.vga);
        }

        if (getParameters().isChangeVmsChipsetToQ35() && updateParams.getVmStaticData().getBiosType() == BiosType.I440FX_SEA_BIOS) {
            updateParams.getVmStaticData().setBiosType(BiosType.Q35_SEA_BIOS);
        }
        return updateParams;
    }

    private boolean isBochsDisplaySupported(VmStatic originalVmStatic) {
        if (originalVmStatic.getBiosType() == null || !originalVmStatic.getBiosType().isOvmf()) {
            return false;
        }
        Version effectiveCompatibilityVersion = CompatibilityVersionUtils.getEffective(originalVmStatic, getCluster());
        return osRepository.getGraphicsAndDisplays(originalVmStatic.getOsId(), effectiveCompatibilityVersion).stream()
                .map(Pair::getSecond)
                .anyMatch(dt -> dt == DisplayType.bochs);
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

    private void updateTemplates(List<Pair<String, String>> failedUpgradeEntities) {
        for (VmTemplate template : templatesLockedForUpdate) {
            if (!shouldUpdateVmBase(template)) {
                continue;
            }
            new CompatibilityVersionUpdater().updateTemplateCompatibilityVersion(template,
                    CompatibilityVersionUtils.getEffective(getVmTemplate(), this::getCluster),
                    getCluster());

            UpdateVmTemplateParameters parameters = new UpdateVmTemplateParameters(template);
            // Locking by UpdateVmTemplate is disabled since templates are already locked in #getExclusiveLocks method.
            parameters.setLockProperties(LockProperties.create(LockProperties.Scope.None));
            parameters.setClusterLevelChangeFromVersion(oldCluster.getCompatibilityVersion());
            parameters.setCompensationEnabled(true);

            updateRngDeviceIfNecessary(template.getId(), template.getCustomCompatibilityVersion(), parameters);
            updateResumeBehavior(template);

            if (getParameters().isChangeVmsChipsetToQ35() && parameters.getVmTemplateData().getBiosType() == BiosType.I440FX_SEA_BIOS) {
                parameters.getVmTemplateData().setBiosType(BiosType.Q35_SEA_BIOS);
            }

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

                failedUpgradeEntities.add(new Pair<>(template.getName(), getFailedMessage(messages)));
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
            Matcher matcher = MESSAGE_PATTERN.matcher(message);
            if (matcher.matches()) {
                return matcher.group("error");
            }
        }
        return "";
    }

    private void updateResumeBehavior(VmBase vmBase) {
        vmHandler.autoSelectResumeBehavior(vmBase);
    }

    private String getEmulatedMachineOfHostInCluster(VDS vds) {
        Set<String> supported = new HashSet<>(Arrays.asList(vds.getSupportedEmulatedMachines().split(",")));
        Version version = getParameters().getCluster().getCompatibilityVersion();
        List<String> available = Config.getValue(ConfigValues.ClusterEmulatedMachines, version.getValue());
        return ClusterEmulatedMachines.build(
                EmulatedMachineCommonUtils.getSupportedByChipset(ChipsetType.I440FX, supported, available),
                EmulatedMachineCommonUtils.getSupportedByChipset(ChipsetType.Q35, supported, available));
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
        allHostsForCluster.forEach(glusterCommandHelper::initGlusterHost);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().isInternalCommand()) {
            return getSucceeded() ? AuditLogType.SYSTEM_UPDATE_CLUSTER
                    : AuditLogType.SYSTEM_UPDATE_CLUSTER_FAILED;
        }

        return getSucceeded() ? AuditLogType.USER_UPDATE_CLUSTER
                : AuditLogType.USER_UPDATE_CLUSTER_FAILED;
    }

    @Override
    protected boolean validate() {
        ClusterValidator clusterValidator = getClusterValidator(oldCluster, getCluster());
        return validate(clusterValidator.oldClusterIsValid())
                && validate(clusterValidator.newNameUnique())
                && validate(clusterValidator.newClusterVersionSupported())
                && validate(clusterValidator.decreaseClusterWithHosts())
                && validate(clusterValidator.decreaseClusterBeneathDc(getClusterValidator(oldCluster)))
                && validate(clusterValidator.decreaseClusterWithPortIsolation())
                && validate(clusterValidator.canChangeStoragePool())
                && validateCpuUpdatable(clusterValidator)
                && validate(clusterValidator.vmInPrev())
                && validateManagementNetworkAndAdditionToStoragePool()
                && validate(clusterValidator.vdsUp())
                && validate(clusterValidator.hostsDown(getParameters().isForceResetEmulatedMachine()))
                && canUpdateCompatibilityVersionOrCpu()
                && validate(clusterValidator.updateSupportedFeatures())
                && hasSuspendedVms()
                && validate(clusterValidator.addMoreThanOneHost())
                && validate(clusterValidator.atLeastOneHostSupportingClusterVersion())
                && validate(clusterValidator.defaultClusterOnLocalfs())
                && validate(clusterValidator.oneServiceEnabled())
                && validate(clusterValidator.mixedClusterServicesSupportedForNewCluster())
                && validate(clusterValidator.disableVirt())
                && validate(clusterValidator.disableGluster())
                && validate(clusterValidator.setTrustedAttestation())
                && validate(clusterValidator.migrationOnError(getArchitecture()))
                && validate(clusterValidator.nonDefaultBiosType())
                && validate(clusterValidator.implicitAffinityGroup())
                && validateClusterPolicy(oldCluster)
                && validateConfiguration()
                && validate(clusterValidator.updateFipsIsLegal());
    }

    private void addValidationVarAndMessage(String varName, Object varValue, EngineMessage message) {
        addValidationMessageVariable(varName, varValue);
        addValidationMessage(message);
    }

    private boolean validateCpuUpdatable(ClusterValidator clusterValidator) {
        boolean cpusExist = checkIfCpusExist();
        return validate(clusterValidator.cpuNotFound(cpusExist))
                && validate(clusterValidator.canAutoDetectCpu())
                && validate(clusterValidator.updateCpuIllegal(cpusExist, checkIfCpusSameManufacture(oldCluster)))
                && validate(clusterValidator.architectureIsLegal(isArchitectureUpdatable()))
                && validate(clusterValidator.cpuUpdatable());
    }

    private boolean validateConfiguration() {
        ClusterValidator newClusterValidator = getClusterValidator(getCluster());
        return validate(newClusterValidator.rngSourcesAllowed())
                && validate(newClusterValidator.memoryOptimizationConfiguration())
                && validate(moveMacs.canMigrateMacsToAnotherMacPool(oldCluster, getNewMacPoolId()))
                && validateDefaultNetworkProvider();
    }

    private boolean validateManagementNetworkAndAdditionToStoragePool() {
        isAddedToStoragePool = oldCluster.getStoragePoolId() == null
                && getCluster().getStoragePoolId() != null;
        return !isAddedToStoragePool || validateManagementNetwork();
    }

    private boolean hasSuspendedVms() {
        boolean notDownVms = false;
        List<VM> vmList = vmDao.getAllForCluster(oldCluster.getId());
        if (isCpuNameChanged()) {
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
                    auditLog(this, AuditLogType.CANNOT_HIBERNATE_RUNNING_VMS_AFTER_CLUSTER_CPU_UPGRADE);
                }
            }
        }
        return true;
    }

    private boolean canUpdateCompatibilityVersionOrCpu() {
        allHostsForCluster = vdsDao.getAllForCluster(oldCluster.getId());
        List<VDS> upVdss = allHostsForCluster.stream()
                .filter(v -> v.getStatus().isEligibleForClusterCpuConfigurationChange())
                .collect(Collectors.toList());
        boolean valid = true;
        List<String> lowerVersionHosts = new ArrayList<>();
        List<String> hostsWithMissingFlags = new ArrayList<>();
        List<String> incompatibleEmulatedMachineHosts = new ArrayList<>();
        for (VDS vds : upVdss) {
            if (!VersionSupport.checkClusterVersionSupported(
                    getCluster().getCompatibilityVersion(),
                    vds)) {
                valid = false;
                lowerVersionHosts.add(vds.getName());
            }
            if (getCluster().supportsVirtService() && !missingServerCpuFlags(vds).isEmpty()) {
                valid = false;
                hostsWithMissingFlags.add(vds.getName());
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
        if (!hostsWithMissingFlags.isEmpty()) {
            addValidationVarAndMessage("host",
                    String.join(", ", hostsWithMissingFlags),
                    EngineMessage.CLUSTER_CANNOT_UPDATE_CPU_WITH_HOSTS_MISSING_FLAGS);
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

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    private boolean isCpuDeprecated() {
        // If the version is being upgrading and the previous or current CPU was deprecated,
        // then skip the validation so that we can adjust the cpu type in the executeCommand() function.
        return isVersionChanged() &&
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

        return oldCluster.getArchitecture() == ArchitectureType.undefined
                || getArchitecture() == oldCluster.getArchitecture();
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
        return cpuFlagsManagerHandler.missingClusterCpuFlags(
                getCluster().getCpuFlags(),
                vds.getCpuFlags());
    }

    private int compareCpuLevels(Cluster otherCluster) {
        return cpuFlagsManagerHandler.compareCpuLevels(getCluster().getCpuName(),
                otherCluster.getCpuName(),
                otherCluster.getCompatibilityVersion());
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
        return LockProperties.create(LockProperties.Scope.Command).withNoWait();
    }

    @Override
    protected boolean validateInputManagementNetwork(NetworkClusterValidatorBase networkClusterValidator) {
        return validate(networkClusterValidator.networkBelongsToClusterDataCenter(getCluster(), getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkNotExternal(getManagementNetwork()));
    }
}

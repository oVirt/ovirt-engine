package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.validator.CpuPinningValidator.isCpuPinningValid;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.HotSetAmountOfMemoryParameters;
import org.ovirt.engine.core.common.action.HotSetNumberOfCpusParameters;
import org.ovirt.engine.core.common.action.HotUnplugMemoryWithoutVmUpdateParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.HugePageUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.UpdateVm;
import org.ovirt.engine.core.common.vdscommands.LeaseVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;

public class UpdateVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{

    private static final Base64 BASE_64 = new Base64(0, null);
    private static final String AUDIT_LOG_MEMORY_HOT_UNPLUG_OPTIONS = "memoryHotUnplugOptions";
    private static final String AUDIT_LOG_OLD_MEMORY_MB = "oldMemoryMb";
    private static final String AUDIT_LOG_NEW_MEMORY_MB = "newMemoryMB";

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VmSlaPolicyUtils vmSlaPolicyUtils;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private InClusterUpgradeValidator clusterUpgradeValidator;
    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private VmInitDao vmInitDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;
    @Inject
    private NetworkHelper networkHelper;
    @Inject
    private IconUtils iconUtils;
    @Inject
    private CloudInitHandler cloudInitHandler;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private AffinityValidator affinityValidator;
    @Inject
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    private VM oldVm;
    private boolean quotaSanityOnly = false;
    private VmStatic newVmStatic;
    private List<GraphicsDevice> cachedGraphics;
    private boolean isUpdateVmTemplateVersion = false;

    private BiConsumer<AuditLogable, AuditLogDirector> affinityGroupLoggingMethod = (a, b) -> {};

    public UpdateVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();

        if (getCluster() != null) {
            setStoragePoolId(getCluster().getStoragePoolId());
        }

        if (isVmExist() && isCompatibilityVersionSupportedByCluster(getEffectiveCompatibilityVersion())) {
            Version compatibilityVersion = getEffectiveCompatibilityVersion();
            getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(
                    compatibilityVersion, getParameters().getVmStaticData());
            getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(
                    compatibilityVersion, getVm().getStaticData());
        }

        vmHandler.updateDefaultTimeZone(getParameters().getVmStaticData());

        vmHandler.autoSelectUsbPolicy(getParameters().getVmStaticData());

        vmHandler.autoSelectResumeBehavior(getParameters().getVmStaticData());

        vmHandler.autoSelectDefaultDisplayType(getVmId(),
                getParameters().getVmStaticData(),
                getCluster(),
                getParameters().getGraphicsDevices());

        updateParametersVmFromInstanceType();

        initNuma();

        updateUSB();

        getVmDeviceUtils().setCompensationContext(getCompensationContextIfEnabledByCaller());

        if (getParameters().getVmStaticData().getBiosType() == null) {
            getParameters().getVmStaticData().setBiosType(getVm().getBiosType());
        }
    }

    private VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return isInternalExecution() ?
                getSucceeded() ? AuditLogType.SYSTEM_UPDATE_VM : AuditLogType.SYSTEM_FAILED_UPDATE_VM
                : getSucceeded() ? AuditLogType.USER_UPDATE_VM : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    protected void executeVmCommand() {
        oldVm = getVm(); // needs to be here for post-actions
        if (isUpdateVmTemplateVersion) {
            updateVmTemplateVersion();
            return; // template version was changed, no more work is required
        }

        newVmStatic = getParameters().getVmStaticData();
        if (isRunningConfigurationNeeded()) {
            logNameChange();
            vmHandler.createNextRunSnapshot(
                    getVm(), getParameters().getVmStaticData(), getParameters(), getCompensationContextIfEnabledByCaller());
            vmHandler.setVmDestroyOnReboot(getVm());
        }

        vmHandler.warnMemorySizeLegal(getParameters().getVm().getStaticData(), getEffectiveCompatibilityVersion());

        // This cannot be reverted using compensation, but it should not be needed
        vmStaticDao.incrementDbGeneration(getVm().getId());

        newVmStatic.setCreationDate(oldVm.getStaticData().getCreationDate());
        newVmStatic.setQuotaId(getQuotaId());

        // save user selected value for hotplug before overriding with db values (when updating running vm)
        VM userVm = new VM();
        userVm.setStaticData(new VmStatic(newVmStatic));

        if (newVmStatic.getCreationDate().equals(DateTime.getMinValue())) {
            newVmStatic.setCreationDate(new Date());
        }

        if (getVm().isRunningOrPaused() && !shouldUpdateForHostedEngineOrKubevirt()) {
            if (!vmHandler.copyNonEditableFieldsToDestination(
                    oldVm.getStaticData(),
                    newVmStatic,
                    isHotSetEnabled(),
                    oldVm.getStatus(),
                    getParameters().isMemoryHotUnplugEnabled())) {
                // fail update vm if some fields could not be copied
                throw new EngineException(EngineError.FAILED_UPDATE_RUNNING_VM);
            }
        } else {
            updateVmNumaNodes();
        }

        if ((getVm().isRunningOrPaused() || getVm().isPreviewSnapshot() || getVm().isSuspended()) && !shouldUpdateForHostedEngineOrKubevirt()) {
            if (getVm().getCustomCompatibilityVersion() == null && getParameters().getClusterLevelChangeFromVersion() != null) {
                // For backward compatibility after cluster version change
                // When running/paused: Set temporary custom compatibility version till the NextRun is applied (VM cold reboot)
                // When snapshot in preview: keep the custom compatibility version even after commit or roll back by undo
                newVmStatic.setCustomCompatibilityVersion(getParameters().getClusterLevelChangeFromVersion());
            }
        }

        updateVmNetworks();
        updateAffinityGroupsAndLabels();
        if (!updateVmLease()) {
            return;
        }

        if (isHotSetEnabled()) {
            hotSetCpus(userVm);
            updateCurrentMemory(userVm);
        }
        final List<Guid> oldIconIds = iconUtils.updateVmIcon(
                oldVm.getStaticData(), newVmStatic, getParameters().getVmLargeIcon());

        if (isCompensationEnabledByCaller()) {
            VmStatic oldStatic = oldVm.getStaticData();
            getCompensationContext().snapshotEntityUpdated(oldStatic);
        }
        resourceManager.getVmManager(getVmId()).update(newVmStatic);

        // Hosted Engine and kubevirt doesn't use next-run snapshots. Instead it requires the configuration
        // for next run to be stored in vm_static table.
        if (getVm().isNotRunning() || shouldUpdateForHostedEngineOrKubevirt()) {
            updateVmPayload();
            getVmDeviceUtils().updateVmDevices(getParameters(), oldVm);
            updateWatchdog();
            updateRngDevice();
            updateGraphicsDevices();
            updateVmHostDevices();
            updateVmDevicesOnEmulatedMachineChange();
            updateVmDevicesOnChipsetChange();
        }
        iconUtils.removeUnusedIcons(oldIconIds, getCompensationContextIfEnabledByCaller());
        vmHandler.updateVmInitToDB(getParameters().getVmStaticData(), getCompensationContextIfEnabledByCaller());

        checkTrustedService();
        liveUpdateCpuProfile();

        // Persist all data in compensation context.
        // It can be done here at the end, because the whole command runs in a transaction.
        compensationStateChanged();

        setSucceeded(true);
    }

    private void updateVmDevicesOnEmulatedMachineChange() {
        if (isEmulatedMachineChanged()) {
            log.info("Emulated machine has changed for VM: {} ({}), the device addresses will be removed.",
                    getVm().getName(),
                    getVm().getId());
            getVmDeviceUtils().removeVmDevicesAddress(getVmId());
            getVmDeviceUtils().resetVmDevicesHash(getVmId());
        }
    }

    private void updateVmDevicesOnChipsetChange() {
        if (isChipsetChanged()) {
            log.info("BIOS chipset type has changed for VM: {} ({}), the disks and devices will be converted to new chipset.",
                    getVm().getName(),
                    getVm().getId());
            getVmHandler().convertVmToNewChipset(getVmId(), getParameters().getVmStaticData().getBiosType().getChipsetType(), getCompensationContextIfEnabledByCaller());
        }
    }

    private boolean shouldUpdateForHostedEngineOrKubevirt() {
        return getVm().isHostedEngine() || !getVm().isManaged();
    }

    private void logNameChange() {
        String runtimeName = vmDynamicDao.get(getVmId()).getRuntimeName();
        String newName = newVmStatic.getName();
        if (!newName.equals(oldVm.getName()) && !newName.equals(runtimeName)) {
            log.info("changing the name of a vm that started as {} to {}", runtimeName, newName);
        }
    }

    private boolean updateVmLease() {
        if (Objects.equals(oldVm.getLeaseStorageDomainId(), newVmStatic.getLeaseStorageDomainId())) {
            return true;
        }

        // Currently, compensation is only used when this command is called from UpdateClusterCommand,
        // and it does not update VM leases.
        // TODO - Add compensation support if needed.
        throwIfCompensationEnabled();

        if (getVm().isNotRunning()) {
            if (!addVmLease(newVmStatic.getLeaseStorageDomainId(), newVmStatic.getId(), false)) {
                return false;
            }
        } else if (isHotSetEnabled()) {
            if (oldVm.getLeaseStorageDomainId() == null) {
                return addVmLease(newVmStatic.getLeaseStorageDomainId(), newVmStatic.getId(), true);
            }
            boolean hotUnplugSucceeded = false;
            try {
                hotUnplugSucceeded = runVdsCommand(VDSCommandType.HotUnplugLease,
                        new LeaseVDSParameters(getVm().getRunOnVds(),
                                oldVm.getId(),
                                oldVm.getLeaseStorageDomainId())).getSucceeded();
            } catch (EngineException e) {
                log.error("Failure in hot unplugging a lease to VM {}, message: {}",
                        oldVm.getId(), e.getMessage());
            }
            if (!hotUnplugSucceeded) {
                auditLog(this, AuditLogType.HOT_UNPLUG_LEASE_FAILED);
            }
        }
        // In case of remove lease only, VM lease info should set to null
        if (oldVm.getLeaseStorageDomainId() != null && newVmStatic.getLeaseStorageDomainId() == null) {
            vmDynamicDao.updateVmLeaseInfo(getVmId(), null);
        }

        // best effort to remove the lease from the previous storage domain
        removeVmLease(oldVm.getLeaseStorageDomainId(), oldVm.getId());
        return true;
    }

    private void liveUpdateCpuProfile(){
        if (getVm().getStatus().isQualifiedForQosChange() &&
                !Objects.equals(oldVm.getCpuProfileId(), newVmStatic.getCpuProfileId())) {
            vmSlaPolicyUtils.refreshCpuQosOfRunningVm(getVm());
        }
    }

    private void updateVmHostDevices() {
        if (isDedicatedVmForVdsChanged()) {
            // Currently, compensation is only used when this command is called from UpdateClusterCommand,
            // and it does not change preferred hosts of the VM.
            // TODO - Add compensation support if needed.
            throwIfCompensationEnabled();

            log.info("Pinned host changed for VM: {}. Dropping configured host devices.", getVm().getName());
            vmDeviceDao.removeVmDevicesByVmIdAndType(getVmId(), VmDeviceGeneralType.HOSTDEV);
        }
    }

    /**
     * Handles a template-version update use case.
     * If vm is down -> updateVmVersionCommand will handle the rest and will preform the actual change.
     * if it's running -> a NEXT_RUN snapshot will be created and the change will take affect only on power down.
     * in both cases the command should end after this function as no more changes are possible.
     */
    private void updateVmTemplateVersion() {
        if (getVm().getStatus() == VMStatus.Down) {
            ActionReturnValue result =
                    runInternalActionWithTasksContext(
                            ActionType.UpdateVmVersion,
                            new UpdateVmVersionParameters(getVmId(),
                                    getParameters().getVm().getVmtGuid(),
                                    getParameters().getVm().isUseLatestVersion()),
                            getLock()
                    );
            if (result.getSucceeded()) {
                getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
            }
            setSucceeded(result.getSucceeded());
            setActionReturnValue(ActionType.UpdateVmVersion);
        } else {
            vmHandler.createNextRunSnapshot(getVm(),
                    getParameters().getVmStaticData(),
                    getParameters(),
                    getCompensationContextIfEnabledByCaller());

            setSucceeded(true);
        }
    }

    private void updateRngDevice() {
        if (!getParameters().isUpdateRngDevice()) {
            return;
        }

        QueryReturnValue query =
                runInternalQuery(QueryType.GetRngDevice, new IdQueryParameters(getParameters().getVmId()));

        List<VmRngDevice> rngDevs = query.getReturnValue();

        ActionReturnValue rngCommandResult = null;
        if (rngDevs.isEmpty()) {
            if (getParameters().getRngDevice() != null) {
                RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), true);
                params.setCompensationEnabled(isCompensationEnabledByCaller());
                rngCommandResult = runInternalAction(ActionType.AddRngDevice, params, cloneContextWithNoCleanupCompensation());
            }
        } else {
            if (getParameters().getRngDevice() == null) {
                RngDeviceParameters params = new RngDeviceParameters(rngDevs.get(0), true);
                params.setCompensationEnabled(isCompensationEnabledByCaller());
                rngCommandResult = runInternalAction(ActionType.RemoveRngDevice, params, cloneContextWithNoCleanupCompensation());
            } else {
                RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), true);
                params.setCompensationEnabled(isCompensationEnabledByCaller());
                params.getRngDevice().setDeviceId(rngDevs.get(0).getDeviceId());
                rngCommandResult = runInternalAction(ActionType.UpdateRngDevice, params, cloneContextWithNoCleanupCompensation());
            }
        }

        if (rngCommandResult != null && !rngCommandResult.getSucceeded()) {
            log.error("Updating RNG device of VM {} ({}) failed. Old RNG device = {}. New RNG device = {}.",
                    getVm().getName(),
                    getVm().getId(),
                    rngDevs.isEmpty() ? null : rngDevs.get(0),
                    getParameters().getRngDevice());
        }
    }

    private void hotSetCpus(VM newVm) {
        int currentSockets = getVm().getNumOfSockets();
        int newNumOfSockets = newVm.getNumOfSockets();

        // try hotplug only if topology (cpuPerSocket, threadsPerCpu) hasn't changed
        if (getVm().getStatus() == VMStatus.Up
                && VmCommonUtils.isCpusToBeHotpluggedOrUnplugged(getVm(), newVm)) {
            HotSetNumberOfCpusParameters params =
                    new HotSetNumberOfCpusParameters(
                            newVmStatic,
                            currentSockets < newNumOfSockets ? PlugAction.PLUG : PlugAction.UNPLUG);
            ActionReturnValue setNumberOfCpusResult =
                    runInternalAction(
                            ActionType.HotSetNumberOfCpus,
                            params, cloneContextAndDetachFromParent());
            // Hosted engine VM does not care if hotplug failed. The requested CPU count is serialized
            // into the OVF store and automatically used during the next HE VM start
            if (!getVm().isHostedEngine()) {
                newVmStatic.setNumOfSockets(setNumberOfCpusResult.getSucceeded() ? newNumOfSockets : currentSockets);
            }
            logHotSetActionEvent(setNumberOfCpusResult, AuditLogType.FAILED_HOT_SET_NUMBER_OF_CPUS);
        }
    }

    private void updateCurrentMemory(VM newVm) {
        int currentMemory = getVm().getMemSizeMb();
        int newAmountOfMemory = newVm.getMemSizeMb();

        if (getVm().getStatus().isNotRunning() || !getVm().isManaged()) {
            newVmStatic.setMemSizeMb(newAmountOfMemory);
            return;
        }

        if (getVm().getStatus() != VMStatus.Up) {
            newVmStatic.setMemSizeMb(currentMemory);
            log.warn("Memory update {}MB -> {}MB of VM {} ({}) left out. Memory can't be updated in current VM state ({}).",
                    currentMemory,
                    newAmountOfMemory,
                    getVm().getName(),
                    getVm().getId(),
                    getVm().getStatus());
            return;
        }

        if (VmCommonUtils.isMemoryToBeHotplugged(getVm(), newVm)) {
            // Temporarily setting to the currentMemory. It will be increased in hotPlugMemory().
            newVmStatic.setMemSizeMb(currentMemory);
            hotPlugMemory(newAmountOfMemory - currentMemory);

            // Hosted engine VM does not care if hotplug failed. The requested memory size is serialized
            // into the OVF store and automatically used during the next HE VM start
            if (getVm().isHostedEngine()) {
                newVmStatic.setMemSizeMb(newAmountOfMemory);
            }
            return;
        }

        if (currentMemory > newAmountOfMemory) {
            final Lock lock = getVmManager().getVmDevicesLock();
            lock.lock();
            try {
                hotUnplugMemory(newVm);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Hot unplug of largest memory device that is smaller or equal to requested memory decrement.
     *
     * <p>The decrement is computed as 'memory of VM in DB' - 'memory of VM from Params'. However user sees value from
     * next run snapshot in the Edit VM dialog.</p>
     *
     * <p>No matter if the memory hot unplug succeeds or not, the next run snapshot always contains values requested by
     * user, not values rounded to the size of memory device.</p>
     */
    private void hotUnplugMemory(VM newVm) {
        if (!getParameters().isMemoryHotUnplugEnabled()) {
            return;
        }
        final List<VmDevice> vmMemoryDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                getVmId(),
                VmDeviceGeneralType.MEMORY,
                VmDeviceType.MEMORY);
        final int oldMemoryMb = oldVm.getMemSizeMb();
        final int oldMinMemoryMb = oldVm.getMinAllocatedMem();
        final List<VmDevice> memoryDevicesToUnplug = MemoryUtils.computeMemoryDevicesToHotUnplug(vmMemoryDevices,
                oldMemoryMb, getParameters().getVm().getMemSizeMb(), getVmManager());
        if (memoryDevicesToUnplug.isEmpty()) {
            logNoDeviceToHotUnplug(vmMemoryDevices);

            // Hosted Engine doesn't use next-run snapshots. Instead it requires the configuration for next run to be stored
            // in vm_static table.
            if (!oldVm.isHostedEngine()) {
                newVmStatic.setMemSizeMb(oldMemoryMb);
                newVmStatic.setMinAllocatedMem(oldMinMemoryMb);
            }
            return;
        }

        final int totalHotUnpluggedMemoryMb = memoryDevicesToUnplug.stream()
                .mapToInt(deviceToHotUnplug -> {
                    final ActionReturnValue hotUnplugReturnValue = runInternalAction(
                            ActionType.HotUnplugMemoryWithoutVmUpdate,
                            new HotUnplugMemoryWithoutVmUpdateParameters(
                                    deviceToHotUnplug.getId(),
                                    newVm.getMinAllocatedMem()),
                            cloneContextAndDetachFromParent());
                    return hotUnplugReturnValue.getSucceeded()
                            ? VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(deviceToHotUnplug).get()
                            : 0;
                })
                .sum();

        // Hosted Engine doesn't use next-run snapshots. Instead it requires the configuration for next run to be stored
        // in vm_static table.
        if (!oldVm.isHostedEngine()) {
            newVmStatic.setMemSizeMb(oldMemoryMb - totalHotUnpluggedMemoryMb);
            newVmStatic.setMinAllocatedMem(totalHotUnpluggedMemoryMb > 0 // at least one hot unplug succeeded
                    ? newVm.getMinAllocatedMem()
                    : oldMinMemoryMb);
        }
    }

    private void logNoDeviceToHotUnplug(List<VmDevice> vmMemoryDevices) {
        final AuditLogType message = vmMemoryDevices.isEmpty()
                ? AuditLogType.NO_MEMORY_DEVICE_TO_HOT_UNPLUG
                : AuditLogType.NO_SUITABLE_MEMORY_DEVICE_TO_HOT_UNPLUG;
        if (!vmMemoryDevices.isEmpty()) {
            final int originalMemoryMb = oldVm.getMemSizeMb();
            addCustomValue(AUDIT_LOG_OLD_MEMORY_MB, String.valueOf(originalMemoryMb));
            addCustomValue(AUDIT_LOG_NEW_MEMORY_MB, String.valueOf(getParameters().getVm().getMemSizeMb()));
            final String unplugOptions = vmMemoryDevices.stream()
                    .filter(VmDeviceCommonUtils::isMemoryDeviceHotUnpluggable)
                    .filter(device -> !getVmManager().isDeviceBeingHotUnlugged(device.getDeviceId()))
                    .map(device -> VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(device).get())
                    .map(deviceSize -> String.format(
                            "%dMB (%dMB)",
                            deviceSize,
                            memoryAfterHotUnplug(originalMemoryMb, deviceSize)))
                    .collect(Collectors.joining(", "));
            addCustomValue(AUDIT_LOG_MEMORY_HOT_UNPLUG_OPTIONS, unplugOptions);
        }
        auditLogDirector.log(this, message);
    }

    private int memoryAfterHotUnplug(int originalMemory, int memoryDeviceSize) {
        final int decrementedSize = originalMemory - memoryDeviceSize;
        return decrementedSize > 0 ? decrementedSize : originalMemory;
    }

    /**
     * Hot plug a memory device.
     *
     * <p>If there isn't memory device of minimal size (i.e. size of memory block), then hot plugged memory is split
     * to two devices of sizes: minimal + the rest.</p>
     *
     * <p>Such behavior is shortcut of "the first hot plugged device has to be of minimal size". The reason for this is
     * that the hot plugged memory is intended to be onlined (make available to the guest OS) as 'online_movable' for it
     * to be unpluggable later on. Memory blocks can be onlined as movable only in order from higher addresses to lower
     * addresses. Trying to online them in a different order fails. Kernel produces events to online memory blocks
     * in arbitrary order, so not all of the blocks may be successfully onlined. But when a memory device of minimal
     * size is hot plugged, it contains just a single memory block, so there is no ambiguity with memory block ordering
     * and the block is always successfully onlined.</p>
     *
     * <p>Following memory hot plugs are not affected by this constraint because the first hot plug extends movable zone
     * of the memory from the first hot plugged device to the end of the memory address space and memory blocks in this
     * movable zone can be onlined as online movable in arbitrary order. Movable zone is not shrunk when memory devices
     * are offlined and hot unplugged.</p>
     */
    private void hotPlugMemory(int memoryHotPlugSize) {
        final int minimalHotPlugDeviceSizeMb = getVm().getClusterArch().getHotplugMemorySizeFactorMb();

        if (requiresSpecialBlock(minimalHotPlugDeviceSizeMb)) {

            if (!hotPlugMemoryDevice(minimalHotPlugDeviceSizeMb)) {
                // If the first hotplug fails, no need to execute the second call
                return;
            }

            int secondPartSizeMb = memoryHotPlugSize - minimalHotPlugDeviceSizeMb;
            if (secondPartSizeMb > 0) {
                hotPlugMemoryDevice(secondPartSizeMb);
            }
        } else {
            hotPlugMemoryDevice(memoryHotPlugSize);
        }
    }

    private boolean requiresSpecialBlock(int minimalHotPlugDeviceSizeMb) {
        if (!osRepository.requiresHotPlugSpecialBlock(getParameters().getVmStaticData().getOsId(),
                getEffectiveCompatibilityVersion())){
            return false;
        }
        final List<VmDevice> memoryDevices = getVmDeviceUtils().getMemoryDevices(getVmId());
        final boolean minimalMemoryDevicePresent = memoryDevices.stream()
                .anyMatch(device -> VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(device)
                        .map(size -> size == minimalHotPlugDeviceSizeMb).orElse(false));
        return !minimalMemoryDevicePresent;
    }

    private boolean hotPlugMemoryDevice(int memHotplugSize) {
        HotSetAmountOfMemoryParameters params =
                new HotSetAmountOfMemoryParameters(
                        newVmStatic,
                        memHotplugSize > 0 ? PlugAction.PLUG : PlugAction.UNPLUG,
                        // We always use node 0, auto-numa should handle the allocation
                        0,
                        memHotplugSize);

        ActionReturnValue setAmountOfMemoryResult =
                runInternalAction(
                        ActionType.HotSetAmountOfMemory,
                        params, cloneContextAndDetachFromParent());

        if (setAmountOfMemoryResult.getSucceeded()) {
            newVmStatic.setMemSizeMb(newVmStatic.getMemSizeMb() + memHotplugSize);
        }
        logHotSetActionEvent(setAmountOfMemoryResult, AuditLogType.FAILED_HOT_SET_MEMORY);

        return setAmountOfMemoryResult.getSucceeded();
    }

    /**
     * add audit log msg for failed hot set in case error was in CDA
     * otherwise internal command will audit log the result
     */
    private void logHotSetActionEvent(ActionReturnValue setActionResult, AuditLogType logType) {
        if (!setActionResult.isValid()) {
            AuditLogable logable = new AuditLogableImpl();
            logable.setVmId(getVmId());
            logable.setVmName(getVmName());
            List<String> validationMessages = backend.getErrorsTranslator()
                    .translateErrorText(setActionResult.getValidationMessages());
            logable.addCustomValue(HotSetNumberOfCpusCommand.LOGABLE_FIELD_ERROR_MESSAGE,
                    StringUtils.join(validationMessages, ","));
            auditLogDirector.log(logable, logType);
        }
    }

    private void checkTrustedService() {
        if (getParameters().getVm().isTrustedService() && !getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED);
        } else if (!getParameters().getVm().isTrustedService() && getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    private void updateWatchdog() {
        // do not update if this flag is not set
        if (getParameters().isUpdateWatchdog()) {
            // Currently, compensation is only used when this command is called from UpdateClusterCommand,
            // and it does not update watchdog.
            // TODO - Add compensation support if needed.
            throwIfCompensationEnabled();

            QueryReturnValue query =
                    runInternalQuery(QueryType.GetWatchdog, new IdQueryParameters(getParameters().getVmId()));
            List<VmWatchdog> watchdogs = query.getReturnValue();

            if (watchdogs.isEmpty() && getParameters().getWatchdog() == null) {
                return;
            }

            WatchdogParameters parameters = new WatchdogParameters();
            parameters.setId(getParameters().getVmId());

            if (getParameters().getWatchdog() != null) {
                parameters.setAction(getParameters().getWatchdog().getAction());
                parameters.setModel(getParameters().getWatchdog().getModel());

                if(watchdogs.isEmpty()) {
                    runInternalAction(ActionType.AddWatchdog, parameters, cloneContextAndDetachFromParent());
                } else {
                    // there is a watchdog in the vm, we have to update.
                    runInternalAction(ActionType.UpdateWatchdog, parameters, cloneContextAndDetachFromParent());
                }
            } else {
                // there is a watchdog in the vm, there should not be any, so let's delete
                runInternalAction(ActionType.RemoveWatchdog, parameters, cloneContextAndDetachFromParent());
            }
        }
    }

    private void updateGraphicsDevices() {
        Set<Map.Entry<GraphicsType, GraphicsDevice>> entries =
                getParameters().getGraphicsDevices().entrySet();
        /* Devices have to be removed first and then added to prevent having multiple devices at the same time
           which is sometimes prohibited (FeatureSupported.multipleGraphicsSupported) */
        entries.stream().filter(entry -> entry.getValue() == null).forEach(entry -> removeGraphicsDevice(entry.getKey()));
        entries.stream().filter(entry -> entry.getValue() != null).forEach(entry -> addOrUpdateGraphicsDevice(entry.getValue()));
    }

    private void removeGraphicsDevice(GraphicsType type) {
        GraphicsDevice existingGraphicsDevice = getGraphicsDevOfType(type);
        if (existingGraphicsDevice != null) {
            GraphicsParameters params = new GraphicsParameters(existingGraphicsDevice);
            params.setCompensationEnabled(isCompensationEnabledByCaller());

            backend.runInternalAction(ActionType.RemoveGraphicsDevice, params, cloneContextWithNoCleanupCompensation());
        }
    }

    private void addOrUpdateGraphicsDevice(GraphicsDevice device) {
        GraphicsDevice existingGraphicsDevice = getGraphicsDevOfType(device.getGraphicsType());
        if (existingGraphicsDevice != null) {
            device.setDeviceId(existingGraphicsDevice.getDeviceId());
        }

        device.setVmId(getVmId());

        GraphicsParameters params = new GraphicsParameters(device);
        params.setCompensationEnabled(isCompensationEnabledByCaller());

        backend.runInternalAction(
                existingGraphicsDevice == null ? ActionType.AddGraphicsDevice : ActionType.UpdateGraphicsDevice,
                params,
                cloneContextWithNoCleanupCompensation());
    }

    private GraphicsDevice getGraphicsDevOfType(GraphicsType type) {
        return getGraphicsDevices().stream().filter(dev -> dev.getGraphicsType() == type).findFirst().orElse(null);
    }

    private List<GraphicsDevice> getGraphicsDevices() {
        if (cachedGraphics == null) {
            cachedGraphics = backend
                    .runInternalQuery(QueryType.GetGraphicsDevices, new IdQueryParameters(getParameters().getVmId())).getReturnValue();
        }
        return cachedGraphics;
    }

    private void updateVmPayload() {
        VmPayload payload = getParameters().getVmPayload();

        if (payload != null || getParameters().isClearPayload()) {
            // Currently, compensation is only used when this command is called from UpdateClusterCommand,
            // and it does not update VM payload.
            // TODO - Add compensation support if needed.
            throwIfCompensationEnabled();

            List<VmDevice> disks = vmDeviceDao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.DISK);
            VmDevice oldPayload = null;
            for (VmDevice disk : disks) {
                if (VmPayload.isPayload(disk.getSpecParams())) {
                    oldPayload = disk;
                    break;
                }
            }

            if (oldPayload != null) {
                vmDeviceDao.remove(oldPayload.getId());
            }

            if (!getParameters().isClearPayload()) {
                getVmDeviceUtils().addManagedDevice(new VmDeviceId(Guid.newGuid(), getVmId()),
                        VmDeviceGeneralType.DISK,
                        payload.getDeviceType(),
                        payload.getSpecParams(),
                        true,
                        true);
            }
        }
    }

    private void updateVmNetworks() {
        // check if the cluster has changed
        if (!Objects.equals(getVm().getClusterId(), getParameters().getVmStaticData().getClusterId())) {
            // Currently, compensation is only used when this command is called from UpdateClusterCommand,
            // and it does not change cluster ID.
            // TODO - Add compensation support if needed.
            throwIfCompensationEnabled();

            List<Network> networks =
                    networkDao.getAllForCluster(getParameters().getVmStaticData().getClusterId());
            List<VmNic> interfaces = vmNicDao.getAllForVm(getParameters().getVmStaticData().getId());

            for (final VmNic iface : interfaces) {
                final Network network = networkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
                boolean networkFound = networks.stream().anyMatch(n -> Objects.equals(n.getId(), network.getId()));

                // if network not exists in cluster we remove the network from the interface
                if (!networkFound) {
                    iface.setVnicProfileId(null);
                    vmNicDao.update(iface);
                }

            }
        }
    }

    private void updateVmNumaNodes() {
        if (!getParameters().isUpdateNuma()) {
            return;
        }

        // Currently, compensation is only used when this command is called from UpdateClusterCommand,
        // and it does not change NUMA nodes.
        // TODO - Add compensation support if needed.
        throwIfCompensationEnabled();

        List<VmNumaNode> newList = getParameters().getVmStaticData().getvNumaNodeList();
        VmNumaNodeOperationParameters params =
                new VmNumaNodeOperationParameters(getParameters().getVm(), new ArrayList<>(newList));

        addLogMessages(backend.runInternalAction(ActionType.SetVmNumaNodes, params));

    }

    private void initNuma() {
        List<VmNumaNode> vNumaNodeList = vmNumaNodeDao.getAllVmNumaNodeByVmId(getParameters().getVmId());
        if (getVm() != null) {
            getVm().setvNumaNodeList(vNumaNodeList);
        }

        // we always need to verify new or existing numa nodes with the updated VM configuration
        if (!getParameters().isUpdateNuma()) {
            getParameters().getVm().setvNumaNodeList(vNumaNodeList);
        }
    }

    private void updateUSB() {
        if (getParameters().getVmStaticData().getDefaultDisplayType() == DisplayType.none && !isConsoleEnabled()) {
            getParameters().getVmStaticData().setUsbPolicy(UsbPolicy.DISABLED);
        }
    }

    private boolean isConsoleEnabled() {
        if (getParameters().isConsoleEnabled() != null) {
            return getParameters().isConsoleEnabled();
        } else {
            return getVmDeviceUtils().hasConsoleDevice(getVmId());
        }
    }

    private void addLogMessages(ActionReturnValue returnValueBase) {
        if (!returnValueBase.getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.NUMA_UPDATE_VM_NUMA_NODE_FAILED);
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateVm.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        VM vmFromDB = getVm();
        VM vmFromParams = getParameters().getVm();

        if (Math.abs(vmFromDB.getVmCreationDate().getTime() - vmFromParams.getVmCreationDate().getTime()) > 1000) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_CREATION_DATE);
        }
        vmFromParams.setVmCreationDate(vmFromDB.getVmCreationDate());

        // check if VM was changed to use latest
        if (vmFromDB.isUseLatestVersion() != vmFromParams.isUseLatestVersion() && vmFromParams.isUseLatestVersion()) {
            // check if a version change is actually required or just let the local command to update this field
            vmFromParams.setVmtGuid(vmTemplateDao.getTemplateWithLatestVersionInChain(getVm().getVmtGuid()).getId());
        }

        // It is not allowed to edit hosted engine VM until it is imported to the engine properly
        if (vmFromDB.isHostedEngine() && !vmFromDB.isManagedHostedEngine()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_UNMANAGED_HOSTED_ENGINE);
        }

        // pool VMs are allowed to change template id, this verifies that the change is only between template versions.
        if (!vmFromDB.getVmtGuid().equals(vmFromParams.getVmtGuid())) {
            VmTemplate origTemplate = vmTemplateDao.get(vmFromDB.getVmtGuid());
            VmTemplate newTemplate = vmTemplateDao.get(vmFromParams.getVmtGuid());
            if (newTemplate == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            } else if (origTemplate != null && !origTemplate.getBaseTemplateId().equals(newTemplate.getBaseTemplateId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_ON_DIFFERENT_CHAIN);

            } else if (vmFromDB.getVmPoolId() != null) {
                isUpdateVmTemplateVersion = true;
                return true; // no more tests are needed because no more changes are allowed in this state
            } else {// template id can be changed for pool VMs only
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_ID_CANT_BE_CHANGED);
            }
        }

        if (getCluster() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        if (vmFromDB.getClusterId() == null) {
            failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        if (!isVmExist()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (StringUtils.isEmpty(vmFromParams.getName())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
        }

        // check that VM name is not too long
        boolean vmNameValidLength = isVmNameValidLength(vmFromParams);
        if (!vmNameValidLength) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
        }

        // Checking if a desktop with same name already exists
        if (!StringUtils.equals(vmFromDB.getName(), vmFromParams.getName())) {
            boolean exists = isVmWithSameNameExists(vmFromParams.getStaticData(), getStoragePoolId());

            if (exists) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            }
        }

        Version customCompatibilityVersionFromParams = vmFromParams.getStaticData().getCustomCompatibilityVersion();
        if (customCompatibilityVersionFromParams != null && !isCompatibilityVersionSupportedByCluster(customCompatibilityVersionFromParams)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CUSTOM_COMPATIBILITY_VERSION_NOT_SUPPORTED,
                    String.format("$Ccv %s", customCompatibilityVersionFromParams));
        }

        if (!validateCustomProperties(vmFromParams.getStaticData())) {
            return false;
        }

        if (!validate(vmHandler.isOsTypeSupported(vmFromParams.getOs(), getCluster().getArchitecture()))) {
            return false;
        }

        if (vmFromParams.getCustomCpuName() == null && !validate(vmHandler.isCpuSupported(
                vmFromParams.getVmOsId(),
                getEffectiveCompatibilityVersion(),
                getCluster().getCpuName()))) {
            return false;
        }

        if (!validate(vmHandler.validateSmartCardDevice(getParameters().getVmStaticData()))) {
            return false;
        }

        if (vmFromParams.isAutoStartup() && vmFromDB.isHostedEngine()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_HOSTED_ENGINE);
        }

        if (vmFromParams.getVmType() == VmType.HighPerformance && vmFromDB.isHostedEngine()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGH_PERFORMANCE_AND_HOSTED_ENGINE);
        }

        if (!areUpdatedFieldsLegal()) {
            return failValidation(vmFromDB.isHostedEngine() ? EngineMessage.VM_CANNOT_UPDATE_HOSTED_ENGINE_FIELD : EngineMessage.VM_CANNOT_UPDATE_ILLEGAL_FIELD);
        }

        if (!vmFromDB.getClusterId().equals(vmFromParams.getClusterId())) {
            return failValidation(EngineMessage.VM_CANNOT_UPDATE_CLUSTER);
        }

        if (!isDedicatedVdsExistOnSameCluster(vmFromParams.getStaticData())) {
            return false;
        }

        // Check if number of monitors passed is legal
        if (getParameters().getVmStaticData().getDefaultDisplayType() != DisplayType.none &&
                !vmHandler.isNumOfMonitorsLegal(
                vmHandler.getResultingVmGraphics(
                        getVmDeviceUtils().getGraphicsTypesOfEntity(getVmId()),
                        getParameters().getGraphicsDevices()),
                getParameters().getVmStaticData().getNumOfMonitors()).isValid()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_NUM_OF_MONITORS);
        }

        // Check PCI and IDE limits are ok
        if (!isValidPciAndIdeLimit(vmFromParams)) {
            return false;
        }

        if (!validate(vmHandler.isVmPriorityValueLegal(vmFromParams.getPriority()))) {
            return false;
        }

        if (!validate(VmValidator.validateCpuSockets(vmFromParams.getStaticData(),
                getEffectiveCompatibilityVersion(),
                getCluster().getArchitecture(),
                osRepository))) {
            return false;
        }

        // check for Vm Payload
        if (getParameters().getVmPayload() != null) {
            if (!checkPayload(getParameters().getVmPayload())) {
                return false;
            }
            // we save the content in base64 string
            for (Map.Entry<String, String> entry : getParameters().getVmPayload().getFiles().entrySet()) {
                entry.setValue(new String(BASE_64.encode(entry.getValue().getBytes()), StandardCharsets.UTF_8));
            }
        }

        // check for Vm Watchdog Model
        if (getParameters().getWatchdog() != null) {
            if (!validate(new VmWatchdogValidator(vmFromParams.getOs(),
                    getParameters().getWatchdog(),
                    getEffectiveCompatibilityVersion()).isValid())) {
                return false;
            }
        }

        // Check if the graphics and display from parameters are supported
        if (!validate(vmHandler.isGraphicsAndDisplaySupported(vmFromParams.getOs(),
                vmHandler.getResultingVmGraphics(getVmDeviceUtils().getGraphicsTypesOfEntity(getVmId()),
                        getParameters().getGraphicsDevices()),
                vmFromParams.getDefaultDisplayType(),
                vmFromParams.getBiosType(),
                getEffectiveCompatibilityVersion()))) {
            return false;
        }

        if (!FeatureSupported.isMigrationSupported(getCluster().getArchitecture(), getEffectiveCompatibilityVersion())
                && vmFromParams.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
            return failValidation(EngineMessage.VM_MIGRATION_IS_NOT_SUPPORTED);
        }

        // check cpuPinning
        if (!validate(isCpuPinningValid(vmFromParams.getCpuPinning(), vmFromParams.getStaticData()))) {
            return false;
        }


        if (!isCpuSharesValid(vmFromParams)) {
            return failValidation(EngineMessage.QOS_CPU_SHARES_OUT_OF_RANGE);
        }

        if (!VmCpuCountHelper.validateCpuCounts(vmFromParams, getEffectiveCompatibilityVersion(),
                getCluster().getArchitecture())) {
            return failValidation(EngineMessage.TOO_MANY_CPU_COMPONENTS);
        }

        if (vmFromParams.isUseHostCpuFlags() && (ArchitectureType.ppc == getCluster().getArchitecture().getFamily())) {
            return failValidation(EngineMessage.USE_HOST_CPU_REQUESTED_ON_UNSUPPORTED_ARCH);
        }

        if (isHotSetEnabled() && !validateCPUHotplug(getParameters().getVmStaticData())) {
            return failValidation(EngineMessage.CPU_HOTPLUG_TOPOLOGY_INVALID);
        }

        if (!validateMemoryAlignment(getParameters().getVmStaticData())) {
            return false;
        }

        final int memorySizeChangeMb = vmFromParams.getMemSizeMb() - vmFromDB.getMemSizeMb();
        if (isHotSetEnabled() && getVm().getStatus() != VMStatus.Down) {
            if (getVm().isNextRunConfigurationExists()) {
                final VmStatic nextRunConfigurationStatic = snapshotVmConfigurationHelper
                        .getVmStaticFromNextRunConfiguration(getVmId());
                if (nextRunConfigurationStatic != null
                        && vmFromParams.getMemSizeMb() != nextRunConfigurationStatic.getMemSizeMb()
                        && nextRunConfigurationStatic.getMemSizeMb() != vmFromDB.getMemSizeMb()) {
                    return failValidation(EngineMessage.HOT_CHANGE_MEMORY_WITH_NEXT_RUN);
                }
            }
            if (memorySizeChangeMb > 0) {
                // Check the DB version here to prevent NullPointerException
                if (!FeatureSupported.hotPlugMemory(vmFromDB.getCompatibilityVersion(), vmFromDB.getClusterArch())) {
                    return failValidation(EngineMessage.HOT_PLUG_MEMORY_IS_NOT_SUPPORTED);
                }

                final int factor = getVm().getClusterArch().getHotplugMemorySizeFactorMb();
                if (memorySizeChangeMb % factor != 0) {
                    addValidationMessageVariable("memoryAdded", String.valueOf(memorySizeChangeMb));
                    addValidationMessageVariable("requiredFactor", String.valueOf(factor));
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOT_PLUGGED_MEMORY_MUST_BE_DIVIDABLE_BY);
                }
            }
        }

        if (isVirtioScsiEnabled())  {
            // Verify OS compatibility
            if (!validate(vmHandler.isOsTypeSupportedForVirtioScsi
                    (vmFromParams.getOs(), getEffectiveCompatibilityVersion()))) {
                return false;
            }
        }

        VmValidator vmValidator = createVmValidator(vmFromParams);

        // A pinned VM, must run on one of its hosts
        if (!validate(vmValidator.isPinnedVmRunningOnDedicatedHost(vmFromDB, vmFromParams.getStaticData()))){
            return false;
        }

        if (Boolean.FALSE.equals(getParameters().isVirtioScsiEnabled()) && !validate(vmValidator.canDisableVirtioScsi(null))) {
            return false;
        }

        if (vmFromParams.getMinAllocatedMem() > vmFromParams.getMemSizeMb()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        if (vmFromParams.getCpuProfileId() == null ||
                !Objects.equals(vmFromDB.getCpuProfileId(), vmFromParams.getCpuProfileId())) {
            if (!setAndValidateCpuProfile()) {
                return false;
            }
        }

        if (isSoundDeviceEnabled() && !osRepository.isSoundDeviceEnabled(getParameters().getVmStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
        }

        if (isTpmEnabled()
                && !getVmDeviceUtils().isTpmDeviceSupported(getParameters().getVmStaticData(), getCluster())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.TPM_DEVICE_REQUESTED_ON_NOT_SUPPORTED_PLATFORM);
        }

        if (!isTpmEnabled() && osRepository.requiresTpm(getParameters().getVmStaticData().getOsId())) {
            return failValidation(EngineMessage.TPM_DEVICE_REQUIRED_BY_OS);
        }

        if (!validate(getNumaValidator().checkVmNumaNodesIntegrity(
                getParameters().getVm(),
                getParameters().getVm().getvNumaNodeList()))) {
            return false;
        }

        if (getParameters().getVmLargeIcon() != null && !validate(IconValidator.validate(
                IconValidator.DimensionsType.LARGE_CUSTOM_ICON,
                getParameters().getVmLargeIcon()))) {
            return false;
        }

        if (getParameters().getVmStaticData() != null
                && getParameters().getVmStaticData().getSmallIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getVmStaticData().getSmallIconId(), "Small"))) {
            return false;
        }

        if (getParameters().getVmStaticData() != null
                && getParameters().getVmStaticData().getLargeIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getVmStaticData().getLargeIconId(), "Large"))) {
            return false;
        }

        if (vmFromParams.getProviderId() != null) {
            Provider<?> provider = providerDao.get(vmFromParams.getProviderId());
            if (provider == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST);
            }

            if (provider.getType() != ProviderType.FOREMAN && provider.getType() != ProviderType.KUBEVIRT) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOST_PROVIDER_TYPE_MISMATCH);
            }
        }

        if (getCluster().isInUpgradeMode()) {
            getParameters().getVm().setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            if (!validate(getClusterUpgradeValidator().isVmReadyForUpgrade(getParameters().getVm()))) {
                return false;
            }
        }

        if (!validateQuota(getParameters().getVmStaticData().getQuotaId())) {
            return false;
        }

        if (!validate(vmHandler.validateMaxMemorySize(
                getParameters().getVmStaticData(),
                getEffectiveCompatibilityVersion()))) {
            return false;
        }

        if (shouldAddLease(getParameters().getVmStaticData())) {
            if (!canAddLease()) {
                return false;
            }
            if (!getVm().isDown() && getParameters().getVmStaticData().getLeaseStorageDomainId() != null
                    && getVm().getLeaseStorageDomainId() != null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOT_SWAPPING_VM_LEASES_NOT_SUPPORTED);
            }
        }

        List<EngineMessage> msgs = cloudInitHandler.validate(getParameters().getVm().getVmInit());
        if (!CollectionUtils.isEmpty(msgs)) {
            return failValidation(msgs);
        }

        final boolean isMemoryHotUnplug = memorySizeChangeMb < 0
                && isHotSetEnabled()
                && getParameters().isMemoryHotUnplugEnabled();
        if (isMemoryHotUnplug
                && !FeatureSupported.hotUnplugMemory(getVm().getCompatibilityVersion(), getVm().getClusterArch())) {
            return failValidation(
                    EngineMessage.ACTION_TYPE_FAILED_MEMORY_HOT_UNPLUG_NOT_SUPPORTED_FOR_COMPAT_VERSION_AND_ARCH,
                    ReplacementUtils.createSetVariableString(
                            "compatibilityVersion", getVm().getCompatibilityVersion()),
                    ReplacementUtils.createSetVariableString(
                            "architecture", getVm().getClusterArch()));
        }

        if (vmFromDB.getMaxMemorySizeMb() < vmFromParams.getMemSizeMb() &&
                vmFromDB.isRunning() &&
                isHotSetEnabled()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MAX_MEMORY_CANNOT_BE_SMALLER_THAN_MEMORY_SIZE,
                    ReplacementUtils.createSetVariableString("maxMemory", vmFromDB.getMaxMemorySizeMb()),
                    ReplacementUtils.createSetVariableString("memory", vmFromParams.getMemSizeMb()));
        }

        if (memorySizeChangeMb != 0 && vmFromDB.isRunning() && isHotSetEnabled()
                && HugePageUtils.isBackedByHugepages(vmFromDB.getStaticData())
                && (memorySizeChangeMb > 0 || (memorySizeChangeMb < 0 && getParameters().isMemoryHotUnplugEnabled()))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MEMORY_HOT_SET_NOT_SUPPORTED_FOR_HUGE_PAGES);
        }

        if (!validate(vmValidator.isBiosTypeSupported(getCluster(), osRepository))) {
            return false;
        }

        if (vmFromDB.getVmPoolId() != null) {
            VmPool vmPool = vmPoolDao.get(vmFromDB.getVmPoolId());
            if (vmPool == null || !vmPool.isStateful()) {
                VmInit oldVmInit = vmInitDao.get(vmFromDB.getId());

                if (!Objects.equals(getParameters().getVmStaticData().getVmInit(), oldVmInit)) {
                    return failValidation(EngineMessage.ACTION_TYPE_CANNOT_CHANGE_INITIAL_RUN_DATA);
                }
            }
        }

        if (!validate(validateAffinityGroups())) {
            return false;
        }

        if (!isIsoPathExists(vmFromParams.getStaticData(), getVm().getStoragePoolId())){
            return failValidation(EngineMessage.ERROR_CANNOT_FIND_ISO_IMAGE_PATH);
        }
        if (!validate(vmHandler.validateCpuPinningPolicy(getParameters().getVmStaticData(),
                getParameters().isUpdateNuma()))) {
            return false;
        }
        return true;
    }

    private boolean isIsoPathExists(VmStatic newVm, Guid storagePoolId) {
        if (StringUtils.isEmpty(newVm.getIsoPath())) {
            return true;
        }
        Guid isoDomainId = isoDomainListSynchronizer.findActiveISODomain(storagePoolId);
        List<RepoImage> repoFilesMap =
                isoDomainListSynchronizer.getCachedIsoListByDomainId(isoDomainId, ImageFileType.ISO);
        repoFilesMap.addAll(diskImageDao.getIsoDisksForStoragePoolAsRepoImages(storagePoolId));
        return repoFilesMap.stream().anyMatch(r -> r.getRepoImageId().equals(newVm.getIsoPath()));
    }

    @Override
    protected boolean shouldAddLease(VmStatic newVm) {
        return super.shouldAddLease(newVm) && !newVm.getLeaseStorageDomainId().equals(getVm().getLeaseStorageDomainId());
    }

    protected boolean isDedicatedVdsExistOnSameCluster(VmBase vm) {
        return validate(vmHandler.validateDedicatedVdsExistOnSameCluster(vm));
    }

    protected boolean isValidPciAndIdeLimit(VM vmFromParams) {
        List<DiskVmElement> diskVmElements = diskVmElementDao.getAllForVm(getVmId());
        List<VmNic> interfaces = vmNicDao.getAllForVm(getVmId());

        return validate(VmValidator.checkPciAndIdeLimit(
                vmFromParams.getOs(),
                getEffectiveCompatibilityVersion(),
                vmFromParams.getNumOfMonitors(),
                interfaces,
                diskVmElements,
                isVirtioScsiEnabled(),
                hasWatchdog(),
                isSoundDeviceEnabled()));
    }

    private boolean isVmExist() {
        return getParameters().getVmStaticData() != null && getVm() != null;
    }

    protected boolean areUpdatedFieldsLegal() {
        return vmHandler.isUpdateValid(getVm().getStaticData(),
                getParameters().getVmStaticData(),
                VMStatus.Down);
    }

    /**
     * check if we need to use running-configuration. Hosted Engine VM and kubevirt always returns false.
     * @return true if vm is running and we change field that has @EditableOnVmStatusField annotation
     *          or runningConfiguration already exist
     */
    private boolean isRunningConfigurationNeeded() {
        if (getVm().isHostedEngine() || !getVm().isManaged()) {
            // Hosted Engine never uses the next run configuration
            return false;
        }

        return getVm().isNextRunConfigurationExists()
                || !vmHandler.isUpdateValid(
                        getVm().getStaticData(),
                        getParameters().getVmStaticData(),
                        getVm().getStatus(),
                        isHotSetEnabled())
                || !vmHandler.isUpdateValidForVmDevices(getVmId(), getVm().getStatus(), getParameters())
                || isClusterLevelChange()
                || memoryNextRunSnapshotRequired();
    }

    /**
     * Annotation {@link org.ovirt.engine.core.common.businessentities.EditableVmField} defines {@link VmBase#memSizeMb}
     * as hot settable however the snapshot is needed
     * <ul>
     *     <li>while increasing memory since hotplug is only allowed for certain values (multiples of 256MiB by default)
     *     </li>
     *     <li>while decreasing memory since hot unplug is only allowed from REST (not allowed from Edit VM dialog)</li>
     *     <li>when VM is not in state {@link VMStatus#Up} or {@link VMStatus#Down} since memory hot (un)plugs can only
     *     be done in state Up</li>
     * </ul>
     * @return true if next run snapshot is needed because of memory change
     */
    private boolean memoryNextRunSnapshotRequired() {
        final int memoryChange = getParameters().getVm().getMemSizeMb() - oldVm.getMemSizeMb();
        return VMStatus.Down != getVm().getStatus()
                && ((memoryChange < 0 && !getParameters().isMemoryHotUnplugEnabled())
                        || memoryChange % getVm().getClusterArch().getHotplugMemorySizeFactorMb() != 0);
    }

    private boolean isClusterLevelChange() {
        Version oldVersion = getParameters().getClusterLevelChangeFromVersion();
        return oldVersion != null &&
                (getVm().isRunningOrPaused() || getVm().isSuspended()) &&
                getVm().getCustomCompatibilityVersion() == null;
    }

    private boolean isHotSetEnabled() {
        return !getParameters().isApplyChangesLater();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (isVmExist()) {
            // user need specific permission to change custom properties
            if (!StringUtils.equals(
                    getVm().getPredefinedProperties(),
                    getParameters().getVmStaticData().getPredefinedProperties())
                    || !StringUtils.equals(
                            getVm().getUserDefinedProperties(),
                            getParameters().getVmStaticData().getUserDefinedProperties())) {
                permissionList.add(new PermissionSubject(getParameters().getVmId(),
                        VdcObjectType.VM,
                        ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
            }

            // host-specific parameters can be changed by administration role only
            if (isDedicatedVmForVdsChanged() || isCpuPinningChanged()) {
                permissionList.add(
                        new PermissionSubject(getParameters().getVmId(),
                                VdcObjectType.VM,
                                ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }

        return permissionList;
    }

    private boolean isDedicatedVmForVdsChanged() {
        List<Guid> paramList = getParameters().getVmStaticData().getDedicatedVmForVdsList();
        List<Guid> vmList = getVm().getDedicatedVmForVdsList();
        if (vmList == null && paramList == null){
            return false;
        }
        if (vmList == null || paramList == null){
            return true;
        }
        //  vmList.equals(paramList) not good enough, the lists order could change
        if (vmList.size() != paramList.size()){
            return true;
        }
        return !paramList.containsAll(vmList);
    }

    private boolean isCpuPinningChanged() {
        return !(getVm().getCpuPinning() == null ?
                getParameters().getVmStaticData().getCpuPinning() == null :
                getVm().getCpuPinning().equals(getParameters().getVmStaticData().getCpuPinning()));
    }

    private boolean isEmulatedMachineChanged() {
        return !Objects.equals(getParameters().getVm().getCustomEmulatedMachine(), getVm().getCustomEmulatedMachine());
    }

    private boolean isChipsetChanged() {
        BiosType newBiosType = getParameters().getVmStaticData().getBiosType();
        BiosType oldBiosType = getVm().getBiosType();
        return  newBiosType.getChipsetType() != oldBiosType.getChipsetType();
    }

    @Override
    public Guid getVmId() {
        if (super.getVmId().equals(Guid.Empty)) {
            super.setVmId(getParameters().getVmStaticData().getId());
        }
        return super.getVmId();
    }

    public static Map<String, Pair<String, String>> getExclusiveLocksForUpdateVm(VM vm) {
        // When updating, please also update UpdateClusterCommand#getExclusiveLocks
        if (!StringUtils.isBlank(vm.getName())) {
            return Collections.singletonMap(vm.getName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_NAME, EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_UPDATED));
        }
        return null;
    }

    public static Map<String, Pair<String, String>> getSharedLocksForUpdateVm(VM vm) {
        // When updating, please also update UpdateClusterCommand#getSharedLocks
        return Collections.singletonMap(
                vm.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.VM,
                        EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_UPDATED));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getExclusiveLocksForUpdateVm(getParameters().getVm());
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return getSharedLocksForUpdateVm(getParameters().getVm());
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        // The cases must be persistent with the create_functions_sp
        if (!getQuotaManager().isVmStatusQuotaCountable(getVm().getStatus())) {
            list.add(new QuotaSanityParameter(getQuotaId()));
            quotaSanityOnly = true;
        } else {
            if (!getQuotaId().equals(getVm().getQuotaId())) {
                list.add(new QuotaClusterConsumptionParameter(getVm().getQuotaId(),
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        getClusterId(),
                        VmCpuCountHelper.getDynamicNumOfCpu(getVm()),
                        getVm().getMemSizeMb()));
                list.add(new QuotaClusterConsumptionParameter(getQuotaId(),
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        getParameters().getVmStaticData().getClusterId(),
                        getParameters().getVmStaticData().getNumOfCpus(),
                        getParameters().getVmStaticData().getMemSizeMb()));
            }
        }
        return list;
    }

    private Guid getQuotaId() {
        return getQuotaManager().getFirstQuotaForUser(
                getParameters().getVmStaticData().getQuotaId(),
                getStoragePoolId(),
                getCurrentUser());
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.VM.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldVm.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVmStaticData().getName();
    }

    @Override
    public void setEntityId(AuditLogable logable) {
        logable.setVmId(oldVm.getId());
    }
    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        // if only quota sanity is checked the user may use a quota he cannot consume
        // (it will be consumed only when the vm will run)
        if (!quotaSanityOnly) {
            super.addQuotaPermissionSubject(quotaPermissionList);
        }
    }

    protected boolean isVirtioScsiEnabled() {
        Boolean virtioScsiEnabled = getParameters().isVirtioScsiEnabled();
        return virtioScsiEnabled != null ? virtioScsiEnabled : isVirtioScsiEnabledForVm(getVmId());
    }

    public boolean isVirtioScsiEnabledForVm(Guid vmId) {
        return getVmDeviceUtils().hasVirtioScsiController(vmId);
    }

    protected boolean isSoundDeviceEnabled() {
        Boolean soundDeviceEnabled = getParameters().isSoundDeviceEnabled();
        return soundDeviceEnabled != null ? soundDeviceEnabled :
                getVmDeviceUtils().hasSoundDevice(getVmId());
    }

    protected boolean isTpmEnabled() {
        Boolean tpmDeviceEnabled = getParameters().isTpmEnabled();
        return tpmDeviceEnabled != null ? tpmDeviceEnabled :
                getVmDeviceUtils().hasTpmDevice(getVmId());
    }

    protected boolean hasWatchdog() {
        return getParameters().getWatchdog() != null;
    }

    public VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }

    protected InClusterUpgradeValidator getClusterUpgradeValidator() {
        return clusterUpgradeValidator;
    }

    private ValidationResult validateAffinityGroups() {
        AffinityValidator.Result result = affinityValidator.validateAffinityUpdateForVm(getClusterId(),
                getVmId(),
                getParameters().getAffinityGroups(),
                getParameters().getAffinityLabels());

        affinityGroupLoggingMethod = result.getLoggingMethod();
        return result.getValidationResult();
    }

    private void updateAffinityGroupsAndLabels() {
        // Currently, this method does not use compensation to revert this operation,
        // because affinity groups are not changed when this command is called as a child of
        // UpdateClusterCommand.

        // TODO - check permissions to modify affinity groups
        List<AffinityGroup> affinityGroups = getParameters().getAffinityGroups();
        if (affinityGroups != null) {
            affinityGroupLoggingMethod.accept(this, auditLogDirector);
            affinityGroupDao.setAffinityGroupsForVm(getVmId(),
                    affinityGroups.stream()
                            .map(AffinityGroup::getId)
                            .collect(Collectors.toList()));
        }

        // TODO - check permissions to modify labels
        List<Label> affinityLabels = getParameters().getAffinityLabels();
        if (affinityLabels != null) {
            List<Guid> labelIds = affinityLabels.stream()
                    .map(Label::getId)
                    .collect(Collectors.toList());
            labelDao.updateLabelsForVm(getVmId(), labelIds);
        }
    }

    @Override
    protected boolean shouldUpdateHostedEngineOvf() {
        return true;
    }

    @Override
    public CommandCallback getCallback() {
        return getVm().getVmPoolId() != null ? callbackProvider.get() : null;
    }
}

package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.validator.CpuPinningValidator.isCpuPinningValid;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfDataUpdater;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.RngDeviceUtils;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.HotSetAmountOfMemoryParameters;
import org.ovirt.engine.core.common.action.HotSetNumberOfCpusParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
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
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.UpdateVm;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionCompletionListener;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.monitoring.VmDevicesMonitoring;

public class UpdateVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{

    private static final Base64 BASE_64 = new Base64(0, null);

    @Inject
    private VmSlaPolicyUtils vmSlaPolicyUtils;
    @Inject
    private VmDevicesMonitoring vmDevicesMonitoring;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private InClusterUpgradeValidator clusterUpgradeValidator;
    @Inject
    private RngDeviceUtils rngDeviceUtils;
    @Inject
    private OvfDataUpdater ovfDataUpdater;
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private SnapshotDao snapshotDao;

    private VM oldVm;
    private boolean quotaSanityOnly = false;
    private VmStatic newVmStatic;
    private VdcReturnValueBase setNumberOfCpusResult;
    private List<GraphicsDevice> cachedGraphics;
    private boolean isUpdateVmTemplateVersion = false;

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
        vmHandler.autoSelectDefaultDisplayType(getVmId(),
                getParameters().getVmStaticData(),
                getCluster(),
                getParameters().getGraphicsDevices());

        updateParametersVmFromInstanceType();

        // we always need to verify new or existing numa nodes with the updated VM configuration
        if (!getParameters().isUpdateNuma()) {
            getParameters().getVm().setvNumaNodeList(vmNumaNodeDao.getAllVmNumaNodeByVmId(getParameters().getVmId()));
        }

        if (getParameters().getVmStaticData().getDefaultDisplayType() == DisplayType.none && !getParameters().isConsoleEnabled()) {
            getParameters().getVmStaticData().setUsbPolicy(UsbPolicy.DISABLED);
        }
    }

    private VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
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
            vmHandler.createNextRunSnapshot(
                    getVm(), getParameters().getVmStaticData(), getParameters(), getCompensationContext());
        } else if (!updateVmLease()) {
            return;
        }

        vmHandler.warnMemorySizeLegal(getParameters().getVm().getStaticData(), getEffectiveCompatibilityVersion());
        vmStaticDao.incrementDbGeneration(getVm().getId());
        newVmStatic.setCreationDate(oldVm.getStaticData().getCreationDate());
        newVmStatic.setQuotaId(getQuotaId());

        // Trigger OVF update for hosted engine VM only
        if (getVm().isHostedEngine()) {
            registerRollbackHandler(new TransactionCompletionListener() {
                @Override
                public void onSuccess() {
                    ovfDataUpdater.triggerNow();
                }

                @Override
                public void onRollback() {
                    // No notification is needed
                }
            });
        }

        // save user selected value for hotplug before overriding with db values (when updating running vm)
        VM userVm = new VM();
        userVm.setStaticData(new VmStatic(newVmStatic));

        if (newVmStatic.getCreationDate().equals(DateTime.getMinValue())) {
            newVmStatic.setCreationDate(new Date());
        }

        if (getVm().isRunningOrPaused() && !getVm().isHostedEngine()) {
            if (!vmHandler.copyNonEditableFieldsToDestination(oldVm.getStaticData(), newVmStatic, isHotSetEnabled())) {
                // fail update vm if some fields could not be copied
                throw new EngineException(EngineError.FAILED_UPDATE_RUNNING_VM);
            }
        }

        if ((getVm().isRunningOrPaused() || getVm().isPreviewSnapshot() || getVm().isSuspended()) && !getVm().isHostedEngine()) {
            if (getVm().getCustomCompatibilityVersion() == null && getParameters().getClusterLevelChangeFromVersion() != null) {
                // For backward compatibility after cluster version change
                // When running/paused: Set temporary custom compatibility version till the NextRun is applied (VM cold reboot)
                // When snapshot in preview: keep the custom compatibility version even after commit or roll back by undo
                newVmStatic.setCustomCompatibilityVersion(getParameters().getClusterLevelChangeFromVersion());
            }
        }

        updateVmNetworks();
        updateVmNumaNodes();
        if (isHotSetEnabled()) {
            hotSetCpus(userVm);
            updateCurrentMemory(userVm);
        }
        final List<Guid> oldIconIds = IconUtils.updateVmIcon(
                oldVm.getStaticData(), newVmStatic, getParameters().getVmLargeIcon());
        resourceManager.getVmManager(getVmId()).update(newVmStatic);
        if (getVm().isNotRunning()) {
            updateVmPayload();
            getVmDeviceUtils().updateVmDevices(getParameters(), oldVm);
            updateWatchdog();
            updateRngDevice();
            updateGraphicsDevices();
            updateVmHostDevices();
            updateDeviceAddresses();
        }
        IconUtils.removeUnusedIcons(oldIconIds);
        vmHandler.updateVmInitToDB(getParameters().getVmStaticData());

        checkTrustedService();
        liveUpdateCpuProfile();
        setSucceeded(true);
    }

    private boolean updateVmLease() {
        if (Objects.equals(oldVm.getLeaseStorageDomainId(), newVmStatic.getLeaseStorageDomainId())) {
            return true;
        }

        if (!addVmLease(newVmStatic.getLeaseStorageDomainId(), newVmStatic.getId())) {
            return false;
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
            VdcReturnValueBase result =
                    runInternalActionWithTasksContext(
                            VdcActionType.UpdateVmVersion,
                            new UpdateVmVersionParameters(getVmId(),
                                    getParameters().getVm().getVmtGuid(),
                                    getParameters().getVm().isUseLatestVersion()),
                            getLock()
                    );
            if (result.getSucceeded()) {
                getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
            }
            setSucceeded(result.getSucceeded());
            setActionReturnValue(VdcActionType.UpdateVmVersion);
        } else {
            vmHandler.createNextRunSnapshot(
                    getVm(), getParameters().getVmStaticData(), getParameters(), getCompensationContext());
            setSucceeded(true);
        }
    }

    private void updateRngDevice() {
        if (!getParameters().isUpdateRngDevice()) {
            return;
        }

        VdcQueryReturnValue query =
                runInternalQuery(VdcQueryType.GetRngDevice, new IdQueryParameters(getParameters().getVmId()));

        List<VmRngDevice> rngDevs = query.getReturnValue();

        VdcReturnValueBase rngCommandResult = null;
        if (rngDevs.isEmpty()) {
            if (getParameters().getRngDevice() != null) {
                RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), true);
                rngCommandResult = runInternalAction(VdcActionType.AddRngDevice, params, cloneContextAndDetachFromParent());
            }
        } else {
            if (getParameters().getRngDevice() == null) {
                RngDeviceParameters params = new RngDeviceParameters(rngDevs.get(0), true);
                rngCommandResult = runInternalAction(VdcActionType.RemoveRngDevice, params, cloneContextAndDetachFromParent());
            } else {
                RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), true);
                params.getRngDevice().setDeviceId(rngDevs.get(0).getDeviceId());
                rngCommandResult = runInternalAction(VdcActionType.UpdateRngDevice, params, cloneContextAndDetachFromParent());
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

    private void updateDeviceAddresses() {
        if (isEmulatedMachineChanged()) {
            log.info("Emulated machine changed for VM: {} ({}). Clearing device addresses.",
                    getVm().getName(),
                    getVm().getId());
            vmDeviceDao.clearAllDeviceAddressesByVmId(getVmId());

            VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(System.nanoTime());
            change.updateVm(getVmId(), VmDevicesMonitoring.EMPTY_HASH);
            change.flush();
        }
    }

    private void hotSetCpus(VM newVm) {
        int currentSockets = getVm().getNumOfSockets();
        int newNumOfSockets = newVm.getNumOfSockets();

        // try hotplug only if topology (cpuPerSocket, threadsPerCpu) hasn't changed
        if (getVm().getStatus() == VMStatus.Up
                && VmCommonUtils.isCpusToBeHotplugged(getVm(), newVm)) {
            HotSetNumberOfCpusParameters params =
                    new HotSetNumberOfCpusParameters(
                            newVmStatic,
                            currentSockets < newNumOfSockets ? PlugAction.PLUG : PlugAction.UNPLUG);
            setNumberOfCpusResult =
                    runInternalAction(
                            VdcActionType.HotSetNumberOfCpus,
                            params, cloneContextAndDetachFromParent());
            // Hosted engine VM does not care if hotplug failed. The requested CPU count is serialized
            // into the OVF store and automatically used during the next HE VM start
            if (!getVm().isHostedEngine()) {
                newVmStatic.setNumOfSockets(setNumberOfCpusResult.getSucceeded() ? newNumOfSockets : currentSockets);
            }
            hotSetCpusLog(params);
        }
    }

    private void updateCurrentMemory(VM newVm) {
        int currentMemory = getVm().getMemSizeMb();
        int newAmountOfMemory = newVm.getMemSizeMb();

        if (getVm().getStatus().isNotRunning()) {
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
            hotSetMemory(currentMemory, newAmountOfMemory);
        }
    }

    private void hotSetMemory(int currentMemory, int newAmountOfMemory) {
        HotSetAmountOfMemoryParameters params =
                new HotSetAmountOfMemoryParameters(
                        newVmStatic,
                        currentMemory < newAmountOfMemory ? PlugAction.PLUG : PlugAction.UNPLUG,
                        // We always use node 0, auto-numa should handle the allocation
                        0);

        VdcReturnValueBase setAmountOfMemoryResult =
                runInternalAction(
                        VdcActionType.HotSetAmountOfMemory,
                        params, cloneContextAndDetachFromParent());
        // Hosted engine VM does not care if hostplug failed. The requested memory size is serialized
        // into the OVF store and automatically used during the next HE VM start
        if (!getVm().isHostedEngine()) {
            newVmStatic.setMemSizeMb(setAmountOfMemoryResult.getSucceeded() ? newAmountOfMemory : currentMemory);
        }
        hotSetMemlog(params, setAmountOfMemoryResult);
    }

    /**
     * add audit log msg for failed hot set in case error was in CDA
     * otherwise internal command will audit log the result
     */
    private void hotSetCpusLog(HotSetNumberOfCpusParameters params) {
        if (!setNumberOfCpusResult.isValid()) {
            AuditLogableBase logable = CommandsFactory.createCommand(VdcActionType.HotSetNumberOfCpus, params);
            List<String> validationMessages = getBackend().getErrorsTranslator().
                    translateErrorText(setNumberOfCpusResult.getValidationMessages());
            logable.addCustomValue(HotSetNumberOfCpusCommand.LOGABLE_FIELD_ERROR_MESSAGE, StringUtils.join(validationMessages, ","));
            auditLogDirector.log(logable, AuditLogType.FAILED_HOT_SET_NUMBER_OF_CPUS);
        }
    }

    /**
     * add audit log msg for failed hot set in case error was in CDA
     * otherwise internal command will audit log the result
     */
    private void hotSetMemlog(HotSetAmountOfMemoryParameters params, VdcReturnValueBase setAmountOfMemoryResult) {
        if (!setAmountOfMemoryResult.isValid()) {
            AuditLogableBase logable = CommandsFactory.createCommand(VdcActionType.HotSetAmountOfMemory, params);
            List<String> validationMessages = getBackend().getErrorsTranslator().
                    translateErrorText(setAmountOfMemoryResult.getValidationMessages());
            logable.addCustomValue(HotSetAmountOfMemoryCommand.LOGABLE_FIELD_ERROR_MESSAGE, StringUtils.join(validationMessages, ","));
            auditLogDirector.log(logable, AuditLogType.FAILED_HOT_SET_MEMORY);
        }
    }

    private void checkTrustedService() {
        if (getParameters().getVm().isTrustedService() && !getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getParameters().getVm().isTrustedService() && getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    private void updateWatchdog() {
        // do not update if this flag is not set
        if (getParameters().isUpdateWatchdog()) {
            VdcQueryReturnValue query =
                    runInternalQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(getParameters().getVmId()));
            List<VmWatchdog> watchdogs = query.getReturnValue();
            if (watchdogs.isEmpty()) {
                if (getParameters().getWatchdog() == null) {
                    // nothing to do, no watchdog and no watchdog to create
                } else {
                    WatchdogParameters parameters = new WatchdogParameters();
                    parameters.setId(getParameters().getVmId());
                    parameters.setAction(getParameters().getWatchdog().getAction());
                    parameters.setModel(getParameters().getWatchdog().getModel());
                    runInternalAction(VdcActionType.AddWatchdog, parameters, cloneContextAndDetachFromParent());
                }
            } else {
                WatchdogParameters watchdogParameters = new WatchdogParameters();
                watchdogParameters.setId(getParameters().getVmId());
                if (getParameters().getWatchdog() == null) {
                    // there is a watchdog in the vm, there should not be any, so let's delete
                    runInternalAction(VdcActionType.RemoveWatchdog, watchdogParameters, cloneContextAndDetachFromParent());
                } else {
                    // there is a watchdog in the vm, we have to update.
                    watchdogParameters.setAction(getParameters().getWatchdog().getAction());
                    watchdogParameters.setModel(getParameters().getWatchdog().getModel());
                    runInternalAction(VdcActionType.UpdateWatchdog, watchdogParameters, cloneContextAndDetachFromParent());
                }
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
            getBackend().runInternalAction(VdcActionType.RemoveGraphicsDevice,
                    new GraphicsParameters(existingGraphicsDevice));
        }
    }

    private void addOrUpdateGraphicsDevice(GraphicsDevice device) {
        GraphicsDevice existingGraphicsDevice = getGraphicsDevOfType(device.getGraphicsType());
        device.setVmId(getVmId());
        getBackend().runInternalAction(
                existingGraphicsDevice == null ? VdcActionType.AddGraphicsDevice : VdcActionType.UpdateGraphicsDevice,
                new GraphicsParameters(device));
    }

    private GraphicsDevice getGraphicsDevOfType(GraphicsType type) {
        return getGraphicsDevices().stream().filter(dev -> dev.getGraphicsType() == type).findFirst().orElse(null);
    }

    private List<GraphicsDevice> getGraphicsDevices() {
        if (cachedGraphics == null) {
            cachedGraphics = getBackend()
                    .runInternalQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(getParameters().getVmId())).getReturnValue();
        }
        return cachedGraphics;
    }

    protected void updateVmPayload() {
        VmPayload payload = getParameters().getVmPayload();

        if (payload != null || getParameters().isClearPayload()) {
            List<VmDevice> disks = vmDeviceDao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.DISK);
            VmDevice oldPayload = null;
            for (VmDevice disk : disks) {
                if (VmPayload.isPayload(disk.getSpecParams())) {
                    oldPayload = disk;
                    break;
                }
            }

            if (oldPayload != null) {
                List<VmDeviceId> devs = new ArrayList<>();
                devs.add(oldPayload.getId());
                vmDeviceDao.removeAll(devs);
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
            List<Network> networks =
                    networkDao.getAllForCluster(getParameters().getVmStaticData().getClusterId());
            List<VmNic> interfaces = vmNicDao.getAllForVm(getParameters().getVmStaticData().getId());

            for (final VmNic iface : interfaces) {
                final Network network = NetworkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
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
        List<VmNumaNode> newList = getParameters().getVmStaticData().getvNumaNodeList();
        VmNumaNodeOperationParameters params =
                new VmNumaNodeOperationParameters(getParameters().getVm(), new ArrayList<>(newList));
            addLogMessages(getBackend().runInternalAction(VdcActionType.SetVmNumaNodes, params));

    }

    private void addLogMessages(VdcReturnValueBase returnValueBase) {
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
            boolean exists = isVmWithSameNameExists(vmFromParams.getName(), getStoragePoolId());

            if (exists) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            }
        }

        Version customCompatibilityVersionFromParams = vmFromParams.getStaticData().getCustomCompatibilityVersion();
        if (customCompatibilityVersionFromParams != null && !isCompatibilityVersionSupportedByCluster(customCompatibilityVersionFromParams)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CUSTOM_COMPATIBILITY_VERSION_NOT_SUPPORTED,
                    String.format("$Ccv %s", customCompatibilityVersionFromParams));
        }

        if (!validateCustomProperties(vmFromParams.getStaticData(), getReturnValue().getValidationMessages())) {
            return false;
        }

        if (!vmHandler.isOsTypeSupported(vmFromParams.getOs(),
                getCluster().getArchitecture(), getReturnValue().getValidationMessages())) {
            return false;
        }

        if (!vmHandler.isCpuSupported(
                vmFromParams.getVmOsId(),
                getEffectiveCompatibilityVersion(),
                getCluster().getCpuName(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        if (getParameters().getVmStaticData().getDefaultDisplayType() != DisplayType.none &&
                vmFromParams.getSingleQxlPci() &&
                !vmHandler.isSingleQxlDeviceLegal(vmFromParams.getDefaultDisplayType(),
                        vmFromParams.getOs(),
                        getReturnValue().getValidationMessages())) {
            return false;
        }

        if (vmFromParams.isAutoStartup() && vmFromDB.isHostedEngine()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_HOSTED_ENGINE);
        }

        if (!areUpdatedFieldsLegal()) {
            return failValidation(vmFromDB.isHostedEngine() ? EngineMessage.VM_CANNOT_UPDATE_HOSTED_ENGINE_FIELD : EngineMessage.VM_CANNOT_UPDATE_ILLEGAL_FIELD);
        }

        if (!vmFromDB.getClusterId().equals(vmFromParams.getClusterId())) {
            return failValidation(EngineMessage.VM_CANNOT_UPDATE_CLUSTER);
        }

        if (!isDedicatedVdsExistOnSameCluster(vmFromParams.getStaticData(), getReturnValue().getValidationMessages())) {
            return false;
        }

        // Check if number of monitors passed is legal
        if (getParameters().getVmStaticData().getDefaultDisplayType() != DisplayType.none &&
                !vmHandler.isNumOfMonitorsLegal(
                vmHandler.getResultingVmGraphics(
                        getVmDeviceUtils().getGraphicsTypesOfEntity(getVmId()),
                        getParameters().getGraphicsDevices()),
                getParameters().getVmStaticData().getNumOfMonitors(),
                getReturnValue().getValidationMessages())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_NUM_OF_MONITORS);
        }

        // Check PCI and IDE limits are ok
        if (!isValidPciAndIdeLimit(vmFromParams)) {
            return false;
        }

        if (!VmHandler.isVmPriorityValueLegal(vmFromParams.getPriority(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        if (!validate(VmValidator.validateCpuSockets(vmFromParams.getStaticData(),
                getEffectiveCompatibilityVersion()))) {
            return false;
        }

        // check for Vm Payload
        if (getParameters().getVmPayload() != null) {
            if (!checkPayload(getParameters().getVmPayload(), vmFromParams.getIsoPath())) {
                return false;
            }
            // we save the content in base64 string
            for (Map.Entry<String, String> entry : getParameters().getVmPayload().getFiles().entrySet()) {
                entry.setValue(new String(BASE_64.encode(entry.getValue().getBytes()), Charset.forName(CharEncoding.UTF_8)));
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
        if (!vmHandler.isGraphicsAndDisplaySupported(vmFromParams.getOs(),
                vmHandler.getResultingVmGraphics(getVmDeviceUtils().getGraphicsTypesOfEntity(getVmId()),
                        getParameters().getGraphicsDevices()),
                vmFromParams.getDefaultDisplayType(),
                getReturnValue().getValidationMessages(),
                getEffectiveCompatibilityVersion())) {
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

        if (!validatePinningAndMigration(getReturnValue().getValidationMessages(),
                getParameters().getVm().getStaticData(), getParameters().getVm().getCpuPinning())) {
            return false;
        }

        if (vmFromParams.isUseHostCpuFlags()
                && vmFromParams.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
            return failValidation(EngineMessage.VM_HOSTCPU_MUST_BE_PINNED_TO_HOST);
        }

        if (!isCpuSharesValid(vmFromParams)) {
            return failValidation(EngineMessage.QOS_CPU_SHARES_OUT_OF_RANGE);
        }

        if (!VmCpuCountHelper.validateCpuCounts(vmFromParams)) {
            return failValidation(EngineMessage.TOO_MANY_CPU_COMPONENTS);
        }

        if (vmFromParams.isUseHostCpuFlags() && (ArchitectureType.ppc == getCluster().getArchitecture().getFamily())) {
            return failValidation(EngineMessage.USE_HOST_CPU_REQUESTED_ON_UNSUPPORTED_ARCH);
        }

        if (!validateCPUHotplug(getParameters().getVmStaticData())) {
            return failValidation(EngineMessage.CPU_HOTPLUG_TOPOLOGY_INVALID);
        }

        if (!validateMemoryAlignment(getParameters().getVmStaticData())) {
            return false;
        }

        if (isVirtioScsiEnabled())  {
            // Verify OS compatibility
            if (!VmHandler.isOsTypeSupportedForVirtioScsi(vmFromParams.getOs(), getEffectiveCompatibilityVersion(),
                    getReturnValue().getValidationMessages())) {
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

        if (isBalloonEnabled() && !osRepository.isBalloonEnabled(getParameters().getVmStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
        }

        if (isSoundDeviceEnabled() && !osRepository.isSoundDeviceEnabled(getParameters().getVmStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
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

            if (provider.getType() != ProviderType.FOREMAN) {
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

        if (!validate(VmHandler.validateMaxMemorySize(
                getParameters().getVmStaticData(),
                getEffectiveCompatibilityVersion()))) {
            return false;
        }

        if (shouldAddLease(getParameters().getVmStaticData())) {
            if (!getParameters().getVmStaticData().isAutoStartup()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_LEASES_ARE_NOT_SUPPORTED_WITH_HA_OFF);
            }
            if (!FeatureSupported.isVmLeasesSupported(getEffectiveCompatibilityVersion())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_LEASES_ARE_NOT_SUPPORTED);
            }
            if (!validateLeaseStorageDomain(getParameters().getVmStaticData().getLeaseStorageDomainId())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean shouldAddLease(VmStatic newVm) {
        return super.shouldAddLease(newVm) && !newVm.getLeaseStorageDomainId().equals(getVm().getLeaseStorageDomainId());
    }

    protected boolean isDedicatedVdsExistOnSameCluster(VmBase vm,
            ArrayList<String> validationMessages) {
        return vmHandler.validateDedicatedVdsExistOnSameCluster(vm, validationMessages);
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
                isBalloonEnabled(),
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
     * check if we need to use running-configuration. Hosted Engine VM always returns false.
     * @return true if vm is running and we change field that has @EditableOnVmStatusField annotation
     *          or runningConfiguration already exist
     */
    private boolean isRunningConfigurationNeeded() {
        if (getVm().isHostedEngine()) {
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
                || isClusterLevelChange();
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
        return !Objects.equals(
                getParameters().getVm().getCustomEmulatedMachine(),
                getVm().getCustomEmulatedMachine());
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
            list.add(new QuotaSanityParameter(getQuotaId(), null));
            quotaSanityOnly = true;
        } else {
            if (!getQuotaId().equals(getVm().getQuotaId())) {
                list.add(new QuotaClusterConsumptionParameter(getVm().getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        getClusterId(),
                        getVm().getNumOfCpus(),
                        getVm().getMemSizeMb()));
                list.add(new QuotaClusterConsumptionParameter(getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        getParameters().getVmStaticData().getClusterId(),
                        getParameters().getVmStaticData().getNumOfCpus(),
                        getParameters().getVmStaticData().getMemSizeMb()));
            }
        }
        return list;
    }

    private Guid getQuotaId() {
        return getQuotaManager().getDefaultQuotaIfNull(
                getParameters().getVmStaticData().getQuotaId(),
                getStoragePoolId());
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
    public void setEntityId(AuditLogableBase logable) {
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

    protected boolean isBalloonEnabled() {
        Boolean balloonEnabled = getParameters().isBalloonEnabled();
        return balloonEnabled != null ? balloonEnabled : getVmDeviceUtils().hasMemoryBalloon(getVmId());
    }

    protected boolean isSoundDeviceEnabled() {
        Boolean soundDeviceEnabled = getParameters().isSoundDeviceEnabled();
        return soundDeviceEnabled != null ? soundDeviceEnabled :
                getVmDeviceUtils().hasSoundDevice(getVmId());
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

}

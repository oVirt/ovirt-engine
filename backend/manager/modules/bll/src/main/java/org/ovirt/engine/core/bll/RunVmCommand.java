package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.architecture.HasMaximumNumberOfDisks;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.RunVmParams.RunVmFlow;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.StartEntity;
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.RngUtils;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class RunVmCommand<T extends RunVmParams> extends RunVmCommandBase<T>
        implements QuotaVdsDependent {

    /** Note: this field should not be used directly, use {@link #isStatelessSnapshotExistsForVm()} instead */
    private Boolean cachedStatelessSnapshotExistsForVm;
    /** Indicates whether there is a possibility that the active snapshot's memory was already restored */
    private boolean memoryFromSnapshotUsed;

    private Guid cachedActiveIsoDomainId;
    private boolean needsHostDevices = false;
    private InitializationType initializationType;
    protected VmPayload vmPayload;

    public static final String ISO_PREFIX = "iso://";
    public static final String STATELESS_SNAPSHOT_DESCRIPTION = "stateless snapshot";

    private static final Logger log = LoggerFactory.getLogger(RunVmCommand.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private HostDeviceManager hostDeviceManager;

    @Inject
    private HostLocking hostLocking;
    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    @Inject
    private NetworkHelper networkHelper;

    protected RunVmCommand(Guid commandId) {
        super(commandId);
    }

    public RunVmCommand(T runVmParams, CommandContext commandContext) {
        super(runVmParams, commandContext);
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, runVmParams.getVmId()));
    }

    @Override
    protected void init() {
        if (getVm() == null) {
            return;
        }

        super.init();
        setStoragePoolId(getVm().getStoragePoolId());
        loadPayload();
        needsHostDevices = hostDeviceManager.checkVmNeedsDirectPassthrough(getVm());
        loadVmInit();
        fetchVmDisksFromDb();
        getVm().setBootSequence(getVm().getDefaultBootSequence());
        getVm().setRunOnce(false);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    protected List<Guid> getPredefinedVdsIdListToRunOn() {
        // This API needs to be preserved to allow RunOnce overrides,
        // but the Preferred host filtering needs to be done in a Policy Unit
        // only to allow the user to disable it.
        return Collections.emptyList();
    }

    protected String getMemoryFromActiveSnapshot() {
        // If the memory from the snapshot could have been restored already, the disks might be
        // non coherent with the memory, thus we don't want to try to restore the memory again
        if (memoryFromSnapshotUsed) {
            return StringUtils.EMPTY;
        }

        if (getFlow() == RunVmFlow.RESUME_HIBERNATE) {
             return getActiveSnapshot().getMemoryVolume();
        }

        if (!FeatureSupported.isMemorySnapshotSupportedByArchitecture(
                getVm().getClusterArch(),
                getVm().getCompatibilityVersion())) {
            return StringUtils.EMPTY;
        }

        return getActiveSnapshot().getMemoryVolume();
    }

    /**
     * Returns the full path file name of Iso file. If the Iso file has prefix of Iso://, then we set the prefix to the
     * path of the domain on the Iso Domain server<BR/>
     * otherwise, returns the original name.<BR/>
     * Note: The prefix is not case sensitive.
     *
     * @param url
     *            - String of the file url. ("iso://initrd.ini" or "/init/initrd.ini".
     * @return String of the full file path.
     */
    protected String getIsoPrefixFilePath(String url) {
        // The initial Url.
        String fullPathFileName = url;

        // If file name got prefix of iso:// then set the path to the Iso domain.
        int prefixLength = ISO_PREFIX.length();
        if (url.length() >= prefixLength && url.substring(0, prefixLength).equalsIgnoreCase(ISO_PREFIX)) {
            fullPathFileName = cdPathWindowsToLinux(url.substring(prefixLength));
        }
        return fullPathFileName;
    }

    protected String cdPathWindowsToLinux(String url) {
        return cdPathWindowsToLinux(url, getVm().getStoragePoolId(), getVdsId());
    }

    private void resumeVm() {
        setVdsId(getVm().getRunOnVds());
        if (getVds() != null) {
            try {
                VDSReturnValue result = getVdsBroker()
                        .runAsyncVdsCommand(
                                VDSCommandType.Resume,
                                new ResumeVDSCommandParameters(getVdsId(), getVm().getId()),
                                this);
                setActionReturnValue(result.getReturnValue());
                setSucceeded(result.getSucceeded());
                ExecutionHandler.setAsyncJob(getExecutionContext(), true);
            } finally {
                freeLock();
            }
        } else {
            setActionReturnValue(getVm().getStatus());
        }
    }

    protected void runVm() {
        setActionReturnValue(VMStatus.Down);
        if (getVdsToRunOn()) {
            VMStatus status = null;
            try {
                acquireHostDevicesLock();
                if (connectLunDisks(getVdsId()) && updateCinderDisksConnections()) {
                    if (!checkRequiredHostDevicesAvailability()) {
                        // if between validate and execute the host-devices were stolen by another VM
                        // (while the host-device lock wasn't being held) we need to bail here
                        throw new EngineException(EngineError.HOST_DEVICES_TAKEN_BY_OTHER_VM);
                    } else {
                        status = createVm();
                        ExecutionHandler.setAsyncJob(getExecutionContext(), true);
                        markHostDevicesAsUsed();
                    }
                }
            } catch(EngineException e) {
                // if the returned exception is such that shoudn't trigger the re-run process,
                // re-throw it. otherwise, continue (the vm will be down and a re-run will be triggered)
                switch (e.getErrorCode()) {
                case Done: // should never get here with errorCode = 'Done' though
                case exist:
                    cleanupPassthroughVnics();
                    reportCompleted();
                    throw e;
                case VDS_NETWORK_ERROR: // probably wrong xml format sent.
                case PROVIDER_FAILURE:
                case HOST_DEVICES_TAKEN_BY_OTHER_VM:
                    runningFailed();
                    throw e;
                default:
                    log.warn("Failed to run VM '{}': {}", getVmName(), e.getMessage());
                }

            } finally {
                releaseHostDevicesLock();
                freeLock();
            }
            setActionReturnValue(status);

            if (status != null && (status.isRunning() || status == VMStatus.RestoringState)) {
                setSucceeded(true);
            } else {
                // Try to rerun Vm on different vds no need to log the command because it is
                // being logged inside the rerun
                log.info("Trying to rerun VM '{}'", getVm().getName());
                setCommandShouldBeLogged(false);
                setSucceeded(true);
                rerun();
            }
        }

        else {
            runningFailed();
        }
    }

    private void cleanupPassthroughVnics() {
        cleanupPassthroughVnics(getVdsId());
    }

    /**
     * Checks if all required Host Devices are available.
     * Should be called with held host-device lock.
     * <br>
     * See {@link #acquireHostDevicesLock()} and {@link #releaseHostDevicesLock()}.
     **/
    private boolean checkRequiredHostDevicesAvailability() {
        if (!needsHostDevices) {
            return true;
        }
        // Only single dedicated host allowed for host devices, verified on validates
        return hostDeviceManager.checkVmHostDeviceAvailability(getVm(), getVm().getDedicatedVmForVdsList().get(0));
    }

    private void markHostDevicesAsUsed() {
        if (needsHostDevices) {
            hostDeviceManager.allocateVmHostDevices(getVm());
        }
    }

    private void acquireHostDevicesLock() {
        if (needsHostDevices) {
            // Only single dedicated host allowed for host devices, verified on validates
            hostLocking.acquireHostDevicesLock(getVm().getDedicatedVmForVdsList().get(0));
        }
    }

    private void releaseHostDevicesLock() {
        if (needsHostDevices) {
            // Only single dedicated host allowed for host devices, verified on validates
            hostLocking.releaseHostDevicesLock(getVm().getDedicatedVmForVdsList().get(0));
        }
    }

    @Override
    protected void executeVmCommand() {
        getVmManager().setPowerOffTimeout(System.nanoTime());
        setActionReturnValue(VMStatus.Down);
        initVm();
        perform();
    }

    @Override
    public void rerun() {
        cleanupPassthroughVnics();
        setFlow(null);
        super.rerun();
    }

    private RunVmFlow setFlow(RunVmFlow flow) {
        getParameters().setCachedFlow(flow);
        return flow;
    }

    /**
     * Determine the flow in which the command should be operating or
     * return the cached flow if it was already computed
     *
     * @return the flow in which the command is operating
     */
    protected RunVmFlow getFlow() {
        RunVmFlow cachedFlow = getParameters().getCachedFlow();
        if (cachedFlow != null) {
            return cachedFlow;
        }

        switch(getVm().getStatus()) {
        case Paused:
            return setFlow(RunVmFlow.RESUME_PAUSE);

        case Suspended:
            return setFlow(RunVmFlow.RESUME_HIBERNATE);

        default:
        }

        if (isRunAsStateless()) {
            fetchVmDisksFromDb();

            if (isStatelessSnapshotExistsForVm()) {
                log.error("VM '{}' ({}) already contains stateless snapshot, removing it",
                        getVm().getName(), getVm().getId());
                return setFlow(RunVmFlow.REMOVE_STATELESS_IMAGES);
            }

            return setFlow(RunVmFlow.CREATE_STATELESS_IMAGES);
        }

        if (!isInternalExecution()
                && isStatelessSnapshotExistsForVm()
                && !isVmPartOfManualPool()) {
            return setFlow(RunVmFlow.REMOVE_STATELESS_IMAGES);
        }

        return setFlow(RunVmFlow.RUN);
    }

    protected void perform() {
        switch(getFlow()) {
        case RESUME_PAUSE:
            resumeVm();
            break;

        case REMOVE_STATELESS_IMAGES:
            removeStatlessSnapshot();
            break;

        case CREATE_STATELESS_IMAGES:
            createStatelessSnapshot();
            break;

        case RESUME_HIBERNATE:
        case RUN:
        default:
            runVm();
        }
    }

    /**
     * @return true if a stateless snapshot exists for the VM, false otherwise
     */
    protected boolean isStatelessSnapshotExistsForVm() {
        if (cachedStatelessSnapshotExistsForVm == null) {
            cachedStatelessSnapshotExistsForVm = snapshotDao.exists(getVm().getId(), SnapshotType.STATELESS);
        }
        return cachedStatelessSnapshotExistsForVm;
    }

    /**
     * Returns the CD path in the following order (from high to low):
     * (1) The path given in the parameters
     * (2) The ISO path stored in the database if the boot sequence contains CD ROM
     * (3) Guest agent tools iso
     * (4) The ISO path stored in the database
     *
     * Note that in (2) we assume that the CD is bootable
     */
    private String chooseCd() {
        if (getParameters().getDiskPath() != null) {
            return getParameters().getDiskPath();
        }

        if (getVm().getBootSequence() != null && getVm().getBootSequence().containsSubsequence(BootSequence.D)) {
            return getVm().getIsoPath();
        }

        String guestToolPath = guestToolsVersionTreatment();
        if (guestToolPath != null) {
            return guestToolPath;
        }

        return getVm().getIsoPath();
    }

    protected IsoDomainListSynchronizer getIsoDomainListSynchronizer() {
        return isoDomainListSynchronizer;
    }

    private void createStatelessSnapshot() {
        warnIfNotAllDisksPermitSnapshots();

        log.info("Creating stateless snapshot for VM '{}' ({})",
                getVm().getName(), getVm().getId());
        CreateAllSnapshotsFromVmParameters createAllSnapshotsFromVmParameters = buildCreateSnapshotParameters();

        ActionReturnValue actionReturnValue = runInternalAction(ActionType.CreateAllSnapshotsFromVm,
                createAllSnapshotsFromVmParameters,
                createContextForStatelessSnapshotCreation());

        // setting lock to null in order not to release lock twice
        setLock(null);
        setSucceeded(actionReturnValue.getSucceeded());
        if (!actionReturnValue.getSucceeded()) {
            if (areDisksLocked(actionReturnValue)) {
                throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
            }
            getReturnValue().setFault(actionReturnValue.getFault());
            log.error("Failed to create stateless snapshot for VM '{}' ({})",
                    getVm().getName(), getVm().getId());
        }
    }

    private CommandContext createContextForStatelessSnapshotCreation() {
        Map<String, String> values = getVmValuesForMsgResolving();
        // Creating snapshots as sub step of run stateless
        Step createSnapshotsStep = addSubStep(StepEnum.EXECUTING,
                StepEnum.CREATING_SNAPSHOTS, values);

        // Add the step as the first step of the new context
        ExecutionContext createSnapshotsCtx = new ExecutionContext();
        createSnapshotsCtx.setMonitored(true);
        createSnapshotsCtx.setStep(createSnapshotsStep);
        getContext().withExecutionContext(createSnapshotsCtx);
        persistCommandIfNeeded();
        return getContext().clone().withoutCompensationContext();
    }

    private CreateAllSnapshotsFromVmParameters buildCreateSnapshotParameters() {
        CreateAllSnapshotsFromVmParameters parameters =
                new CreateAllSnapshotsFromVmParameters(getVm().getId(), STATELESS_SNAPSHOT_DESCRIPTION, false);
        parameters.setShouldBeLogged(false);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setSnapshotType(SnapshotType.STATELESS);
        return parameters;
    }

    private boolean areDisksLocked(ActionReturnValue actionReturnValue) {
        return actionReturnValue.getValidationMessages().contains(
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED.name());
    }


    private void warnIfNotAllDisksPermitSnapshots() {
        for (Disk disk : getVm().getDiskMap().values()) {
            if (!disk.isAllowSnapshot()) {
                auditLogDirector.log(this,
                        AuditLogType.USER_RUN_VM_AS_STATELESS_WITH_DISKS_NOT_ALLOWING_SNAPSHOT);
                break;
            }
        }
    }

    protected Map<String, String> getVmValuesForMsgResolving() {
        return Collections.singletonMap(VdcObjectType.VM.name().toLowerCase(), getVmName());
    }

    private void removeStatlessSnapshot() {
        runInternalAction(ActionType.ProcessDownVm,
                new ProcessDownVmParameters(getVm().getId(), true),
                ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
        // setting lock to null in order not to release lock twice
        setLock(null);
        setSucceeded(true);
    }

    protected VMStatus createVm() {
        updateCdPath();

        if (!StringUtils.isEmpty(getParameters().getFloppyPath())) {
            getVm().setFloppyPath(cdPathWindowsToLinux(getParameters().getFloppyPath()));
        }

        // Set path for initrd and kernel image.
        if (!StringUtils.isEmpty(getVm().getInitrdUrl())) {
            getVm().setInitrdUrl(getIsoPrefixFilePath(getVm().getInitrdUrl()));
        }

        if (!StringUtils.isEmpty(getVm().getKernelUrl())) {
            getVm().setKernelUrl(getIsoPrefixFilePath(getVm().getKernelUrl()));
        }

        initParametersForExternalNetworks();

        VMStatus vmStatus = (VMStatus) getVdsBroker()
                .runAsyncVdsCommand(VDSCommandType.Create, buildCreateVmParameters(), this).getReturnValue();

        // Don't use the memory from the active snapshot anymore if there's a chance that disks were changed
        memoryFromSnapshotUsed = vmStatus.isRunning() || vmStatus == VMStatus.RestoringState;

        // After VM was create (or not), we can remove the quota vds group memory.
        return vmStatus;
    }

    protected void updateCurrentCd(String cdPath) {
        cdPath = StringUtils.isEmpty(cdPath) ? null : cdPath;
        vmHandler.updateCurrentCd(getVm(), cdPath);
    }


    /**
     * Initialize the parameters for the VDSM command of VM creation
     *
     * @return the VDS create VM parameters
     */
    protected CreateVDSCommandParameters buildCreateVmParameters() {
        CreateVDSCommandParameters parameters  = new CreateVDSCommandParameters(getVdsId(), getVm());
        parameters.setRunInUnknownStatus(getParameters().isRunInUnknownStatus());
        parameters.setVmPayload(vmPayload);
        String memoryFromActiveSnapshot = getMemoryFromActiveSnapshot();
        if (StringUtils.isNotEmpty(memoryFromActiveSnapshot)) {
            parameters.setHibernationVolHandle(memoryFromActiveSnapshot);
            parameters.setDownSince(getVm().getStatus() == VMStatus.Suspended ?
                    getVm().getLastStopTime()
                    : getActiveSnapshot().getCreationDate());
        }
        parameters.setPassthroughVnicToVfMap(flushPassthroughVnicToVfMap());
        if (initializationType == InitializationType.Sysprep
                && osRepository.isWindows(getVm().getVmOsId())
                && (getVm().getFloppyPath() == null || "".equals(getVm().getFloppyPath()))) {
            parameters.setInitializationType(InitializationType.Sysprep);
        }
        if (initializationType == InitializationType.CloudInit && !osRepository.isWindows(getVm().getVmOsId())) {
            parameters.setInitializationType(InitializationType.CloudInit);
        }
        return parameters;
    }

    protected void initParametersForExternalNetworks() {
        if (getVm().getInterfaces().isEmpty()) {
            return;
        }

        Map<VmDeviceId, VmDevice> nicDevices =
                Entities.businessEntitiesById(vmDeviceDao.getVmDeviceByVmIdAndType(getVmId(),
                        VmDeviceGeneralType.INTERFACE));

        for (VmNic iface : getVm().getInterfaces()) {
            VnicProfile vnicProfile = vnicProfileDao.get(iface.getVnicProfileId());
            Network network = networkHelper.getNetworkByVnicProfile(vnicProfile);
            VmDevice vmDevice = nicDevices.get(new VmDeviceId(iface.getId(), getVmId()));
            if (network != null && network.isExternal() && vmDevice.isPlugged()) {
                Provider<?> provider = providerDao.get(network.getProvidedBy().getProviderId());
                NetworkProviderProxy providerProxy = providerProxyFactory.create(provider);
                Map<String, String> deviceProperties = providerProxy.allocate(network, vnicProfile, iface, getVds());

                getVm().getRuntimeDeviceCustomProperties().put(vmDevice.getId(), deviceProperties);
            }
        }
    }

    protected Map<Guid, String> flushPassthroughVnicToVfMap() {
        Map<Guid, String> passthroughVnicToVf = getVnicToVfMap(getVdsId());
        vfScheduler.cleanVmData(getVmId());
        return passthroughVnicToVf;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (getFlow() == RunVmFlow.REMOVE_STATELESS_IMAGES) {
                return AuditLogType.USER_RUN_VM_FAILURE_STATELESS_SNAPSHOT_LEFT;
            }
            if (getFlow() == RunVmFlow.RESUME_PAUSE) {
                return getSucceeded() ? AuditLogType.USER_RESUME_VM : AuditLogType.USER_FAILED_RESUME_VM;
            } else if (isInternalExecution()) {
                if (getSucceeded()) {
                    boolean isStateless = isStatelessSnapshotExistsForVm();
                    if (isStateless) {
                        return AuditLogType.VDS_INITIATED_RUN_VM_AS_STATELESS;
                    } else if (getFlow() == RunVmFlow.CREATE_STATELESS_IMAGES) {
                        return AuditLogType.VDS_INITIATED_RUN_AS_STATELESS_VM_NOT_YET_RUNNING;
                    } else {
                        return AuditLogType.VDS_INITIATED_RUN_VM;
                    }
                }

                return AuditLogType.VDS_INITIATED_RUN_VM_FAILED;
            } else {
                return getSucceeded() ?
                        getActionReturnValue() == VMStatus.Up ?
                               isVmRunningOnNonDefaultVds() ?
                                       AuditLogType.USER_RUN_VM_ON_NON_DEFAULT_VDS
                                       : isStatelessSnapshotExistsForVm() ?
                                               AuditLogType.USER_RUN_VM_AS_STATELESS
                                               : AuditLogType.USER_RUN_VM
                                : _isRerun ?
                                        AuditLogType.VDS_INITIATED_RUN_VM
                                        : getVm().isRunAndPause() ?
                                                AuditLogType.USER_INITIATED_RUN_VM_AND_PAUSE
                                                : getFlow() == RunVmFlow.CREATE_STATELESS_IMAGES ?
                                                        AuditLogType.USER_INITIATED_RUN_VM
                                                        : AuditLogType.USER_STARTED_VM
                        : _isRerun ? AuditLogType.USER_INITIATED_RUN_VM_FAILED : AuditLogType.USER_FAILED_RUN_VM;
            }

        case END_SUCCESS:
            // if not running as stateless, or if succeeded running as
            // stateless,
            // command should be with 'CommandShouldBeLogged = false':
            return isStatelessSnapshotExistsForVm() && !getSucceeded() ?
                    AuditLogType.USER_RUN_VM_AS_STATELESS_FINISHED_FAILURE : AuditLogType.UNASSIGNED;

        case END_FAILURE:
            // if not running as stateless, command should
            // be with 'CommandShouldBeLogged = false':
            return isStatelessSnapshotExistsForVm() ?
                    AuditLogType.USER_RUN_VM_AS_STATELESS_FINISHED_FAILURE : AuditLogType.UNASSIGNED;

        default:
            // all other cases should be with 'CommandShouldBeLogged =
            // false':
            return AuditLogType.UNASSIGNED;
        }
    }

    protected boolean isVmRunningOnNonDefaultVds() {
        return !getVm().getDedicatedVmForVdsList().isEmpty()
                && !getVm().getDedicatedVmForVdsList().contains(getVm().getRunOnVds());
    }

    /**
     * @return true if we need to create the VM object, false otherwise
     */
    private boolean isInitVmRequired() {
        return EnumSet.of(RunVmFlow.RUN, RunVmFlow.RESUME_HIBERNATE).contains(getFlow());
    }

    protected void initVm() {
        if (!isInitVmRequired()) {
            return;
        }

        fetchVmDisksFromDb();
        updateVmDevicesOnRun();
        updateGraphicsAndDisplayInfos();

        getVm().setRunAndPause(getParameters().getRunAndPause() == null ? getVm().isRunAndPause() : getParameters().getRunAndPause());

        // Clear the first user:
        getVm().setConsoleUserId(null);

        updateVmInit();

        // if we asked for floppy from Iso Domain we cannot
        // have floppy payload since we are limited to only one floppy device
        if (!StringUtils.isEmpty(getParameters().getFloppyPath()) && isPayloadExists(VmDeviceType.FLOPPY)) {
            vmPayload = null;
        }

        updateVmGuestAgentVersion();

        // update dynamic cluster-parameters
        if (getVm().getCpuName() == null) { // no run-once data -> use static field or inherit from cluster
            if (getVm().getCustomCpuName() != null) {
                getVm().setCpuName(getVm().getCustomCpuName());
            } else {
                // get what cpu flags should be passed to vdsm according to the cluster
                getVm().setCpuName(getCpuFlagsManagerHandler().getCpuId(
                        getVm().getClusterCpuName(),
                        getVm().getCompatibilityVersion()));
            }
        }
        if (getVm().getEmulatedMachine() == null) {
            getVm().setEmulatedMachine(getEffectiveEmulatedMachine());
        }
    }

    protected String getEffectiveEmulatedMachine() {
        if (getVm().getCustomEmulatedMachine() != null) {
            return getVm().getCustomEmulatedMachine();
        }

        // The 'default' to be set
        String recentClusterDefault = getCluster().getEmulatedMachine();
        if (getVm().getCustomCompatibilityVersion() == null) {
            return recentClusterDefault;
        }

        String bestMatch = findBestMatchForEmulatedMachine(
                recentClusterDefault,
                Config.getValue(
                        ConfigValues.ClusterEmulatedMachines,
                        getVm().getCustomCompatibilityVersion().getValue()));
        log.info("Emulated machine '{}' selected since Custom Compatibility Version is set for '{}'", bestMatch, getVm());
        return bestMatch;
    }

    protected String findBestMatchForEmulatedMachine(
            String currentEmulatedMachine,
            List<String> candidateEmulatedMachines) {
        if (candidateEmulatedMachines.contains(currentEmulatedMachine)) {
            return currentEmulatedMachine;
        }
        return candidateEmulatedMachines
                .stream()
                .max(Comparator.comparingInt(s -> StringUtils.indexOfDifference(currentEmulatedMachine, s)))
                .orElse(currentEmulatedMachine);
    }

    /**
     * This methods sets graphics infos of a VM to correspond to graphics devices set in DB
     * No Display Type update is needed here.
     */
    protected void updateGraphicsAndDisplayInfos() {
        for (VmDevice vmDevice : vmDeviceDao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.GRAPHICS)) {
            getVm().getGraphicsInfos().put(GraphicsType.fromString(vmDevice.getDevice()), new GraphicsInfo());
        }
    }

    protected boolean isPayloadExists(VmDeviceType deviceType) {
        return vmPayload != null && vmPayload.getDeviceType().equals(deviceType);
    }

    protected void loadPayload() {
        List<VmDevice> disks = vmDeviceDao.getVmDeviceByVmIdAndType(getVm().getId(), VmDeviceGeneralType.DISK);
        for (VmDevice disk : disks) {
            if (disk.isManaged() && VmPayload.isPayload(disk.getSpecParams())) {
                vmPayload = new VmPayload(disk);
                break;
            }
        }
    }

    protected void fetchVmDisksFromDb() {
        if (getVm().getDiskMap().isEmpty()) {
            vmHandler.updateDisksFromDb(getVm());
        }
    }

    protected void updateVmDevicesOnRun() {
        // Before running the VM we update its devices, as they may
        // need to be changed due to configuration option change
        getVmDeviceUtils().updateVmDevicesOnRun(getVm());
    }

    protected void updateCdPath() {
        final String cdPath = chooseCd();
        if (StringUtils.isNotEmpty(cdPath)) {
            log.info("Running VM with attached cd '{}'", cdPath);
        }
        updateCurrentCd(cdPath);
        getVm().setCdPath(cdPathWindowsToLinux(cdPath));
    }

    protected void updateVmGuestAgentVersion() {
        vmHandler.updateVmGuestAgentVersion(getVm());
    }

    protected void updateVmInit() {
        if (getParameters().getInitializationType() == null) {
            vmHandler.updateVmInitFromDB(getVm().getStaticData(), false);
            if (!getVm().isInitialized() && getVm().getVmInit() != null) {
                if (osRepository.isWindows(getVm().getVmOsId())) {
                    if (!isPayloadExists(VmDeviceType.FLOPPY)) {
                        initializationType = InitializationType.Sysprep;
                    }
                }
                else {
                    if (!isPayloadExists(VmDeviceType.CDROM)) {
                        initializationType = InitializationType.CloudInit;
                    }
                }
            }
        } else if (getParameters().getInitializationType() != InitializationType.None) {
            initializationType = getParameters().getInitializationType();
            // If the user asked for sysprep/cloud-init via run-once we eliminate
            // the payload since we can only have one media (Floppy/CDROM) per payload.
            if (getParameters().getInitializationType() == InitializationType.Sysprep &&
                    isPayloadExists(VmDeviceType.FLOPPY)) {
                vmPayload = null;
            } else if (getParameters().getInitializationType() == InitializationType.CloudInit &&
                    isPayloadExists(VmDeviceType.CDROM)) {
                vmPayload = null;
            }
        }
    }

    protected boolean getVdsToRunOn() {
        Optional<Guid> vdsToRunOn =
                schedulingManager.schedule(getCluster(),
                        getVm(),
                        getRunVdssList(),
                        getVdsWhiteList(),
                        getPredefinedVdsIdListToRunOn(),
                        new ArrayList<>(),
                        this,
                        getCorrelationId());
        setVdsId(vdsToRunOn.orElse(null));
        if (vdsToRunOn.isPresent()) {
            getRunVdssList().add(vdsToRunOn.get());
        }

        setVds(null);
        setVdsName(null);
        if (getVdsId().equals(Guid.Empty)) {
            log.error("Can't find VDS to run the VM '{}' on, so this VM will not be run.", getVmId());
            return false;
        }

        if (getVds() == null) {
            EngineException outEx = new EngineException(EngineError.RESOURCE_MANAGER_VDS_NOT_FOUND);
            log.error("VmHandler::{}: {}", getClass().getName(), outEx.getMessage());
            return false;
        }
        return true;
    }

    /**
     * If vds version greater then vm's and vm not running with cd and there is appropriate RhevAgentTools image -
     * add it to vm as cd.
     */
    private String guestToolsVersionTreatment() {
        boolean attachCd = false;
        String selectedToolsVersion = "";
        String selectedToolsClusterVersion = "";
        Guid isoDomainId = getActiveIsoDomainId();
        if (osRepository.isWindows(getVm().getVmOsId()) && null != isoDomainId) {

            // get cluster version of the vm tools
            Version vmToolsClusterVersion = null;
            if (getVm().getHasAgent()) {
                Version clusterVer = getVm().getPartialVersion();
                if (new Version("4.4").equals(clusterVer)) {
                    vmToolsClusterVersion = new Version("2.1");
                } else {
                    vmToolsClusterVersion = clusterVer;
                }
            }

            // Fetch cached Iso files from active Iso domain.
            List<RepoImage> repoFilesMap =
                    getIsoDomainListSynchronizer().getCachedIsoListByDomainId(isoDomainId, ImageFileType.ISO);
            Version bestClusterVer = null;
            int bestToolVer = 0;
            for (RepoImage map : repoFilesMap) {
                String fileName = StringUtils.defaultString(map.getRepoImageId(), "");
                Matcher matchToolPattern =
                        Pattern.compile(IsoDomainListSynchronizer.REGEX_TOOL_PATTERN).matcher(fileName);
                if (matchToolPattern.find()) {
                    // Get cluster version and tool version of Iso tool.
                    Version clusterVer = new Version(matchToolPattern.group(IsoDomainListSynchronizer.TOOL_CLUSTER_LEVEL));
                    int toolVersion = Integer.parseInt(matchToolPattern.group(IsoDomainListSynchronizer.TOOL_VERSION));

                    if (clusterVer.compareTo(getVm().getCompatibilityVersion()) <= 0) {
                        if ((bestClusterVer == null)
                                || (clusterVer.compareTo(bestClusterVer) > 0)) {
                            bestToolVer = toolVersion;
                            bestClusterVer = clusterVer;
                        } else if (clusterVer.equals(bestClusterVer) && toolVersion > bestToolVer) {
                            bestToolVer = toolVersion;
                            bestClusterVer = clusterVer;
                        }
                    }
                }
            }

            if (bestClusterVer != null
                    && (vmToolsClusterVersion == null
                            || vmToolsClusterVersion.compareTo(bestClusterVer) < 0 || (vmToolsClusterVersion.equals(bestClusterVer)
                            && getVm().getHasAgent() &&
                    getVm().getGuestAgentVersion().getBuild() < bestToolVer))) {
                // Vm has no tools or there are new tools
                selectedToolsVersion = Integer.toString(bestToolVer);
                selectedToolsClusterVersion = bestClusterVer.toString();
                attachCd = true;
            }
        }

        if (attachCd) {
            String rhevToolsPath =
                    String.format("%1$s%2$s_%3$s.iso", IsoDomainListSynchronizer.getGuestToolsSetupIsoPrefix(),
                            selectedToolsClusterVersion, selectedToolsVersion);

            String isoDir = (String) runVdsCommand(VDSCommandType.IsoDirectory,
                    new IrsBaseVDSCommandParameters(getVm().getStoragePoolId())).getReturnValue();
            rhevToolsPath = isoDir + File.separator + rhevToolsPath;

            return rhevToolsPath;
        }
        return null;
    }

    @Override
    protected boolean validate() {
        VM vm = getVm();

        if (vm == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!validateObject(vm.getStaticData())) {
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().getCustomCompatibilityVersion() != null &&
                vm.getCustomCompatibilityVersion().less(getStoragePool().getCompatibilityVersion())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_COMATIBILITY_VERSION_NOT_SUPPORTED,
                    String.format("$VmName %1$s", getVm().getName()),
                    String.format("$VmVersion %1$s", getVm().getCustomCompatibilityVersion().toString()),
                    String.format("$DcVersion %1$s", getStoragePool().getCompatibilityVersion()));
        }

        RunVmValidator runVmValidator = getRunVmValidator();

        if (!runVmValidator.canRunVm(
                getReturnValue().getValidationMessages(),
                getStoragePool(),
                getRunVdssList(),
                getVdsWhiteList(),
                getCluster(),
                getParameters().isRunInUnknownStatus())) {
            return false;
        }

        if (!validate(runVmValidator.validateNetworkInterfaces())) {
            return false;
        }

        if (!validate(runVmValidator.validateVmLease())) {
            return false;
        }

        if (!checkRngDeviceClusterCompatibility()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_RNG_SOURCE_NOT_SUPPORTED);
        }

        if (!validate(checkDisksInBackupStorage())) {
            return false;
        }

        boolean isWindowsOs = osRepository.isWindows(getVm().getVmOsId());

        boolean isCloudInitEnabled = (!getVm().isInitialized() && getVm().getVmInit() != null && !isWindowsOs) ||
                (getParameters().getInitializationType() == InitializationType.CloudInit);

        if (isCloudInitEnabled && hasMaximumNumberOfDisks()) {
            return failValidation(EngineMessage.VMPAYLOAD_CDROM_OR_CLOUD_INIT_MAXIMUM_DEVICES);
        }

        if (!validate(vmHandler.isCpuSupported(
                getVm().getVmOsId(),
                getVm().getCompatibilityVersion(),
                getCluster().getCpuName()))) {
            return false;
        }

        try {
            acquireHostDevicesLock();
            if (!checkRequiredHostDevicesAvailability()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOST_DEVICE_NOT_AVAILABLE);
            }
        } finally {
            releaseHostDevicesLock();
        }

        if (!validate(runVmValidator.validateUsbDevices(getVm().getStaticData()))) {
            return false;
        }

        return true;
    }

    protected void loadVmInit() {
        if (getVm().getVmInit() == null) {
            vmHandler.updateVmInitFromDB(getVm().getStaticData(), false);
        }
    }

    protected boolean hasMaximumNumberOfDisks() {
        return ArchStrategyFactory.getStrategy(getVm().getClusterArch()).run(new HasMaximumNumberOfDisks(getVm().getId())).returnValue();
    }

    /**
     * Checks whether rng device of vm is required by cluster, which is requirement for running vm.
     *
     * @return true if the source of vm rng device is required by cluster (and therefore supported by hosts in cluster)
     */
    boolean checkRngDeviceClusterCompatibility() {
        List<VmDevice> rngDevs =
                vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(getVmId(), VmDeviceGeneralType.RNG, VmDeviceType.VIRTIO);

        if (rngDevs.isEmpty()) {
            return true;
        }

        VmRngDevice rngDevice = new VmRngDevice(rngDevs.get(0));
        final RngUtils.RngValidationResult rngValidationResult =
                RngUtils.validate(getCluster(), rngDevice);
        switch (rngValidationResult) {
            case VALID:
                return true;
            case UNSUPPORTED_URANDOM_OR_RANDOM:
                log.warn("Running VM {}({}) with rng source {} that is not supported in cluster {}.",
                        getVm().getName(),
                        getVm().getId(),
                        rngDevice.getSource(),
                        getCluster().getName());
                return true;
            case INVALID:
                return false;
            default:
                throw new RuntimeException("Unknown enum constant " + rngValidationResult);
        }

    }

    protected ValidationResult checkDisksInBackupStorage() {
        return new MultipleStorageDomainsValidator(getVm().getStoragePoolId(),
                Stream.concat(getVm().getDiskMap()
                        .values()
                        .stream()
                        .filter(DisksFilter.ONLY_PLUGGED)
                        .filter(DisksFilter.ONLY_IMAGES)
                        .map(DiskImage.class::cast)
                        .flatMap(vmDisk -> vmDisk.getStorageIds().stream()),
                        Stream.of(getVm().getLeaseStorageDomainId()).filter(Objects::nonNull))
                .collect(Collectors.toSet()))
                .allDomainsNotBackupDomains();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(StartEntity.class);
        return super.getValidationGroups();
    }

    protected RunVmValidator getRunVmValidator() {
        return Injector.injectMembers(new RunVmValidator(getVm(), getParameters(), isInternalExecution(), getActiveIsoDomainId()));
    }

    protected Guid getActiveIsoDomainId() {
        if (cachedActiveIsoDomainId == null) {
            cachedActiveIsoDomainId = getIsoDomainListSynchronizer()
                    .findActiveISODomain(getVm().getStoragePoolId());
        }

        return cachedActiveIsoDomainId;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__RUN);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    private boolean shouldEndSnapshotCreation() {
        return getFlow() == RunVmFlow.CREATE_STATELESS_IMAGES && isStatelessSnapshotExistsForVm();
    }

    @Override
    protected void endSuccessfully() {
        if (shouldEndSnapshotCreation()) {
            getBackend().endAction(ActionType.CreateAllSnapshotsFromVm,
                    getParameters().getImagesParameters().get(0),
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());

            getParameters().setShouldBeLogged(false);
            getParameters().setRunAsStateless(false);
            getParameters().setCachedFlow(null);

            setSucceeded(getBackend().runInternalAction(
                    getActionType(), getParameters(), createContextForRunStatelessVm()).getSucceeded());
            if (!getSucceeded()) {
                getParameters().setShouldBeLogged(true);
                log.error("Could not run VM '{}' ({}) in stateless mode",
                        getVm().getName(), getVm().getId());
                // could not run the vm don't try to run the end action again
                getReturnValue().setEndActionTryAgain(false);
            }
        }

        // Hibernation (VMStatus.Suspended) treatment:
        else {
            super.endSuccessfully();
        }
    }

    private CommandContext createContextForRunStatelessVm() {
        Step step = getExecutionContext().getStep();

        if (step == null) {
            return cloneContextAndDetachFromParent();
        }

        // Retrieve the job object and its steps as this the endSuccessfully stage of command execution -
        // at this is a new instance of the command is used
        // (comparing with the execution state) so all information on the job and steps should be retrieved.
        Job job = jobRepository.getJobWithSteps(step.getJobId());
        Step executingStep = job.getDirectStep(StepEnum.EXECUTING);
        // We would like to to set the run stateless step as substep of executing step
        setInternalExecution(true);

        ExecutionContext runStatelessVmCtx = new ExecutionContext();
        // The internal command should be monitored for tasks
        runStatelessVmCtx.setMonitored(true);
        Step runStatelessStep =
                executionHandler.addSubStep(getExecutionContext(),
                        executingStep,
                        StepEnum.RUN_STATELESS_VM,
                        ExecutionMessageDirector.resolveStepMessage(StepEnum.RUN_STATELESS_VM,
                                getVmValuesForMsgResolving()));
        // This is needed in order to end the job upon execution of the steps of the child command
        runStatelessVmCtx.setShouldEndJob(true);
        runStatelessVmCtx.setJob(job);
        // Since run stateless step involves invocation of command, we should set the run stateless vm step as
        // the "beginning step" of the child command.
        runStatelessVmCtx.setStep(runStatelessStep);
        return cloneContextAndDetachFromParent().withExecutionContext(runStatelessVmCtx);
    }

    @Override
    protected void endWithFailure() {
        if (shouldEndSnapshotCreation()) {
            ActionReturnValue actionReturnValue = getBackend().endAction(ActionType.CreateAllSnapshotsFromVm,
                    getParameters().getImagesParameters().get(0), cloneContext().withoutExecutionContext()
                            .withoutLock());

            setSucceeded(actionReturnValue.getSucceeded());
            // we are not running the VM, of course,
            // since we couldn't create a snapshot.
        }

        else {
            super.endWithFailure();
        }
    }

    @Override
    public void runningSucceded() {
        removeMemoryFromActiveSnapshot();
        setFlow(RunVmFlow.RUNNING_SUCCEEDED);
        vmStaticDao.incrementDbGeneration(getVm().getId());
        super.runningSucceded();
    }

    @Override
    protected void runningFailed() {
        cleanupPassthroughVnics();
        if (memoryFromSnapshotUsed) {
            removeMemoryFromActiveSnapshot();
        }
        super.runningFailed();
    }

    private void removeMemoryFromActiveSnapshot() {
        String memory = getActiveSnapshot().getMemoryVolume();
        if (StringUtils.isEmpty(memory)) {
            return;
        }

        snapshotDao.removeMemoryFromActiveSnapshot(getVmId());
        // If the memory volumes are not used by any other snapshot, we can remove them
        if (snapshotDao.getNumOfSnapshotsByMemory(memory) == 0) {
            removeMemoryDisks(memory);
        }
    }

    /**
     * @return true if the VM should run as stateless
     */
    protected boolean isRunAsStateless() {
        return getParameters().getRunAsStateless() != null ?
                getParameters().getRunAsStateless()
                : getVm().getVmPoolId() == null && getVm().isStateless();
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        list.add(new QuotaClusterConsumptionParameter(getVm().getQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getVm().getClusterId(),
                getVm().getCpuPerSocket() * getVm().getNumOfSockets(),
                getVm().getMemSizeMb()));
        return list;
    }

    protected boolean isVmPartOfManualPool() {
        if (getVm().getVmPoolId() == null) {
            return false;
        }

        final VmPool vmPool = vmPoolDao.get(getVm().getVmPoolId());
        return vmPool.getVmPoolType().equals(VmPoolType.MANUAL);
    }

    @Override
    protected void endExecutionMonitoring() {
        if (getVm().isRunAndPause() && vmDynamicDao.get(getVmId()).getStatus() == VMStatus.Paused) {
            final ExecutionContext executionContext = getExecutionContext();
            executionContext.setShouldEndJob(true);
            executionHandler.endJob(executionContext, true);
        } else {
            super.endExecutionMonitoring();
        }
    }

    /**
     * Initial white list for scheduler (empty == all hosts)
     */
    protected List<Guid> getVdsWhiteList() {
        return Collections.emptyList();
    }

    /**
     * Since this callback is called by the VdsUpdateRunTimeInfo thread, we don't want it
     * to fetch the VM using {@link #getVm()}, as the thread that invokes {@link #rerun()},
     * which runs in parallel, is doing setVm(null) to refresh the VM, and because of this
     * race we might end up with null VM. so we fetch the static part of the VM from the DB.
     */
    @Override
    public void onPowerringUp() {
        decreasePendingVm(vmStaticDao.get(getVmId()));
    }

    @Override
    public void actualDowntimeReported(int actualDowntime) {
        // nothing to do
    }

    @Override
    public CommandCallback getCallback() {
        return getFlow().isStateless() ? callbackProvider.get() : null;
    }
}

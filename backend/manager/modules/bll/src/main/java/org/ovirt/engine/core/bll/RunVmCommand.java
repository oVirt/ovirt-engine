package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import org.ovirt.engine.core.bll.kubevirt.KubevirtMonitoring;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.EmulatedMachineUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.RunVmParams.RunVmFlow;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
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
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmStaticDao;
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
    private KubevirtMonitoring kubevirt;
    @Inject
    private HostLocking hostLocking;
    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;
    @Inject
    private VmPoolMonitor vmPoolMonitor;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private DiskDao diskDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;
    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;

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

    protected boolean shouldRestoreMemory() {
        // If the memory from the snapshot could have been restored already, the disks might be
        // non coherent with the memory, thus we don't want to try to restore the memory again
        return !memoryFromSnapshotUsed &&
                (getFlow() == RunVmFlow.RESUME_HIBERNATE ||
                FeatureSupported.isMemorySnapshotSupportedByArchitecture(
                getVm().getClusterArch(),
                getVm().getCompatibilityVersion())) &&
                getActiveSnapshot().containsMemory();
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
                VDSReturnValue result = vdsBroker
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

    private void addMissingLeaseInfoToVmIfNeeded() {
        // This API checks if the lease information is missing when a lease Storage
        // Domain has been specified and resets the lease information during the launching
        // of the VM. This may occur during upgrading from Version 4.1 to higher versions
        // due to a change that moved the lease information data from the VM Static to VM
        // Dynamic DB Tables.
        if (getVm().getLeaseStorageDomainId() != null && getVm().getLeaseInfo() == null) {
            ActionReturnValue retVal = runInternalAction(ActionType.GetVmLeaseInfo,
                    new VmLeaseParameters(getVm().getStoragePoolId(),
                            getVm().getLeaseStorageDomainId(),
                            getParameters().getVmId()));
            if (retVal == null || !retVal.getSucceeded()) {
                throw new EngineException(EngineError.INVALID_HA_VM_LEASE);
            }
            getVm().setLeaseInfo(retVal.getActionReturnValue());
            vmDynamicDao.updateVmLeaseInfo(getParameters().getVmId(), getVm().getLeaseInfo());
        }
        return;
    }

    protected void runVm() {
        setActionReturnValue(VMStatus.Down);
        if (getVdsToRunOn()) {
            warnIfVmNotFitInNumaNode();
            VMStatus status = null;
            try {
                acquireHostDevicesLock();
                addMissingLeaseInfoToVmIfNeeded();
                if (connectLunDisks(getVdsId()) && updateCinderDisksConnections() &&
                        managedBlockStorageCommandUtil.attachManagedBlockStorageDisks(getVm(),
                                vmHandler, getVds())) {
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
                case INVALID_HA_VM_LEASE:
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
        } else {
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
        switch (getVm().getOrigin()) {
        case KUBEVIRT:
            runKubevirtVm();
            break;
        default:
            getVmManager().setPowerOffTimeout(System.nanoTime());
            setActionReturnValue(VMStatus.Down);
            initVm();
            perform();
        }
    }

    private void runKubevirtVm() {
        kubevirt.start(getVm());
        getVm().setStatus(VMStatus.WaitForLaunch);
        getVmManager().update(getVm().getDynamicData());
        setSucceeded(true);
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

        if (FeatureSupported.isWindowsGuestToolsSupported(getVm().getCompatibilityVersion())) {
            // Auto-attaching WGT is removed since 4.4.
            String guestToolPath = guestToolsVersionTreatment(isoDomainListSynchronizer.getRegexToolPattern());

            if (guestToolPath != null) {
                return guestToolPath;
            }
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
        CreateSnapshotForVmParameters createAllSnapshotsFromVmParameters = buildCreateSnapshotParameters();

        ActionReturnValue actionReturnValue = runInternalAction(ActionType.CreateSnapshotForVm,
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

    private CreateSnapshotForVmParameters buildCreateSnapshotParameters() {
        CreateSnapshotForVmParameters parameters =
                new CreateSnapshotForVmParameters(getVm().getId(), STATELESS_SNAPSHOT_DESCRIPTION, false);
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
        // set the path for windows guest tools secondary cd-rom
        if (getParameters().isAttachWgt()) {
            getVm().setWgtCdPath(cdPathWindowsToLinux(guestToolsVersionTreatment(vmHandler.getRegexVirtIoIsoPattern())));
        }

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

        initParametersForExternalNetworks(getVds(), false);

        VMStatus vmStatus = (VMStatus) vdsBroker
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
        if (shouldRestoreMemory()) {
            DiskImage memoryDump = (DiskImage) diskDao.get(getActiveSnapshot().getMemoryDiskId());
            DiskImage memoryConf = (DiskImage) diskDao.get(getActiveSnapshot().getMetadataDiskId());
            parameters.setMemoryDumpImage(memoryDump);
            parameters.setMemoryConfImage(memoryConf);

            parameters.setDownSince(getVm().getStatus() == VMStatus.Suspended ?
                    getVm().getLastStopTime()
                    : getActiveSnapshot().getCreationDate());
        }
        parameters.setPassthroughVnicToVfMap(flushPassthroughVnicToVfMap());
        if (initializationType == InitializationType.Sysprep
                && osRepository.isSysprep(getVm().getVmOsId())
                && (getVm().getFloppyPath() == null || "".equals(getVm().getFloppyPath()))) {
            parameters.setInitializationType(InitializationType.Sysprep);
        }
        if (initializationType == InitializationType.CloudInit && osRepository.isCloudInit(getVm().getVmOsId())) {
            parameters.setInitializationType(InitializationType.CloudInit);
        }
        if (initializationType == InitializationType.Ignition && osRepository.isIgnition(getVm().getVmOsId())) {
            parameters.setInitializationType(InitializationType.Ignition);
        }
        return parameters;
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
                addCustomValue("DueToError", " ");
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

        getVmManager().resetExternalDataStatus();
        fetchVmDisksFromDb();
        updateVmDevicesOnRun();
        updateGraphicsAndDisplayInfos();
        updateUsbController();

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

        updateCpuName();

        if (getVm().getEmulatedMachine() == null) {
            getVm().setEmulatedMachine(getEffectiveEmulatedMachine());
        }
    }

    private void updateCpuName() {
        // do not set cpuName if using passthrough or it has already been set (e.g from run once command)
        if (getVm().isUsingCpuPassthrough()
                || getVm().getCpuName() != null) {
            return;
        }

        // use custom cpu name if set
        if (getVm().getCustomCpuName() != null) {
            getVm().setCpuName(getVm().getCustomCpuName());
        } else {
            // use cluster value if the compatibility versions of vm and cluster are the same
            if (getCluster().getCompatibilityVersion().equals(getVm().getCompatibilityVersion())) {
                getVm().setCpuName(getCluster().getCpuVerb());
            } else {
                // use configured value if the compatibility versions of vm and cluster are different
                getVm().setCpuName(getCpuFlagsManagerHandler().getCpuId(
                        getVm().getClusterCpuName(),
                        getVm().getCompatibilityVersion()));
            }
        }
    }

    protected String getEffectiveEmulatedMachine() {
        return EmulatedMachineUtils.getEffective(getVm(), this::getCluster);
    }

    private void updateUsbController() {
        if (getVm().getClusterArch().getFamily() == ArchitectureType.ppc
                && getVm().getUsbPolicy() == UsbPolicy.DISABLED
                && getVm().getVmType() == VmType.HighPerformance) {
            if (getVm().getDefaultDisplayType() != DisplayType.none) {
                // if the VM is set with a console, let libvirt add a corresponding usb controller needed
                // for input devices (to make the console usable)
                getVmDeviceUtils().removeUsbControllers(getVm().getId());
                return;
            }
            // otherwise, make sure that headless VM is set with disabled USB controller
            if (!getVmDeviceUtils().isUsbControllerDisabled(getVm().getId())) {
                getVmDeviceUtils().removeUsbControllers(getVm().getId());
                getVmDeviceUtils().addDisableUsbControllers(getVm().getId());
                return;
            }
        }
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
            if (!getVm().isInitialized() && getVm().getVmInit() != null || getParameters().isInitialize()) {
                if (osRepository.isSysprep(getVm().getVmOsId())) {
                    if (!isPayloadExists(VmDeviceType.FLOPPY)) {
                        initializationType = InitializationType.Sysprep;
                    }
                } else {
                    if (!isPayloadExists(VmDeviceType.CDROM)) {
                        if(osRepository.isCloudInit(getVm().getVmOsId())) {
                            initializationType = InitializationType.CloudInit;
                        } else {
                            initializationType = InitializationType.Ignition;
                        }
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
            } else if (getParameters().getInitializationType() == InitializationType.Ignition &&
                    isPayloadExists(VmDeviceType.CDROM)) {
                vmPayload = null;
            }
        }
    }

    protected boolean getVdsToRunOn() {
        Optional<Guid> vdsToRunOn = schedulingManager.prepareCall(getCluster())
                .hostBlackList(getRunVdssList())
                .hostWhiteList(getVdsWhiteList())
                .destHostIdList(getPredefinedVdsIdListToRunOn())
                .delay(true)
                .correlationId(getCorrelationId())
                .schedule(getVm());

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

        // CPU flags passthrough is special and does not use
        // the cpuName so we can store the flags used for
        // starting the VM here (scheduler needs those for
        // checking migration compatibility in the future)
        if (getVm().isUsingCpuPassthrough()) {
            getVm().setCpuName(getVds().getCpuFlags());
            getVm().setUseHostCpuFlags(true);
        }

        addCpuAndNumaPinning();

        return true;
    }

    private void warnIfVmNotFitInNumaNode() {
        if (!getVds().isNumaSupport()) {
            return;
        }

        // If the VM has NUMA nodes defined, the check is skipped
        List<VmNumaNode> vmNumaNodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(getVmId());
        if (!vmNumaNodes.isEmpty()) {
            return;
        }

        List<VdsNumaNode> hostNodes = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(getVdsId());
        boolean vmFitsToSomeNumaNode = hostNodes.stream()
                .filter(node -> getVm().getMemSizeMb() <= node.getMemTotal())
                .anyMatch(node -> VmCpuCountHelper.getDynamicNumOfCpu(getVm()) <= node.getCpuIds().size());

        if (!vmFitsToSomeNumaNode) {
            addCustomValue("HostName", getVdsName());
            auditLogDirector.log(this, AuditLogType.VM_DOES_NOT_FIT_TO_SINGLE_NUMA_NODE);
        }
    }

    /**
     * If vds version greater then vm's and vm not running with cd and there is appropriate RhevAgentTools image -
     * add it to vm as cd.
     */
    private String guestToolsVersionTreatment(String regex) {
        boolean attachCd = false;
        String selectedCd = "";
        List<RepoImage> repoFilesData = diskImageDao.getIsoDisksForStoragePoolAsRepoImages(getVm().getStoragePoolId());
        Guid isoDomainId = getActiveIsoDomainId();
        if (osRepository.isWindows(getVm().getVmOsId())) {

            // get cluster version of the vm tools
            Version vmToolsClusterVersion = null;
            Version guestTools = new Version(vmHandler.currentOvirtGuestAgentVersion(getVm()));
            if (!guestTools.isNotValid()) {
                vmToolsClusterVersion = new Version(guestTools.getMajor(), guestTools.getMinor());
            }

            // Fetch cached Iso files from active Iso domain.
            List<RepoImage> repoFiles =
                    getIsoDomainListSynchronizer().getCachedIsoListByDomainId(isoDomainId, ImageFileType.ISO);
            repoFiles.addAll(repoFilesData);
            Version bestClusterVer = null;
            int bestToolVer = 0;
            for (RepoImage repo : repoFiles) {
                String fileName = repo.getRepoImageName() == null ?
                        StringUtils.defaultString(repo.getRepoImageId(), "") : repo.getRepoImageName();
                Matcher matchToolPattern =
                        Pattern.compile(regex).matcher(fileName.toLowerCase());
                if (matchToolPattern.find()) {
                    // Get cluster version and tool version of Iso tool.
                    Version clusterVer = new Version(matchToolPattern.group(IsoDomainListSynchronizer.TOOL_CLUSTER_LEVEL));
                    int toolVersion = Integer.parseInt(matchToolPattern.group(IsoDomainListSynchronizer.TOOL_VERSION));

                    if (clusterVer.compareTo(getVm().getCompatibilityVersion()) <= 0) {
                        if ((bestClusterVer == null)
                                || (clusterVer.compareTo(bestClusterVer) > 0)
                                || (clusterVer.equals(bestClusterVer) && toolVersion > bestToolVer)) {
                            bestToolVer = toolVersion;
                            bestClusterVer = clusterVer;
                            selectedCd = fileName;
                        }
                    }
                }
            }

            if (bestClusterVer != null
                && (vmToolsClusterVersion == null
                    || vmToolsClusterVersion.compareTo(bestClusterVer) < 0
                    || vmToolsClusterVersion.equals(bestClusterVer) && guestTools.getBuild() < bestToolVer)) {
                // Vm has no tools or there are new tools
                attachCd = true;
            }
        }

        if (attachCd) {
            String toolsName = selectedCd;
            // See if the disk comes from a data domain
            RepoImage dataDisk = repoFilesData.stream()
                    .filter(m -> m.getRepoImageName().equals(toolsName))
                    .findFirst()
                    .orElse(null);
            if (dataDisk != null){
                return dataDisk.getRepoImageId();
            }
            String isoDir = (String) runVdsCommand(VDSCommandType.IsoDirectory,
                    new IrsBaseVDSCommandParameters(getVm().getStoragePoolId())).getReturnValue();
            selectedCd = isoDir + File.separator + selectedCd;

            return selectedCd;
        }
        return null;
    }

    @Override
    protected boolean validateImpl() {
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
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_COMPATIBILITY_VERSION_NOT_SUPPORTED,
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

        checkVmLeaseStorageDomain();

        if (!checkRngDeviceClusterCompatibility()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_RNG_SOURCE_NOT_SUPPORTED);
        }

        if (!validate(checkDisksInBackupStorage())) {
            return false;
        }

        boolean isCloudInitEnabled = (!getVm().isInitialized() && getVm().getVmInit() != null && osRepository.isCloudInit(getVm().getVmOsId())) ||
                (getParameters().getInitializationType() == InitializationType.CloudInit);

        boolean isIgnitionEnabled = (!getVm().isInitialized() && getVm().getVmInit() != null && osRepository.isIgnition(getVm().getVmOsId())) ||
                (getParameters().getInitializationType() == InitializationType.Ignition);

        if ((isCloudInitEnabled || isIgnitionEnabled) && hasMaximumNumberOfDisks()) {
            return failValidation(EngineMessage.VMPAYLOAD_CDROM_OR_CLOUD_INIT_MAXIMUM_DEVICES);
        }

        if (getVm().getCustomCpuName() == null && !validate(vmHandler.isCpuSupported(
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

        if (FeatureSupported.isBiosTypeSupported(getCluster().getCompatibilityVersion())
                && getVm().getBiosType() != BiosType.I440FX_SEA_BIOS
                && getCluster().getArchitecture().getFamily() != ArchitectureType.x86) {
            return failValidation(EngineMessage.NON_DEFAULT_BIOS_TYPE_FOR_X86_ONLY);
        }

        if (!getVm().getStatus().equals(VMStatus.Paused) && isVmDuringBackup()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
        }

        return true;
    }

    private void addCpuAndNumaPinning() {
        if (getVm().getCpuPinningPolicy() == CpuPinningPolicy.RESIZE_AND_PIN_NUMA) {
            vmHandler.updateCpuAndNumaPinning(getVm(), getVdsId());

            List<VmNumaNode> newVmNumaList = getVm().getvNumaNodeList();
            VmNumaNodeOperationParameters params =
                    new VmNumaNodeOperationParameters(getVm(), new ArrayList<>(newVmNumaList));

            if (!backend.runInternalAction(ActionType.SetVmNumaNodes, params).getSucceeded()) {
                auditLogDirector.log(this, AuditLogType.NUMA_UPDATE_VM_NUMA_NODE_FAILED);
                throw new EngineException(EngineError.FAILED_NUMA_UPDATE);
            }
        }
    }

    protected void checkVmLeaseStorageDomain() {
        if (getVm().getLeaseStorageDomainId() != null) {
            // Engine will allow HA VM with a lease to run even if the status of the storage domain that holds the lease
            // is not active, it will allow the VM to run in case of a disaster that causes the SPM without
            // a power management to go down which will eventually set the DC to 'down' state.
            StorageDomain leaseStorageDomain =
                    storageDomainDao.getForStoragePool(getVm().getLeaseStorageDomainId(), getVm().getStoragePoolId());
            StorageDomainValidator storageDomainValidator = new StorageDomainValidator(leaseStorageDomain);
            ValidationResult validationResult = storageDomainValidator.isDomainExistAndActive();
            if (!validate(validationResult)) {
                log.warn("The VM lease storage domain '{}' status is not active, "
                                + "VM '{}' will fail to run in case the storage domain isn't reachable",
                        leaseStorageDomain.getName(),
                        getVm().getName());
            }
        }
    }

    @Override
    protected void logValidationFailed() {
        addCustomValue("DueToError",
                " due to a failed validation: " +
                        backend.getErrorsTranslator().translateErrorText(getReturnValue().getValidationMessages()));
        auditLogDirector.log(this, AuditLogType.USER_FAILED_RUN_VM);
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
            backend.endAction(ActionType.CreateSnapshotForVm,
                    getParameters().getImagesParameters().get(0),
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());

            getParameters().setShouldBeLogged(false);
            getParameters().setRunAsStateless(false);
            getParameters().setCachedFlow(null);

            setSucceeded(backend.runInternalAction(
                    getActionType(), getParameters(), createContextForRunStatelessVm()).getSucceeded());
            if (!getSucceeded()) {
                getParameters().setShouldBeLogged(true);
                log.error("Could not run VM '{}' ({}) in stateless mode",
                        getVm().getName(), getVm().getId());
                // could not run the vm don't try to run the end action again
                getReturnValue().setEndActionTryAgain(false);
            }
        } else {
            // Hibernation (VMStatus.Suspended) treatment:
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
            ActionReturnValue actionReturnValue = backend.endAction(ActionType.CreateSnapshotForVm,
                    getParameters().getImagesParameters().get(0), cloneContext().withoutExecutionContext()
                            .withoutLock());

            setSucceeded(actionReturnValue.getSucceeded());
            // we are not running the VM, of course,
            // since we couldn't create a snapshot.
        } else {
            super.endWithFailure();
        }
        if (getVm().getVmPoolId() != null) {
            vmPoolMonitor.startingVmCompleted(getVmId(), "endWithFailure");
        }
    }

    @Override
    public void runningSucceded() {
        removeMemoryFromActiveSnapshot();
        setFlow(RunVmFlow.RUNNING_SUCCEEDED);
        vmStaticDao.incrementDbGeneration(getVm().getId());
        super.runningSucceded();
        if (getVm().getVmPoolId() != null) {
            vmPoolMonitor.startingVmCompleted(getVmId(), "runningSucceded");
        }
    }

    @Override
    protected void runningFailed() {
        cleanupPassthroughVnics();
        if (memoryFromSnapshotUsed) {
            removeMemoryFromActiveSnapshot();
        }
        super.runningFailed();
        if (getVm().getVmPoolId() != null) {
            vmPoolMonitor.startingVmCompleted(getVmId(), "runningFailed");
        }
    }

    @Override
    public void reportCompleted() {
        super.reportCompleted();
        if (getVm().getVmPoolId() != null) {
            vmPoolMonitor.startingVmCompleted(getVmId(), "reportCompleted");
        }
    }

    private void removeMemoryFromActiveSnapshot() {
        // getActiveSnapshot fetches eagerly from the DB, cache it so we can remove the memory volumes
        Snapshot activeSnapshot = getActiveSnapshot();
        if (!activeSnapshot.containsMemory()) {
            return;
        }

        snapshotDao.removeMemoryFromActiveSnapshot(getVmId());
        // If the memory volumes are not used by any other snapshot, we can remove them
        if (snapshotDao.getNumOfSnapshotsByDisks(activeSnapshot) == 0) {
            removeMemoryDisks(activeSnapshot);
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
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getVm().getClusterId(),
                VmCpuCountHelper.isDynamicCpuTopologySet(getVm()) ?
                        getVm().getCurrentCoresPerSocket() * getVm().getCurrentSockets() :
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
    public void migrationProgressReported(int progress) {
        // nothing to do
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

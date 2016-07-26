package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.architecture.HasMaximumNumberOfDisks;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.network.host.VfScheduler;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.scheduling.VdsFreeMemoryChecker;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSyncronizer;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.RunVmParams.RunVmFlow;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
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
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.StartEntity;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
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

    @Inject
    private NetworkDeviceHelper networkDeviceHelper;

    @Inject
    private VfScheduler vfScheduler;

    public static final String ISO_PREFIX = "iso://";
    public static final String STATELESS_SNAPSHOT_DESCRIPTION = "stateless snapshot";

    private static final Logger log = LoggerFactory.getLogger(RunVmCommand.class);

    @Inject
    private HostDeviceManager hostDeviceManager;

    protected RunVmCommand(Guid commandId) {
        super(commandId);
    }

    public RunVmCommand(T runVmParams, CommandContext commandContext) {
        super(runVmParams, commandContext);
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, runVmParams.getVmId()));
        setStoragePoolId(getVm() != null ? getVm().getStoragePoolId() : null);
    }

    @Override
    protected void init() {
        super.init();
        if (getVm() != null) {
            // Load payload from Database (only if none was sent via the parameters)
            loadPayloadDevice();
            needsHostDevices = hostDeviceManager.checkVmNeedsDirectPassthrough(getVm());
            loadVmInit();
            fetchVmDisksFromDb();
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    protected List<Guid> getPredefinedVdsIdListToRunOn() {
        return getVm().getDedicatedVmForVdsList();
    }

    private String getMemoryFromActiveSnapshot() {
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
     * Sets up the command specific boot parameters. This method is not expected to be
     * extended, however it can be overridden (e.g. the children will not call the super)
     */
    protected void refreshBootParameters(RunVmParams runVmParameters) {
        getVm().setBootSequence(getVm().getDefaultBootSequence());
        getVm().setRunOnce(false);
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
        return ImagesHandler.cdPathWindowsToLinux(url, getVm().getStoragePoolId(), getVdsId());
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
            hostDeviceManager.acquireHostDevicesLock(getVm().getDedicatedVmForVdsList().get(0));
        }
    }

    private void releaseHostDevicesLock() {
        if (needsHostDevices) {
            // Only single dedicated host allowed for host devices, verified on validates
            hostDeviceManager.releaseHostDevicesLock(getVm().getDedicatedVmForVdsList().get(0));
        }
    }

    private void cleanupPassthroughVnics() {
        Map<Guid, String> vnicToVfMap = getVnicToVfMap();
        if (vnicToVfMap != null) {
            networkDeviceHelper.setVmIdOnVfs(getVdsId(), null, new HashSet<>(vnicToVfMap.values()));
        }

        vfScheduler.cleanVmData(getVmId());
    }

    private Map<Guid, String> getVnicToVfMap() {
        Guid hostId = getVdsId();
        return hostId == null ? null : vfScheduler.getVnicToVfMap(getVmId(), hostId);
    }

    @Override
    protected void executeVmCommand() {
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
            if (getVm().getDiskList().isEmpty()) {
                // If there are no snappable disks, there is no meaning for
                // running as stateless, log a warning and run normally
                warnIfNotAllDisksPermitSnapshots();
                return setFlow(RunVmFlow.RUN);
            }

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
            removeVmStatlessImages();
            break;

        case CREATE_STATELESS_IMAGES:
            createVmStatelessImages();
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
            cachedStatelessSnapshotExistsForVm = getSnapshotDao().exists(getVm().getId(), SnapshotType.STATELESS);
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

    protected IsoDomainListSyncronizer getIsoDomainListSyncronizer() {
        return IsoDomainListSyncronizer.getInstance();
    }

    private void createVmStatelessImages() {
        warnIfNotAllDisksPermitSnapshots();

        log.info("Creating stateless snapshot for VM '{}' ({})",
                getVm().getName(), getVm().getId());
        CreateAllSnapshotsFromVmParameters createAllSnapshotsFromVmParameters = buildCreateSnapshotParameters();

        VdcReturnValueBase vdcReturnValue = runInternalAction(VdcActionType.CreateAllSnapshotsFromVm,
                createAllSnapshotsFromVmParameters,
                createContextForStatelessSnapshotCreation());

        // setting lock to null in order not to release lock twice
        setLock(null);
        setSucceeded(vdcReturnValue.getSucceeded());
        if (!vdcReturnValue.getSucceeded()) {
            if (areDisksLocked(vdcReturnValue)) {
                throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
            }
            getReturnValue().setFault(vdcReturnValue.getFault());
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
                new CreateAllSnapshotsFromVmParameters(getVm().getId(), STATELESS_SNAPSHOT_DESCRIPTION);
        parameters.setShouldBeLogged(false);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setSnapshotType(SnapshotType.STATELESS);
        return parameters;
    }

    private boolean areDisksLocked(VdcReturnValueBase vdcReturnValue) {
        return vdcReturnValue.getValidationMessages().contains(
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

    private void removeVmStatlessImages() {
        runInternalAction(VdcActionType.ProcessDownVm,
                new ProcessDownVmParameters(getVm().getId(), true),
                ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
        // setting lock to null in order not to release lock twice
        setLock(null);
        setSucceeded(true);
    }

    protected VMStatus createVm() {
        final String cdPath = chooseCd();
        if (StringUtils.isNotEmpty(cdPath)) {
            log.info("Running VM with attached cd '{}'", cdPath);
        }
        updateCurrentCd(cdPath);
        getVm().setCdPath(cdPathWindowsToLinux(cdPath));

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

        initParametersForPassthroughVnics();

        VMStatus vmStatus = (VMStatus) getVdsBroker()
                .runAsyncVdsCommand(VDSCommandType.CreateVm, buildCreateVmParameters(), this).getReturnValue();

        // Don't use the memory from the active snapshot anymore if there's a chance that disks were changed
        memoryFromSnapshotUsed = vmStatus.isRunning() || vmStatus == VMStatus.RestoringState;

        // After VM was create (or not), we can remove the quota vds group memory.
        return vmStatus;
    }

    protected void updateCurrentCd(String cdPath) {
        VmHandler.updateCurrentCd(getVdsId(), getVm(), cdPath);
    }


    /**
     * Initialize the parameters for the VDSM command of VM creation
     *
     * @return the VDS create VM parameters
     */
    protected CreateVmVDSCommandParameters buildCreateVmParameters() {
        return new CreateVmVDSCommandParameters(getVdsId(), getVm());
    }

    protected void initParametersForExternalNetworks() {
        if (getVm().getInterfaces().isEmpty()) {
            return;
        }

        Map<VmDeviceId, VmDevice> nicDevices =
                Entities.businessEntitiesById(getDbFacade().getVmDeviceDao().getVmDeviceByVmIdAndType(getVmId(),
                        VmDeviceGeneralType.INTERFACE));

        for (VmNic iface : getVm().getInterfaces()) {
            VnicProfile vnicProfile = getDbFacade().getVnicProfileDao().get(iface.getVnicProfileId());
            Network network = NetworkHelper.getNetworkByVnicProfile(vnicProfile);
            VmDevice vmDevice = nicDevices.get(new VmDeviceId(iface.getId(), getVmId()));
            if (network != null && network.isExternal() && vmDevice.getIsPlugged()) {
                Provider<?> provider = getDbFacade().getProviderDao().get(network.getProvidedBy().getProviderId());
                NetworkProviderProxy providerProxy = ProviderProxyFactory.getInstance().create(provider);
                Map<String, String> deviceProperties = providerProxy.allocate(network, vnicProfile, iface, getVds());

                getVm().getRuntimeDeviceCustomProperties().put(vmDevice.getId(), deviceProperties);
            }
        }
    }

    protected void initParametersForPassthroughVnics() {
        getVm().setPassthroughVnicToVfMap(getVnicToVfMap());
        vfScheduler.cleanVmData(getVmId());
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
                                                : getTaskIdList().isEmpty() ?
                                                        AuditLogType.USER_STARTED_VM
                                                        : AuditLogType.USER_INITIATED_RUN_VM
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
        // reevaluate boot parameters if VM was executed with 'run once'
        refreshBootParameters(getParameters());

        // Before running the VM we update its devices, as they may
        // need to be changed due to configuration option change
        VmDeviceUtils.updateVmDevicesOnRun(getVm().getStaticData());

        updateGraphicsInfos();

        getVm().setKvmEnable(getParameters().getKvmEnable());
        getVm().setRunAndPause(getParameters().getRunAndPause() == null ? getVm().isRunAndPause() : getParameters().getRunAndPause());
        getVm().setAcpiEnable(getParameters().getAcpiEnable());
        if (getParameters().getBootMenuEnabled() != null) {
            getVm().setBootMenuEnabled(getParameters().getBootMenuEnabled());
        }
        if (getParameters().getSpiceFileTransferEnabled() != null) {
            getVm().setSpiceFileTransferEnabled(getParameters().getSpiceFileTransferEnabled());
        }
        if (getParameters().getSpiceCopyPasteEnabled() != null) {
            getVm().setSpiceCopyPasteEnabled(getParameters().getSpiceCopyPasteEnabled());
        }

        // Clear the first user:
        getVm().setConsoleUserId(null);

        if (getParameters().getInitializationType() == null) {
            VmHandler.updateVmInitFromDB(getVm().getStaticData(), false);
            if (!getVm().isInitialized() && getVm().getVmInit() != null) {
                getVm().setInitializationType(InitializationType.None);
                if (osRepository.isWindows(getVm().getVmOsId())) {
                    if (!isPayloadExists(VmDeviceType.FLOPPY)) {
                        getVm().setInitializationType(InitializationType.Sysprep);
                    }
                }
                else if (getVm().getVmInit() != null) {
                    if (!isPayloadExists(VmDeviceType.CDROM)) {
                        getVm().setInitializationType(InitializationType.CloudInit);
                    }
                }
            }
        } else if (getParameters().getInitializationType() != InitializationType.None) {
            getVm().setInitializationType(getParameters().getInitializationType());
            // If the user asked for sysprep/cloud-init via run-once we eliminate
            // the payload since we can only have one media (Floppy/CDROM) per payload.
            if (getParameters().getInitializationType() == InitializationType.Sysprep &&
                    isPayloadExists(VmDeviceType.FLOPPY)) {
                getVm().setVmPayload(null);
            } else if (getParameters().getInitializationType() == InitializationType.CloudInit &&
                    isPayloadExists(VmDeviceType.CDROM)) {
                getVm().setVmPayload(null);
            }
        }
        // if we asked for floppy from Iso Domain we cannot
        // have floppy payload since we are limited to only one floppy device
        if (!StringUtils.isEmpty(getParameters().getFloppyPath()) && isPayloadExists(VmDeviceType.FLOPPY)) {
            getVm().setVmPayload(null);
        }

        VmHandler.updateVmGuestAgentVersion(getVm());

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
            getVm().setEmulatedMachine(getVm().getCustomEmulatedMachine() != null ?
                    getVm().getCustomEmulatedMachine() :
                    getCluster().getEmulatedMachine());
        }

        getVm().setHibernationVolHandle(getMemoryFromActiveSnapshot());
    }

    /**
     * This methods sets graphics infos of a VM to correspond to graphics devices set in DB
     */
    protected void updateGraphicsInfos() {
        for (VmDevice vmDevice : getVmDeviceDao()
                .getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.GRAPHICS)) {
            getVm().getGraphicsInfos().put(GraphicsType.fromString(vmDevice.getDevice()), new GraphicsInfo());
        }
    }

    protected boolean isPayloadExists(VmDeviceType deviceType) {
        return getVm().getVmPayload() != null && getVm().getVmPayload().getDeviceType().equals(deviceType);
    }

    protected void loadPayloadDevice() {
        if (getParameters().getVmPayload() == null) {
            VmPayload payload = getVmPayloadByDeviceType(VmDeviceType.CDROM);
            if (payload != null) {
                getVm().setVmPayload(payload);
            } else {
                getVm().setVmPayload(getVmPayloadByDeviceType(VmDeviceType.FLOPPY));
            }
        }
    }

    protected VmPayload getVmPayloadByDeviceType(VmDeviceType deviceType) {
        List<VmDevice> vmDevices = getVmDeviceDao()
                .getVmDeviceByVmIdTypeAndDevice(getVm().getId(),
                        VmDeviceGeneralType.DISK,
                        deviceType.getName());
        for (VmDevice vmDevice : vmDevices) {
            if (vmDevice.getIsManaged() && VmPayload.isPayload(vmDevice.getSpecParams())) {
                return new VmPayload(vmDevice);
            }
        }
        return null;
    }

    protected void fetchVmDisksFromDb() {
        if (getVm().getDiskMap().isEmpty()) {
            VmHandler.updateDisksFromDb(getVm());
        }
    }

    protected boolean getVdsToRunOn() {
        Guid vdsToRunOn =
                schedulingManager.schedule(getCluster(),
                        getVm(),
                        getRunVdssList(),
                        getVdsWhiteList(),
                        getPredefinedVdsIdListToRunOn(),
                        new ArrayList<>(),
                        new VdsFreeMemoryChecker(this),
                        getCorrelationId());
        setVdsId(vdsToRunOn);
        if (vdsToRunOn != null && !Guid.Empty.equals(vdsToRunOn)) {
            getRunVdssList().add(vdsToRunOn);
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
                    getIsoDomainListSyncronizer().getCachedIsoListByDomainId(isoDomainId, ImageFileType.ISO);
            Version bestClusterVer = null;
            int bestToolVer = 0;
            for (RepoImage map : repoFilesMap) {
                String fileName = StringUtils.defaultString(map.getRepoImageId(), "");
                Matcher matchToolPattern =
                        Pattern.compile(IsoDomainListSyncronizer.REGEX_TOOL_PATTERN).matcher(fileName);
                if (matchToolPattern.find()) {
                    // Get cluster version and tool version of Iso tool.
                    Version clusterVer = new Version(matchToolPattern.group(IsoDomainListSyncronizer.TOOL_CLUSTER_LEVEL));
                    int toolVersion = Integer.parseInt(matchToolPattern.group(IsoDomainListSyncronizer.TOOL_VERSION));

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
                    String.format("%1$s%2$s_%3$s.iso", IsoDomainListSyncronizer.getGuestToolsSetupIsoPrefix(),
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

        RunVmValidator runVmValidator = getRunVmValidator();

        if (!runVmValidator.canRunVm(
                getReturnValue().getValidationMessages(),
                getStoragePool(),
                getRunVdssList(),
                getVdsWhiteList(),
                getPredefinedVdsIdListToRunOn(),
                getCluster())) {
            return false;
        }

        if (!validate(runVmValidator.validateNetworkInterfaces())) {
            return false;
        }

        // check for Vm Payload
        if (getParameters().getVmPayload() != null) {

            if (checkPayload(getParameters().getVmPayload(), getParameters().getDiskPath()) &&
                    !StringUtils.isEmpty(getParameters().getFloppyPath()) &&
                    getParameters().getVmPayload().getDeviceType() == VmDeviceType.FLOPPY) {
                return failValidation(EngineMessage.VMPAYLOAD_FLOPPY_EXCEEDED);
            }

            getVm().setVmPayload(getParameters().getVmPayload());
        }

        if (!checkRngDeviceClusterCompatibility()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_RNG_SOURCE_NOT_SUPPORTED);
        }

        boolean isWindowsOs = osRepository.isWindows(getVm().getVmOsId());

        boolean isCloudInitEnabled = (!getVm().isInitialized() && getVm().getVmInit() != null && !isWindowsOs) ||
                (getParameters().getInitializationType() == InitializationType.CloudInit);

        boolean hasCdromPayload = getParameters().getVmPayload() != null &&
                getParameters().getVmPayload().getDeviceType() == VmDeviceType.CDROM;

        if ((hasCdromPayload || isCloudInitEnabled) && hasMaximumNumberOfDisks()) {
            return failValidation(EngineMessage.VMPAYLOAD_CDROM_OR_CLOUD_INIT_MAXIMUM_DEVICES);
        }

        // Note: that we are setting the payload from database in the ctor.
        //
        // Checking if the user sent Payload and Sysprep/Cloud-init at the same media -
        // Currently we cannot use two payloads in the same media (cdrom/floppy)
        if (getParameters().getInitializationType() != null) {
           if (getParameters().getInitializationType() == InitializationType.Sysprep && getParameters().getVmPayload() != null &&
                   getParameters().getVmPayload().getDeviceType() == VmDeviceType.FLOPPY) {
               return failValidation(EngineMessage.VMPAYLOAD_FLOPPY_WITH_SYSPREP);
           } else if (getParameters().getInitializationType() == InitializationType.CloudInit && getParameters().getVmPayload() != null &&
                   getParameters().getVmPayload().getDeviceType() == VmDeviceType.CDROM) {
               return failValidation(EngineMessage.VMPAYLOAD_CDROM_WITH_CLOUD_INIT);
           }
        }

        if (!VmHandler.isCpuSupported(
                getVm().getVmOsId(),
                getVm().getCompatibilityVersion(),
                getCluster().getCpuName(),
                getReturnValue().getValidationMessages())) {
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

        return true;
    }

    protected void loadVmInit() {
        if (getVm().getVmInit() == null) {
            VmHandler.updateVmInitFromDB(getVm().getStaticData(), false);
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
                getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(getVmId(), VmDeviceGeneralType.RNG, VmDeviceType.VIRTIO.getName());

        if (!rngDevs.isEmpty()) {
            VmRngDevice rngDev = new VmRngDevice(rngDevs.get(0));

            if (!getCluster().getRequiredRngSources().contains(rngDev.getSource())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(StartEntity.class);
        return super.getValidationGroups();
    }

    protected RunVmValidator getRunVmValidator() {
        return new RunVmValidator(getVm(), getParameters(), isInternalExecution(), getActiveIsoDomainId());
    }

    protected Guid getActiveIsoDomainId() {
        if (cachedActiveIsoDomainId == null) {
            cachedActiveIsoDomainId = getIsoDomainListSyncronizer()
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
            getBackend().endAction(VdcActionType.CreateAllSnapshotsFromVm,
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
        Job job = JobRepositoryFactory.getJobRepository().getJobWithSteps(step.getJobId());
        Step executingStep = job.getDirectStep(StepEnum.EXECUTING);
        // We would like to to set the run stateless step as substep of executing step
        setInternalExecution(true);

        ExecutionContext runStatelessVmCtx = new ExecutionContext();
        // The internal command should be monitored for tasks
        runStatelessVmCtx.setMonitored(true);
        Step runStatelessStep =
                ExecutionHandler.addSubStep(getExecutionContext(),
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
            VdcReturnValueBase vdcReturnValue = getBackend().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                    getParameters().getImagesParameters().get(0), cloneContext().withoutExecutionContext()
                            .withoutLock());

            setSucceeded(vdcReturnValue.getSucceeded());
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

        getSnapshotDao().removeMemoryFromActiveSnapshot(getVmId());
        // If the memory volumes are not used by any other snapshot, we can remove them
        if (getSnapshotDao().getNumOfSnapshotsByMemory(memory) == 0) {
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
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        // special permission is needed to use custom properties
        if (!StringUtils.isEmpty(getParameters().getCustomProperties())) {
            permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
        }

        return permissionList;
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

        final VmPool vmPool = getDbFacade().getVmPoolDao().get(getVm().getVmPoolId());
        return vmPool.getVmPoolType().equals(VmPoolType.MANUAL);
    }

    @Override
    protected void endExecutionMonitoring() {
        if (getVm().isRunAndPause() && getVmDynamicDao().get(getVmId()).getStatus() == VMStatus.Paused) {
            final ExecutionContext executionContext = getExecutionContext();
            executionContext.setShouldEndJob(true);
            ExecutionHandler.endJob(executionContext, true);
        } else {
            super.endExecutionMonitoring();
        }
    }

    // initial white list (null == all hosts)
    protected List<Guid> getVdsWhiteList() {
        return null;
    }

    /**
     * Since this callback is called by the VdsUpdateRunTimeInfo thread, we don't want it
     * to fetch the VM using {@link #getVm()}, as the thread that invokes {@link #rerun()},
     * which runs in parallel, is doing setVm(null) to refresh the VM, and because of this
     * race we might end up with null VM. so we fetch the static part of the VM from the DB.
     */
    @Override
    public void onPowerringUp() {
        decreasePendingVm(getVmStaticDao().get(getVmId()));
    }

    @Override
    public CommandCallback getCallback() {
        return getFlow().isStateless() ? new ConcurrentChildCommandsExecutionCallback() : null;
    }
}

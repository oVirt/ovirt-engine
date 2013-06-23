package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.scheduling.VdsFreeMemoryChecker;
import org.ovirt.engine.core.bll.scheduling.VdsSelector;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.osinfo.OsRepositoryImpl;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;


@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class RunVmCommand<T extends RunVmParams> extends RunVmCommandBase<T>
        implements QuotaVdsDependent {

    private static final RunVmValidator runVmValidator = new RunVmValidator();
    private String _cdImagePath = "";
    private String _floppyImagePath = "";
    private boolean mResume;
    /** Note: this field should not be used directly, use {@link #isVmRunningStateless()} instead */
    private Boolean cachedVmIsRunningStateless;
    private boolean isFailedStatlessSnapshot;
    /** Indicates whether restoration of memory from snapshot is supported for the VM */
    private boolean memorySnapshotSupported;
    /** The memory volume which is stored in the active snapshot of the VM */
    private String memoryVolumeFromSnapshot = StringUtils.EMPTY;
    /** This flag is used to indicate that the disks might be dirty since the memory
     *  from the active snapshot was restored so the memory should not be used */
    private boolean memoryFromSnapshotIrrelevant;

    protected RunVmCommand(Guid commandId) {
        super(commandId);
    }

    public RunVmCommand(T runVmParams) {
        super(runVmParams);
        getParameters().setEntityId(runVmParams.getVmId());
        setStoragePoolId(getVm() != null ? getVm().getStoragePoolId() : null);
        initRunVmCommand();
    }

    @Override
    protected VDS getDestinationVds() {
        if (_destinationVds == null) {
            Guid vdsId =
                    getParameters().getDestinationVdsId() != null ? getParameters().getDestinationVdsId()
                            : getVm().getDedicatedVmForVds() != null ? new Guid(getVm().getDedicatedVmForVds()
                                    .toString())
                                    : null;
            if (vdsId != null) {
                _destinationVds = getVdsDAO().get(vdsId);
            }
        }
        return _destinationVds;
    }

    private void initRunVmCommand() {
        RunVmParams runVmParameters = getParameters();
        if (!StringUtils.isEmpty(runVmParameters.getDiskPath())) {
            _cdImagePath = ImagesHandler.cdPathWindowsToLinux(runVmParameters.getDiskPath(), getVm()
                    .getStoragePoolId());
        }
        if (!StringUtils.isEmpty(runVmParameters.getFloppyPath())) {
            _floppyImagePath = ImagesHandler.cdPathWindowsToLinux(runVmParameters.getFloppyPath(), getVm()
                    .getStoragePoolId());
        }

        if (getVm() != null) {
            Guid destVdsId = (getDestinationVds() != null) ? (Guid) getDestinationVds().getId() : null;
            setVdsSelector(new VdsSelector(getVm(), destVdsId, new VdsFreeMemoryChecker(this)));

            refreshBootParameters(runVmParameters);
            getVm().setLastStartTime(new Date());

            // set vm disks
            VmHandler.updateDisksForVm(getVm(), getDiskDao().getAllForVm(getVm().getId()));

            if (getVm().getStatus() != VMStatus.Suspended) {
                memorySnapshotSupported = FeatureSupported.memorySnapshot(getVm().getVdsGroupCompatibilityVersion());
                // If the VM is not hibernated, save the hibernation volume from the baseline snapshot
                memoryVolumeFromSnapshot = getActiveSnapshot().getMemoryVolume();
            }
        }
    }

    private Snapshot getActiveSnapshot() {
        return getSnapshotDao().get(getVm().getId(), SnapshotType.ACTIVE);
    }

    private SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    /**
     * Sets up the command specific boot parameters. This method is not expected to be
     * extended, however it can be overridden (e.g. the children will not call the super)
     */
    protected void refreshBootParameters(RunVmParams runVmParameters) {
        if (runVmParameters == null) {
            return;
        }

        getVm().setBootSequence(getVm().getDefaultBootSequence());
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
        String isoPrefixName = "iso://";
        // The initial Url.
        String fullPathFileName = url;

        // If file name got prefix of iso:// then set the path to the Iso domain.
        int prefixLength = isoPrefixName.length();
        if (url.length() >= prefixLength && (url.substring(0, prefixLength)).equalsIgnoreCase(isoPrefixName)) {
            fullPathFileName = cdPathWindowsToLinux(url.substring(prefixLength, url.length()));
        }
        return fullPathFileName;
    }

    protected String cdPathWindowsToLinux(String url) {
        return ImagesHandler.cdPathWindowsToLinux(url, getVm().getStoragePoolId());
    }

    private void resumeVm() {
        mResume = true;
        setVdsId(new Guid(getVm().getRunOnVds().toString()));
        if (getVds() != null) {
            try {
                incrementVdsPendingVmsCount();
                VDSReturnValue result = getBackend()
                        .getResourceManager()
                        .RunAsyncVdsCommand(VDSCommandType.Resume,
                                new ResumeVDSCommandParameters(getVdsId(), getVm().getId()), this);
                setActionReturnValue(result.getReturnValue());
                setSucceeded(result.getSucceeded());
                ExecutionHandler.setAsyncJob(getExecutionContext(), true);
            } finally {
                freeLock();
                decrementVdsPendingVmsCount();
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
                VmHandler.UpdateVmGuestAgentVersion(getVm());
                incrementVdsPendingVmsCount();
                attachCd();
                if (connectLunDisks(getVdsId())) {
                    status = createVm();
                    ExecutionHandler.setAsyncJob(getExecutionContext(), true);
                }
            } catch(VdcBLLException e) {
                VdcBllErrors errorCode = e.getErrorCode();

                // if the returned exception is such that shoudn't trigger the re-run process,
                // re-throw it. otherwise, continue (the vm will be down and a re-run will be triggered)
                switch (errorCode) {
                case Done: // should never get here with errorCode = 'Done' though
                case exist:
                case VDS_NETWORK_ERROR: // probably wrong xml format sent.
                case PROVIDER_FAILURE:
                    throw e;
                default:
                }

            } finally {
                freeLock();
                decrementVdsPendingVmsCount();
            }
            setActionReturnValue(status);

            if (status != null && (status.isRunning() || status == VMStatus.RestoringState)) {
                setSucceeded(true);
            } else {
                // Try to rerun Vm on different vds no need to log the command because it is
                // being logged inside the rerun
                log.infoFormat("Failed to run desktop {0}, rerun", getVm().getName());
                setCommandShouldBeLogged(false);
                setSucceeded(true);
                rerun();
            }
        }

        else {
            failedToRunVm();
            setSucceeded(false);
            _isRerun = false;
        }
    }


    @Override
    protected void executeVmCommand() {
        // Before running the VM we update its devices, as they may need to be changed due to
        // configuration option change
        VmDeviceUtils.updateVmDevices(getVm().getStaticData());
        setActionReturnValue(VMStatus.Down);
        if (initVm()) {
            if (getVm().getStatus() == VMStatus.Paused) { // resume
                resumeVm();
            } else { // run vm
                if (!_isRerun && Boolean.TRUE.equals(getParameters().getRunAsStateless())
                        && getVm().getStatus() != VMStatus.Suspended) {
                    if (getVm().getDiskList().isEmpty()) { // If there are no snappable disks, there is no meaning for
                                                           // running as stateless, log a warning and run normally
                        warnIfNotAllDisksPermitSnapshots();
                        runVm();
                    }
                    else {
                        statelessVmTreatment();
                    }
                } else if (!isInternalExecution() && !_isRerun
                        && getVm().getStatus() != VMStatus.Suspended
                        && isStatelessSnapshotExistsForVm()
                        && !isVMPartOfManualPool()) {
                    removeVmStatlessImages();
                } else {
                    runVm();
                }
            }
        } else {
            setActionReturnValue(getVm().getStatus());
        }
    }

    private boolean isStatelessSnapshotExistsForVm() {
        return getSnapshotDao().exists(getVm().getId(), SnapshotType.STATELESS);
    }

    /**
     * Handles the cd attachment. Set the VM CDPath to the ISO Path stored in the database (default) Call
     * GuestToolsVersionTreatment to override CDPath by guest tools if needed. If the CD symbol ('D') is contained in
     * the Boot Sequence (at any place) set again the CDPath to the ISO Path that was stored in the database, and we
     * assume that this CD is bootable. So , priorities are (from low to high) : 1)Default 2)Tools 3)Boot Sequence
     */
    private void attachCd() {
        Guid storagePoolId = getVm().getStoragePoolId();

        boolean isIsoFound = (getIsoDomainListSyncronizer().findActiveISODomain(storagePoolId) != null);
        if (isIsoFound) {
            if (StringUtils.isEmpty(getVm().getCdPath())) {
                getVm().setCdPath(getVm().getIsoPath());
                guestToolsVersionTreatment();
                if (getVm().getBootSequence() != null && getVm().getBootSequence().containsSubsequence(BootSequence.D)) {
                    getVm().setCdPath(getVm().getIsoPath());
                }
                getVm().setCdPath(ImagesHandler.cdPathWindowsToLinux(getVm().getCdPath(), getVm().getStoragePoolId()));
            }
        } else if (!StringUtils.isEmpty(getVm().getIsoPath())) {
            getVm().setCdPath("");
            setSucceeded(false);
            throw new VdcBLLException(VdcBllErrors.NO_ACTIVE_ISO_DOMAIN_IN_DATA_CENTER);
        }

    }

    protected IsoDomainListSyncronizer getIsoDomainListSyncronizer() {
        return IsoDomainListSyncronizer.getInstance();
    }

    private void statelessVmTreatment() {
        warnIfNotAllDisksPermitSnapshots();

        if (isStatelessSnapshotExistsForVm()) {
            log.errorFormat(
                    "RunVmAsStateless - {0} - found existing vm images in stateless_vm_image_map table - skipped creating snapshots.",
                    getVm().getName());
            removeVmStatlessImages();
        } else {
            log.infoFormat("VdcBll.RunVmCommand.RunVmAsStateless - Creating snapshot for stateless vm {0} - {1}",
                    getVm().getName(), getVm().getId());
            CreateAllSnapshotsFromVmParameters createAllSnapshotsFromVmParameters = buildCreateSnapshotParameters();

            Map<String, String> values = getVmValuesForMsgResolving();

            // Creating snapshots as sub step of run stateless
            Step createSnapshotsStep = addSubStep(StepEnum.EXECUTING,
                    StepEnum.CREATING_SNAPSHOTS, values);

            // Add the step as the first step of the new context
            ExecutionContext createSnapshotsCtx = new ExecutionContext();
            createSnapshotsCtx.setMonitored(true);
            createSnapshotsCtx.setStep(createSnapshotsStep);
            VdcReturnValueBase vdcReturnValue =
                    getBackend().runInternalAction(VdcActionType.CreateAllSnapshotsFromVm,
                            createAllSnapshotsFromVmParameters,
                            new CommandContext(createSnapshotsCtx, getCompensationContext(), getLock()));

            // setting lock to null in order not to release lock twice
            setLock(null);
            setSucceeded(vdcReturnValue.getSucceeded());

            if (vdcReturnValue.getSucceeded()) {

                getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                // save RunVmParams so we'll know how to run
                // the stateless VM in the EndAction part.
                VmHandler.updateDisksFromDb(getVm());
            } else {
                if (areDisksLocked(vdcReturnValue)) {
                    throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
                }
                getReturnValue().setFault(vdcReturnValue.getFault());
                log.errorFormat("RunVmAsStateless - {0} - failed to create snapshots", getVm().getName());
            }
        }
    }

    private CreateAllSnapshotsFromVmParameters buildCreateSnapshotParameters() {
        CreateAllSnapshotsFromVmParameters createAllSnapshotsFromVmParameters =
                new CreateAllSnapshotsFromVmParameters(getVm().getId(), "stateless snapshot");
        createAllSnapshotsFromVmParameters.setShouldBeLogged(false);
        createAllSnapshotsFromVmParameters.setParentCommand(getActionType());
        createAllSnapshotsFromVmParameters.setParentParameters(getParameters());
        createAllSnapshotsFromVmParameters.setEntityId(getParameters().getEntityId());
        createAllSnapshotsFromVmParameters.setSnapshotType(SnapshotType.STATELESS);
        return createAllSnapshotsFromVmParameters;
    }

    private boolean areDisksLocked(VdcReturnValueBase vdcReturnValue) {
        return vdcReturnValue.getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED.name());
    }


    private void warnIfNotAllDisksPermitSnapshots() {
        for (Disk disk : getVm().getDiskMap().values()) {
            if (!disk.isAllowSnapshot()) {
                AuditLogDirector.log(this,
                        AuditLogType.USER_RUN_VM_AS_STATELESS_WITH_DISKS_NOT_ALLOWING_SNAPSHOT);
                break;
            }
        }
    }

    protected Map<String, String> getVmValuesForMsgResolving() {
        return Collections.singletonMap(VdcObjectType.VM.name().toLowerCase(), getVmName());
    }

    private void removeVmStatlessImages() {
        isFailedStatlessSnapshot = true;
        VmPoolHandler.ProcessVmPoolOnStopVm(getVm().getId(), new CommandContext(getExecutionContext(), getLock()));
        // setting lock to null in order not to release lock twice
        setLock(null);
        setSucceeded(true);
    }

    private void decrementVdsPendingVmsCount() {
        synchronized (_vds_pending_vm_count) {
            int i = _vds_pending_vm_count.get(getVdsId());
            _vds_pending_vm_count.put(getVdsId(), i - 1);
        }
    }

    private void incrementVdsPendingVmsCount() {
        synchronized (_vds_pending_vm_count) {
            if (!_vds_pending_vm_count.containsKey(getVdsId())) {
                _vds_pending_vm_count.put(getVdsId(), 1);
            } else {
                int i = _vds_pending_vm_count.get(getVdsId());
                _vds_pending_vm_count.put(getVdsId(), i + 1);
            }
        }
    }

    protected VMStatus createVm() {

        // reevaluate boot parameters if VM was executed with 'run once'
        refreshBootParameters(getParameters());

        getVm().setLastStartTime(new Date());

        // Set path for initrd and kernel image.
        if (!StringUtils.isEmpty(getVm().getInitrdUrl())) {
            getVm().setInitrdUrl(getIsoPrefixFilePath(getVm().getInitrdUrl()));
        }

        if (!StringUtils.isEmpty(getVm().getKernelUrl())) {
            getVm().setKernelUrl(getIsoPrefixFilePath(getVm().getKernelUrl()));
        }

        VMStatus vmStatus = (VMStatus) getBackend()
                .getResourceManager()
                .RunAsyncVdsCommand(VDSCommandType.CreateVm, initCreateVmParams(), this).getReturnValue();

        // Don't use the memory from the active snapshot anymore if there's a chance that disks were changed
        memoryFromSnapshotIrrelevant = vmStatus.isRunning() || vmStatus == VMStatus.RestoringState;

        // After VM was create (or not), we can remove the quota vds group memory.
        return vmStatus;
    }


    /**
     * Initialize the parameters for the VDSM command of VM creation
     * @return the VDS create VM parameters
     */
    protected CreateVmVDSCommandParameters initCreateVmParams() {
        VM vmToBeCreated = getVm();

        if (!vmToBeCreated.getInterfaces().isEmpty()) {
            initParametersForExternalNetworks();
        }

        if (vmToBeCreated.getStatus() == VMStatus.Suspended) {
            return new CreateVmVDSCommandParameters(getVdsId(), vmToBeCreated);
        }

        if (!memorySnapshotSupported || memoryFromSnapshotIrrelevant) {
            vmToBeCreated.setHibernationVolHandle(StringUtils.EMPTY);
            return new CreateVmVDSCommandParameters(getVdsId(), vmToBeCreated);
        }

        // otherwise, use the memory that is saved on the active snapshot (might be empty)
        vmToBeCreated.setHibernationVolHandle(memoryVolumeFromSnapshot);
        CreateVmVDSCommandParameters parameters =
                new CreateVmVDSCommandParameters(getVdsId(), vmToBeCreated);
        // Mark that the hibernation volume should be cleared from the VM right after the sync part of
        // the create verb is finished (unlike hibernation volume that is created by hibernate command)
        parameters.setClearHibernationVolumes(true);
        return parameters;
    }

    protected void initParametersForExternalNetworks() {
        Map<String, Network> clusterNetworks =
                Entities.entitiesByName(getDbFacade().getNetworkDao().getAllForCluster(getVm().getVdsGroupId()));
        Map<VmDeviceId, VmDevice> nicDevices =
                Entities.businessEntitiesById(getDbFacade().getVmDeviceDao().getVmDeviceByVmIdAndType(getVmId(),
                        VmDeviceGeneralType.INTERFACE));

        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            String networkName = iface.getNetworkName();
            Network network = (networkName == null) ? null : clusterNetworks.get(networkName);
            VmDevice vmDevice = nicDevices.get(new VmDeviceId(iface.getId(), getVmId()));
            if (network != null && network.getProvidedBy() != null && vmDevice.getIsPlugged()) {
                Provider<?> provider = getDbFacade().getProviderDao().get(network.getProvidedBy().getProviderId());
                NetworkProviderProxy providerProxy = ProviderProxyFactory.getInstance().create(provider);
                Map<String, String> deviceProperties = providerProxy.allocate(network, iface);

                getVm().getRuntimeDeviceCustomProperties().put(vmDevice, deviceProperties);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (isFailedStatlessSnapshot) {
                return AuditLogType.USER_RUN_VM_FAILURE_STATELESS_SNAPSHOT_LEFT;
            }
            if (mResume) {
                return getSucceeded() ? AuditLogType.USER_RESUME_VM : AuditLogType.USER_FAILED_RESUME_VM;
            } else if (isInternalExecution()) {
                return getSucceeded() ?
                        (isStatelessSnapshotExistsForVm() ? AuditLogType.VDS_INITIATED_RUN_VM_AS_STATELESS
                                : AuditLogType.VDS_INITIATED_RUN_VM) :
                        AuditLogType.VDS_INITIATED_RUN_VM_FAILED;
            } else {
                return getSucceeded() ?
                        (VMStatus) getActionReturnValue() == VMStatus.Up ?
                                getParameters() != null && getParameters().getDestinationVdsId() == null
                                        && getVm().getDedicatedVmForVds() != null
                                        && !getVm().getRunOnVds().equals(getVm().getDedicatedVmForVds()) ?
                                                AuditLogType.USER_RUN_VM_ON_NON_DEFAULT_VDS :
                                                (isStatelessSnapshotExistsForVm() ? AuditLogType.USER_RUN_VM_AS_STATELESS : AuditLogType.USER_RUN_VM)
                                : _isRerun ?
                                        AuditLogType.VDS_INITIATED_RUN_VM
                                        : getTaskIdList().size() > 0 ?
                                                AuditLogType.USER_INITIATED_RUN_VM
                                                : isRunAndPaused() ? AuditLogType.USER_INITIATED_RUN_VM_AND_PAUSE
                                                        : AuditLogType.USER_STARTED_VM
                        : _isRerun ? AuditLogType.USER_INITIATED_RUN_VM_FAILED : AuditLogType.USER_FAILED_RUN_VM;
            }

        case END_SUCCESS:
            // if not running as stateless, or if succeeded running as
            // stateless,
            // command should be with 'CommandShouldBeLogged = false':
            return isVmRunningStateless() && !getSucceeded() ? AuditLogType.USER_RUN_VM_AS_STATELESS_FINISHED_FAILURE
                    : AuditLogType.UNASSIGNED;

        case END_FAILURE:
            // if not running as stateless, command should
            // be with 'CommandShouldBeLogged = false':
            return isVmRunningStateless() ? AuditLogType.USER_RUN_VM_AS_STATELESS_FINISHED_FAILURE
                    : AuditLogType.UNASSIGNED;

        default:
            // all other cases should be with 'CommandShouldBeLogged =
            // false':
            return AuditLogType.UNASSIGNED;
        }
    }

    protected boolean initVm() {
        if (getVm() == null) {
            log.warnFormat("ResourceManager::{0}::No such vm (where id = '{1}' )in database", getClass().getName(),
                    getVmId().toString());
            throw new VdcBLLException(VdcBllErrors.DB_NO_SUCH_VM);
        }
        if ((getVm().getStatus() == VMStatus.ImageIllegal) || (getVm().getStatus() == VMStatus.ImageLocked)) {
            log.warnFormat("ResourceManager::{0}::vm '{1}' has {2}", getClass().getName(), getVmId().toString(),
                    (getVm().getStatus() == VMStatus.ImageLocked ? "a locked image" : "an illegal image"));
            setActionReturnValue(getVm().getStatus());
            return false;
        } else if (!getSnapshotsValidator().vmNotDuringSnapshot(getVmId()).isValid()) {
            log.warnFormat("ResourceManager::{0}::VM {1} is during snapshot",
                    getClass().getName(),
                    getVmId().toString());
            return false;
        } else {
            handleMemoryAdjustments();
            VmHandler.updateDisksFromDb(getVm());
            getVm().setCdPath(_cdImagePath);
            getVm().setFloppyPath(_floppyImagePath);
            getVm().setKvmEnable(getParameters().getKvmEnable());
            getVm().setRunAndPause(getParameters().getRunAndPause() == null ? getVm().isRunAndPause() : getParameters().getRunAndPause());
            getVm().setAcpiEnable(getParameters().getAcpiEnable());

            // Clear the first user:
            getVm().setConsoleUserId(null);
            getParameters().setRunAsStateless(getParameters().getRunAsStateless() != null ? getParameters().getRunAsStateless()
                    : getVm().isStateless());

            getVm().setDisplayType(getParameters().getUseVnc() == null ?
                    getVm().getDefaultDisplayType() :
                     // if Use Vnc is not null it means runVM was launch from the run once command, thus
                     // the VM can run with display type which is different from its default display type
                    (getParameters().getUseVnc() ? DisplayType.vnc : DisplayType.qxl));

            if (getParameters().getReinitialize()) {
                getVm().setUseSysPrep(true);
            }
            // if we attach floppy we don't need the sysprep
            if (!StringUtils.isEmpty(getVm().getFloppyPath())) {
                getVmStaticDAO().update(getVm().getStaticData());
            }
            // get what cpu flags should be passed to vdsm according to cluster
            // cpu name
            getVm().setVdsGroupCpuFlagsData(
                    CpuFlagsManagerHandler.GetVDSVerbDataByCpuName(getVm().getVdsGroupCpuName(), getVm()
                            .getVdsGroupCompatibilityVersion()));
            return true;
        }
    }

    protected void handleMemoryAdjustments() {
        // nothing to do in RunVmCommand class
    }

    protected boolean getVdsToRunOn() {
        // use destination vds or default vds or none
        List<VDS> vdsList = getVdsDAO()
                .getAllOfTypes(new VDSType[] { VDSType.VDS, VDSType.oVirtNode });
        Guid vdsToRunOn = getVdsSelector().getVdsToRunOn(vdsList, getRunVdssList(), false);
        setVdsId(vdsToRunOn);
        if (vdsToRunOn != null && !Guid.Empty.equals(vdsToRunOn)) {
            getRunVdssList().add(vdsToRunOn);
        }
        VmHandler.UpdateVmGuestAgentVersion(getVm());
        setVds(null);
        setVdsName(null);
        if (getVdsId().equals(Guid.Empty)) {
            log.errorFormat("Can't find VDS to run the VM {0} on, so this VM will not be run.", getVmId());
            return false;
        }

        if (getVds() == null) {
            VdcBLLException outEx = new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_VDS_NOT_FOUND);
            log.error(String.format("VmHandler::%1$s", getClass().getName()), outEx);
            return false;
        }
        return true;
    }

    /**
     * If vds version greater then vm's and vm not running with cd and there is appropriate RhevAgentTools image -
     * add it to vm as cd.
     */
    private void guestToolsVersionTreatment() {
        boolean attachCd = false;
        String selectedToolsVersion = "";
        String selectedToolsClusterVersion = "";
        StorageDomain isoDomain = null;
        if (OsRepositoryImpl.INSTANCE.isWindows(getVm().getVmOsId())
                && (null != (isoDomain =
                        LinqUtils.firstOrNull(getStorageDomainDAO().getAllForStoragePool(getVm().getStoragePoolId()),
                                new Predicate<StorageDomain>() {
                                    @Override
                                    public boolean eval(StorageDomain domain) {
                                        return domain.getStorageDomainType() == StorageDomainType.ISO;
                                    }
                                }))
                        && isoDomain.getStatus() == StorageDomainStatus.Active && StringUtils.isEmpty(_cdImagePath))) {

            // get cluster version of the vm tools
            Version vmToolsClusterVersion = null;
            if (getVm().getHasAgent()) {
                Version clusterVer = getVm().getPartialVersion();
                if (Version.OpEquality(clusterVer, new Version("4.4"))) {
                    vmToolsClusterVersion = new Version("2.1");
                } else {
                    vmToolsClusterVersion = clusterVer;
                }
            }

            // Fetch cached Iso files from active Iso domain.
            List<RepoFileMetaData> repoFilesMap =
                    getIsoDomainListSyncronizer().getCachedIsoListByDomainId(isoDomain.getId(),
                            ImageFileType.ISO);
            Version bestClusterVer = null;
            int bestToolVer = 0;
            for (RepoFileMetaData map : repoFilesMap) {
                String fileName = StringUtils.defaultString(map.getRepoImageId(), "");
                Matcher matchToolPattern =
                        Pattern.compile(IsoDomainListSyncronizer.REGEX_TOOL_PATTERN).matcher(fileName);
                if (matchToolPattern.find()) {
                    // Get cluster version and tool version of Iso tool.
                    Version clusterVer = new Version(matchToolPattern.group(IsoDomainListSyncronizer.TOOL_CLUSTER_LEVEL));
                    int toolVersion = Integer.parseInt(matchToolPattern.group(IsoDomainListSyncronizer.TOOL_VERSION));

                    if (clusterVer.compareTo(getVm().getVdsGroupCompatibilityVersion()) <= 0) {
                        if ((bestClusterVer == null)
                                || (clusterVer.compareTo(bestClusterVer) > 0)) {
                            bestToolVer = toolVersion;
                            bestClusterVer = clusterVer;
                        } else if ((Version.OpEquality(clusterVer, bestClusterVer))
                                && (toolVersion > bestToolVer)) {
                            bestToolVer = toolVersion;
                            bestClusterVer = clusterVer;
                        }
                    }
                }
            }

            if (bestClusterVer != null
                    && (vmToolsClusterVersion == null
                            || vmToolsClusterVersion.compareTo(bestClusterVer) < 0 || (Version
                            .OpEquality(vmToolsClusterVersion, bestClusterVer) && getVm().getHasAgent() &&
                    getVm().getGuestAgentVersion().getBuild() < bestToolVer))) {
                // Vm has no tools or there are new tools
                selectedToolsVersion = (Integer.toString(bestToolVer));
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

            getVm().setCdPath(ImagesHandler.cdPathWindowsToLinux(rhevToolsPath, getVm().getStoragePoolId()));
        }
    }

    @Override
    protected boolean canDoAction() {
        VM vm = getVm();

        if (vm == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (vm.getStatus() == VMStatus.Paused) {
            // if VM is paused, it was already checked before that it is capable to run
            return true;
        }
        List<String> messages = getReturnValue().getCanDoActionMessages();
        List<Disk> vmDisks = getDiskDao().getAllForVm(vm.getId(), true);
        boolean canDoAction =
                getRunVmValidator().canRunVm(vm,
                        messages,
                        vmDisks,
                        getParameters().getBootSequence(),
                        getStoragePool(),
                        isInternalExecution(),
                        getParameters().getDiskPath(),
                        getParameters().getFloppyPath(),
                        getParameters().getRunAsStateless(),
                        getVdsSelector(),
                        getRunVdssList()) &&
                        validateNetworkInterfaces();

        // check for Vm Payload
        if (canDoAction && getParameters().getVmPayload() != null) {
            canDoAction = checkPayload(getParameters().getVmPayload(),
                    getParameters().getDiskPath());

            if (canDoAction && !StringUtils.isEmpty(getParameters().getFloppyPath()) &&
                    getParameters().getVmPayload().getType() == VmDeviceType.FLOPPY) {
                addCanDoActionMessage(VdcBllMessages.VMPAYLOAD_FLOPPY_EXCEEDED);
                canDoAction = false;
            }
            else {
                getVm().setVmPayload(getParameters().getVmPayload());
            }
        }

        return canDoAction;
    }

    protected VmValidator getVmValidator(VM vm) {
        return new VmValidator(vm);
    }

    protected RunVmValidator getRunVmValidator() {
        return runVmValidator;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RUN);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected void endSuccessfully() {
        if (isVmRunningStateless()) {
            CreateAllSnapshotsFromVmParameters createSnapshotParameters = buildCreateSnapshotParameters();
            createSnapshotParameters.setImagesParameters(getParameters().getImagesParameters());
            getBackend().EndAction(VdcActionType.CreateAllSnapshotsFromVm, createSnapshotParameters);

            getParameters().setShouldBeLogged(false);
            getParameters().setRunAsStateless(false);
            ExecutionContext runStatelessVmCtx = new ExecutionContext();
            Step step = getExecutionContext().getStep();
            // Retrieve the job object and its steps as this the endSuccessfully stage of command execution -
            // at this is a new instance of the command is used
            // (comparing with the execution state) so all information on the job and steps should be retrieved.
            Job job = JobRepositoryFactory.getJobRepository().getJobWithSteps(step.getJobId());
            Step executingStep = job.getDirectStep(StepEnum.EXECUTING);
            // We would like to to set the run stateless step as substep of executing step
            setInternalExecution(true);
            // The internal command should be monitored for tasks
            runStatelessVmCtx.setMonitored(true);
            Step runStatelessStep =
                    ExecutionHandler.addSubStep(getExecutionContext(),
                            executingStep,
                            StepEnum.RUN_STATELESS_VM,
                            ExecutionMessageDirector.resolveStepMessage(StepEnum.RUN_STATELESS_VM,
                                    getVmValuesForMsgResolving()));
            // This is needed in order to end the job upon exextuion of the steps of the child command
            runStatelessVmCtx.setShouldEndJob(true);
            // Since run stateless step involves invocation of command, we should set the run stateless vm step as
            // the "beginning step" of the child command.
            runStatelessVmCtx.setStep(runStatelessStep);
            setSucceeded(getBackend()
                    .runInternalAction(getActionType(), getParameters(), new CommandContext(runStatelessVmCtx))
                    .getSucceeded());
            if (!getSucceeded()) {
                // could not run the vm don't try to run the end action again
                log.warnFormat("Could not run the vm {0} on RunVm.EndSuccessfully", getVm().getName());
                getReturnValue().setEndActionTryAgain(false);
            }
        }

        // Hibernation (VMStatus.Suspended) treatment:
        else {
            super.endSuccessfully();
        }
    }

    @Override
    protected void endWithFailure() {
        if (isVmRunningStateless()) {
            CreateAllSnapshotsFromVmParameters createSnapshotParameters = buildCreateSnapshotParameters();
            createSnapshotParameters.setImagesParameters(getParameters().getImagesParameters());
            VdcReturnValueBase vdcReturnValue = getBackend().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                    createSnapshotParameters, new CommandContext(getCompensationContext()));

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
        super.runningSucceded();
    }

    @Override
    protected void failedToRunVm() {
        if (memoryFromSnapshotIrrelevant) {
            removeMemoryFromActiveSnapshot();
        }
        super.failedToRunVm();
    }

    private void removeMemoryFromActiveSnapshot() {
        if (memoryVolumeFromSnapshot.isEmpty()) {
            return;
        }

        // If the active snapshot is the only one that points to the memory volume we can remove it
        if (getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolumeFromSnapshot) == 1) {
            removeMemoryVolumes(memoryVolumeFromSnapshot, getActionType(), true);
        }
        getSnapshotDao().removeMemoryFromActiveSnapshot(getVmId());
    }

    private boolean isVmRunningStateless() {
        if (cachedVmIsRunningStateless == null) {
            cachedVmIsRunningStateless = isStatelessSnapshotExistsForVm();
        }
        return cachedVmIsRunningStateless;
    }

    /**
     * @return true if all VM network interfaces are valid
     */
    protected boolean validateNetworkInterfaces() {
        Map<String, VmNetworkInterface> interfaceNetworkMap = Entities.interfacesByNetworkName(getVm().getInterfaces());
        Set<String> interfaceNetworkNames = interfaceNetworkMap.keySet();
        List<Network> clusterNetworks = getNetworkDAO().getAllForCluster(getVm().getVdsGroupId());
        Set<String> clusterNetworksNames = Entities.objectNames(clusterNetworks);

        return isVmInterfacesConfigured() &&
                isVmInterfacesAttachedToClusterNetworks(clusterNetworksNames, interfaceNetworkNames) &&
                isVmInterfacesAttachedToVmNetworks(clusterNetworks, interfaceNetworkNames);
    }

    /**
     * Checking that the interfaces are all configured, interfaces with no network are allowed only if network linking
     * is supported.
     *
     * @return true if all VM network interfaces are attached to existing cluster networks, or to no network (when
     *         network linking is supported).
     */
    private boolean isVmInterfacesConfigured() {
        for (VmNetworkInterface nic : getVm().getInterfaces()) {
            if (nic.getNetworkName() == null) {
                if (!FeatureSupported.networkLinking(getVm().getVdsGroupCompatibilityVersion())) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_INTERFACE_NETWORK_NOT_CONFIGURED);
                    return false;
                } else {
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * @param clusterNetworksNames
     *            cluster logical networks names
     * @param interfaceNetworkNames
     *            VM interface network names
     * @return true if all VM network interfaces are attached to existing cluster networks
     */
    private boolean isVmInterfacesAttachedToClusterNetworks(final Set<String> clusterNetworkNames,
            final Set<String> interfaceNetworkNames) {

        Set<String> result = new HashSet<String>(interfaceNetworkNames);
        result.removeAll(clusterNetworkNames);
        if (FeatureSupported.networkLinking(getVm().getVdsGroupCompatibilityVersion())) {
            result.remove(null);
        }

        // If after removing the cluster network names we still have objects, then we have interface on networks that
        // aren't
        // attached to the cluster
        if (result.size() > 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_NOT_IN_CLUSTER);
            addCanDoActionMessage(String.format("$networks %1$s", StringUtils.join(result, ",")));
            return false;
        }
        return true;
    }

    /**
     * @param clusterNetworks
     *            cluster logical networks
     * @param interfaceNetworkNames
     *            VM interface network names
     * @return true if all VM network interfaces are attached to VM networks
     */
    private boolean isVmInterfacesAttachedToVmNetworks(final List<Network> clusterNetworks,
            Set<String> interfaceNetworkNames) {
        List<String> nonVmNetworkNames =
                NetworkUtils.filterNonVmNetworkNames(clusterNetworks, interfaceNetworkNames);

        if (nonVmNetworkNames.size() > 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NOT_A_VM_NETWORK);
            addCanDoActionMessage(String.format("$networks %1$s", StringUtils.join(nonVmNetworkNames, ",")));
            return false;
        }
        return true;
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

        // check, if user can override default target host for VM
        if (getVm() != null) {
            final Guid destinationVdsId = getParameters().getDestinationVdsId();
            if (destinationVdsId != null && !destinationVdsId.equals(getVm().getDedicatedVmForVds())) {
                permissionList.add(new PermissionSubject(getParameters().getVmId(),
                    VdcObjectType.VM,
                    ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }

        return permissionList;
    }

    private static final Log log = LogFactory.getLog(RunVmCommand.class);

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        list.add(new QuotaVdsGroupConsumptionParameter(getVm().getQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getVm().getVdsGroupId(),
                getVm().getCpuPerSocket() * getVm().getNumOfSockets(),
                getVm().getMemSizeMb()));
        return list;
    }

    private boolean isVMPartOfManualPool() {
        if (getVm().getVmPoolId() == null) {
            return false;
        }

        final VmPool vmPool = getDbFacade().getVmPoolDao().get(getVm().getVmPoolId());
        return vmPool.getVmPoolType().equals(VmPoolType.Manual);
    }

    private boolean isRunAndPaused() {
        return getVm().isRunAndPause() && getVmDynamicDao().get(getVmId()).getStatus() == VMStatus.Paused;
    }

    @Override
    public void reportCompleted() {
        if (isRunAndPaused()) {
            final ExecutionContext executionContext = getExecutionContext();
            executionContext.setShouldEndJob(true);
            ExecutionHandler.endJob(executionContext, true);
        } else {
            super.reportCompleted();
        }
    }
}

package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;


@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class RunVmCommand<T extends RunVmParams> extends RunVmCommandBase<T>
        implements QuotaVdsDependent {

    private static final long serialVersionUID = 3317745769686161108L;
    private String _cdImagePath = "";
    private String _floppyImagePath = "";
    private boolean mResume;
    private boolean _isVmRunningStateless;
    private boolean isFailedStatlessSnapshot;

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
            setVdsSelector(new VdsSelector(getVm(), destVdsId, true, new VdsFreeMemoryChecker(this)));

            refreshBootParameters(runVmParameters);
            getVm().setLastStartTime(new Date());

            // set vm disks
            VmHandler.updateDisksForVm(getVm(), getDiskDao().getAllForVm(getVm().getId()));
        }
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
                case PROTOCOL_ERROR: // probably wrong xml format sent.
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
                // Try to rerun Vm on different vds
                // no need to log the command because it is being logged inside
                // the rerun
                log.infoFormat("Failed to run desktop {0}, rerun", getVm().getVmName());
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
        // Before running the VM we update its devices, as they may need to be changed due to configuration option
        // change
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
                } else if (!getParameters().getIsInternal() && !_isRerun
                        && getVm().getStatus() != VMStatus.Suspended
                        && statelessSnapshotExistsForVm()) {
                    removeVmStatlessImages();
                } else {
                    runVm();
                }
            }
        } else {
            setActionReturnValue(getVm().getStatus());
        }
    }

    private boolean statelessSnapshotExistsForVm() {
        return getDbFacade().getSnapshotDao().exists(getVm().getId(), SnapshotType.STATELESS);
    }

    /**
     * Handles the cd attachment. Set the VM CDPath to the ISO Path stored in the database (default) Call
     * GuestToolsVersionTreatment to override CDPath by guest tools if needed. If the CD symbol ('D') is contained in
     * the Boot Sequence (at any place) set again the CDPath to the ISO Path that was stored in the database, and we
     * assume that this CD is bootable. So , priorities are (from low to high) : 1)Default 2)Tools 3)Boot Sequence
     */
    private void attachCd() {
        Guid storagePoolId = getVm().getStoragePoolId();

        boolean isIsoFound = (getVmRunHandler().findActiveISODomain(storagePoolId) != null);
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

    private void statelessVmTreatment() {
        warnIfNotAllDisksPermitSnapshots();

        if (statelessSnapshotExistsForVm()) {
            log.errorFormat(
                    "RunVmAsStateless - {0} - found existing vm images in stateless_vm_image_map table - skipped creating snapshots.",
                    getVm().getVmName());
            removeVmStatlessImages();
        } else {
            log.infoFormat("VdcBll.RunVmCommand.RunVmAsStateless - Creating snapshot for stateless vm {0} - {1}",
                    getVm().getVmName(), getVm().getId());
            CreateAllSnapshotsFromVmParameters createAllSnapshotsFromVmParameters =
                    new CreateAllSnapshotsFromVmParameters(getVm().getId(), "stateless snapshot");
            createAllSnapshotsFromVmParameters.setShouldBeLogged(false);
            createAllSnapshotsFromVmParameters.setParentCommand(getActionType());
            createAllSnapshotsFromVmParameters.setParentParameters(getParameters());
            createAllSnapshotsFromVmParameters.setEntityId(getParameters().getEntityId());
            createAllSnapshotsFromVmParameters.setSnapshotType(SnapshotType.STATELESS);

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
                getParameters().getImagesParameters().add(createAllSnapshotsFromVmParameters);

                getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                // save RunVmParams so we'll know how to run
                // the stateless VM in the EndAction part.
                VmHandler.updateDisksFromDb(getVm());
            } else {
                if (areDisksLocked(vdcReturnValue)) {
                    throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
                }
                getReturnValue().setFault(vdcReturnValue.getFault());
                log.errorFormat("RunVmAsStateless - {0} - failed to create snapshots", getVm().getVmName());
            }
        }
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
                .RunAsyncVdsCommand(VDSCommandType.CreateVm, initVdsCreateVmParams(), this).getReturnValue();

        // After VM was create (or not), we can remove the quota vds group memory.
        return vmStatus;
    }

    /**
     * Initial the parameters for the VDSM command for VM creation
     * @return the VDS create VM parameters
     */
    protected CreateVmVDSCommandParameters initVdsCreateVmParams() {
        return new CreateVmVDSCommandParameters(getVdsId(), getVm());
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
            } else if (getParameters() != null && getParameters().getIsInternal()) {
                return getSucceeded() ? AuditLogType.VDS_INITIATED_RUN_VM : AuditLogType.VDS_INITIATED_RUN_VM_FAILED;
            } else {
                return getSucceeded() ?
                        (VMStatus) getActionReturnValue() == VMStatus.Up ?
                                getParameters() != null && getParameters().getDestinationVdsId() == null
                                        && getVm().getDedicatedVmForVds() != null
                                        && !getVm().getRunOnVds().equals(getVm().getDedicatedVmForVds()) ?
                                        AuditLogType.USER_RUN_VM_ON_NON_DEFAULT_VDS
                                        : AuditLogType.USER_RUN_VM
                                : _isRerun ?
                                        AuditLogType.VDS_INITIATED_RUN_VM
                                        : getTaskIdList().size() > 0 ?
                                                AuditLogType.USER_INITIATED_RUN_VM
                                                : AuditLogType.USER_STARTED_VM
                        : _isRerun ? AuditLogType.USER_INITIATED_RUN_VM_FAILED : AuditLogType.USER_FAILED_RUN_VM;
            }

        case END_SUCCESS:
            // if not running as stateless, or if succeeded running as
            // stateless,
            // command should be with 'CommandShouldBeLogged = false':
            return _isVmRunningStateless && !getSucceeded() ? AuditLogType.USER_RUN_VM_AS_STATELESS_FINISHED_FAILURE
                    : AuditLogType.UNASSIGNED;

        case END_FAILURE:
            // if not running as stateless, command should
            // be with 'CommandShouldBeLogged = false':
            return _isVmRunningStateless ? AuditLogType.USER_RUN_VM_AS_STATELESS_FINISHED_FAILURE
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
            getVm().setRunAndPause(getParameters().getRunAndPause());
            getVm().setAcpiEnable(getParameters().getAcpiEnable());

            // Clear the first user:
            getVm().setConsoleUserId(null);

            getParameters().setRunAsStateless(getVmRunHandler().shouldVmRunAsStateless(getParameters(), getVm()));

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
        setVdsId(getVdsSelector().getVdsToRunOn(false));
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
        VmHandler.UpdateVmGuestAgentVersion(getVm());
        storage_domains isoDomain = null;
        if (getVm().getVmOs().isWindows()
                && (null != (isoDomain =
                        LinqUtils.firstOrNull(getStorageDomainDAO().getAllForStoragePool(getVm().getStoragePoolId()),
                                new Predicate<storage_domains>() {
                                    @Override
                                    public boolean eval(storage_domains domain) {
                                        return domain.getstorage_domain_type() == StorageDomainType.ISO;
                                    }
                                }))
                        && isoDomain.getstatus() == StorageDomainStatus.Active && StringUtils.isEmpty(_cdImagePath))) {

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
                    IsoDomainListSyncronizer.getInstance().getCachedIsoListByDomainId(isoDomain.getId(),
                            FileTypeExtension.ISO);
            Version bestClusterVer = null;
            int bestToolVer = 0;
            for (RepoFileMetaData map : repoFilesMap) {
                String fileName = map.getRepoFileName() != null ? map.getRepoFileName() : "";
                Matcher matchToolPattern =
                        Pattern.compile(IsoDomainListSyncronizer.getRegexToolPattern()).matcher(fileName);
                if (matchToolPattern.find()) {
                    // Get cluster version and tool version of Iso tool.
                    // TODO: Should be group name string support in java7.
                    Version clusterVer = new Version(matchToolPattern.group(1));
                    int toolVersion = Integer.parseInt(matchToolPattern.group(3));

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
            rhevToolsPath = Path.Combine(isoDir, rhevToolsPath);

            getVm().setCdPath(ImagesHandler.cdPathWindowsToLinux(rhevToolsPath, getVm().getStoragePoolId()));
        }
    }

    @Override
    protected boolean canDoAction() {
        // setting the RunVmParams Internal flag according to the command Internal flag.
        // we can not use only the command Internal flag and remove this flag from RunVmParams
        // since canRunVm is static and can not call non-static method isInternalExecution
        getParameters().setIsInternal(isInternalExecution());

        boolean canDoAction = canRunVm();

        canDoAction = canDoAction && validateNetworkInterfaces();

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

    protected boolean canRunVm() {
        return getVmRunHandler().canRunVm(getVm(),
                getReturnValue().getCanDoActionMessages(),
                getParameters(),
                getVdsSelector(),
                getSnapshotsValidator(), getVmPropertiesUtils());
    }

    protected VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RUN);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected void endSuccessfully() {
        setIsVmRunningStateless();

        if (_isVmRunningStateless) {
            VdcActionParametersBase createSnapshotParameters = getParameters().getImagesParameters().get(0);
            if (createSnapshotParameters != null) {
                createSnapshotParameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            }
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
            getParameters().setIsInternal(true);
            // The iternal command should be monitored for tasks
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
                // could not run the vm don't try to run the end action
                // again
                log.warnFormat("Could not run the vm {0} on RunVm.EndSuccessfully", getVm().getVmName());
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
        setIsVmRunningStateless();
        if (_isVmRunningStateless) {
            VdcReturnValueBase vdcReturnValue = getBackend().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                    getParameters().getImagesParameters().get(0), new CommandContext(getCompensationContext()));

            setSucceeded(vdcReturnValue.getSucceeded());
            // we are not running the VM, of course,
            // since we couldn't create a snpashot.
        }

        else {
            super.endWithFailure();
        }
    }

    private void setIsVmRunningStateless() {
        _isVmRunningStateless = statelessSnapshotExistsForVm();
    }

    /**
     * @return true if all VM network interfaces are valid
     */
    private boolean validateNetworkInterfaces() {
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
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(), LockMessagesMatchUtil.VM);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        // special permission is needed to use custom properties
        if (!StringUtils.isEmpty(getParameters().getCustomProperties())) {
            permissionList.add(new PermissionSubject(getParameters().getVmId(),
                    VdcObjectType.VM,
                    ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
        }
        return permissionList;
    }

    protected VmRunHandler getVmRunHandler() {
        return VmRunHandler.getInstance();
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
}

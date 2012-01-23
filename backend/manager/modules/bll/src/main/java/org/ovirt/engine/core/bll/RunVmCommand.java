package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;

public class RunVmCommand<T extends RunVmParams> extends RunVmCommandBase<T> {
    // this const represent the character for network in the boot sequence
    // options
    private static final char NETWORK_BOOT_SEQUENCE_CHAR = 'N';
    private static final char cd = (BootSequence.D).toString().charAt(0);
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
        super.setVmId(runVmParams.getVmId());
        getParameters().setEntityId(runVmParams.getVmId());
        InitRunVmCommand();
    }

    @Override
    protected VDS getDestinationVds() {
        if (_destinationVds == null) {
            Guid vdsId =
                    getParameters().getDestinationVdsId() != null ? getParameters().getDestinationVdsId()
                            : getVm().getdedicated_vm_for_vds() != null ? new Guid(getVm().getdedicated_vm_for_vds()
                                    .toString())
                                    : null;
            if (vdsId != null) {
                _destinationVds = DbFacade.getInstance().getVdsDAO().get(vdsId);
            }
        }
        return _destinationVds;
    }

    private void InitRunVmCommand() {
        RunVmParams runVmParameters = getParameters();
        if (!StringHelper.isNullOrEmpty(runVmParameters.getDiskPath())) {
            _cdImagePath = ImagesHandler.cdPathWindowsToLinux(runVmParameters.getDiskPath(), getVm()
                    .getstorage_pool_id());
        }
        if (!StringHelper.isNullOrEmpty(runVmParameters.getFloppyPath())) {
            _floppyImagePath = ImagesHandler.cdPathWindowsToLinux(runVmParameters.getFloppyPath(), getVm()
                    .getstorage_pool_id());
        }

        if (getVm() != null) {
            Guid destVdsId = (getDestinationVds() != null) ? (Guid) getDestinationVds().getvds_id() : null;
            setVdsSelector(new VdsSelector(getVm(), destVdsId, true));

            refreshBootParameters(runVmParameters);
        }
    }

    /**
     * Refresh the associated values of the VM boot parameters with the values from the command parameters. The method
     * is used when VM is reloaded from the DB while its parameters hasn't been persisted (e.g. when running 'as once')
     * @param runVmParameters
     */
    private void refreshBootParameters(RunVmParams runVmParameters) {
        // if not run once then use default boot sequence
        refreshBootSequenceParameter(runVmParameters);

        if (!StringHelper.isNullOrEmpty(runVmParameters.getinitrd_url())) {
            getVm().setinitrd_url(runVmParameters.getinitrd_url());
        }

        if (!StringHelper.isNullOrEmpty(runVmParameters.getkernel_url())) {
            getVm().setkernel_url(runVmParameters.getkernel_url());
        }

        if (!StringHelper.isNullOrEmpty(runVmParameters.getkernel_params())) {
            getVm().setkernel_params(runVmParameters.getkernel_params());
        }

        if (!StringHelper.isNullOrEmpty(runVmParameters.getCustomProperties())) {
            getVm().setCustomProperties(runVmParameters.getCustomProperties());
        }
    }

    private void refreshBootSequenceParameter(RunVmParams runVmParameters) {
        if (runVmParameters != null) {
            getVm().setboot_sequence(((runVmParameters.getBootSequence()) != null) ? runVmParameters.getBootSequence()
                    : getVm().getdefault_boot_sequence());
        }
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
    private String getIsoPrefixFilePath(String url) {
        String isoPrefixName = "iso://";
        // The initial Url.
        String fullPathFileName = url;

        // If file name got prefix of iso:// then set the path to the Iso domain.
        int prefixLength = isoPrefixName.length();
        if (url.length() >= prefixLength && (url.substring(0, prefixLength)).equalsIgnoreCase(isoPrefixName)) {
            fullPathFileName =
                    ImagesHandler.cdPathWindowsToLinux(url.substring(prefixLength, url.length()), getVm()
                            .getstorage_pool_id());
        }
        return fullPathFileName;
    }

    private void ResumeVm() {
        mResume = true;
        // Vds = ResourceManager.Instance.getVds(Vm.run_on_vds.Value);
        setVdsId(new Guid(getVm().getrun_on_vds().toString()));
        if (getVds() != null) {
            try {
                IncrementVdsPendingVmsCount();
                VDSReturnValue result = Backend
                        .getInstance()
                        .getResourceManager()
                        .RunAsyncVdsCommand(VDSCommandType.Resume,
                                new ResumeVDSCommandParameters(getVdsId(), getVm().getvm_guid()), this);
                setActionReturnValue(result.getReturnValue());

                setSucceeded(result.getSucceeded());
            } finally {
                DecrementVdsPendingVmsCount();
            }
        } else {
            setActionReturnValue(getVm().getstatus());
        }
    }

    protected void RunVm() {
        setActionReturnValue(VMStatus.Down);
        if (GetVdsToRunOn()) {
            VMStatus status;
            try {
                IncrementVdsPendingVmsCount();
                AttachCd();
                status = CreateVm();
            } finally {
                DecrementVdsPendingVmsCount();
            }
            setActionReturnValue(status);

            if (VM.isStatusUp(status) || status == VMStatus.RestoringState) {
                setSucceeded(true);
            } else {
                // Try to rerun Vm on different vds
                // no need to log the command because it is being logged inside
                // the rerun
                log.infoFormat("Failed to run desktop {0}, rerun", getVm().getvm_name());
                setCommandShouldBeLogged(false);
                setSucceeded(true);
                Rerun();
            }
        }

        else {
            FailedToRunVm();
            setSucceeded(false);
            _isRerun = false;
        }
    }

    @Override
    protected void ExecuteVmCommand() {
        setActionReturnValue(VMStatus.Down);
        if (InitVm()) {
            if (getVm().getstatus() == VMStatus.Paused) { // resume
                ResumeVm();
            } else { // run vm
                if (!_isRerun && Boolean.TRUE.equals(getParameters().getRunAsStateless()) && !getVm().getDiskList().isEmpty()
                        && getVm().getstatus() != VMStatus.Suspended) {
                    StatelessVmTreatment();
                } else if (!getParameters().getIsInternal() && !_isRerun
                        && getVm().getstatus() != VMStatus.Suspended
                        && DbFacade.getInstance()
                                .getDiskImageDAO()
                                .getAllStatelessVmImageMapsForVm(getVm().getvm_guid())
                                .size() > 0) {
                    removeVmStatlessImages();
                } else {
                    RunVm();
                }
            }
        } else {
            setActionReturnValue(getVm().getstatus());
        }
    }

    /**
     * Handles the cd attachment. Set the VM CDPath to the ISO Path stored in the database (default) Call
     * GuestToolsVersionTreatment to override CDPath by guest tools if needed. If the CD symbol ('C') is contained in
     * the Boot Sequence (at any place) set again the CDPath to the ISO Path that was stored in the database, and we
     * assume that this CD is bootable. So , priorities are (from low to high) : 1)Default 2)Tools 3)Boot Sequence
     */
    private void AttachCd() {
        Guid storagePoolId = getVm().getstorage_pool_id();

        boolean isIsoFound = (findActiveISODomain(storagePoolId) != null);
        if (isIsoFound) {
            if (StringHelper.isNullOrEmpty(getVm().getCdPath())) {
                getVm().setCdPath(getVm().getiso_path());
                GuestToolsVersionTreatment();
                // LINQ Vm.boot_sequence.toString().Contains(cd))
                refreshBootSequenceParameter(getParameters());
                if (getVm().getboot_sequence().toString().indexOf(cd) > -1) {
                    getVm().setCdPath(getVm().getiso_path());
                }
                getVm().setCdPath(ImagesHandler.cdPathWindowsToLinux(getVm().getCdPath(), getVm().getstorage_pool_id()));
            }
        } else if (!StringHelper.isNullOrEmpty(getVm().getiso_path())) {
            getVm().setCdPath("");
            log.error("Can not attach CD without active ISO domain");
        }

    }

    private void StatelessVmTreatment() {
        /**
         * if one of vm's images is in the DB dont do anything.
         */
        if (DbFacade.getInstance().getDiskImageDAO().getAllStatelessVmImageMapsForVm(getVm().getvm_guid()).size() == 0) {
            log.infoFormat("VdcBll.RunVmCommand.RunVmAsStateless - Creating snapshot for stateless vm {0} - {1}",
                    getVm().getvm_name(), getVm().getvm_guid());
            lockVmWithCompensationIfNeeded();

            CreateAllSnapshotsFromVmParameters tempVar = new CreateAllSnapshotsFromVmParameters(getVm().getvm_guid(),
                    "stateless snapshot");
            tempVar.setShouldBeLogged(false);
            tempVar.setParentCommand(VdcActionType.RunVm);
            tempVar.setEntityId(getParameters().getEntityId());
            CreateAllSnapshotsFromVmParameters p = tempVar;

            VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                    VdcActionType.CreateAllSnapshotsFromVm, p, getCompensationContext());

            setSucceeded(vdcReturnValue.getSucceeded());
            if (vdcReturnValue.getSucceeded()) {
                // saving all vm images in order to return to them (not using
                // RestoreAllSnapshots)
                for (DiskImage disk : getVm().getDiskMap().values()) {
                    /**
                     * add new stateless vm image to db
                     */
                    DbFacade.getInstance().getDiskImageDAO().addStatelessVmImageMap(
                            new stateless_vm_image_map(disk.getId(), disk.getinternal_drive_mapping(), getVm()
                                    .getvm_guid()));
                }

                getParameters().getImagesParameters().add(p);

                getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                // save RunVmParams so we'll know how to run
                // the stateless VM in the EndAction part.
                VmHandler.updateDisksFromDb(getVm());
            } else {
                if (vdcReturnValue.getCanDoActionMessages().contains(
                        VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_LOCKED.name())) {
                    throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
                } else {
                    getReturnValue().setFault(vdcReturnValue.getFault());
                }
                log.errorFormat("RunVmAsStateless - {0} - failed to create snapshots", getVm().getvm_name());
            }
        } else {
            log.errorFormat(
                    "RunVmAsStateless - {0} - found existing vm images in stateless_vm_image_map table - skipped creating snapshots.",
                    getVm().getvm_name());
            removeVmStatlessImages();
        }
    }

    private void removeVmStatlessImages() {
        isFailedStatlessSnapshot = true;
        VmPoolHandler.removeVmStatelessImages(getVm().getvm_guid());
        setSucceeded(true);
    }

    private void DecrementVdsPendingVmsCount() {
        synchronized (_vds_pending_vm_count) {
            int i = _vds_pending_vm_count.get(getVdsId());
            _vds_pending_vm_count.put(getVdsId(), i - 1);
        }
    }

    private void IncrementVdsPendingVmsCount() {
        synchronized (_vds_pending_vm_count) {
            if (!_vds_pending_vm_count.containsKey(getVdsId())) {
                _vds_pending_vm_count.put(getVdsId(), 1);
            } else {
                int i = _vds_pending_vm_count.get(getVdsId());
                _vds_pending_vm_count.put(getVdsId(), i + 1);
            }
        }
    }

    protected VMStatus CreateVm() {

        // reevaluate boot parameters if VM was executed with 'run once'
        refreshBootParameters(getParameters());

        // Set path for initrd and kernel image.
        if (!StringHelper.isNullOrEmpty(getVm().getinitrd_url())) {
            getVm().setinitrd_url(getIsoPrefixFilePath(getVm().getinitrd_url()));
        }

        if (!StringHelper.isNullOrEmpty(getVm().getkernel_url())) {
            getVm().setkernel_url(getIsoPrefixFilePath(getVm().getkernel_url()));
        }

        return (VMStatus) Backend
                .getInstance()
                .getResourceManager()
                .RunAsyncVdsCommand(VDSCommandType.CreateVm, initVdsCreateVmParams(), this).getReturnValue();
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
            if(isFailedStatlessSnapshot) {
                return AuditLogType.USER_RUN_VM_FAILURE_STATELESS_SNAPSHOT_LEFT;
            }
            if (mResume) {
                return getSucceeded() ? AuditLogType.USER_RESUME_VM : AuditLogType.USER_FAILED_RESUME_VM;
            } else if (getParameters() != null && getParameters().getIsInternal()) {
                return getSucceeded() ? AuditLogType.VDS_INITIATED_RUN_VM : AuditLogType.VDS_INITIATED_RUN_VM_FAILED;
            } else {
                return getSucceeded() ? (VMStatus) getActionReturnValue() == VMStatus.Up ? (getParameters() != null
                        && getParameters().getDestinationVdsId() == null && getVm().getdedicated_vm_for_vds() != null && !getVm()
                        .getrun_on_vds().equals(getVm().getdedicated_vm_for_vds())) ? AuditLogType.USER_RUN_VM_ON_NON_DEFAULT_VDS
                        : AuditLogType.USER_RUN_VM
                        : _isRerun ? AuditLogType.VDS_INITIATED_RUN_VM
                                : getTaskIdList().size() > 0 ? AuditLogType.USER_INITIATED_RUN_VM
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

    protected boolean InitVm() {
        if (getVm() == null) {
            log.warnFormat("ResourceManager::{0}::No such vm (where id = '{1}' )in database", getClass().getName(),
                    getVmId().toString());
            throw new VdcBLLException(VdcBllErrors.DB_NO_SUCH_VM);
        }
        if ((getVm().getstatus() == VMStatus.ImageIllegal) || (getVm().getstatus() == VMStatus.ImageLocked)) {
            log.warnFormat("ResourceManager::{0}::vm '{1}' has {2}", getClass().getName(), getVmId().toString(),
                    (getVm().getstatus() == VMStatus.ImageLocked ? "a locked image" : "an illegal image"));
            setActionReturnValue(getVm().getstatus());
            return false;
        } else {
            HandleMemoryAdjustments();
            VmHandler.updateDisksFromDb(getVm());
            getVm().setCdPath(_cdImagePath);
            getVm().setFloppyPath(_floppyImagePath);
            getVm().setkvm_enable(getParameters().getKvmEnable());
            getVm().setRunAndPause(getParameters().getRunAndPause());
            getVm().setacpi_enable(getParameters().getAcpiEnable());
            getParameters().setRunAsStateless(shouldVmRunAsStateless(getParameters(), getVm()));
            // if Use Vnc is null it means runVM was launch not from the run
            // once command
            if (getParameters().getUseVnc() != null) {
                getVm().setdisplay_type(getParameters().getUseVnc() ? DisplayType.vnc : DisplayType.qxl);
            } else {
                getVm().setdisplay_type(getVm().getdefault_display_type());
            }
            if (getParameters().getReinitialize()) {
                getVm().setUseSysPrep(true);
            }
            // if we attach floppy we don't need the sysprep
            if (!StringHelper.isNullOrEmpty(getVm().getFloppyPath())) {
                DbFacade.getInstance().getVmStaticDAO().update(getVm().getStaticData());
            }
            // get what cpu flags should be passed to vdsm according to cluster
            // cpu name
            getVm().setvds_group_cpu_flags_data(
                    CpuFlagsManagerHandler.GetVDSVerbDataByCpuName(getVm().getvds_group_cpu_name(), getVm()
                            .getvds_group_compatibility_version()));
            return true;
        }
    }

    protected void HandleMemoryAdjustments() {
        // nothing to do in RunVmCommand class
    }

    protected boolean GetVdsToRunOn() {
        // use destination vds or default vds or none
        setVdsId(getVdsSelector().GetVdsToRunOn());
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
     * If vdss version greater then vm's and vm not running with cd and there is appropriate QumranetAgentTools image -
     * add it to vm as cd.
     */
    private void GuestToolsVersionTreatment() {
        boolean attachCd = false;
        String selectedToolsVersion = "";
        String selectedToolsClusterVersion = "";
        VmHandler.UpdateVmGuestAgentVersion(getVm());
        storage_domains isoDomain = null;
        if (!getVm().getvm_os().isLinux()
                && (null != (isoDomain =
                        LinqUtils.firstOrNull(
                                DbFacade.getInstance()
                                        .getStorageDomainDAO()
                                        .getAllForStoragePool(getVm().getstorage_pool_id()),
                                new Predicate<storage_domains>() {
                                    @Override
                                    public boolean eval(storage_domains domain) {
                                        return domain.getstorage_domain_type() == StorageDomainType.ISO;
                                    }
                                }))
                        && isoDomain.getstatus() == StorageDomainStatus.Active && StringHelper
                        .isNullOrEmpty(_cdImagePath))) {

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
                    IsoDomainListSyncronizer.getInstance().getCachedIsoListByDomainId(isoDomain.getid(),
                            FileTypeExtension.ISO);
            Version bestClusterVer = null;
            int bestToolVer = 0;
            for (RepoFileMetaData map : repoFilesMap) {
                String fileName = map.getRepoFileName() != null ? map.getRepoFileName() : "";
                Matcher matchToolPattern = Pattern.compile(IsoDomainListSyncronizer.regexToolPattern).matcher(fileName);
                if (matchToolPattern.find()) {
                    // Get cluster version and tool version of Iso tool.
                    // TODO: Should be group name string support in java7.
                    Version clusterVer = new Version(matchToolPattern.group(1));
                    int toolVersion = Integer.parseInt(matchToolPattern.group(3));

                    if (clusterVer.compareTo(getVm().getvds_group_compatibility_version()) <= 0) {
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
            // if minimalVdsRev isn't empty use new iso files name convention
            // string qumranetToolsPath = minimalVdsRev == string.Empty
            // ?
            // string.Format("{0}{1}.iso", GuestToolsSetupIsoPrefix, revision)
            // :
            // // format is RHEV-ToolsSetup_tools_ver_vds_min_ver
            // string.Format("{0}{1}_{2}.iso", GuestToolsSetupIsoPrefix,
            // revision,
            // minimalVdsRev);
            String qumranetToolsPath =
                    String.format("%1$s%2$s_%3$s.iso", IsoDomainListSyncronizer.guestToolsSetupIsoPrefix,
                            selectedToolsClusterVersion, selectedToolsVersion);

            String isoDir = (String) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.IsoDirectory,
                            new IrsBaseVDSCommandParameters(getVm().getstorage_pool_id())).getReturnValue();
            qumranetToolsPath = Path.Combine(isoDir, qumranetToolsPath);

            getVm().setCdPath(ImagesHandler.cdPathWindowsToLinux(qumranetToolsPath, getVm().getstorage_pool_id()));
        }
    }

    public boolean CanRunVm() {
        return CanRunVm(getVm(), getReturnValue().getCanDoActionMessages(), getParameters(), getVdsSelector());
    }

    public static boolean CanRunVm(VM vm, java.util.ArrayList<String> message, RunVmParams runParams,
                                   VdsSelector vdsSelector) {
        boolean retValue = true;

        List<VmPropertiesUtils.ValidationError> validationErrors = null;

        if (vm == null) {
            retValue = false;
            if (message != null) {
                message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND.toString());
            }

        } else if (!(validationErrors = VmPropertiesUtils.validateVMProperties(vm.getStaticData())).isEmpty()) {
            handleCustomPropertiesError(validationErrors, message);
            retValue = false;
        } else {

            BootSequence boot_sequence = ((runParams.getBootSequence()) != null) ? runParams.getBootSequence() : vm
                    .getdefault_boot_sequence();
            List<DiskImage> vmImages = DbFacade.getInstance().getDiskImageDAO().getAllForVm(vm.getvm_guid());
            Guid storagePoolId = vm.getstorage_pool_id();
            // Block from running a VM with no HDD when its first boot device is
            // HD
            // and no other boot devices are configured
            if (vmImages.isEmpty() && StringHelper.EqOp(boot_sequence.toString(), BootSequence.C.toString())) {
                message.add(VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK.toString());
                retValue = false;
            } else {
                // If CD appears as first and there is no ISO in storage
                // pool/ISO inactive -
                // you cannot run this VM

                if ((findActiveISODomain(storagePoolId) == null) && boot_sequence.toString().length() > 0
                        && (boot_sequence.toString().charAt(0) == cd)) {
                    message.add(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO.toString());
                    retValue = false;
                }

                // custom properties allowed only from cluster 2.3
                else if (!StringHelper.isNullOrEmpty(vm.getStaticData().getCustomProperties()) &&
                        !Config.<Boolean> GetValue(ConfigValues.SupportCustomProperties,
                                vm.getvds_group_compatibility_version().getValue())) {

                    message.add(VdcBllMessages.CUSTOM_VM_PROPERTIES_INVALID_VALUES_NOT_ALLOWED_IN_CURRENT_CLUSTER.toString());
                    retValue = false;

                } else {
                    // if there is network in the boot sequence, check that the
                    // vm has network,
                    // otherwise the vm cannot be run in vdsm
                    if (boot_sequence.toString().indexOf(NETWORK_BOOT_SEQUENCE_CHAR) > -1
                            && DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getvm_guid()).size() == 0) {
                        message.add(VdcBllMessages.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK.toString());
                        retValue = false;
                    } else {
                        if (vmImages.size() > 0) {
                            Guid storageDomainId = vmImages.get(0).getstorage_id().getValue();
                            // check isValid, storageDomain and diskSpace only
                            // if VM is not HA VM
                            if (!ImagesHandler
                                    .PerformImagesChecks(vm.getvm_guid(), message, vm.getstorage_pool_id(),
                                            storageDomainId, !vm.getauto_startup(), true, false, false, false, false,
                                            !vm.getauto_startup() && !storageDomainId.equals(Guid.Empty)
                                                    || !runParams.getIsInternal() && vm.getauto_startup(),
                                            !vm.getauto_startup() || !runParams.getIsInternal() && vm.getauto_startup())) {
                                retValue = false;
                            }
                            // Check if iso and floppy path exists
                            else if (!vm.getauto_startup()
                                    && !validateIsoPath(findActiveISODomain(vm.getstorage_pool_id()),
                                            runParams,
                                            message)) {
                                retValue = false;
                            } else {
                                boolean isVmDuringInit = ((Boolean) Backend
                                        .getInstance()
                                        .getResourceManager()
                                        .RunVdsCommand(VDSCommandType.IsVmDuringInitiating,
                                                new IsVmDuringInitiatingVDSCommandParameters(vm.getvm_guid()))
                                        .getReturnValue()).booleanValue();
                                if (vm.isStatusUp() || (vm.getstatus() == VMStatus.NotResponding) || isVmDuringInit) {
                                    retValue = false;
                                    if (message != null) {
                                        message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING.toString());
                                    }
                                } else if (vm.getstatus() == VMStatus.Paused && vm.getrun_on_vds() != null) {
                                    VDS vds = DbFacade.getInstance().getVdsDAO().get(
                                            new Guid(vm.getrun_on_vds().toString()));
                                    if (vds.getstatus() != VDSStatus.Up) {
                                        retValue = false;
                                        if (message != null) {
                                            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL.toString());
                                        }
                                    }
                                }

                                boolean isStatelessVm = shouldVmRunAsStateless(runParams, vm);

                                if (retValue && isStatelessVm && ImagesHandler.isVmInPreview(vmImages)) {
                                    retValue = false;
                                    message.add(VdcBllMessages.VM_CANNOT_RUN_STATELESS_WHILE_IN_PREVIEW.toString());
                                }

                                // if the VM itself is stateless or run once as stateless
                                if (retValue && isStatelessVm && vm.getauto_startup()) {
                                    retValue = false;
                                    message.add(VdcBllMessages.VM_CANNOT_RUN_STATELESS_HA.toString());
                                }

                                retValue = retValue == false ? retValue : vdsSelector.CanFindVdsToRunOn(message, false);

                                /**
                                 * only if can do action ok then check with actions matrix that status is valid for this
                                 * action
                                 */
                                if (retValue
                                        && !VdcActionUtils.CanExecute(new java.util.ArrayList<IVdcQueryable>(
                                                java.util.Arrays.asList(new IVdcQueryable[] { vm })), VM.class,
                                                VdcActionType.RunVm)) {
                                    message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL.toString());
                                    retValue = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return retValue;
    }

    @SuppressWarnings("unchecked")
    protected static boolean validateIsoPath(Guid storageDomainId,
                                             RunVmParams runParams,
                                             java.util.ArrayList<String> messages) {
        if (!StringHelper.isNullOrEmpty(runParams.getDiskPath())) {
            if (storageDomainId == null) {
                messages.add(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO.toString());
                return false;
            }
            boolean retValForIso = false;
            VdcQueryReturnValue ret = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetAllIsoImagesList,
                    new GetAllIsoImagesListParameters(storageDomainId));
            if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
                List<RepoFileMetaData> repoFileNameList = (List<RepoFileMetaData>) ret.getReturnValue();
                if (repoFileNameList != null) {
                    for (RepoFileMetaData isoFileMetaData : (List<RepoFileMetaData>) ret.getReturnValue()) {
                        if (isoFileMetaData.getRepoFileName().equals(runParams.getDiskPath())) {
                            retValForIso = true;
                            break;
                        }
                    }
                }
            }
            if (!retValForIso) {
                messages.add(VdcBllMessages.ERROR_CANNOT_FIND_ISO_IMAGE_PATH.toString());
                return false;
            }
        }

        if (!StringHelper.isNullOrEmpty(runParams.getFloppyPath())) {
            boolean retValForFloppy = false;
            VdcQueryReturnValue ret = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetAllFloppyImagesList,
                    new GetAllIsoImagesListParameters(storageDomainId));
            if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
                List<RepoFileMetaData> repoFileNameList = (List<RepoFileMetaData>) ret.getReturnValue();
                if (repoFileNameList != null) {

                    for (RepoFileMetaData isoFileMetaData : (List<RepoFileMetaData>) ret.getReturnValue()) {
                        if (isoFileMetaData.getRepoFileName().equals(runParams.getFloppyPath())) {
                            retValForFloppy = true;
                            break;
                        }
                    }
                }
            }
            if (!retValForFloppy) {
                messages.add(VdcBllMessages.ERROR_CANNOT_FIND_FLOPPY_IMAGE_PATH.toString());
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean canDoAction() {
        // setting the RunVmParams Internal flag according to the command Internal flag.
        // we can not use only the command Internal flag and remove this flag from RunVmParams
        // since canRunVm is static and can not call non-static method isInternalExecution
        getParameters().setIsInternal(isInternalExecution());
        boolean canDoAction = CanRunVm();
        if (!canDoAction) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RUN);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }

        return canDoAction;
    }

    @Override
    protected void EndSuccessfully() {
        SetIsVmRunningStateless();

        if (_isVmRunningStateless) {
            if (DbFacade.getInstance().getDiskImageDAO().getAllStatelessVmImageMapsForVm(getVmId()).size() > 0) {
                VdcActionParametersBase createSnapshotParameters = getParameters().getImagesParameters().get(0);
                if (createSnapshotParameters != null) {
                    createSnapshotParameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                }
                VdcReturnValueBase vdcReturnValue = Backend.getInstance().EndAction(
                        VdcActionType.CreateAllSnapshotsFromVm, createSnapshotParameters);

                getParameters().setShouldBeLogged(false);
                getParameters().setRunAsStateless(false);
                getParameters().setIsInternal(true);
                setSucceeded(Backend.getInstance().runInternalAction(VdcActionType.RunVm, getParameters())
                        .getSucceeded());
                if (!getSucceeded()) {
                    // could not run the vm don't try to run the end action
                    // again
                    log.warnFormat("Could not run the vm {0} on RunVm.EndSuccessfully", getVm().getvm_name());
                    getReturnValue().setEndActionTryAgain(false);
                }
            } else
            // the stateless-snapshot no longer exists (probably due to
            // ProcessVmPoolOnStopVm
            // treatment) -> no point in running the VM or retrying to
            // EndAction:
            {
                getReturnValue().setEndActionTryAgain(false);
            }
        }

        /**
         * Hibernation (VMStatus.Suspended) treatment:
         */
        else {
            super.EndSuccessfully();
        }
    }

    @Override
    protected void EndWithFailure() {
        SetIsVmRunningStateless();

        if (_isVmRunningStateless) {
            VdcReturnValueBase vdcReturnValue = Backend.getInstance().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                    getParameters().getImagesParameters().get(0), getCompensationContext());

            if (getVm() != null) {
                VmHandler.updateDisksFromDb(getVm());

                for (DiskImage disk : getVm().getDiskMap().values()) {
                    /**
                     * remove stateless vm image from db:
                     */
                    DbFacade.getInstance().getDiskImageDAO().removeStatelessVmImageMap(disk.getId());
                }
            } else {
                setCommandShouldBeLogged(false);
                log.warn("RunVmCommand::EndWithFailure [stateless]: Vm is null - not performing full EndAction");
            }

            setSucceeded(vdcReturnValue.getSucceeded());
            // we are not running the VM, of course,
            // since we couldn't create a snpashot.
        }

        else {
            super.EndWithFailure();
        }
    }

    private void SetIsVmRunningStateless() {
        List<stateless_vm_image_map> list = DbFacade.getInstance().getDiskImageDAO().getAllStatelessVmImageMapsForVm(
                getVmId());
        _isVmRunningStateless = (list != null && list.size() > 0);
    }

    /**
     * Checks if there is an active ISO domain in the storage pool. If so returns the Iso Guid, otherwise returns null.
     *
     * @param storagePoolId
     *            The storage pool id.
     * @return Iso Guid of active Iso, and null if not.
     */
    public static Guid findActiveISODomain(Guid storagePoolId) {
        Guid isoGuid = null;
        List<storage_domains> domains = DbFacade.getInstance().getStorageDomainDAO().getAllForStoragePool(
                storagePoolId);
        for (storage_domains domain : domains) {
            if (domain.getstorage_domain_type() == StorageDomainType.ISO) {
                storage_domains sd = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(domain.getid(),
                        storagePoolId);
                if (sd != null && sd.getstatus() == StorageDomainStatus.Active) {
                    isoGuid = sd.getid();
                    break;
                }
            }
        }
        return isoGuid;
    }

    private static boolean shouldVmRunAsStateless(RunVmParams param, VM vm) {
        if (param.getRunAsStateless() != null) {
            return param.getRunAsStateless();
        }
        return vm.getis_stateless();
    }

    private static LogCompat log = LogFactoryCompat.getLog(RunVmCommand.class);
}

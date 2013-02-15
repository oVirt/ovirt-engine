package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;

/** A utility class for verifying running a vm*/
public class VmRunHandler {
    private static final VmRunHandler instance = new VmRunHandler();
    private static final Log log = LogFactory.getLog(VmHandler.class);

    public static VmRunHandler getInstance() {
        return instance;
    }

    public boolean canRunVm(VM vm, ArrayList<String> message, RunVmParams runParams,
            VdsSelector vdsSelector, SnapshotsValidator snapshotsValidator, VmPropertiesUtils vmPropsUtils) {
        boolean retValue = true;

        List<VmPropertiesUtils.ValidationError> validationErrors = null;

        if (vm == null) {
            retValue = false;
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND.toString());
        } else if (!(validationErrors =
                vmPropsUtils.validateVMProperties(vm.getVdsGroupCompatibilityVersion(),
                        vm.getStaticData())).isEmpty()) {
            VmHandler.handleCustomPropertiesError(validationErrors, message);
            retValue = false;
        } else {
            BootSequence boot_sequence = ((runParams.getBootSequence()) != null) ? runParams.getBootSequence() : vm
                    .getDefaultBootSequence();
            Guid storagePoolId = vm.getStoragePoolId();
            // Block from running a VM with no HDD when its first boot device is
            // HD
            // and no other boot devices are configured
            List<Disk> vmDisks = getPluggedDisks(vm);
            if (boot_sequence == BootSequence.C && vmDisks.size() == 0) {
                String messageStr = !vmDisks.isEmpty() ?
                        VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_PLUGGED_DISK.toString() :
                        VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK.toString();

                message.add(messageStr);
                retValue = false;
            } else {
                // If CD appears as first and there is no ISO in storage
                // pool/ISO inactive -
                // you cannot run this VM

                if (boot_sequence == BootSequence.CD && findActiveISODomain(storagePoolId) == null) {
                    message.add(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO.toString());
                    retValue = false;
                } else {
                    // if there is network in the boot sequence, check that the
                    // vm has network,
                    // otherwise the vm cannot be run in vdsm
                    if (boot_sequence == BootSequence.N
                            && DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForVm(vm.getId()).size() == 0) {
                        message.add(VdcBllMessages.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK.toString());
                        retValue = false;
                    } else if (vmDisks.size() > 0) {
                        ValidationResult vmDuringSnapshotResult =
                                snapshotsValidator.vmNotDuringSnapshot(vm.getId());
                        if (!vmDuringSnapshotResult.isValid()) {
                            message.add(vmDuringSnapshotResult.getMessage().name());
                            retValue = false;
                        }

                        if (retValue) {
                            storage_pool sp = getStoragePoolDAO().get(vm.getStoragePoolId());
                            ValidationResult spUpResult = new StoragePoolValidator(sp).isUp();
                            if (!spUpResult.isValid()) {
                                message.add(spUpResult.getMessage().name());
                                retValue = false;
                            }
                        }

                        if (retValue && !performImageChecksForRunningVm(vm, message, runParams, vmDisks)) {
                            retValue = false;
                        }

                        ValidationResult vmNotLockedResult = new VmValidator(vm).vmNotLocked();
                        if (!vmNotLockedResult.isValid()) {
                            message.add(vmNotLockedResult.getMessage().name());
                            retValue = false;
                        }

                        // Check if iso and floppy path exists
                        if (retValue && !vm.isAutoStartup()
                                && !validateIsoPath(findActiveISODomain(vm.getStoragePoolId()),
                                        runParams,
                                        message)) {
                            retValue = false;
                        } else if (retValue) {
                            boolean isVmDuringInit = ((Boolean) getBackend()
                                    .getResourceManager()
                                    .RunVdsCommand(VDSCommandType.IsVmDuringInitiating,
                                            new IsVmDuringInitiatingVDSCommandParameters(vm.getId()))
                                    .getReturnValue()).booleanValue();
                            if (vm.isRunning() || (vm.getStatus() == VMStatus.NotResponding) || isVmDuringInit) {
                                retValue = false;
                                message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING.toString());
                            } else if (vm.getStatus() == VMStatus.Paused && vm.getRunOnVds() != null) {
                                VDS vds = DbFacade.getInstance().getVdsDao().get(
                                        new Guid(vm.getRunOnVds().toString()));
                                if (vds.getStatus() != VDSStatus.Up) {
                                    retValue = false;
                                    message.add(VdcBllMessages.VAR__HOST_STATUS__UP.toString());
                                    message.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL.toString());
                                }
                            }

                            boolean isStatelessVm = shouldVmRunAsStateless(runParams, vm);

                            if (retValue && isStatelessVm && !snapshotsValidator.vmNotInPreview(vm.getId()).isValid()) {
                                retValue = false;
                                message.add(VdcBllMessages.VM_CANNOT_RUN_STATELESS_WHILE_IN_PREVIEW.toString());
                            }

                            // if the VM itself is stateless or run once as stateless
                            if (retValue && isStatelessVm && vm.isAutoStartup()) {
                                retValue = false;
                                message.add(VdcBllMessages.VM_CANNOT_RUN_STATELESS_HA.toString());
                            }

                            if (retValue && isStatelessVm && !hasSpaceForSnapshots(vm, message)) {
                                return false;
                            }

                            retValue = retValue == false ? retValue : vdsSelector.canFindVdsToRunOn(message, false);

                            /**
                             * only if can do action ok then check with actions matrix that status is valid for this
                             * action
                             */
                            if (retValue
                                    && !VdcActionUtils.CanExecute(Arrays.asList(vm), VM.class,
                                            VdcActionType.RunVm)) {
                                retValue = false;
                                message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL.toString());
                            }
                        }
                    }
                }
            }
        }
        return retValue;
    }

    /**
     * check that we can create snapshots for all disks
     *
     * @param vm
     * @return true if all storage domains have enough space to create snapshots for this VM plugged disks
     */
    public boolean hasSpaceForSnapshots(VM vm, ArrayList<String> message) {
        Integer minSnapshotSize = Config.<Integer> GetValue(ConfigValues.InitStorageSparseSizeInGB);
        for (Entry<StorageDomain, Integer> e : mapStorageDomainsToNumOfDisks(vm).entrySet()) {
            if (!destinationHasSpace(e.getKey(), minSnapshotSize * e.getValue(), message)) {
                return false;
            }
        }
        return true;
    }

    private boolean destinationHasSpace(StorageDomain storageDomain, long sizeRequested, ArrayList<String> message) {
        return validate(new StorageDomainValidator(storageDomain).isDomainHasSpaceForRequest(sizeRequested), message);
    }

    protected boolean validate(ValidationResult validationResult, ArrayList<String> message) {
        if (!validationResult.isValid()) {
            message.add(validationResult.getMessage().name());
            if (validationResult.getVariableReplacements() != null) {
                for (String variableReplacement : validationResult.getVariableReplacements()) {
                    message.add(variableReplacement);
                }
            }
        }
        return validationResult.isValid();
    }

    /**
     * map the VM number of pluggable and snapable disks from their domain.
     *
     * @param vm
     * @return
     */
    public Map<StorageDomain, Integer> mapStorageDomainsToNumOfDisks(VM vm) {
        Map<StorageDomain, Integer> map = new HashMap<StorageDomain, Integer>();
        for (Disk disk : getPluggedDisks(vm)) {
            if (disk.isAllowSnapshot()) {
                for (StorageDomain domain : getStorageDomainDAO().getAllStorageDomainsByImageId(((DiskImage) disk).getImageId())) {
                    map.put(domain, map.containsKey(domain) ? Integer.valueOf(map.get(domain) + 1) : Integer.valueOf(1));
                }
            }
        }
        return map;
    }

    /**
     * Check isValid, storageDomain and diskSpace only if VM is not HA VM
     */
    protected boolean performImageChecksForRunningVm
            (VM vm, List<String> message, RunVmParams runParams, List<Disk> vmDisks) {
        return ImagesHandler.PerformImagesChecks(message,
                vm.getStoragePoolId(), Guid.Empty, !vm.isAutoStartup(),
                true, false, false,
                !vm.isAutoStartup() || !runParams.getIsInternal() && vm.isAutoStartup(),
                !vm.isAutoStartup() || !runParams.getIsInternal() && vm.isAutoStartup(),
                vmDisks);
    }

    /**
     * The following method should return only plugged images which are attached to VM,
     * only those images are relevant for the RunVmCommand
     * @param vm
     * @return
     */
    protected List<Disk> getPluggedDisks(VM vm) {
        List<Disk> diskImages = getDiskDao().getAllForVm(vm.getId());
        List<VmDevice> diskVmDevices = getVmDeviceDAO().getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                VmDeviceType.DISK.getName(),
                VmDeviceType.DISK.getName());
        List<Disk> result = new ArrayList<Disk>();
        for (Disk diskImage : diskImages) {
            for (VmDevice diskVmDevice : diskVmDevices) {
                if (diskImage.getId().equals(diskVmDevice.getDeviceId())) {
                    if (diskVmDevice.getIsPlugged()) {
                        result.add(diskImage);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks if there is an active ISO domain in the storage pool. If so returns the Iso Guid, otherwise returns null.
     *
     * @param storagePoolId
     *            The storage pool id.
     * @return Iso Guid of active Iso, and null if not.
     */
    public Guid findActiveISODomain(Guid storagePoolId) {
        Guid isoGuid = null;
        List<StorageDomain> domains = getStorageDomainDAO().getAllForStoragePool(
                storagePoolId);
        for (StorageDomain domain : domains) {
            if (domain.getStorageDomainType() == StorageDomainType.ISO) {
                StorageDomain sd = getStorageDomainDAO().getForStoragePool(domain.getId(),
                        storagePoolId);
                if (sd != null && sd.getStatus() == StorageDomainStatus.Active) {
                    isoGuid = sd.getId();
                    break;
                }
            }
        }
        return isoGuid;
    }

    @SuppressWarnings("unchecked")
    private boolean validateIsoPath(Guid storageDomainId,
            RunVmParams runParams,
            ArrayList<String> messages) {
        if (!StringUtils.isEmpty(runParams.getDiskPath())) {
            if (storageDomainId == null) {
                messages.add(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO.toString());
                return false;
            }
            boolean retValForIso = false;
            VdcQueryReturnValue ret =
                    getBackend().runInternalQuery(VdcQueryType.GetAllIsoImagesList,
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

        if (!StringUtils.isEmpty(runParams.getFloppyPath())) {
            boolean retValForFloppy = false;
            VdcQueryReturnValue ret =
                    getBackend().runInternalQuery(VdcQueryType.GetAllFloppyImagesList,
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

    public boolean shouldVmRunAsStateless(RunVmParams param, VM vm) {
        if (param.getRunAsStateless() != null) {
            return param.getRunAsStateless();
        }
        return vm.isStateless();
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return DbFacade.getInstance().getVmDeviceDao();
    }

    protected StorageDomainDAO getStorageDomainDAO() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    protected StoragePoolDAO getStoragePoolDAO() {
        return DbFacade.getInstance().getStoragePoolDao();
    }
}

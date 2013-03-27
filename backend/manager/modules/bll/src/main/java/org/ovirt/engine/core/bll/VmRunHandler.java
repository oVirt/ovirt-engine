package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;

/** A utility class for verifying running a vm*/
public class VmRunHandler {
    private static final VmRunHandler instance = new VmRunHandler();

    public static VmRunHandler getInstance() {
        return instance;
    }

    /**
     * This method checks whether the given VM is capable to run.
     *
     * @param vm not null {@link VM}
     * @param message
     * @param runParams
     * @param vdsSelector
     * @param snapshotsValidator
     * @param vmPropsUtils
     * @return true if the given VM can run with the given properties, false otherwise
     */
    public boolean canRunVm(VM vm, ArrayList<String> message, RunVmParams runParams,
            VdsSelector vdsSelector, SnapshotsValidator snapshotsValidator) {
        boolean retValue = true;

        List<Disk> vmDisks = getDiskDao().getAllForVm(vm.getId(), true);
        if (retValue) {
            ValidationResult vmNotLockedResult = new VmValidator(vm).vmNotLocked();
            if (!vmNotLockedResult.isValid()) {
                message.add(vmNotLockedResult.getMessage().name());
                retValue = false;
            }
        }

        if (retValue) {
            ValidationResult vmDuringSnapshotResult =
                    snapshotsValidator.vmNotDuringSnapshot(vm.getId());
            if (!vmDuringSnapshotResult.isValid()) {
                message.add(vmDuringSnapshotResult.getMessage().name());
                retValue = false;
            }
        }
        List<DiskImage> vmImages = ImagesHandler.filterImageDisks(vmDisks, true, false);
        if (retValue && !vmImages.isEmpty()) {
            StoragePool sp = getStoragePoolDAO().get(vm.getStoragePoolId());
            ValidationResult spUpResult = new StoragePoolValidator(sp).isUp();
            if (!spUpResult.isValid()) {
                message.add(spUpResult.getMessage().name());
                retValue = false;
            }

            if (retValue) {
                retValue = performStorageDomainChecks(vm, message, runParams, vmImages);
            }

            if (retValue) {
                retValue = performImageChecksForRunningVm(message, vmImages);
            }

            // Check if iso and floppy path exists
            if (retValue && !vm.isAutoStartup()
                    && !validateIsoPath(getIsoDomainListSyncronizer()
                            .findActiveISODomain(vm.getStoragePoolId()),
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
            }
        }

        retValue = retValue == false ? retValue : vdsSelector.canFindVdsToRunOn(message, false);

        /**
         * only if can do action ok then check with actions matrix that status is valid for this action
         */
        if (retValue
                && !VdcActionUtils.CanExecute(Arrays.asList(vm), VM.class,
                        VdcActionType.RunVm)) {
            retValue = false;
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL.toString());
        }
        return retValue;
    }

    protected IsoDomainListSyncronizer getIsoDomainListSyncronizer() {
        return IsoDomainListSyncronizer.getInstance();
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

    protected boolean validate(ValidationResult validationResult, List<String> message) {
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
        for (Disk disk : getDiskDao().getAllForVm(vm.getId(), true)) {
            if (disk.isAllowSnapshot()) {
                for (StorageDomain domain : getStorageDomainDAO().getAllStorageDomainsByImageId(((DiskImage) disk).getImageId())) {
                    map.put(domain, map.containsKey(domain) ? Integer.valueOf(map.get(domain) + 1) : Integer.valueOf(1));
                }
            }
        }
        return map;
    }

    /**
     * Check storage domains. Storage domain status and disk space are checked only for non-HA VMs.
     *
     * @param vm
     *            The VM to run
     * @param message
     *            The error messages to append to
     * @param runParams
     *            The parameters for runnign the VM
     * @param vmImages
     *            The VM's image disks
     * @return <code>true</code> if the VM can be run, <code>false</code> if not
     */
    protected boolean performStorageDomainChecks(VM vm,
            List<String> message,
            RunVmParams runParams,
            List<DiskImage> vmImages) {
        if (!vm.isAutoStartup() || !runParams.getIsInternal()) {
            Set<Guid> storageDomainIds = ImagesHandler.getAllStorageIdsForImageIds(vmImages);
            MultipleStorageDomainsValidator storageDomainValidator =
                    new MultipleStorageDomainsValidator(vm.getStoragePoolId(), storageDomainIds);
            if (!validate(storageDomainValidator.allDomainsExistAndActive(), message)) {
                return false;
            }

            if (!vm.isAutoStartup()
                    && !validate(storageDomainValidator.allDomainsWithinThresholds(), message)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check isValid only if VM is not HA VM
     */
    protected boolean performImageChecksForRunningVm(List<String> message, List<DiskImage> vmDisks) {
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(vmDisks);
        return validate(diskImagesValidator.diskImagesNotLocked(), message);
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
                    getBackend().runInternalQuery(VdcQueryType.GetImagesList,
                            new GetImagesListParameters(storageDomainId, ImageFileType.ISO));
            if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
                List<RepoFileMetaData> repoFileNameList = (List<RepoFileMetaData>) ret.getReturnValue();
                if (repoFileNameList != null) {
                    for (RepoFileMetaData isoFileMetaData : (List<RepoFileMetaData>) ret.getReturnValue()) {
                        if (isoFileMetaData.getRepoImageId().equals(runParams.getDiskPath())) {
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
                    getBackend().runInternalQuery(VdcQueryType.GetImagesList,
                            new GetImagesListParameters(storageDomainId, ImageFileType.Floppy));
            if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
                List<RepoFileMetaData> repoFileNameList = (List<RepoFileMetaData>) ret.getReturnValue();
                if (repoFileNameList != null) {

                    for (RepoFileMetaData isoFileMetaData : (List<RepoFileMetaData>) ret.getReturnValue()) {
                        if (isoFileMetaData.getRepoImageId().equals(runParams.getFloppyPath())) {
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

    protected StorageDomainDAO getStorageDomainDAO() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    protected StoragePoolDAO getStoragePoolDAO() {
        return DbFacade.getInstance().getStoragePoolDao();
    }
}

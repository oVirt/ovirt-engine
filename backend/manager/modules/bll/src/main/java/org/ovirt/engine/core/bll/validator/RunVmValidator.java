package org.ovirt.engine.core.bll.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.IsoDomainListSyncronizer;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsSelector;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.businessentities.BootSequence;
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
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;

public class RunVmValidator {

    public boolean validateVmProperties(VM vm, List<String> messages) {
        List<VmPropertiesUtils.ValidationError> validationErrors =
                getVmPropertiesUtils().validateVMProperties(
                        vm.getVdsGroupCompatibilityVersion(),
                        vm.getStaticData());

        if (!validationErrors.isEmpty()) {
            VmHandler.handleCustomPropertiesError(validationErrors, messages);
            return false;
        }

        return true;
    }

    public ValidationResult validateBootSequence(VM vm, BootSequence bootSequence, List<Disk> vmDisks) {
        BootSequence boot_sequence = (bootSequence != null) ?
                bootSequence : vm.getDefaultBootSequence();
        Guid storagePoolId = vm.getStoragePoolId();
        // Block from running a VM with no HDD when its first boot device is
        // HD and no other boot devices are configured
        if (boot_sequence == BootSequence.C && vmDisks.size() == 0) {
            return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK);
        } else {
            // If CD appears as first and there is no ISO in storage
            // pool/ISO inactive -
            // you cannot run this VM

            if (boot_sequence == BootSequence.CD
                    && getIsoDomainListSyncronizer().findActiveISODomain(storagePoolId) == null) {
                return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
            } else {
                // if there is network in the boot sequence, check that the
                // vm has network,
                // otherwise the vm cannot be run in vdsm
                if (boot_sequence == BootSequence.N
                        && getVmNetworkInterfaceDao().getAllForVm(vm.getId()).size() == 0) {
                    return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK);
                }
            }
        }
        return ValidationResult.VALID;

    }

    /**
     * Check storage domains. Storage domain status and disk space are checked only for non-HA VMs.
     *
     * @param vm
     *            The VM to run
     * @param message
     *            The error messages to append to
     * @param isInternalExecution
     *            Command is internal?
     * @param vmImages
     *            The VM's image disks
     * @return <code>true</code> if the VM can be run, <code>false</code> if not
     */
    public boolean validateStorageDomains(VM vm,
            List<String> message,
            boolean isInternalExecution,
            List<DiskImage> vmImages) {
        if (!vm.isAutoStartup() || !isInternalExecution) {
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
    public boolean validateImagesForRunVm(List<String> message, List<DiskImage> vmDisks) {
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(vmDisks);
        return validate(diskImagesValidator.diskImagesNotLocked(), message);
    }

    @SuppressWarnings("unchecked")
    public ValidationResult validateIsoPath(boolean isAutoStartup,
            Guid storageDomainId,
            String diskPath,
            String floppyPath) {
        if (isAutoStartup) {
            return ValidationResult.VALID;
        }
        if (!StringUtils.isEmpty(diskPath)) {
            if (storageDomainId == null) {
                return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
            }
            boolean retValForIso = false;
            VdcQueryReturnValue ret =
                    getBackend().runInternalQuery(VdcQueryType.GetImagesList,
                            new GetImagesListParameters(storageDomainId, ImageFileType.ISO));
            if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
                List<RepoFileMetaData> repoFileNameList = (List<RepoFileMetaData>) ret.getReturnValue();
                if (repoFileNameList != null) {
                    for (RepoFileMetaData isoFileMetaData : (List<RepoFileMetaData>) ret.getReturnValue()) {
                        if (isoFileMetaData.getRepoImageId().equals(diskPath)) {
                            retValForIso = true;
                            break;
                        }
                    }
                }
            }
            if (!retValForIso) {
                return new ValidationResult(VdcBllMessages.ERROR_CANNOT_FIND_ISO_IMAGE_PATH);
            }
        }

        if (!StringUtils.isEmpty(floppyPath)) {
            boolean retValForFloppy = false;
            VdcQueryReturnValue ret =
                    getBackend().runInternalQuery(VdcQueryType.GetImagesList,
                            new GetImagesListParameters(storageDomainId, ImageFileType.Floppy));
            if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
                List<RepoFileMetaData> repoFileNameList = (List<RepoFileMetaData>) ret.getReturnValue();
                if (repoFileNameList != null) {

                    for (RepoFileMetaData isoFileMetaData : (List<RepoFileMetaData>) ret.getReturnValue()) {
                        if (isoFileMetaData.getRepoImageId().equals(floppyPath)) {
                            retValForFloppy = true;
                            break;
                        }
                    }
                }
            }
            if (!retValForFloppy) {
                return new ValidationResult(VdcBllMessages.ERROR_CANNOT_FIND_FLOPPY_IMAGE_PATH);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmDuringInitialization(VM vm) {
        boolean isVmDuringInit = isVmDuringInitiating(vm);
        if (vm.isRunning() || vm.getStatus() == VMStatus.NotResponding || isVmDuringInit) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }
        return ValidationResult.VALID;
    }

    protected boolean isVmDuringInitiating(VM vm) {
        return ((Boolean) getBackend()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.IsVmDuringInitiating,
                        new IsVmDuringInitiatingVDSCommandParameters(vm.getId()))
                .getReturnValue()).booleanValue();
    }

    public boolean validateVdsStatus(VM vm, List<String> messages) {
        if (vm.getStatus() == VMStatus.Paused && vm.getRunOnVds() != null) {
            VDS vds = getVdsDao().get(
                    new Guid(vm.getRunOnVds().toString()));
            if (vds.getStatus() != VDSStatus.Up) {
                messages.add(VdcBllMessages.VAR__HOST_STATUS__UP.toString());
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL.toString());
                return false;
            }
        }
        return true;
    }

    public ValidationResult validateStatelessVm(VM vm, List<Disk> plugDisks, Boolean stateless) {
        boolean isStatelessVm = stateless != null ? stateless : vm.isStateless();
        if (!isStatelessVm) {
            return ValidationResult.VALID;
        }

        ValidationResult previewValidation = getSnapshotValidator().vmNotInPreview(vm.getId());
        if (!previewValidation.isValid()) {
            return previewValidation;
        }

        // if the VM itself is stateless or run once as stateless
        if (vm.isAutoStartup()) {
            return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_STATELESS_HA);
        }

        ValidationResult hasSpaceValidation = hasSpaceForSnapshots(vm, plugDisks);
        if (!hasSpaceValidation.isValid()) {
            return hasSpaceValidation;
        }
        return ValidationResult.VALID;
    }

    protected SnapshotsValidator getSnapshotValidator() {
        return new SnapshotsValidator();
    }

    /**
     * check that we can create snapshots for all disks
     * @param vm
     * @return true if all storage domains have enough space to create snapshots for this VM plugged disks
     */
    public ValidationResult hasSpaceForSnapshots(VM vm, List<Disk> plugDisks) {
        Integer minSnapshotSize = Config.<Integer> GetValue(ConfigValues.InitStorageSparseSizeInGB);
        Map<StorageDomain, Integer> mapStorageDomainsToNumOfDisks = mapStorageDomainsToNumOfDisks(vm, plugDisks);
        for (Entry<StorageDomain, Integer> e : mapStorageDomainsToNumOfDisks.entrySet()) {
            ValidationResult validationResult =
                    new StorageDomainValidator(e.getKey()).isDomainHasSpaceForRequest(minSnapshotSize * e.getValue());
            if (!validationResult.isValid()) {
                return validationResult;
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * map the VM number of pluggable and snapable disks from their domain.
     * @param vm
     * @return
     */
    public Map<StorageDomain, Integer> mapStorageDomainsToNumOfDisks(VM vm, List<Disk> plugDisks) {
        Map<StorageDomain, Integer> map = new HashMap<StorageDomain, Integer>();
        for (Disk disk : plugDisks) {
            if (disk.isAllowSnapshot()) {
                for (StorageDomain domain : getStorageDomainDAO().getAllStorageDomainsByImageId(((DiskImage) disk).getImageId())) {
                    map.put(domain, map.containsKey(domain) ? Integer.valueOf(map.get(domain) + 1) : Integer.valueOf(1));
                }
            }
        }
        return map;
    }

    protected VdsDAO getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected StorageDomainDAO getStorageDomainDAO() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    protected IsoDomainListSyncronizer getIsoDomainListSyncronizer() {
        return IsoDomainListSyncronizer.getInstance();
    }

    protected VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
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

    // Compatibility method for static VmPoolCommandBase.canRunPoolVm
    // who uses the same validation as runVmCommand
    public boolean canRunVm(VM vm,
            List<String> messages,
            List<Disk> vmDisks,
            BootSequence bootSequence,
            StoragePool storagePool,
            boolean isInternalExecution,
            String diskPath,
            String floppyPath,
            Boolean runAsStateless, VdsSelector vdsSelector) {
        if (!validateVmProperties(vm, messages)) {
            return false;
        }
        ValidationResult result = validateBootSequence(vm, bootSequence, vmDisks);
        if (!result.isValid()) {
            messages.add(result.getMessage().toString());
            return false;
        }
        result = new VmValidator(vm).vmNotLocked();
        if (!result.isValid()) {
            messages.add(result.getMessage().toString());
            return false;
        }
        result = getSnapshotValidator().vmNotDuringSnapshot(vm.getId());
        if (!result.isValid()) {
            messages.add(result.getMessage().toString());
            return false;
        }
        List<DiskImage> images = ImagesHandler.filterImageDisks(vmDisks, true, false);
        if (!images.isEmpty()) {
            result = new StoragePoolValidator(storagePool).isUp();
            if (!result.isValid()) {
                messages.add(result.getMessage().toString());
                return false;
            }
            if (!validateStorageDomains(vm, messages, isInternalExecution, images)) {
                return false;
            }
            if (!validateImagesForRunVm(messages, images)) {
                return false;
            }
            result = validateIsoPath(vm.isAutoStartup(), vm.getStoragePoolId(), diskPath, floppyPath);
            if (!result.isValid()) {
                messages.add(result.getMessage().toString());
                return false;
            }
            result = vmDuringInitialization(vm);
            if (!result.isValid()) {
                messages.add(result.getMessage().toString());
                return false;
            }
            if (!validateVdsStatus(vm, messages)) {
                return false;
            }
            result = validateStatelessVm(vm, vmDisks, runAsStateless);
            if (!result.isValid()) {
                messages.add(result.getMessage().toString());
                return false;
            }
        }
        if (!vdsSelector.canFindVdsToRunOn(messages, false)) {
            return false;
        }

        return true;
    }

}

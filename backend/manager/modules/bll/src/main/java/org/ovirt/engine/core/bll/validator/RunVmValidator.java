package org.ovirt.engine.core.bll.validator;

import java.util.Arrays;
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
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.customprop.ValidationError;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils;

public class RunVmValidator {

    public boolean validateVmProperties(VM vm, List<String> messages) {
        List<ValidationError> validationErrors =
                getVmPropertiesUtils().validateVMProperties(
                        vm.getVdsGroupCompatibilityVersion(),
                        vm.getStaticData());

        if (!validationErrors.isEmpty()) {
            VmPropertiesUtils.getInstance().handleCustomPropertiesError(validationErrors, messages);
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
        if (boot_sequence == BootSequence.C && vmDisks.isEmpty()) {
            return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK);
        }

        // If CD appears as first and there is no ISO in storage
        // pool/ISO inactive - you cannot run this VM
        if (boot_sequence == BootSequence.CD
                && getIsoDomainListSyncronizer().findActiveISODomain(storagePoolId) == null) {
            return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
        }

        // if there is network in the boot sequence, check that the
        // vm has network, otherwise the vm cannot be run in vdsm
        if (boot_sequence == BootSequence.N
                && getVmNicDao().getAllForVm(vm.getId()).isEmpty()) {
            return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK);
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
    public ValidationResult validateStorageDomains(VM vm, boolean isInternalExecution,
            List<DiskImage> vmImages) {
        if (!vm.isAutoStartup() || !isInternalExecution) {
            Set<Guid> storageDomainIds = ImagesHandler.getAllStorageIdsForImageIds(vmImages);
            MultipleStorageDomainsValidator storageDomainValidator =
                    new MultipleStorageDomainsValidator(vm.getStoragePoolId(), storageDomainIds);

            ValidationResult result = storageDomainValidator.allDomainsExistAndActive();
            if (!result.isValid()) {
                return result;
            }

            result = !vm.isAutoStartup() ? storageDomainValidator.allDomainsWithinThresholds()
                    : ValidationResult.VALID;
            if (!result.isValid()) {
                return result;
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Check isValid only if VM is not HA VM
     */
    public ValidationResult validateImagesForRunVm(VM vm, List<DiskImage> vmDisks) {
        return !vm.isAutoStartup() ?
                new DiskImagesValidator(vmDisks).diskImagesNotLocked() : ValidationResult.VALID;
    }

    public ValidationResult validateIsoPath(VM vm, String diskPath, String floppyPath) {
        if (vm.isAutoStartup() || (StringUtils.isEmpty(diskPath) && StringUtils.isEmpty(floppyPath))) {
            return ValidationResult.VALID;
        }

        Guid storageDomainId = getIsoDomainListSyncronizer().findActiveISODomain(vm.getStoragePoolId());
        if (storageDomainId == null) {
            return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
        }

        if (!StringUtils.isEmpty(diskPath) && !isRepoImageExists(diskPath, storageDomainId, ImageFileType.ISO)) {
            return new ValidationResult(VdcBllMessages.ERROR_CANNOT_FIND_ISO_IMAGE_PATH);
        }

        if (!StringUtils.isEmpty(floppyPath) && !isRepoImageExists(floppyPath, storageDomainId, ImageFileType.Floppy)) {
            return new ValidationResult(VdcBllMessages.ERROR_CANNOT_FIND_FLOPPY_IMAGE_PATH);
        }

        return ValidationResult.VALID;
    }

    @SuppressWarnings("unchecked")
    private boolean isRepoImageExists(String repoImagePath, Guid storageDomainId, ImageFileType imageFileType) {
        VdcQueryReturnValue ret = getBackend().runInternalQuery(
                VdcQueryType.GetImagesList,
                new GetImagesListParameters(storageDomainId, imageFileType));

        if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
            for (RepoImage isoFileMetaData : (List<RepoImage>) ret.getReturnValue()) {
                if (repoImagePath.equals(isoFileMetaData.getRepoImageId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public ValidationResult vmDuringInitialization(VM vm) {
        if (vm.isRunning() || vm.getStatus() == VMStatus.NotResponding ||
                isVmDuringInitiating(vm)) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }
        return ValidationResult.VALID;
    }

    protected boolean isVmDuringInitiating(VM vm) {
        return (Boolean) getBackend()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.IsVmDuringInitiating,
                        new IsVmDuringInitiatingVDSCommandParameters(vm.getId()))
                .getReturnValue();
    }

    public ValidationResult validateVdsStatus(VM vm) {
        if (vm.getStatus() == VMStatus.Paused && vm.getRunOnVds() != null) {
            VDS vds = getVdsDao().get(new Guid(vm.getRunOnVds().toString()));
            if (vds.getStatus() != VDSStatus.Up) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                        VdcBllMessages.VAR__HOST_STATUS__UP.toString());
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validateStatelessVm(VM vm, List<Disk> plugDisks, Boolean stateless) {
        // if the VM is not stateless, there is nothing to check
        if (stateless != null ? !stateless : !vm.isStateless()) {
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

    public ValidationResult validateVmStatusUsingMatrix(VM vm) {
        if (!VdcActionUtils.CanExecute(Arrays.asList(vm), VM.class,
                VdcActionType.RunVm)) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
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

    public ValidationResult validateStoragePoolUp(VM vm, StoragePool storagePool) {
        return !vm.isAutoStartup() ?
                new StoragePoolValidator(storagePool).isUp() : ValidationResult.VALID;
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

    protected VmNicDao getVmNicDao() {
        return DbFacade.getInstance().getVmNicDao();
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

    /**
     * A general method for run vm validations. used in runVmCommand and in VmPoolCommandBase
     * @param vm
     * @param messages
     * @param vmDisks
     * @param bootSequence
     * @param isInternalExecution
     * @param storagePool
     * @param diskPath
     * @param floppyPath
     * @param runAsStateless
     * @param vdsSelector
     * @param vdsBlackList
     *            - hosts that we already tried to run on
     * @return
     */
    public boolean canRunVm(VM vm,
            List<String> messages,
            List<Disk> vmDisks,
            BootSequence bootSequence,
            StoragePool storagePool,
            boolean isInternalExecution,
            String diskPath,
            String floppyPath,
            Boolean runAsStateless,
            List<Guid> vdsBlackList,
            Guid destVds,
            VDSGroup vdsGroup) {

        if (!validateVmProperties(vm, messages) ||
                !validate(validateBootSequence(vm, bootSequence, vmDisks), messages) ||
                !validate(new VmValidator(vm).vmNotLocked(), messages) ||
                !validate(getSnapshotValidator().vmNotDuringSnapshot(vm.getId()), messages) ||
                !validate(validateVmStatusUsingMatrix(vm), messages) ||
                !validate(validateIsoPath(vm, diskPath, floppyPath), messages)  ||
                !validate(vmDuringInitialization(vm), messages) ||
                !validate(validateVdsStatus(vm), messages) ||
                !validate(validateStatelessVm(vm, vmDisks, runAsStateless), messages)) {
            return false;
        }

        List<DiskImage> images = ImagesHandler.filterImageDisks(vmDisks, true, false);
        if (!images.isEmpty() && (
                !validate(validateStoragePoolUp(vm, storagePool), messages) ||
                !validate(validateStorageDomains(vm, isInternalExecution, images), messages) ||
                !validate(validateImagesForRunVm(vm, images), messages))) {
            return false;
        }

        if (!SchedulingManager.getInstance().canSchedule(
                vdsGroup, vm, vdsBlackList, null, destVds, messages)) {
            return false;
        }

        return true;
    }
}

package org.ovirt.engine.core.bll.validator;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.DiskHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleDiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class RunVmValidator {

    private VM vm;
    private RunVmParams runVmParam;
    private boolean isInternalExecution;
    private Guid activeIsoDomainId;

    private List<Disk> cachedVmDisks;
    private List<DiskImage> cachedVmImageDisks;
    private List<DiskImage> cachedVmMemoryDisks;
    private Set<String> cachedInterfaceNetworkNames;
    private List<Network> cachedClusterNetworks;
    private Set<String> cachedClusterNetworksNames;
    private Map<Disk, DiskVmElement> cachedVmDveMap;

    @Inject
    private SnapshotsValidator snapshotsValidator;

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    @Inject
    private VmValidationUtils vmValidationUtils;

    @Inject
    private DiskHandler diskHandler;

    @Inject
    private SchedulingManager schedulingManager;

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private VmDeviceDao vmDeviceDao;

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private VmNicDao vmNicDao;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private BackendInternal backend;

    @Inject
    private VDSBrokerFrontend resourceManager;

    public RunVmValidator(VM vm, RunVmParams rumVmParam, boolean isInternalExecution, Guid activeIsoDomainId) {
        this.vm = vm;
        this.runVmParam = rumVmParam;
        this.isInternalExecution = isInternalExecution;
        this.activeIsoDomainId = activeIsoDomainId;
    }

    /**
     * Used for testings
     */
    protected RunVmValidator() {
    }

    /**
     * A general method for run vm validations. used in runVmCommand and in VmPoolCommandBase
     *
     * @param vdsBlackList
     *            - hosts that we already tried to run on
     * @param vdsWhiteList
     *            - initial host list, mainly runOnSpecificHost (runOnce/migrateToHost)
     */
    public boolean canRunVm(List<String> messages, StoragePool storagePool,
            List<Guid> vdsBlackList,
            List<Guid> vdsWhiteList,
            Cluster cluster,
            boolean runInUnknownStatus) {

        if (vm.getOrigin() == OriginType.KUBEVIRT) {
            return true;
        }

        if (vm.getStatus() == VMStatus.Paused) {
            // if the VM is paused, we should only check the VDS status
            // as the rest of the checks were already checked before
            return validate(validateVdsStatus(vm), messages);
        }

        if (vm.getStatus() == VMStatus.Suspended) {
            return validate(new VmValidator(vm).vmNotLocked(), messages) &&
                   validate(snapshotsValidator.vmNotDuringSnapshot(vm.getId()), messages) &&
                   validate(validateVmStatusUsingMatrix(vm), messages) &&
                   validate(validateStoragePoolUp(vm, storagePool, getVmImageDisks()), messages) &&
                   validate(vmDuringInitialization(vm), messages) &&
                   validate(validateStorageDomains(vm, isInternalExecution, getVmImageDisks(), true), messages) &&
                   validate(validateStorageDomains(vm, isInternalExecution, getVmMemoryDisks(), false), messages) &&
                   validate(validateImagesForRunVm(vm, getVmImageDisks()), messages) &&
                   validate(validateDisksPassDiscard(vm), messages) &&
                   !schedulingManager.prepareCall(cluster)
                        .hostBlackList(vdsBlackList)
                        .hostWhiteList(vdsWhiteList)
                        .outputMessages(messages)
                        .canSchedule(vm).isEmpty();
        }

        return
                validateVmProperties(vm, messages) &&
                validate(validateBootSequence(vm, getVmDisks()), messages) &&
                validate(validateDisplayType(), messages) &&
                validate(new VmValidator(vm).vmNotLocked(), messages) &&
                validate(snapshotsValidator.vmNotDuringSnapshot(vm.getId()), messages) &&
                ((runInUnknownStatus && vm.getStatus() == VMStatus.Unknown) || validate(validateVmStatusUsingMatrix(vm), messages)) &&
                validate(validateStoragePoolUp(vm, storagePool, getVmImageDisks()), messages) &&
                validate(validateIsoPath(vm, runVmParam.getDiskPath(), runVmParam.getFloppyPath(), activeIsoDomainId), messages)  &&
                validate(vmDuringInitialization(vm), messages) &&
                validate(validateStatelessVm(vm, runVmParam.getRunAsStateless()), messages) &&
                validate(validateFloppy(), messages) &&
                validate(validateStorageDomains(vm, isInternalExecution, getVmImageDisks(), true), messages) &&
                validate(validateStorageDomains(vm, isInternalExecution, getVmMemoryDisks(), false), messages) &&
                validate(validateImagesForRunVm(vm, getVmImageDisks()), messages) &&
                validate(validateDisksPassDiscard(vm), messages) &&
                validate(validateMemorySize(vm), messages) &&
                validate(validateHostBlockDevicePath(vm), messages) &&
                !schedulingManager.prepareCall(cluster)
                        .hostBlackList(vdsBlackList)
                        .hostWhiteList(vdsWhiteList)
                        .outputMessages(messages)
                        .canSchedule(vm).isEmpty();
    }

    private List<DiskImage> filterReadOnlyAndPreallocatedDisks(List<DiskImage> vmImageDisks) {
        return vmImageDisks.stream()
                .filter(disk -> !(disk.getVolumeType() == VolumeType.Preallocated ||
                        getVmDiskVmElementMap().get(disk).isReadOnly()))
                .collect(Collectors.toList());
    }

    protected ValidationResult validateMemorySize(VM vm) {
        int maxSize = VmCommonUtils.maxMemorySizeWithHotplugInMb(vm);
        if (vm.getMemSizeMb() > maxSize) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MEMORY_EXCEEDS_SUPPORTED_LIMIT);
        }

        return ValidationResult.VALID;
    }

    private ValidationResult validateFloppy() {

        if (StringUtils.isNotEmpty(runVmParam.getFloppyPath())
                && !vmValidationUtils.isFloppySupported(vm.getOs(), vm.getCompatibilityVersion())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_FLOPPY_IS_NOT_SUPPORTED_BY_OS);
        }

        return ValidationResult.VALID;
    }

    /**
     * @return true if all VM network interfaces are valid
     */
    public ValidationResult validateNetworkInterfaces() {
        ValidationResult validationResult = validateInterfacesAttachedToClusterNetworks(getClusterNetworksNames(), getInterfaceNetworkNames());
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validationResult = validateInterfacesAttachedToVmNetworks(getClusterNetworks(), getInterfaceNetworkNames());
        if (!validationResult.isValid()) {
            return validationResult;
        }

        return ValidationResult.VALID;
    }

    /**
     * @return true if USB controllers are valid for the vm
     */
    public ValidationResult validateUsbDevices(VmBase vm) {
        if (vm.getUsbPolicy() == UsbPolicy.DISABLED) {
            final Collection<VmDevice> usbControllers = getVmDeviceUtils().getUsbControllers(vm.getId());
            final List<VmDevice> unmanagedControllers = usbControllers.stream().filter(d -> !d.isManaged()).collect(Collectors.toList());

            if (unmanagedControllers.size() > 1) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_USB_UNMANAGED_DEV_EXCEEDED_LIMIT);
            }
        }
        return ValidationResult.VALID;
    }

    private ValidationResult validateDisplayType() {
        if (!vmValidationUtils.isGraphicsAndDisplaySupported(vm.getOs(),
                vm.getCompatibilityVersion(),
                getVmActiveGraphics(),
                vm.getDefaultDisplayType())) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_VM_DISPLAY_TYPE_IS_NOT_SUPPORTED_BY_OS);
        }

        return ValidationResult.VALID;
    }

    private Set<GraphicsType> getVmActiveGraphics() {
        if (!vm.getGraphicsInfos().isEmpty()) { // graphics overriden in runonce
            return vm.getGraphicsInfos().keySet();
        } else {
            List<VmDevice> graphicDevices =
                    vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.GRAPHICS);

            Set<GraphicsType> graphicsTypes = new HashSet<>();

            for (VmDevice graphicDevice : graphicDevices) {
                GraphicsType type = GraphicsType.fromString(graphicDevice.getDevice());
                graphicsTypes.add(type);
            }

            return graphicsTypes;
        }
    }

    protected boolean validateVmProperties(VM vm, List<String> messages) {
        return getVmPropertiesUtils().validateVmProperties(
                        vm.getCompatibilityVersion(),
                        vm.getCustomProperties(),
                        messages);
    }

    protected ValidationResult validateBootSequence(VM vm, List<Disk> vmDisks) {
        BootSequence bootSequence = vm.getBootSequence();
        // Block from running a VM with no HDD when its first boot device is
        // HD and no other boot devices are configured
        if (bootSequence == BootSequence.C && vmDisks.isEmpty()) {
            return new ValidationResult(EngineMessage.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK);
        }

        // if there is network in the boot sequence, check that the
        // vm has network, otherwise the vm cannot be run in vdsm
        if (bootSequence == BootSequence.N && vmNicDao.getAllForVm(vm.getId()).isEmpty()) {
            return new ValidationResult(EngineMessage.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK);
        }

        return ValidationResult.VALID;
    }

    /**
     * Check storage domains. Storage domain status and disk space are checked only for non-HA VMs.
     *
     * @param vm
     *            The VM to run
     * @param isInternalExecution
     *            Command is internal?
     * @param validateThresholds
     *            Is the validation for storage domains thresholds needed
     * @param vmImages
     *            The VM's image disks
     * @return <code>true</code> if the VM can be run, <code>false</code> if not
     */
    private ValidationResult validateStorageDomains(VM vm, boolean isInternalExecution,
            List<DiskImage> vmImages, boolean validateThresholds) {
        if (vmImages.isEmpty()) {
            return ValidationResult.VALID;
        }

        if (!vm.isAutoStartup() || !isInternalExecution) {
            // In order to check the storage domains statuses we need a set of all the VM images
            Set<Guid> storageDomainIds = ImagesHandler.getAllStorageIdsForImageIds(vmImages);
            MultipleStorageDomainsValidator storageDomainValidator =
                    new MultipleStorageDomainsValidator(vm.getStoragePoolId(), storageDomainIds);

            ValidationResult result = storageDomainValidator.allDomainsExistAndActive();
            if (!result.isValid()) {
                return result;
            }

            if (validateThresholds) {
                // In order to check the storage domain thresholds we need a set of
                // non-preallocated and non read-only images
                Set<Guid> filteredStorageDomainIds =
                        ImagesHandler.getAllStorageIdsForImageIds(filterReadOnlyAndPreallocatedDisks(vmImages));
                MultipleStorageDomainsValidator filteredStorageDomainValidator =
                        new MultipleStorageDomainsValidator(vm.getStoragePoolId(), filteredStorageDomainIds);

                result = !vm.isAutoStartup() ? filteredStorageDomainValidator.allDomainsWithinThresholds()
                        : ValidationResult.VALID;
                if (!result.isValid()) {
                    return result;
                }
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Check isValid only if VM is not HA VM
     */
    private ValidationResult validateImagesForRunVm(VM vm, List<DiskImage> vmDisks) {
        if (vmDisks.isEmpty() || (vm.isAutoStartup() && isInternalExecution)) {
            return ValidationResult.VALID;
        }
        return new DiskImagesValidator(vmDisks).diskImagesNotLocked();
    }

    protected ValidationResult validateDisksPassDiscard(VM vm) {
        Map<Guid, Guid> diskIdToDestSdId = getVmDisks().stream()
                .collect(Collectors.toMap(Disk::getId,
                        disk -> disk.getDiskStorageType() == DiskStorageType.IMAGE ?
                                ((DiskImage) disk).getStorageIds().get(0) : Guid.Empty));

        MultipleDiskVmElementValidator multipleDiskVmElementValidator =
                createMultipleDiskVmElementValidator(getVmDiskVmElementMap());
        return multipleDiskVmElementValidator.isPassDiscardSupportedForDestSds(diskIdToDestSdId);
    }

    protected MultipleDiskVmElementValidator createMultipleDiskVmElementValidator(
            Map<Disk, DiskVmElement> diskToDiskVmElement) {
        return new MultipleDiskVmElementValidator(diskToDiskVmElement);
    }

    protected ValidationResult validateIsoPath(VM vm, String diskPath, String floppyPath, Guid activeIsoDomainId) {
        if (vm.isAutoStartup()) {
            return ValidationResult.VALID;
        }

        if (StringUtils.isEmpty(vm.getIsoPath()) && StringUtils.isEmpty(diskPath) && StringUtils.isEmpty(floppyPath)) {
            return ValidationResult.VALID;
        }

        if (!StringUtils.isEmpty(floppyPath) && activeIsoDomainId == null) {
            return new ValidationResult(EngineMessage.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
        }

        String effectiveIsoPath = StringUtils.isEmpty(diskPath) ? vm.getIsoPath() : diskPath;

        if (!StringUtils.isEmpty(effectiveIsoPath)) {
            if (effectiveIsoPath.matches(ValidationUtils.GUID)) {
                BaseDisk disk = diskDao.get(Guid.createGuidFromString(effectiveIsoPath));
                if (disk == null || disk.getContentType() != DiskContentType.ISO) {
                    return new ValidationResult(EngineMessage.ERROR_CANNOT_FIND_ISO_IMAGE_PATH);
                }
                ImageStatus imageStatus = ((DiskImage) disk).getImageStatus(); // ISO disks are always images
                if (imageStatus != ImageStatus.OK) {
                    return new ValidationResult(EngineMessage.ERROR_ISO_IMAGE_STATUS_ILLEGAL,
                            String.format("$status %s", imageStatus.toString()));
                }
                Guid domainId = ((DiskImage) disk).getStorageIds().get(0);
                StoragePoolIsoMap spim = storagePoolIsoMapDao.get(new StoragePoolIsoMapId(domainId, vm.getStoragePoolId()));
                if (spim == null || spim.getStatus() != StorageDomainStatus.Active) {
                    return new ValidationResult(EngineMessage.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
                }
            } else if (activeIsoDomainId == null) {
                return new ValidationResult(EngineMessage.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
            } else if (!isRepoImageExists(effectiveIsoPath, activeIsoDomainId, ImageFileType.ISO)) {
                return new ValidationResult(EngineMessage.ERROR_CANNOT_FIND_ISO_IMAGE_PATH);
            }

            return ValidationResult.VALID;
        }

        if (!StringUtils.isEmpty(floppyPath) && !isRepoImageExists(floppyPath, activeIsoDomainId, ImageFileType.Floppy)) {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_FIND_FLOPPY_IMAGE_PATH);
        }

        return ValidationResult.VALID;
    }

    protected ValidationResult vmDuringInitialization(VM vm) {
        if (vm.isRunning() || vm.getStatus() == VMStatus.NotResponding ||
                isVmDuringInitiating(vm)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }

        return ValidationResult.VALID;
    }

    private ValidationResult validateVdsStatus(VM vm) {
        if (vm.getStatus() == VMStatus.Paused && vm.getRunOnVds() != null &&
                getVdsDynamic(vm.getRunOnVds()).getStatus() != VDSStatus.Up) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                    EngineMessage.VAR__HOST_STATUS__UP.toString());
        }

        return ValidationResult.VALID;
    }

    protected ValidationResult validateStatelessVm(VM vm, Boolean stateless) {
        // if the VM is not stateless, there is nothing to check
        if (stateless != null ? !stateless : !vm.isStateless()) {
            return ValidationResult.VALID;
        }

        ValidationResult previewValidation = snapshotsValidator.vmNotInPreview(vm.getId());
        if (!previewValidation.isValid()) {
            return previewValidation;
        }

        // if the VM itself is stateless or run once as stateless
        if (vm.isAutoStartup()) {
            return new ValidationResult(EngineMessage.VM_CANNOT_RUN_STATELESS_HA);
        }

        ValidationResult hasSpaceValidation = hasSpaceForSnapshots();
        if (!hasSpaceValidation.isValid()) {
            return hasSpaceValidation;
        }
        return ValidationResult.VALID;
    }

    private ValidationResult validateVmStatusUsingMatrix(VM vm) {
        if (!ActionUtils.canExecute(Collections.singletonList(vm), VM.class,
                ActionType.RunVm)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(vm.getStatus()));
        }

        return ValidationResult.VALID;
    }


    /**
     * check that we can create snapshots for all disks
     * return true if all storage domains have enough space to create snapshots for this VM plugged disks
     */
    protected ValidationResult hasSpaceForSnapshots() {
        List<Disk> disks = diskDao.getAllForVm(vm.getId());
        List<DiskImage> allDisks = DisksFilter.filterImageDisks(disks, ONLY_SNAPABLE);

        Set<Guid> sdIds = ImagesHandler.getAllStorageIdsForImageIds(allDisks);

        MultipleStorageDomainsValidator msdValidator = getStorageDomainsValidator(sdIds);
        ValidationResult retVal = msdValidator.allDomainsWithinThresholds();
        if (retVal == ValidationResult.VALID) {
            return msdValidator.allDomainsHaveSpaceForNewDisks(allDisks);
        }
        return retVal;
    }

    private MultipleStorageDomainsValidator getStorageDomainsValidator(Collection<Guid> sdIds) {
        Guid spId = vm.getStoragePoolId();
        return new MultipleStorageDomainsValidator(spId, sdIds);
    }

    private ValidationResult validateStoragePoolUp(VM vm, StoragePool storagePool, List<DiskImage> vmImages) {
        if (vmImages.isEmpty() || (vm.isAutoStartup() && isInternalExecution)) {
            return ValidationResult.VALID;
        }
        return new StoragePoolValidator(storagePool).existsAndUp();
    }

    /**
     * @param clusterNetworkNames cluster logical networks names
     * @param interfaceNetworkNames VM interface network names
     * @return true if all VM network interfaces are attached to existing cluster networks
     */
    private ValidationResult validateInterfacesAttachedToClusterNetworks(
            final Set<String> clusterNetworkNames, final Set<String> interfaceNetworkNames) {

        Set<String> result = new HashSet<>(interfaceNetworkNames);
        result.removeAll(clusterNetworkNames);
        result.remove(null);

        // If after removing the cluster network names we still have objects, then we have interface on networks that
        // aren't attached to the cluster
        return result.isEmpty() ?
                ValidationResult.VALID
                : new ValidationResult(
                        EngineMessage.ACTION_TYPE_FAILED_NETWORK_NOT_IN_CLUSTER,
                        String.format("$networks %1$s", StringUtils.join(result, ",")));
    }

    /**
     * @param clusterNetworks
     *            cluster logical networks
     * @param interfaceNetworkNames
     *            VM interface network names
     * @return true if all VM network interfaces are attached to VM networks
     */
    private ValidationResult validateInterfacesAttachedToVmNetworks(final List<Network> clusterNetworks,
            Set<String> interfaceNetworkNames) {
        List<String> nonVmNetworkNames =
                NetworkUtils.filterNonVmNetworkNames(clusterNetworks, interfaceNetworkNames);

        return nonVmNetworkNames.isEmpty() ?
                ValidationResult.VALID
                : new ValidationResult(
                        EngineMessage.ACTION_TYPE_FAILED_NOT_A_VM_NETWORK,
                        String.format("$networks %1$s", StringUtils.join(nonVmNetworkNames, ",")));
    }

    private ValidationResult validateHostBlockDevicePath(VM vm) {
        String scsiHostdevProperty = getVmPropertiesUtils()
                .getVMProperties(vm.getCompatibilityVersion(), vm.getStaticData()).get("scsi_hostdev");
        if (scsiHostdevProperty == null || scsiHostdevProperty.equals("scsi_generic")) {
            return ValidationResult.VALID;
        }
        List<Guid> pinnedHostIds = vm.getDedicatedVmForVdsList();
        if (pinnedHostIds.isEmpty()) {
            return ValidationResult.VALID;
        }
        // single dedicated host allowed
        Guid hostId = pinnedHostIds.get(0);
        boolean missingPath = hostDeviceDao.getVmExtendedHostDevicesByVmId(vm.getId()).stream()
                .filter(h -> h.getHostId().equals(hostId))
                .filter(HostDevice::isScsi)
                .map(HostDevice::getBlockPath)
                .anyMatch(Objects::isNull);
        if (missingPath) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MISSING_HOST_BLOCK_DEVICE_PATH);
        }
        return ValidationResult.VALID;
    }

    ///////////////////////
    /// Utility methods ///
    ///////////////////////

    private boolean validate(ValidationResult validationResult, List<String> message) {
        if (!validationResult.isValid()) {
            message.addAll(validationResult.getMessagesAsStrings());
            message.addAll(validationResult.getVariableReplacements());
        }
        return validationResult.isValid();
    }

    protected VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    private boolean isRepoImageExists(String repoImagePath, Guid storageDomainId, ImageFileType imageFileType) {
        QueryReturnValue ret = backend.runInternalQuery(
                QueryType.GetImagesList,
                new GetImagesListParameters(storageDomainId, imageFileType));

        if (ret != null && ret.getReturnValue() != null && ret.getSucceeded()) {
            for (RepoImage isoFileMetaData : ret.<List<RepoImage>>getReturnValue()) {
                if (repoImagePath.equals(isoFileMetaData.getRepoImageId())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isVmDuringInitiating(VM vm) {
        return (Boolean) resourceManager
                .runVdsCommand(VDSCommandType.IsVmDuringInitiating,
                        new IsVmDuringInitiatingVDSCommandParameters(vm.getId()))
                .getReturnValue();
    }

    private VdsDynamic getVdsDynamic(Guid vdsId) {
        return vdsDynamicDao.get(vdsId);
    }

    protected List<Disk> getVmDisks() {
        if (cachedVmDisks == null) {
            cachedVmDisks = diskDao.getAllForVm(vm.getId(), true);
        }

        return cachedVmDisks;
    }

    protected Map<Disk, DiskVmElement> getVmDiskVmElementMap() {
        if (cachedVmDveMap == null) {
            Map<Guid, Disk> disksMap = getVmDisks().stream().collect(Collectors.toMap(Disk::getId, Function.identity()));
            cachedVmDveMap = diskHandler.getDiskToDiskVmElementMap(vm.getId(), disksMap);
        }

        return cachedVmDveMap;
    }

    private List<DiskImage> getVmImageDisks() {
        if (cachedVmImageDisks == null) {
            cachedVmImageDisks = DisksFilter.filterImageDisks(getVmDisks(), ONLY_NOT_SHAREABLE);
            cachedVmImageDisks.addAll(DisksFilter.filterCinderDisks(getVmDisks(), ONLY_PLUGGED));
            cachedVmImageDisks.addAll(DisksFilter.filterManagedBlockStorageDisks(getVmDisks(), ONLY_PLUGGED));
        }

        return cachedVmImageDisks;
    }

    private List<DiskImage> getVmMemoryDisks() {
        if (cachedVmMemoryDisks == null) {
            cachedVmMemoryDisks = new ArrayList<>();
            Snapshot activeSnapshot = snapshotDao.get(vm.getId(), Snapshot.SnapshotType.ACTIVE);
            DiskImage memoryDump = (DiskImage) diskDao.get(activeSnapshot.getMemoryDiskId());
            DiskImage metadata = (DiskImage) diskDao.get(activeSnapshot.getMetadataDiskId());
            if (memoryDump != null) {
                cachedVmMemoryDisks.add(memoryDump);
            }
            if (metadata != null) {
                cachedVmMemoryDisks.add(metadata);
            }
        }

        return cachedVmMemoryDisks;
    }

    private Set<String> getInterfaceNetworkNames() {
        if (cachedInterfaceNetworkNames == null) {
            cachedInterfaceNetworkNames =
                    vm.getInterfaces().stream().map(VmNetworkInterface::getNetworkName).collect(Collectors.toSet());
        }

        return cachedInterfaceNetworkNames;
    }

    private List<Network> getClusterNetworks() {
        if (cachedClusterNetworks == null) {
            cachedClusterNetworks =  networkDao.getAllForCluster(vm.getClusterId());
        }

        return cachedClusterNetworks;
    }

    private Set<String> getClusterNetworksNames() {
        if (cachedClusterNetworksNames == null) {
            cachedClusterNetworksNames = getClusterNetworks().stream().map(Network::getName).collect(Collectors.toSet());
        }

        return cachedClusterNetworksNames;
    }

    protected VmDeviceUtils getVmDeviceUtils() {
        return vmDeviceUtils;
    }
}

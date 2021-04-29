package org.ovirt.engine.core.bll.storage.disk.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMapId;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.PrepareImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.monitoring.FullListAdapter;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ImagesHandler {
    public static final String DISK = "_Disk";
    public static final String DefaultDriveName = "1";
    private static final Logger log = LoggerFactory.getLogger(ImagesHandler.class);

    @Inject
    private VDSBrokerFrontend resourceManager;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private BaseDiskDao baseDiskDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    @Inject
    private VmDeviceDao vmDeviceDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    private DiskLunMapDao diskLunMapDao;

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    @Inject
    private StorageHelperDirector storageHelperDirector;

    @Inject
    private ClusterUtils clusterUtils;

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    @Inject
    private FullListAdapter fullListAdapter;

    @Inject
    private DiskProfileHelper diskProfileHelper;

    @Inject
    private VmDao vmDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private VmCheckpointDao vmCheckpointDao;

    @Inject
    private MetadataDiskDescriptionHandler metadataDiskDescriptionHandler;

    /**
     * The following method will find all images and storages where they located for provide template and will fill an
     * diskInfoDestinationMap by imageId mapping on active storage id where image is located. The second map is
     * mapping of founded storage ids to storage object
     */
    public void fillImagesMapBasedOnTemplate(VmTemplate template,
            Map<Guid, DiskImage> diskInfoDestinationMap,
            Map<Guid, StorageDomain> destStorages) {
        List<StorageDomain> domains = storageDomainDao.getAllForStoragePool(template.getStoragePoolId());
        fillImagesMapBasedOnTemplate(template, domains, diskInfoDestinationMap, destStorages);
    }

    private static boolean isDomainValidDestination(StorageDomain storageDomain) {
        StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
        return validator.isDomainExistAndActive().isValid() && validator.domainIsValidDestination().isValid();
    }

    public static void fillImagesMapBasedOnTemplate(VmTemplate template,
            List<StorageDomain> domains,
            Map<Guid, DiskImage> diskInfoDestinationMap,
            Map<Guid, StorageDomain> destStorages) {
        Map<Guid, StorageDomain> storageDomainsMap = domains.stream().filter(ImagesHandler::isDomainValidDestination)
                .collect(Collectors.toMap(StorageDomain::getId, Function.identity()));

        for (DiskImage image : template.getDiskTemplateMap().values()) {
            for (Guid storageId : image.getStorageIds()) {
                if (storageDomainsMap.containsKey(storageId)) {
                    ArrayList<Guid> storageIds = new ArrayList<>();
                    storageIds.add(storageId);
                    image.setStorageIds(storageIds);
                    diskInfoDestinationMap.put(image.getId(), image);
                    break;
                }
            }
        }

        if (destStorages != null) {
            for (DiskImage diskImage : diskInfoDestinationMap.values()) {
                Guid storageDomainId = diskImage.getStorageIds().get(0);
                destStorages.put(storageDomainId, storageDomainsMap.get(storageDomainId));
            }
        }
    }

    public static boolean setDiskAlias(BaseDisk disk, VM vm) {
        return setDiskAlias(disk, vm, nullSafeGetCount(vm));
    }

    public static boolean setDiskAlias(BaseDisk disk, VM vm, int count) {
        if (disk == null) {
            log.error("Disk object is null");
            return false;
        }

        String vmName =  nullSafeGetVmName(vm);
        disk.setDiskAlias(getSuggestedDiskAlias(disk, vmName, count));
        return true;
    }

    private static String nullSafeGetVmName(VM vm) {
        return vm == null ? "" : vm.getName();
    }

    private static int nullSafeGetCount(VM vm) {
        return vm == null ? 1 : vm.getDiskMapCount() + 1;
    }

    /**
     * Suggests an alias for a disk.
     * If the disk does not already have an alias, one will be generated for it.
     * The generated alias will be formed as prefix_DiskXXX, where XXX is an ordinal.
     *
     * @param disk
     *            - The disk that (possibly) requires a new alias
     * @param diskPrefix
     *            - The prefix for the newly generated alias
     * @param count
     *            - The ordinal of disk to create an alias for (first, second, etc.).
     * @return The suggested alias
     */
    public static String getSuggestedDiskAlias(BaseDisk disk, String diskPrefix, int count) {
        String diskAlias;
        if (disk == null) {
            diskAlias = getDefaultDiskAlias(diskPrefix, DefaultDriveName);
            log.warn("Disk object is null, the suggested default disk alias to be used is '{}'",
                    diskAlias);
        } else {
            String defaultAlias = getDefaultDiskAlias(diskPrefix, String.valueOf(count));
            diskAlias = getDiskAliasWithDefault(disk, defaultAlias);
        }
        return diskAlias;
    }

    /**
     * Returns an alias for the given disk. If the disk already has an alias, it is returned. If not,
     * {@code aliasIfNull} is returned.
     *
     * @param disk
     *            The disk
     * @param aliasIfNull
     *            The alias to return if the disk does not have an alias
     * @return The alias in question
     */
    public static String getDiskAliasWithDefault(BaseDisk disk, String aliasIfNull) {
        String diskAlias = disk.getDiskAlias();
        if (StringUtils.isEmpty(diskAlias)) {
            log.info("Disk alias retrieved from the client is null or empty, the suggested default disk alias to be"
                            + " used is '{}'",
                    aliasIfNull);
            return aliasIfNull;
        }
        return diskAlias;
    }

    public static String getDefaultDiskAlias(String prefix, String suffix) {
        return prefix + DISK + suffix;
    }

    public static Map<Guid, List<DiskImage>> buildStorageToDiskMap(Collection<DiskImage> images,
            Map<Guid, DiskImage> diskInfoDestinationMap) {
        Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<>();
        for (DiskImage disk : images) {
            DiskImage diskImage = diskInfoDestinationMap.get(disk.getId());
            Guid storageDomainId = diskImage.getStorageIds().get(0);
            List<DiskImage> diskList = storageToDisksMap.get(storageDomainId);
            if (diskList == null) {
                diskList = new ArrayList<>();
                storageToDisksMap.put(storageDomainId, diskList);
            }
            diskList.add(disk);
        }
        return storageToDisksMap;
    }

    /**
     * Adds a disk image (Adds image, disk and relevant entities)
     *
     * @param image
     *            DiskImage to add
     * @param active
     *            true if the image should be added as active
     * @param imageStorageDomainMap
     *            storage domain map entry to map between the image and its storage domain
     */
    private void addDiskImage(DiskImage image, boolean active, ImageStorageDomainMap imageStorageDomainMap, Guid vmId) {
        try {
            addImage(image, active, imageStorageDomainMap);
            addDiskToVmIfNotExists(image, vmId);
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new EngineException(EngineError.DB, ex);
        }
    }

    /**
     * Gets a map of DiskImage IDs to DiskImage objects
     *
     * @param diskImages
     *            collection of DiskImage objects to create the map for
     * @return map object is the collection is not null
     */
    public static Map<Guid, DiskImage> getDiskImagesByIdMap(Collection<DiskImage> diskImages) {
        return diskImages.stream().collect(Collectors.toMap(d -> d.getImageId(), Function.identity()));
    }

    /**
     * Calculates the max size of required for cloned DiskImages with collapse.
     * The space should be calculated according to the volumes type and format.
     *
     *      | File Domain                             | Block Domain
     * -----|-----------------------------------------|-------------
     * qcow | preallocated : disk capacity            | min(used ,capacity)
     *      | sparse: min(used ,capacity)             |
     * -----|-----------------------------------------|-------------
     * raw  | preallocated: disk capacity             | disk capacity
     *      | sparse: min(used,capacity)              |
     *
     * */
    public static double getTotalActualSizeOfDisk(DiskImage diskImage, StorageDomainStatic storageDomain) {
        double sizeForDisk = diskImage.getSize();
        if ((storageDomain.getStorageType().isFileDomain() && diskImage.getVolumeType() == VolumeType.Sparse) ||
                storageDomain.getStorageType().isBlockDomain() && diskImage.getVolumeFormat() == VolumeFormat.COW) {
            double usedSpace = diskImage.getActualDiskWithSnapshotsSizeInBytes();
            sizeForDisk = Math.min(diskImage.getSize(), usedSpace);
        }

        return sizeForDisk;
    }

    public static boolean isImageInitialSizeSupported(StorageType storageType) {
        return storageType.isBlockDomain();
    }

    /**
     * Returns a list of image IDs for the specified DiskImages collection.
     *
     * @param diskImages collection of DiskImages
     * @return list of image IDs ordered by the order of the retrieved list.
     */
    public static List<Guid> getDiskImageIds(List<DiskImage> diskImages) {
        return diskImages.stream().map(DiskImage::getImageId).collect(Collectors.toList());
    }

    /**
     * Adds a disk image (Adds image, disk, and relevant entities , but not VmDevice) This may be useful for Clone VMs,
     * where besides adding images it is required to copy all vm devices (VmDeviceUtils.copyVmDevices) from the source
     * VM
     *
     * @param image
     *            image to add
     * @param active
     *            true if to add as active image
     * @param imageStorageDomainMap
     *            entry of image storagte domain map
     */
    private void addDiskImageWithNoVmDevice(DiskImage image,
            boolean active,
            ImageStorageDomainMap imageStorageDomainMap) {
        try {
            addImage(image, active, imageStorageDomainMap);
            addDisk(image);
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new EngineException(EngineError.DB, ex);
        }
    }

    /**
     * Adds a disk image (Adds image, disk, and relevant entities , but not VmDevice) This may be useful for Clone VMs,
     * where besides adding images it is required to copy all vm devices (VmDeviceUtils.copyVmDevices) from the source
     * VM.
     */
    public void addDiskImageWithNoVmDevice(DiskImage image) {
        addDiskImageWithNoVmDevice(image,
                image.getActive(),
                new ImageStorageDomainMap(image.getImageId(),
                        image.getStorageIds().get(0),
                        image.getQuotaId(),
                        image.getDiskProfileId()));
    }

    /**
     * Adds disk to a VM without creating a VmDevice entry
     *
     * @param disk
     *            disk to add
     */
    private void addDisk(BaseDisk disk) {
        if (!baseDiskDao.exists(disk.getId())) {
            baseDiskDao.save(disk);
        }
    }

    /**
     * Adds a disk image (Adds image with active flag according to the value in image, using the first storage domain in
     * the storage id as entry to the storage domain map)
     *
     * @param image
     *            DiskImage to add
     */
    public void addDiskImage(DiskImage image, Guid vmId) {
        addDiskImage(image, image.getActive(), new ImageStorageDomainMap(image.getImageId(), image.getStorageIds()
                .get(0), image.getQuotaId(), image.getDiskProfileId()), vmId);
    }

    /**
     * Add image and related entities to DB (Adds image, disk image dynamic and image storage domain map)
     *
     * @param image
     *            the image to add
     * @param active
     *            if true the image will be active
     * @param imageStorageDomainMap
     *            entry of mapping between the storage domain and the image
     */
    public void addImage(DiskImage image, boolean active, ImageStorageDomainMap imageStorageDomainMap) {
        image.setActive(active);
        imageDao.save(image.getImage());
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(image.getImageId());
        diskDynamic.setActualSize(image.getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);
        if (imageStorageDomainMap != null) {
            imageStorageDomainMapDao.save(imageStorageDomainMap);
        }
    }

    /**
     * Add disk if it does not exist to a given vm
     *
     * @param disk
     *            the disk to add
     * @param vmId
     *            the ID of the vm to add to if the disk does not exist for this VM
     */
    private void addDiskToVmIfNotExists(BaseDisk disk, Guid vmId) {
        if (!baseDiskDao.exists(disk.getId())) {
            addDiskToVm(disk, vmId);
        }
    }

    /**
     * Adds disk to vm
     *
     * @param disk
     *            the disk to add
     * @param vmId
     *            the ID of the VM to add to
     */
    public void addDiskToVm(BaseDisk disk, Guid vmId) {
        baseDiskDao.save(disk);
        if (disk.getDiskVmElementForVm(vmId) != null) {
            diskVmElementDao.save(disk.getDiskVmElementForVm(vmId));
        }
        vmDeviceUtils.addDiskDevice(vmId, disk.getId());
    }

    /**
     * The following method unify saving of image, it will be also saved with its storage
     * mapping.
     */
    public ImageStorageDomainMap saveImage(DiskImage diskImage) {
        imageDao.save(diskImage.getImage());
        ImageStorageDomainMap imageStorageDomainMap = new ImageStorageDomainMap(diskImage.getImageId(),
                diskImage.getStorageIds()
                        .get(0), diskImage.getQuotaId(), diskImage.getDiskProfileId());
        imageStorageDomainMapDao.save(imageStorageDomainMap);
        return imageStorageDomainMap;
    }

    public boolean isImagesExists(List<DiskImage> images, Guid storagePoolId) {
        return images.stream().allMatch(image -> isImageExist(storagePoolId, image) != null);
    }

    private DiskImage isImageExist(Guid storagePoolId, DiskImage image) {
        DiskImage fromIrs = null;
        Guid storageDomainId = image.getStorageIds().get(0);
        Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;
        try {
            fromIrs = (DiskImage) resourceManager
                    .runVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    image.getImageId())).getReturnValue();
        } catch (Exception e) {
            log.debug("Unable to get image info from storage", e);
        }
        return fromIrs;
    }

    public static boolean checkImageConfiguration(StorageDomainStatic storageDomain,
            DiskImageBase diskInfo, List<String> messages) {
        if (!checkImageConfiguration(storageDomain, diskInfo.getVolumeType(), diskInfo.getVolumeFormat(), diskInfo.getBackup())) {
            // not supported
            messages.add(EngineMessage.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED.toString());
            messages.add(String.format("$%1$s %2$s", "volumeFormat", diskInfo.getVolumeFormat()));
            messages.add(String.format("$%1$s %2$s", "volumeType", diskInfo.getVolumeType()));
            messages.add(String.format("$%1$s %2$s", "backup", diskInfo.getBackup()));
            return false;
        }
        return true;
    }

    private static boolean checkImageConfiguration(StorageDomainStatic storageDomain, VolumeType volumeType, VolumeFormat volumeFormat, DiskBackup diskBackup) {
        return !((volumeType == VolumeType.Preallocated && volumeFormat == VolumeFormat.COW && diskBackup != DiskBackup.Incremental)
                || (volumeFormat == VolumeFormat.RAW && diskBackup == DiskBackup.Incremental)
                || (storageDomain.getStorageType().isBlockDomain() && volumeType == VolumeType.Sparse && volumeFormat == VolumeFormat.RAW)
                || volumeFormat == VolumeFormat.Unassigned
                || volumeType == VolumeType.Unassigned);
    }

    /**
     * @param images The images to get the storage domain IDs for
     * @return A unique {@link Set} of all the storage domain IDs relevant to all the given images
     */
    public static Set<Guid> getAllStorageIdsForImageIds(Collection<DiskImage> images) {
        return images.stream().flatMap(image -> image.getStorageIds().stream()).collect(Collectors.toSet());
    }

    public void fillImagesBySnapshots(VM vm) {
        vm.getDiskMap().values().stream().filter(disk -> disk.getDiskStorageType().isInternal()).forEach(disk -> {
            DiskImage diskImage = (DiskImage) disk;
            diskImage.getSnapshots().addAll(diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId()));
        });
    }

    /**
     * Gets a List of DiskImage objects, which include active and non-active (snapshots) disks.
     * Aggregates the snapshot for each Disk.
     *
     * @param images
     *            List of Disk objects to aggregate their snapshots
     * @return List of active DiskImages objects which related to their snapshots
     */
    public Collection<DiskImage> aggregateDiskImagesSnapshots(Collection<DiskImage> images){
        Map<Guid, DiskImage> diskImagesMap = new HashMap<>();

        // Get active diskImages
        images.forEach(diskImage -> {
            if (diskImage.getActive()) {
                diskImage.getSnapshots().add(DiskImage.copyOf(diskImage));
                diskImagesMap.put(diskImage.getId(), diskImage);
            }
        });

        // Update diskImages snapshots
        images.forEach(diskImage -> {
            if (!diskImage.getActive()) {
                DiskImage activeImage = diskImagesMap.get(diskImage.getId());
                if (activeImage != null) {
                    activeImage.getSnapshots().add(diskImage);
                }
            }
        });

        return diskImagesMap.values();
    }

    public void removeDiskImage(DiskImage diskImage, Guid vmId) {
        try {
            removeDiskFromVm(vmId, diskImage.getId());
            removeImage(diskImage);
        } catch (RuntimeException ex) {
            log.error("Failed to remove disk image and related entities from db: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new EngineException(EngineError.DB, ex);
        }
    }

    public void removeLunDisk(LunDisk lunDisk) {
        vmDeviceDao.remove(new VmDeviceId(lunDisk.getId(), null));
        LUNs lun = lunDisk.getLun();
        diskLunMapDao.remove(new DiskLunMapId(lunDisk.getId(), lun.getLUNId()));
        baseDiskDao.remove(lunDisk.getId());

        lun.setLunConnections(storageServerConnectionDao.getAllForLun(lun.getLUNId()));

        if (!lun.getLunConnections().isEmpty()) {
            storageHelperDirector.getItem(
                    lun.getLunConnections().get(0).getStorageType()).removeLun(lun);
        } else {
            // if there are no connections then the lun is fcp.
            storageHelperDirector.getItem(StorageType.FCP).removeLun(lun);
        }

    }

    // the last image in each list is the leaf
    public static Map<Guid, List<DiskImage>> getImagesLeaf(List<DiskImage> images) {
        Map<Guid, List<DiskImage>> retVal = images.stream().collect(Collectors.groupingBy(DiskImage::getId));
        retVal.values().forEach(ImagesHandler::sortImageList);
        return retVal;
    }

    public static void sortImageList(List<DiskImage> images) {
        List<DiskImage> hold = new ArrayList<>();
        DiskImage curr = null;

        // find the first image
        for (int i = 0; i < images.size(); i++) {
            int pos = getFirstImage(images, images.get(i));
            if (pos == -1) {
                curr = images.get(i);
                hold.add(images.get(i));
                images.remove(images.get(i));
                break;
            }
        }

        while (images.size() > 0) {
            int pos = getNextImage(images, curr);
            if (pos == -1) {
                log.error("Image list error in SortImageList");
                break;
            }
            curr = images.get(pos);
            hold.add(images.get(pos));
            images.remove(images.get(pos));
        }

        images.addAll(hold);
    }

    // function return the index of image that is its child
    private static int getNextImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getParentId().equals(curr.getImageId())) {
                return i;
            }
        }
        return -1;
    }

    // function return the index of the image that has no parent
    private static int getFirstImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getImageId())) {
                return i;
            }
        }
        return -1;
    }

    private void removeImage(DiskImage diskImage) {
        imageStorageDomainMapDao.remove(diskImage.getImageId());
        diskImageDynamicDao.remove(diskImage.getImageId());
        imageDao.remove(diskImage.getImageId());
    }

    private void removeDiskFromVm(Guid vmGuid, Guid diskId) {
        vmDeviceDao.remove(new VmDeviceId(diskId, vmGuid));
        baseDiskDao.remove(diskId);
    }

    public void updateImageStatus(Guid imageId, ImageStatus imageStatus) {
        imageDao.updateStatus(imageId, imageStatus);
    }

    public void updateAllDiskImageSnapshotsStatusWithCompensation(final Guid diskId,
            final ImageStatus status,
            ImageStatus statusForCompensation,
            final CompensationContext compensationContext) {
        updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(Collections.singletonList(diskId), status, statusForCompensation, compensationContext);
    }

    public DiskImage getSnapshotLeaf(Guid diskId) {
        List<DiskImage> diskSnapshots = diskImageDao.getAllSnapshotsForImageGroup(diskId);
        sortImageList(diskSnapshots);
        return diskSnapshots.get(diskSnapshots.size() - 1);
    }

    public List<DiskImage> getCinderLeafImages(List<Disk> disks) {
        return disks.stream().filter(DisksFilter.ONLY_CINDER).map(d -> getSnapshotLeaf(d.getId())).collect(Collectors.toList());
    }

    public List<DiskImage> getManagedBlockStorageSnapshots(List<Disk> disks) {
        return disks.stream().filter(DisksFilter.ONLY_MANAGED_BLOCK_STORAGE).map(d -> getSnapshotLeaf(d.getId())).collect(Collectors.toList());
    }

    public void updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(final Collection<Guid> diskIds,
                                                                         final ImageStatus status,
                                                                         ImageStatus statusForCompensation,
                                                                         final CompensationContext compensationContext) {
        if (compensationContext != null) {
            diskIds.stream()
                    .flatMap(diskId -> diskImageDao.getAllSnapshotsForImageGroup(diskId).stream())
                    .forEach(diskSnapshot -> {
                        diskSnapshot.setImageStatus(statusForCompensation);
                        compensationContext.snapshotEntityStatus(diskSnapshot.getImage());
                    });

            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                diskIds.forEach(diskId -> imageDao.updateStatusOfImagesByImageGroupId(diskId, status));
                compensationContext.stateChanged();
                return null;
            });
        } else {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                diskIds.forEach(diskId -> imageDao.updateStatusOfImagesByImageGroupId(diskId, status));
                return null;
            });
        }
    }

    private static DiskImage getDiskImageById(Guid id, Collection<DiskImage> diskImages) {
        return diskImages.stream().filter(disk -> disk.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Returns the subtraction set of the specified image lists (based on images' IDs)
     * @param images full list
     * @param imagesToSubtract images to subtract list
     * @return the subtraction set
     */
    public static List<DiskImage> imagesSubtract(Collection<DiskImage> images, Collection<DiskImage> imagesToSubtract) {
        return images
                .stream()
                .filter(image -> getDiskImageById(image.getId(), imagesToSubtract) == null).collect(Collectors.toList());
    }

    /**
     * Returns the intersection set of the specified image lists (based on images' IDs)
     * @param images1 1st list
     * @param images2 2nd list
     * @return the intersection set
     */
    public static List<DiskImage> imagesIntersection(Collection<DiskImage> images1, Collection<DiskImage> images2) {
        return images1
                .stream()
                .filter(image -> getDiskImageById(image.getId(), images2) != null).collect(Collectors.toList());
    }

    /**
     * Prepare a single {@link org.ovirt.engine.core.common.businessentities.Snapshot} object representing a snapshot of a given VM without the given disk.
     */
    public Snapshot prepareSnapshotConfigWithoutImageSingleImage(Snapshot snapshot, Guid imageId, OvfManager ovfManager) {
        return prepareSnapshotConfigWithAlternateImage(snapshot, imageId, null, ovfManager);
    }

    /**
     * Prepare a single {@link org.ovirt.engine.core.common.businessentities.Snapshot} object representing a snapshot of a given VM without the given disk,
     * substituting a new disk in its place if a new disk is provided to the method.
     */
    public Snapshot prepareSnapshotConfigWithAlternateImage(Snapshot snapshot, Guid oldImageId, DiskImage newImage, OvfManager ovfManager) {
        if (snapshot == null) {
            return null;
        }
        try {
            String snapConfig = snapshot.getVmConfiguration();

            if (snapshot.isVmConfigurationAvailable() && snapConfig != null) {
                VM vmSnapshot = new VM();
                FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vmSnapshot);
                ovfManager.importVm(snapConfig, vmSnapshot, fullEntityOvfData);

                // Remove the image from the disk list
                Iterator<DiskImage> diskIter = fullEntityOvfData.getDiskImages().iterator();
                while (diskIter.hasNext()) {
                    DiskImage imageInList = diskIter.next();
                    if (imageInList.getImageId().equals(oldImageId)) {
                        log.debug("Recreating vmSnapshot '{}' without the image '{}'", snapshot.getId(), oldImageId);
                        diskIter.remove();
                        break;
                    }
                }

                if (newImage != null) {
                    log.debug("Adding image '{}' to vmSnapshot '{}'", newImage.getImageId(), snapshot.getId());
                    newImage.setDiskVmElements(Collections.singletonList(diskVmElementDao.get(new VmDeviceId(newImage.getId(), vmSnapshot.getId()))));
                    fullEntityOvfData.getDiskImages().add(newImage);
                }

                Version compatibilityVersion =
                        Optional.ofNullable(vmSnapshot.getStaticData().getClusterCompatibilityVersionOrigin())
                        .orElse(Version.getLowest());
                compatibilityVersion = compatibilityVersion.less(Version.getLowest()) ? Version.getLowest() : compatibilityVersion;

                FullEntityOvfData fullEntityOvfDataForExport = new FullEntityOvfData(vmSnapshot);
                fullEntityOvfDataForExport.setDiskImages(fullEntityOvfData.getDiskImages());
                String newOvf =
                        ovfManager.exportVm(vmSnapshot, fullEntityOvfDataForExport, compatibilityVersion);
                snapshot.setVmConfiguration(newOvf);
            }
        } catch (OvfReaderException e) {
            log.error("Can't remove image '{}' from snapshot '{}'", oldImageId, snapshot.getId());
        }
        return snapshot;
    }

    public DiskImage createDiskImageWithExcessData(DiskImage diskImage, Guid sdId) {
        DiskImage dummy = DiskImage.copyOf(diskImage);
        dummy.setStorageIds(new ArrayList<>(Collections.singletonList(sdId)));
        dummy.getSnapshots().addAll(diskImageDao.getAllSnapshotsForLeaf(dummy.getImageId()));
        return dummy;
    }

    /**
     * This method is used for storage allocation validations, where the disks are the template's,
     * which could have another volume type/format than the target disk volume type/format, which is not yet created.
     * "Real" override for these values is done in CreateSnapshotCommand, when creating the new DiskImages.
     */
    public static List<DiskImage> getDisksDummiesForStorageAllocations(Collection<DiskImage> originalDisks) {
        List<DiskImage> diskDummies = new ArrayList<>(originalDisks.size());
        for (DiskImage diskImage : originalDisks) {
            DiskImage clone = DiskImage.copyOf(diskImage);
            clone.setVolumeType(VolumeType.Sparse);
            clone.setVolumeFormat(VolumeFormat.COW);
            diskDummies.add(clone);
        }
        return diskDummies;
    }

    public List<DiskImage> getSnapshotsDummiesForStorageAllocations(Collection<DiskImage> originalDisks) {
        List<DiskImage> diskDummies = new ArrayList<>();
        for (DiskImage snapshot : originalDisks) {
            DiskImage clone = DiskImage.copyOf(snapshot);
            // Add the child snapshot into which the deleted snapshot is going to be merged to the
            // DiskImage for StorageDomainValidator to handle
            List<DiskImage> snapshots = diskImageDao.getAllSnapshotsForParent(clone.getImageId());
            clone.getSnapshots().clear();
            clone.getSnapshots().add(clone); // Add the clone itself since snapshots should contain the entire chain.
            clone.getSnapshots().addAll(snapshots);
            diskDummies.add(clone);
        }
        return diskDummies;
    }

    public DiskImage getVolumeInfoFromVdsm(Guid storagePoolId, Guid newStorageDomainID, Guid newImageGroupId,
                                      Guid newImageId) {
        return (DiskImage) vdsCommandsHelper.runVdsCommandWithFailover(
                VDSCommandType.GetVolumeInfo,
                new GetVolumeInfoVDSCommandParameters(storagePoolId, newStorageDomainID, newImageGroupId,
                        newImageId), storagePoolId, null).getReturnValue();
    }

    public QemuImageInfo getQemuImageInfoFromVdsm(Guid storagePoolId,
            Guid newStorageDomainID,
            Guid newImageGroupId,
            Guid newImageId,
            Guid vdsId,
            boolean shouldPrepareAndTeardown) {
        if (vdsId == null) {
            vdsId = vdsCommandsHelper.getHostForExecution(storagePoolId);
        }
        QemuImageInfo qemuImageInfo = null;
        if (shouldPrepareAndTeardown) {
            prepareImage(storagePoolId, newStorageDomainID, newImageGroupId, newImageId, vdsId);
        }
        try {
            qemuImageInfo = (QemuImageInfo) resourceManager
                    .runVdsCommand(VDSCommandType.GetQemuImageInfo,
                            new GetVolumeInfoVDSCommandParameters(vdsId,
                                    storagePoolId,
                                    newStorageDomainID,
                                    newImageGroupId,
                                    newImageId)).getReturnValue();
        } catch (Exception e) {
            log.error("Unable to get qemu image info from storage", e);
        } finally {
            if (shouldPrepareAndTeardown) {
                try {
                    teardownImage(storagePoolId, newStorageDomainID, newImageGroupId, newImageId, vdsId);
                } catch (Exception e) {
                    log.warn("Unable to tear down image", e);
                }
            }
        }
        return qemuImageInfo;
    }

    public DiskImage cloneDiskImage(Guid storageDomainId,
            Guid newImageGroupId,
            Guid newImageGuid,
            DiskImage srcDiskImage,
            Guid diskProfileId,
            Guid snapshotId,
            DiskImage diskImageFromClient) {

        DiskImage clonedDiskImage = DiskImage.copyOf(srcDiskImage);
        clonedDiskImage.setImageId(newImageGuid);
        clonedDiskImage.setParentId(Guid.Empty);
        clonedDiskImage.setImageTemplateId(Guid.Empty);
        clonedDiskImage.setVmSnapshotId(snapshotId);
        clonedDiskImage.setId(newImageGroupId);
        clonedDiskImage.setLastModifiedDate(new Date());
        clonedDiskImage.setVolumeFormat(srcDiskImage.getVolumeFormat());
        clonedDiskImage.setVolumeType(srcDiskImage.getVolumeType());
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(storageDomainId);
        clonedDiskImage.setStorageIds(storageIds);
        clonedDiskImage.setDiskProfileId(diskProfileId);

        // If volume information was changed at client , use its volume information.
        // If volume information was not changed at client - use the volume information of the ancestral image
        if (diskImageFromClient != null) {
            if (volumeInfoChanged(diskImageFromClient, srcDiskImage)) {
                changeVolumeInfo(clonedDiskImage, diskImageFromClient);
            } else {
                DiskImage ancestorDiskImage = diskImageDao.getAncestor(srcDiskImage.getImageId());
                changeVolumeInfo(clonedDiskImage, ancestorDiskImage);
            }
        } else {
            DiskImage ancestorDiskImage = diskImageDao.getAncestor(srcDiskImage.getImageId());
            changeVolumeInfo(clonedDiskImage, ancestorDiskImage);
        }

        return clonedDiskImage;
    }

    private static boolean volumeInfoChanged(DiskImage diskImageFromClient, DiskImage srcDiskImage) {
        return diskImageFromClient.getVolumeFormat() != srcDiskImage.getVolumeFormat() || diskImageFromClient.getVolumeType() != srcDiskImage.getVolumeType();
    }

    private static void changeVolumeInfo(DiskImage clonedDiskImage, DiskImage diskImageFromClient) {
        clonedDiskImage.setVolumeFormat(diskImageFromClient.getVolumeFormat());
        clonedDiskImage.setVolumeType(diskImageFromClient.getVolumeType());
    }

    public static long computeCowImageNeededSize(VolumeFormat sourceFormat, long actualSize) {
        // When vdsm creates a COW volume with provided initial size the size is multiplied by 1.1 to prevent a
        // case in which we won't have enough space. If the source is already COW we don't need the additional
        // space.
        return sourceFormat == VolumeFormat.COW
                ? Double.valueOf(Math.ceil(actualSize / StorageConstants.QCOW_OVERHEAD_FACTOR)).longValue()
                : actualSize;
    }

    /**
     * This method is use to compute the initial size of a disk image based on the source disk image size,
     * including all the existing snapshots.
     * It is needed when copying/moving an existing image with collapse in order to create the destination disk image
     * with the right size from the beginning, saving the process of extending the allocation.
     *
     * @param sourceImage The source disk image
     * @param destFormat The volume format of the destination image (COW/RAW)
     * @param srcDomain The storage domain where the disk image is copied from
     * @param dstDomain The storage domain where the disk image will be copied to
     * @return the computed initial size in bytes or null if it is not needed/supported
     */
    public Long determineTotalImageInitialSize(DiskImage sourceImage,
            VolumeFormat destFormat,
            Guid srcDomain,
            Guid dstDomain) {

        if (isInitialSizeSupportedForFormat(destFormat, dstDomain)) {

            double totalSizeForClonedDisk = getTotalActualSizeOfDisk(sourceImage,
            storageDomainDao.get(srcDomain).getStorageStaticData());

            return computeCowImageNeededSize(sourceImage.getVolumeFormat(), Double.valueOf(totalSizeForClonedDisk).longValue());
        }
        return null;
    }

    public boolean isInitialSizeSupportedForFormat(VolumeFormat destFormat, Guid dstDomain) {
        return destFormat == VolumeFormat.COW &&
                isImageInitialSizeSupported(storageDomainDao.get(dstDomain).getStorageType());
    }

    public VDSReturnValue prepareImage(Guid storagePoolId,
                                    Guid newStorageDomainID,
                                    Guid newImageGroupId,
                                    Guid newImageId,
                                    Guid vdsId) {
        return resourceManager.runVdsCommand(VDSCommandType.PrepareImage, new PrepareImageVDSCommandParameters(vdsId,
                storagePoolId,
                newStorageDomainID,
                newImageGroupId,
                newImageId,
                true));
    }

    public VDSReturnValue teardownImage(Guid storagePoolId,
                                     Guid newStorageDomainID,
                                     Guid newImageGroupId,
                                     Guid newImageId,
                                     Guid vdsId) {
        return resourceManager.runVdsCommand(VDSCommandType.TeardownImage,
                new ImageActionsVDSCommandParameters(vdsId,
                        storagePoolId,
                        newStorageDomainID,
                        newImageGroupId,
                        newImageId));
    }

    public Set<Guid> getVolumeChain(Guid vmId, Guid vdsId, DiskImage activeImage) {
        Map[] vms = null;
        try {
            vms = getVms(vdsId, vmId);
        } catch (EngineException e) {
            log.error("Failed to retrieve images list of VM {}.", vmId, e);
        }

        if (vms == null || vms.length == 0) {
            log.error("Failed to retrieve VM information");
            return null;
        }

        Map vm = vms[0];
        if (vm == null || vm.get(VdsProperties.vm_guid) == null) {
            log.error("Received incomplete VM information");
            return null;
        }

        Guid receivedVmId = new Guid((String) vm.get(VdsProperties.vm_guid));
        if (!receivedVmId.equals(vmId)) {
            log.error("Invalid VM returned when querying status: expected '{}', got '{}'",
                    vmId, receivedVmId);
            return null;
        }

        Set<Guid> images = new HashSet<>();
        for (Object o : (Object[]) vm.get(VdsProperties.Devices)) {
            Map device = (Map<String, Object>) o;
            if (VmDeviceType.DISK.getName().equals(device.get(VdsProperties.Device))
                    && !device.get(VdsProperties.ImageId).equals("mapper")
                    && activeImage.getId().equals(Guid.createGuidFromString(
                    (String) device.get(VdsProperties.ImageId)))) {
                Object[] volumeChain = (Object[]) device.get(VdsProperties.VolumeChain);
                for (Object v : volumeChain) {
                    Map<String, Object> volume = (Map<String, Object>) v;
                    images.add(Guid.createGuidFromString((String) volume.get(VdsProperties.VolumeId)));
                }
                break;
            }
        }
        return images;
    }

    private Map[] getVms(Guid vdsId, Guid vmId) {
        return (Map[]) fullListAdapter.getVmFullList(
                vdsId,
                Collections.singletonList(vmId),
                true)
                .getReturnValue();
    }

    public Map<DiskImage, DiskImage> mapChainToNewIDs(Guid sourceImageGroupID,
            Guid newImageGroupID,
            Guid targetStorageDomainID,
            DbUser user) {
        List<DiskImage> oldChain = diskImageDao.getAllSnapshotsForImageGroup(sourceImageGroupID);
        Map<DiskImage, DiskImage> oldToNewChain = new HashMap<>(oldChain.size());
        sortImageList(oldChain);
        Guid nextParentId = oldChain.get(0).getImageTemplateId() != Guid.Empty ? oldChain.get(0).getParentId() : Guid.Empty;

        for (DiskImage diskImage : oldChain) {
            DiskImage newImage = DiskImage.copyOf(diskImage);
            newImage.setParentId(nextParentId);
            newImage.setId(newImageGroupID);
            newImage.setStorageIds(Arrays.asList(targetStorageDomainID));
            nextParentId = Guid.newGuid();
            newImage.setImageId(nextParentId);
            newImage.setVmSnapshotId(null);
            diskProfileHelper.setAndValidateDiskProfiles(Map.of(newImage, targetStorageDomainID), user);
            oldToNewChain.put(diskImage, newImage);
        }

        return oldToNewChain;
    }

    public Guid getHostForMeasurement(Guid storagePoolID, Guid imageGroupID) {
        Map<Boolean, List<VM>> vms = vmDao.getForDisk(imageGroupID, true);
        if (vms != null && !vms.computeIfAbsent(Boolean.TRUE, b -> new ArrayList<>()).isEmpty()) {
            Optional<VM> runningVM = vms.get(Boolean.TRUE)
                    .stream()
                    .filter(VM::isRunning)
                    .findAny();
            if (runningVM.isPresent()) {
                Guid hostId = runningVM.get().getRunOnVds();
                return FeatureSupported.isMeasureVolumeSupported(vdsDao.get(hostId)) ? hostId : null;
            }
        }

        return vdsCommandsHelper.getHostForExecution(storagePoolID,
                vds -> FeatureSupported.isMeasureVolumeSupported(vds));
    }

    public Version getSpmCompatibilityVersion(Guid storagePoolId) {
        StoragePool storagePool = storagePoolDao.get(storagePoolId);
        VDS vds = vdsDao.get(storagePool.getSpmVdsId());
        return vds.getClusterCompatibilityVersion();
    }

    public String getJsonDiskDescription(Disk disk) {
        try {
            return metadataDiskDescriptionHandler.generateJsonDiskDescription(disk);
        } catch (IOException e) {
            log.error("Exception while generating json for disk. ERROR: '{}'", e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    public boolean shouldUseDiskBitmaps(Version version, DiskImage diskImage) {
        return FeatureSupported.isBackupModeAndBitmapsOperationsSupported(version) &&
                vmCheckpointDao.isDiskIncludedInCheckpoint(diskImage.getId()) &&
                diskImage.isQcowFormat();
    }
}

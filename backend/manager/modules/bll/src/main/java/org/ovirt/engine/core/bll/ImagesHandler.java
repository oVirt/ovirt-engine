package org.ovirt.engine.core.bll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.storage.StorageHelperDirector;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DiskLunMapId;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsAndPoolIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImagesHandler {
    public static final String DISK = "_Disk";
    public static final String DISK_ALIAS = "DiskAlias";
    public static final String DISK_DESCRIPTION = "DiskDescription";
    public static final String DefaultDriveName = "1";
    private static final Logger log = LoggerFactory.getLogger(ImagesHandler.class);

    /**
     * The following method will find all images and storages where they located for provide template and will fill an
     * diskInfoDestinationMap by imageId mapping on active storage id where image is located. The second map is
     * mapping of founded storage ids to storage object
     * @param template
     * @param diskInfoDestinationMap
     * @param destStorages
     */
    public static void fillImagesMapBasedOnTemplate(VmTemplate template,
            Map<Guid, DiskImage> diskInfoDestinationMap,
            Map<Guid, StorageDomain> destStorages) {
        List<StorageDomain> domains =
                DbFacade.getInstance()
                        .getStorageDomainDao()
                        .getAllForStoragePool(template.getStoragePoolId());
        fillImagesMapBasedOnTemplate(template, domains, diskInfoDestinationMap, destStorages);
    }

    public static void fillImagesMapBasedOnTemplate(VmTemplate template,
            List<StorageDomain> domains,
            Map<Guid, DiskImage> diskInfoDestinationMap,
            Map<Guid, StorageDomain> destStorages) {
        Map<Guid, StorageDomain> storageDomainsMap = new HashMap<Guid, StorageDomain>();
        for (StorageDomain storageDomain : domains) {
            StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
            if (validator.isDomainExistAndActive().isValid() && validator.domainIsValidDestination().isValid()) {
                storageDomainsMap.put(storageDomain.getId(), storageDomain);
            }
        }
        for (DiskImage image : template.getDiskTemplateMap().values()) {
            for (Guid storageId : image.getStorageIds()) {
                if (storageDomainsMap.containsKey(storageId)) {
                    ArrayList<Guid> storageIds = new ArrayList<Guid>();
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
     * {@link #aliasIfNull} is returned.
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
        Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage disk : images) {
            DiskImage diskImage = diskInfoDestinationMap.get(disk.getId());
            Guid storageDomainId = diskImage.getStorageIds().get(0);
            List<DiskImage> diskList = storageToDisksMap.get(storageDomainId);
            if (diskList == null) {
                diskList = new ArrayList<DiskImage>();
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
    public static void addDiskImage(DiskImage image, boolean active, image_storage_domain_map imageStorageDomainMap, Guid vmId) {
        try {
            addImage(image, active, imageStorageDomainMap);
            addDiskToVmIfNotExists(image, vmId);
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
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
        Map<Guid, DiskImage> result = new HashMap<>();
        if (diskImages != null) {
            for (DiskImage diskImage : diskImages) {
                result.put(diskImage.getImageId(), diskImage);
            }
        }
        return result;
    }

    /**
     * Returns a collection of image IDs by a specified DiskImages collection
     *
     * @param diskImages collection of DiskImages
     * @return list of image IDs
     */
    public static Collection<Guid> getDiskImageIds(Collection<DiskImage> diskImages) {
        Collection<Guid> result = new ArrayList<>();
        if (diskImages != null) {
            for (DiskImage diskImage : diskImages) {
                result.add(diskImage.getImageId());
            }
        }
        return result;
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
    public static void addDiskImageWithNoVmDevice(DiskImage image,
            boolean active,
            image_storage_domain_map imageStorageDomainMap) {
        try {
            addImage(image, active, imageStorageDomainMap);
            addDisk(image);
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
        }
    }

    /**
     * Adds a disk image (Adds image, disk, and relevant entities , but not VmDevice) This may be useful for Clone VMs,
     * where besides adding images it is required to copy all vm devices (VmDeviceUtils.copyVmDevices) from the source
     * VM
     *
     * @param image
     */
    public static void addDiskImageWithNoVmDevice(DiskImage image) {
        addDiskImageWithNoVmDevice(image,
                image.getActive(),
                new image_storage_domain_map(image.getImageId(),
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
    public static void addDisk(BaseDisk disk) {
        if (!DbFacade.getInstance().getBaseDiskDao().exists(disk.getId())) {
            DbFacade.getInstance().getBaseDiskDao().save(disk);
        }
    }

    /**
     * Adds a disk image (Adds image with active flag according to the value in image, using the first storage domain in
     * the storage id as entry to the storage domain map)
     *
     * @param image
     *            DiskImage to add
     */
    public static void addDiskImage(DiskImage image, Guid vmId) {
        addDiskImage(image, image.getActive(), new image_storage_domain_map(image.getImageId(), image.getStorageIds()
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
    public static void addImage(DiskImage image, boolean active, image_storage_domain_map imageStorageDomainMap) {
        image.setActive(active);
        DbFacade.getInstance().getImageDao().save(image.getImage());
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(image.getImageId());
        diskDynamic.setactual_size(image.getActualSizeInBytes());
        DbFacade.getInstance().getDiskImageDynamicDao().save(diskDynamic);
        if (imageStorageDomainMap != null) {
            DbFacade.getInstance().getImageStorageDomainMapDao().save(imageStorageDomainMap);
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
    public static void addDiskToVmIfNotExists(BaseDisk disk, Guid vmId) {
        if (!DbFacade.getInstance().getBaseDiskDao().exists(disk.getId())) {
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
    public static void addDiskToVm(BaseDisk disk, Guid vmId) {
        DbFacade.getInstance().getBaseDiskDao().save(disk);
        VmDeviceUtils.addManagedDevice(new VmDeviceId(disk.getId(), vmId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK,
                null,
                true,
                false,
                null);
    }

    /**
     * This function was developed especially for GUI needs. It returns a list of all the snapshots of current image of
     * a specific VM. If there are two images mapped to same VM, it's assumed that this is a TryBackToImage case and the
     * function returns a list of snapshots of inactive images. In this case the parent of the active image appears to
     * be trybackfrom image
     *
     * @param imageId
     * @return
     */
    public static List<DiskImage> getAllImageSnapshots(Guid imageId) {
        return DbFacade.getInstance().getDiskImageDao().getAllSnapshotsForLeaf(imageId);
    }

    public static String cdPathWindowsToLinux(String windowsPath, Guid storagePoolId, Guid vdsId) {
        if (StringUtils.isEmpty(windowsPath)) {
            return ""; // empty string is used for 'eject'
        }
        return cdPathWindowsToLinux(windowsPath, (String) Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.IsoPrefix, new VdsAndPoolIDVDSParametersBase(vdsId, storagePoolId))
                .getReturnValue());
    }

    public static String cdPathWindowsToLinux(String windowsPath, String isoPrefix) {
        if (StringUtils.isEmpty(windowsPath)) {
            return windowsPath; // empty string is used for 'eject'.
        }
        String fileName = new File(windowsPath).getName();
        return String.format("%1$s/%2$s", isoPrefix, fileName);
    }

    public static boolean isImagesExists(Iterable<DiskImage> images, Guid storagePoolId) {
        return isImagesExists(images, storagePoolId, new ArrayList<DiskImage>());
    }

    private static boolean isImagesExists(Iterable<DiskImage> images, Guid storagePoolId, ArrayList<DiskImage> irsImages) {
        boolean returnValue = true;

        for (DiskImage image : images) {
            DiskImage fromIrs = isImageExist(storagePoolId, image);
            if (fromIrs == null) {
                returnValue = false;
                break;
            }

            irsImages.add(fromIrs);
        }
        return returnValue;
    }

    private static DiskImage isImageExist(Guid storagePoolId, DiskImage image) {
        DiskImage fromIrs = null;
        try {
            Guid storageDomainId = image.getStorageIds().get(0);
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;
            fromIrs = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    image.getImageId())).getReturnValue();
        } catch (Exception e) {
            log.debug("Unable to get image info from from storage", e);
        }
        return fromIrs;
    }

    public static boolean checkImageConfiguration(StorageDomainStatic storageDomain,
            DiskImageBase diskInfo, List<String> messages) {
        if (!checkImageConfiguration(storageDomain, diskInfo.getVolumeType(), diskInfo.getVolumeFormat())) {
            // not supported
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED.toString());
            messages.add(String.format("$%1$s %2$s", "volumeFormat", diskInfo.getVolumeFormat()));
            messages.add(String.format("$%1$s %2$s", "volumeType", diskInfo.getVolumeType()));
            return false;
        }
        return true;
    }

    public static boolean checkImageConfiguration(StorageDomainStatic storageDomain, VolumeType volumeType, VolumeFormat volumeFormat) {
        return !((volumeType == VolumeType.Preallocated && volumeFormat == VolumeFormat.COW)
                || (storageDomain.getStorageType().isBlockDomain() && volumeType == VolumeType.Sparse && volumeFormat == VolumeFormat.RAW)
                || volumeFormat == VolumeFormat.Unassigned
                || volumeType == VolumeType.Unassigned);
    }

    public static boolean checkImagesConfiguration(Guid storageDomainId,
            Collection<? extends Disk> disksConfigList,
            List<String> messages) {
        boolean result = true;
        StorageDomainStatic storageDomain = DbFacade.getInstance().getStorageDomainStaticDao().get(storageDomainId);
        for (Disk diskInfo : disksConfigList) {
            if (DiskStorageType.IMAGE == diskInfo.getDiskStorageType()) {
                result = checkImageConfiguration(storageDomain, (DiskImage) diskInfo, messages);
            }
            if (!result)
                break;
        }
        return result;
    }


    public static Map<Guid, Set<Guid>> findDomainsInApplicableStatusForDisks(Iterable<DiskImage> diskImages,
            Map<Guid, StorageDomain> storageDomains,
            Set<StorageDomainStatus> applicableStatuses) {
        Map<Guid, Set<Guid>> disksApplicableDomainsMap = new HashMap<>();
        for (DiskImage diskImage : diskImages) {
            Set<Guid> diskApplicableDomain = new HashSet<>();
            for (Guid storageDomainID : diskImage.getStorageIds()) {
                StorageDomain domain = storageDomains.get(storageDomainID);
                if (applicableStatuses.contains(domain.getStatus())) {
                    diskApplicableDomain.add(domain.getId());
                }
            }
            disksApplicableDomainsMap.put(diskImage.getId(), diskApplicableDomain);
        }
        return disksApplicableDomainsMap;
    }

    /**
     * Sum and return the size of the given disks and their snapshots
     * Note: This method assumes that the given {@diskImages} are already loaded with their snapshots.
     * @param the disk images for size summing
     * @return The total size taken by the disks and their snapshots
     */
    public static double sumImagesTotalSizeWithSnapshotSize(Collection<DiskImage> diskImages) {
        double sum = 0;
        for (DiskImage diskImage : diskImages) {
            sum += diskImage.getActualDiskWithSnapshotsSize();
        }
        return sum;
    }

    public static List<DiskImage> getPluggedActiveImagesForVm(Guid vmId) {
        return filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(vmId, true), true, false, true);
    }

    /**
     * @return A unique {@link Set} of all the storage domain IDs relevant to all the given images
     * @param images The images to get the storage domain IDs for
     */
    public static Set<Guid> getAllStorageIdsForImageIds(Collection<DiskImage> images) {
        Set<Guid> domainsIds = new HashSet<Guid>();
        for (DiskImage image : images) {
            domainsIds.addAll(image.getStorageIds());
        }
        return domainsIds;
    }

    public static void fillImagesBySnapshots(VM vm) {
        for (Disk disk : vm.getDiskMap().values()) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.getSnapshots().addAll(getAllImageSnapshots(diskImage.getImageId()));
            }
        }
    }

    /**
     * Filter image disks by attributes.
     *
     *
     * @param listOfDisks
     *            - The list of disks to be filtered.
     * @param allowOnlyNotShareableDisks
     *            - Indication whether to allow only disks that are not shareable
     * @param allowOnlySnapableDisks
     *            - Indication whether to allow only disks which are allowed to be snapshoted.
     * @param allowOnlyActiveDisks
     *            - Indication whether to allow only disks that are not disk snapshots.
     * @return - List filtered of disk images according to the given filters.
     */
    public static List<DiskImage> filterImageDisks(Collection<? extends Disk> listOfDisks,
                                                   boolean allowOnlyNotShareableDisks,
                                                   boolean allowOnlySnapableDisks,
                                                   boolean allowOnlyActiveDisks) {
        List<DiskImage> diskImages = new ArrayList<DiskImage>();
        for (Disk disk : listOfDisks) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                    (!allowOnlyNotShareableDisks || !disk.isShareable()) &&
                    (!allowOnlySnapableDisks || disk.isAllowSnapshot()) &&
                    (!allowOnlyActiveDisks || Boolean.TRUE.equals(((DiskImage)disk).getActive()))) {
                diskImages.add((DiskImage) disk);
            }
        }
        return diskImages;
    }

    public static List<LunDisk> filterDiskBasedOnLuns(Collection<Disk> listOfDisks) {
        List<LunDisk> lunDisks = new ArrayList<LunDisk>();
        for (Disk disk : listOfDisks) {
            if (disk.getDiskStorageType() == DiskStorageType.LUN) {
                lunDisks.add((LunDisk) disk);
            }
        }
        return lunDisks;
    }

    public static void removeDiskImage(DiskImage diskImage, Guid vmId) {
        try {
            removeDiskFromVm(vmId, diskImage.getId());
            removeImage(diskImage);
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
        }
    }

    public static void removeLunDisk(LunDisk lunDisk) {
        DbFacade.getInstance()
                .getVmDeviceDao()
                .remove(new VmDeviceId(lunDisk.getId(),
                        null));
        LUNs lun = lunDisk.getLun();
        DbFacade.getInstance()
                .getDiskLunMapDao()
                .remove(new DiskLunMapId(lunDisk.getId(), lun.getLUN_id()));
        DbFacade.getInstance().getBaseDiskDao().remove(lunDisk.getId());

        lun.setLunConnections(new ArrayList<StorageServerConnections>(DbFacade.getInstance()
                .getStorageServerConnectionDao()
                .getAllForLun(lun.getLUN_id())));

        if (!lun.getLunConnections().isEmpty()) {
            StorageHelperDirector.getInstance().getItem(
                    lun.getLunConnections().get(0).getstorage_type()).removeLun(lun);
        } else {
            // if there are no connections then the lun is fcp.
            StorageHelperDirector.getInstance().getItem(StorageType.FCP).removeLun(lun);
        }

    }

    // the last image in each list is the leaf
    public static Map<Guid, List<DiskImage>> getImagesLeaf(List<DiskImage> images) {
        Map<Guid, List<DiskImage>> retVal = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage image : images) {
            MultiValueMapUtils.addToMap(image.getId(), image, retVal);
        }

        for (List<DiskImage> list : retVal.values()) {
            sortImageList(list);
        }
        return retVal;
    }

    public static void sortImageList(List<DiskImage> images) {
        List<DiskImage> hold = new ArrayList<DiskImage>();
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

        for (DiskImage image : hold) {
            images.add(image);
        }
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

    public static void removeImage(DiskImage diskImage) {
        DbFacade.getInstance()
                .getImageStorageDomainMapDao()
                .remove(diskImage.getImageId());
        DbFacade.getInstance().getDiskImageDynamicDao().remove(diskImage.getImageId());
        DbFacade.getInstance().getImageDao().remove(diskImage.getImageId());
    }

    public static void removeDiskFromVm(Guid vmGuid, Guid diskId) {
        DbFacade.getInstance().getVmDeviceDao().remove(new VmDeviceId(diskId, vmGuid));
        DbFacade.getInstance().getBaseDiskDao().remove(diskId);
    }

    public static void updateImageStatus(Guid imageId, ImageStatus imageStatus) {
        DbFacade.getInstance().getImageDao().updateStatus(imageId, imageStatus);
    }

    public static void updateAllDiskImageSnapshotsStatusWithCompensation(final Guid diskId,
            final ImageStatus status,
            ImageStatus statusForCompensation,
            final CompensationContext compensationContext) {
        updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(Collections.singletonList(diskId), status, statusForCompensation, compensationContext);
    }

    public static void updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(final Collection<Guid> diskIds,
                                                                         final ImageStatus status,
                                                                         ImageStatus statusForCompensation,
                                                                         final CompensationContext compensationContext) {
        if (compensationContext != null) {
            for (Guid diskId : diskIds) {
                List<DiskImage> diskSnapshots =
                        DbFacade.getInstance().getDiskImageDao().getAllSnapshotsForImageGroup(diskId);
                for (DiskImage diskSnapshot : diskSnapshots) {
                    diskSnapshot.setImageStatus(statusForCompensation);
                    compensationContext.snapshotEntityStatus(diskSnapshot.getImage());
                }
            }

            TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    for (Guid diskId : diskIds) {
                        DbFacade.getInstance().getImageDao().updateStatusOfImagesByImageGroupId(diskId, status);
                    }
                    compensationContext.stateChanged();
                    return null;
                }
            });
        } else {

            TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    for (Guid diskId : diskIds) {
                        updateAllDiskImageSnapshotsStatus(diskId, status);
                    }
                    return null;
                }
            });
        }
    }

    public static void updateAllDiskImageSnapshotsStatus(Guid diskId, ImageStatus status) {
        DbFacade.getInstance().getImageDao().updateStatusOfImagesByImageGroupId(diskId, status);
    }

    private static DiskImage getDiskImageById(Guid id, Iterable<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getId().equals(id)) {
                return diskImage;
            }
        }
        return null;
    }

    /**
     * Returns the subtraction set of the specified image lists (based on images' IDs)
     * @param images full list
     * @param imagesToSubtract images to subtract list
     * @return the subtraction set
     */
    public static List<DiskImage> imagesSubtract(Iterable<DiskImage> images, Iterable<DiskImage> imagesToSubtract) {
        List<DiskImage> subtract = new ArrayList<>();
        for (DiskImage image : images) {
            if (getDiskImageById(image.getId(), imagesToSubtract) == null) {
                subtract.add(image);
            }
        }
        return subtract;
    }

    /**
     * Returns the intersection set of the specified image lists (based on images' IDs)
     * @param images1 1st list
     * @param images2 2nd list
     * @return the intersection set
     */
    public static List<DiskImage> imagesIntersection(Iterable<DiskImage> images1, Iterable<DiskImage> images2) {
        List<DiskImage> intersection = new ArrayList<>();
        for (DiskImage image : images1) {
            if (getDiskImageById(image.getId(), images2) != null) {
                intersection.add(image);
            }
        }
        return intersection;
    }

    /**
     * Prepare a single {@link org.ovirt.engine.core.common.businessentities.Snapshot} object representing a snapshot of a given VM without the given disk.
     */
    public static Snapshot prepareSnapshotConfigWithoutImageSingleImage(Snapshot snapshot, Guid imageId) {
        return prepareSnapshotConfigWithAlternateImage(snapshot, imageId, null);
    }


    /**
     * Prepare a single {@link org.ovirt.engine.core.common.businessentities.Snapshot} object representing a snapshot of a given VM without the given disk,
     * substituting a new disk in its place if a new disk is provided to the method.
     */
    public static Snapshot prepareSnapshotConfigWithAlternateImage(Snapshot snapshot, Guid oldImageId, DiskImage newImage) {
        try {
            OvfManager ovfManager = new OvfManager();
            String snapConfig = snapshot.getVmConfiguration();

            if (snapshot.isVmConfigurationAvailable() && snapConfig != null) {
                VM vmSnapshot = new VM();
                ArrayList<DiskImage> snapshotImages = new ArrayList<DiskImage>();

                ovfManager.ImportVm(snapConfig,
                        vmSnapshot,
                        snapshotImages,
                        new ArrayList<VmNetworkInterface>());

                // Remove the image from the disk list
                Iterator<DiskImage> diskIter = snapshotImages.iterator();
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
                    snapshotImages.add(newImage);
                }

                String newOvf = ovfManager.ExportVm(vmSnapshot, snapshotImages, ClusterUtils.getCompatibilityVersion(vmSnapshot));
                snapshot.setVmConfiguration(newOvf);
            }
        } catch (OvfReaderException e) {
            log.error("Can't remove image '{}' from snapshot '{}'", oldImageId, snapshot.getId());
        }
        return snapshot;
    }

    public static DiskImage createDiskImageWithExcessData(DiskImage diskImage, Guid sdId) {
        DiskImage dummy = DiskImage.copyOf(diskImage);
        dummy.setStorageIds(new ArrayList<Guid>(Collections.singletonList(sdId)));
        dummy.getSnapshots().addAll(getAllImageSnapshots(dummy.getImageId()));
        return dummy;
    }

    /**
     * Creates and returns a Json string containing the disk alias and the disk description. The disk alias and
     * description are preserved in the disk meta data. If the meta data will be added with more fields
     * UpdateVmDiskCommand should be changed accordingly.
     */
    public static String getJsonDiskDescription(String diskAlias, String diskDescription) throws IOException {
        Map<String, Object> description = new TreeMap<>();
        description.put(DISK_ALIAS, diskAlias);
        description.put(DISK_DESCRIPTION, diskDescription != null ? diskDescription : "");
        return JsonHelper.mapToJson(description, false);
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
            clone.setvolumeFormat(VolumeFormat.COW);
            diskDummies.add(clone);
        }
        return diskDummies;
    }

    public static List<DiskImage> getSnapshotsDummiesForStorageAllocations(Collection<DiskImage> originalDisks) {
        List<DiskImage> diskDummies = new ArrayList<>();
        for (DiskImage snapshot : originalDisks) {
            DiskImage clone = DiskImage.copyOf(snapshot);
            // Add the child snapshot into which the deleted snapshot is going to be merged to the
            // DiskImage for StorageDomainValidator to handle
            List<DiskImage> snapshots = DbFacade.getInstance().getDiskImageDao().getAllSnapshotsForParent(clone.getImageId());
            clone.getSnapshots().clear();
            clone.getSnapshots().add(clone); // Add the clone itself since snapshots should contain the entire chain.
            clone.getSnapshots().addAll(snapshots);
            diskDummies.add(clone);
        }
        return diskDummies;
    }

}

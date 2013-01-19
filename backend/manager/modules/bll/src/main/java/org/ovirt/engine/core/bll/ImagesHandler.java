package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.storage.StorageHelperDirector;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
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
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class ImagesHandler {
    public static final String DISK = "_Disk";
    public static final String DefaultDriveName = "1";
    private static final Log log = LogFactory.getLog(ImagesHandler.class);

    /**
     * The following method will find all images and storages where they located for provide template and will fill an
     * diskInfoDestinationMap by imageId mapping on active storage id where image is located. The second map is
     * mapping of founded storage ids to storage object
     * @param template
     * @param diskInfoDestinationMap
     * @param destStorages
     * @param notCheckSize - if we need to perform a size check for storage or not
     */
    public static void fillImagesMapBasedOnTemplate(VmTemplate template,
            Map<Guid, DiskImage> diskInfoDestinationMap,
            Map<Guid, storage_domains> destStorages, boolean notCheckSize) {
        List<storage_domains> domains =
                DbFacade.getInstance()
                        .getStorageDomainDao()
                        .getAllForStoragePool(template.getstorage_pool_id().getValue());
        fillImagesMapBasedOnTemplate(template, domains, diskInfoDestinationMap, destStorages, notCheckSize);
    }

    public static void fillImagesMapBasedOnTemplate(VmTemplate template,
            List<storage_domains> domains,
            Map<Guid, DiskImage> diskInfoDestinationMap,
            Map<Guid, storage_domains> destStorages, boolean notCheckSize) {
        Map<Guid, storage_domains> storageDomainsMap = new HashMap<Guid, storage_domains>();
        for (storage_domains storageDomain : domains) {
            StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
            ArrayList<String> messages = new ArrayList<String>();
            if (validator.isDomainExistAndActive(messages) && validator.domainIsValidDestination(messages)
                    && (notCheckSize || StorageDomainSpaceChecker.isBelowThresholds(storageDomain))) {
                storageDomainsMap.put(storageDomain.getId(), storageDomain);
            }
        }
        for (DiskImage image : template.getDiskMap().values()) {
            for (Guid storageId : image.getstorage_ids()) {
                if (storageDomainsMap.containsKey(storageId)) {
                    ArrayList<Guid> storageIds = new ArrayList<Guid>();
                    storageIds.add(storageId);
                    image.setstorage_ids(storageIds);
                    diskInfoDestinationMap.put(image.getId(), image);
                    break;
                }
            }
        }

        if (destStorages != null) {
            for (DiskImage diskImage : diskInfoDestinationMap.values()) {
                Guid storageDomainId = diskImage.getstorage_ids().get(0);
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
        return vm == null ? "" : vm.getVmName();
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
            log.warnFormat("Disk object is null, the suggested default disk alias to be used is {0}",
                    diskAlias);
        } else {
            diskAlias = disk.getDiskAlias();
            if (StringUtils.isEmpty(diskAlias)) {
                diskAlias = getDefaultDiskAlias(diskPrefix, String.valueOf(count));
                log.infoFormat("Disk alias retrieved from the client is null or empty, the suggested default disk alias to be used is {0}",
                        diskAlias);
            }
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
            Guid storageDomainId = diskImage.getstorage_ids().get(0);
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
            log.error("Failed adding new disk image and related entities to db", ex);
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
        Map<Guid, DiskImage> result = null;
        if (diskImages != null) {
            result = new HashMap<Guid, DiskImage>();
            for (DiskImage diskImage : diskImages) {
                result.put(diskImage.getImageId(), diskImage);
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
            log.error("Failed adding new disk image and related entities to db", ex);
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
                image.getactive(),
                new image_storage_domain_map(image.getImageId(), image.getstorage_ids().get(0)));
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
        addDiskImage(image, image.getactive(), new image_storage_domain_map(image.getImageId(), image.getstorage_ids()
                .get(0)), vmId);
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
        image.setactive(active);
        DbFacade.getInstance().getImageDao().save(image.getImage());
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(image.getImageId());
        diskDynamic.setactual_size(image.getactual_size());
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
                VmDeviceType.DISK,
                VmDeviceType.DISK,
                null,
                true,
                false);
    }

    /**
     * This function was developed especially for GUI needs. It returns a list of all the snapshots of current image of
     * a specific VM. If there are two images mapped to same VM, it's assumed that this is a TryBackToImage case and the
     * function returns a list of snapshots of inactive images. In this case the parent of the active image appears to
     * be trybackfrom image
     *
     * @param imageId
     * @param imageTemplateId
     * @return
     */
    public static ArrayList<DiskImage> getAllImageSnapshots(Guid imageId, Guid imageTemplateId) {
        ArrayList<DiskImage> snapshots = new ArrayList<DiskImage>();
        Guid curImage = imageId;
        while (!imageTemplateId.equals(curImage) && !curImage.equals(Guid.Empty)) {
            DiskImage curDiskImage = DbFacade.getInstance().getDiskImageDao().getSnapshotById(curImage);
            snapshots.add(curDiskImage);
            curImage = curDiskImage.getParentId();
        }
        return snapshots;
    }

    public static String cdPathWindowsToLinux(String windowsPath, Guid storagePoolId) {
        return cdPathWindowsToLinux(windowsPath, (String) Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.IsoPrefix, new IrsBaseVDSCommandParameters(storagePoolId))
                .getReturnValue());
    }

    public static String cdPathWindowsToLinux(String windowsPath, String isoPrefix) {
        if (StringUtils.isEmpty(windowsPath)) {
            return windowsPath; // empty string is used for 'eject'.
        }
        String fileName = Path.GetFileName(windowsPath);
        return String.format("%1$s/%2$s", isoPrefix, fileName);
    }

    public static boolean isImagesExists(Iterable<DiskImage> images, Guid storagePoolId, Guid storageDomainId) {
        return isImagesExists(images, storagePoolId, storageDomainId, new ArrayList<DiskImage>());
    }

    private static boolean isImagesExists(Iterable<DiskImage> images, Guid storagePoolId, Guid domainId,
            ArrayList<DiskImage> irsImages) {
        boolean returnValue = true;

        for (DiskImage image : images) {
            Guid storageDomainId = !Guid.Empty.equals(domainId) ? domainId : image.getstorage_ids().get(0);
            DiskImage fromIrs = isImageExist(storagePoolId, storageDomainId, image);
            if (fromIrs == null) {
                returnValue = false;
                break;
            }

            irsImages.add(fromIrs);
        }
        return returnValue;
    }

    private static DiskImage isImageExist(Guid storagePoolId,
            Guid domainId,
            DiskImage image) {
        DiskImage fromIrs = null;
        try {
            Guid storageDomainId =
                    image.getstorage_ids() != null && !image.getstorage_ids().isEmpty() ? image.getstorage_ids()
                            .get(0) : domainId;
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;
            fromIrs = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    image.getImageId())).getReturnValue();
        } catch (Exception e) {
            log.debug("Unable to get image info from from storage.", e);
        }
        return fromIrs;
    }

    public static boolean CheckImageConfiguration(StorageDomainStatic storageDomain,
            DiskImageBase diskInfo, List<String> messages) {
        boolean result = true;
        if ((diskInfo.getvolume_type() == VolumeType.Preallocated && diskInfo.getvolume_format() == VolumeFormat.COW)
                || ((storageDomain.getstorage_type() == StorageType.FCP || storageDomain.getstorage_type() == StorageType.ISCSI) && (diskInfo
                        .getvolume_type() == VolumeType.Sparse && diskInfo.getvolume_format() == VolumeFormat.RAW))
                || (diskInfo.getvolume_format() == VolumeFormat.Unassigned || diskInfo.getvolume_type() == VolumeType.Unassigned)) {
            // not supported
            result = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED.toString());
        }
        return result;
    }

    public static boolean CheckImagesConfiguration(Guid storageDomainId,
            Collection<? extends Disk> disksConfigList,
            List<String> messages) {
        boolean result = true;
        StorageDomainStatic storageDomain = DbFacade.getInstance().getStorageDomainStaticDao().get(storageDomainId);
        for (Disk diskInfo : disksConfigList) {
            if (DiskStorageType.IMAGE == diskInfo.getDiskStorageType()) {
                result = CheckImageConfiguration(storageDomain, (DiskImage) diskInfo, messages);
            }
            if (!result)
                break;
        }
        return result;
    }


    public static boolean isStoragePoolValid(Guid storagePoolId) {
        storage_pool pool = DbFacade.getInstance().getStoragePoolDao().get(storagePoolId);
        if (pool == null || pool.getstatus() != StoragePoolStatus.Up) {
             return false;
        }
        return true;
    }


    public static boolean PerformImagesChecks(Guid vmId,
            List<String> messages,
            Guid storagePoolId,
            Guid storageDomainId,
            boolean diskSpaceCheck,
            boolean checkImagesLocked,
            boolean checkImagesIllegal,
            boolean checkImagesExist,
            boolean checkStorageDomain,
            boolean checkIsValid,
            Collection<? extends Disk> diskImageList) {

        boolean returnValue = true;

        if (checkIsValid && !isStoragePoolValid(storagePoolId)) {
                returnValue = false;
                ListUtils.nullSafeAdd(messages, VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND.toString());
        }

        List<DiskImage> images = getImages(vmId, diskImageList);
        if (returnValue && checkImagesLocked) {
            returnValue = checkImagesLocked(messages, images);
        }

        if (returnValue && checkIsValid) {
            if (images.size() > 0) {
                returnValue = returnValue &&
                        checkDiskImages(messages,
                                storagePoolId,
                                storageDomainId,
                                diskSpaceCheck,
                                checkImagesIllegal,
                                checkImagesExist,
                                checkStorageDomain,
                                images);
            } else if (checkImagesExist) {
                returnValue = false;
                ListUtils.nullSafeAdd(messages, VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS.toString());
            }
        }
        return returnValue;
    }

    public static boolean checkImagesLocked(Guid vmId, List<String> messages) {
        return checkImagesLocked(messages, getImages(vmId, null));
    }

    private static boolean checkImagesLocked(List<String> messages, List<DiskImage> images) {
        boolean returnValue = true;
        List<String> lockedDisksAliases = new ArrayList<String>();
        for (DiskImage diskImage : images) {
            if (diskImage.getimageStatus() == ImageStatus.LOCKED) {
                lockedDisksAliases.add(diskImage.getDiskAlias());
                returnValue = false;
            }
        }

        if (lockedDisksAliases.size() > 0) {
            ListUtils.nullSafeAdd(messages, VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED.toString());
            ListUtils.nullSafeAdd(messages,
                    String.format("$%1$s %2$s", "diskAliases", StringUtils.join(lockedDisksAliases, ", ")));
        }

        return returnValue;
    }

    private static List<DiskImage> getImages(Guid vmId, Collection<? extends Disk> diskImageList) {
        if (diskImageList == null) {
            return filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(vmId), true, false);
        }

        return filterImageDisks(diskImageList, true, false);
    }

    private static boolean checkDiskImages(List<String> messages,
            Guid storagePoolId,
            Guid storageDomainId,
            boolean diskSpaceCheck,
            boolean checkImagesIllegal,
            boolean checkImagesExist,
            boolean checkStorageDomain,
            List<DiskImage> images) {
        boolean returnValue = true;
        ArrayList<DiskImage> irsImages = new ArrayList<DiskImage>();

        if (diskSpaceCheck || checkStorageDomain) {
            Set<Guid> domainsIds = new HashSet<Guid>();
            if (!Guid.Empty.equals(storageDomainId)) {
                domainsIds.add(storageDomainId);
            } else {
                for (DiskImage image : images) {
                    domainsIds.add(image.getstorage_ids().get(0));
                }
            }
            for (Guid domainId : domainsIds) {
                storage_domains domain = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                        domainId, storagePoolId);
                if (checkStorageDomain) {
                    StorageDomainValidator storageDomainValidator =
                            new StorageDomainValidator(domain);
                    returnValue = storageDomainValidator.isDomainExistAndActive(messages);
                    if (!returnValue) {
                        break;
                    }
                }
                if (diskSpaceCheck && returnValue && !StorageDomainSpaceChecker.isBelowThresholds(domain)) {
                    returnValue = false;
                    ListUtils.nullSafeAdd(messages, VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString());
                    break;
                }
            }
        }

        if (returnValue && checkImagesExist) {
            boolean isImagesExist = isImagesExists(images, storagePoolId, storageDomainId, irsImages);
            if (!isImagesExist) {
                returnValue = false;
                ListUtils.nullSafeAdd(messages, VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST.toString());
            }
        }
        if (returnValue && checkImagesIllegal) {
            returnValue = CheckImagesLegality(messages, images, irsImages);
        }
        return returnValue;
    }

    private static boolean CheckImagesLegality
            (List<String> messages, List<DiskImage> images, List<DiskImage> irsImages) {
        boolean returnValue = true;
        int i = 0;
        for (DiskImage diskImage : images) {
            if (diskImage != null) {
                DiskImage image = irsImages.get(i++);
                if (image.getimageStatus() != ImageStatus.OK) {
                    diskImage.setimageStatus(image.getimageStatus());
                    DbFacade.getInstance().getImageDao().update(diskImage.getImage());
                    returnValue = false;
                    ListUtils.nullSafeAdd(messages,
                            VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL.toString());
                    break;
                }
            }
        }
        return returnValue;
    }

    public static void fillImagesBySnapshots(VM vm) {
        for (Disk disk : vm.getDiskMap().values()) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.getSnapshots().addAll(
                        ImagesHandler.getAllImageSnapshots(diskImage.getImageId(),
                                diskImage.getit_guid()));
            }
        }
    }

    /**
     * Filter image disks by attributes.
     * @param listOfDisks - The list of disks to be filtered.
     * @param filterNotShareableDisks - Indication whether to filter disks which are not shareable.
     * @param filterAllowSnapshotDisks - Indication whether to filter disks which are allowed to be snapshot.
     * @return - List filtered of disk images.
     */
    public static List<DiskImage> filterImageDisks(Collection<? extends Disk> listOfDisks,
            boolean filterNotShareableDisks,
            boolean filterAllowSnapshotDisks) {
        List<DiskImage> diskImages = new ArrayList<DiskImage>();
        for (Disk disk : listOfDisks) {
            if ((!filterNotShareableDisks || !disk.isShareable())
                    && disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                    (!filterAllowSnapshotDisks || disk.isAllowSnapshot())) {
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
            log.error("Failed adding new disk image and related entities to db", ex);
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

}

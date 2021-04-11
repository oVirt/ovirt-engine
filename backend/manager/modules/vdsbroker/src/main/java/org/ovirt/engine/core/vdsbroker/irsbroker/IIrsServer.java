package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.vdsbroker.LeaseInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResizeStorageDomainPVMapReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturn;


public interface IIrsServer {

    void close();

    OneUuidReturn createVolume(String sdUUID, String spUUID, String imgGUID, String size, int volFormat,
            int volType, String diskType, String volUUID, String descr, String srcImgGUID, String srcVolUUID,
            String initialSize, boolean shouldAddBitmaps);

    OneUuidReturn copyImage(String sdUUID, String spUUID, String vmGUID, String srcImgGUID, String srcVolUUID,
            String dstImgGUID, String dstVolUUID, String descr, String dstSdUUID, int volType, int volFormat,
            int preallocate, String postZero, String force);

    OneUuidReturn copyImage(String sdUUID, String spUUID, String vmGUID, String srcImgGUID, String srcVolUUID,
            String dstImgGUID, String dstVolUUID, String descr, String dstSdUUID, int volType, int volFormat,
            int preallocate, String postZero, Boolean discard, String force);

    OneUuidReturn downloadImage(Map methodInfo, String spUUID, String sdUUID, String dstImgGUID, String dstVolUUID);

    OneUuidReturn uploadImage(Map methodInfo, String spUUID, String sdUUID, String srcImgGUID, String srcVolUUID);

    VolumeListReturn reconcileVolumeChain(String spUUID, String sdUUID, String imgGUID,
            String leafVolUUID);

    OneUuidReturn deleteVolume(String sdUUID, String spUUID, String imgGUID, String[] volUUID,
            String postZero, String force);

    OneUuidReturn deleteVolume(String sdUUID, String spUUID, String imgGUID, String[] volUUID,
            String postZero, Boolean discard, String force);

    StatusOnlyReturn setVolumeDescription(String sdUUID, String spUUID, String imgGUID, String volUUID, String description);

    FileStatsReturn getFileStats(String sdUUID, String pattern, boolean caseSensitive);

    StorageStatusReturn activateStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturn deactivateStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    StatusOnlyReturn detachStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    StatusOnlyReturn forcedDetachStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturn attachStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturn setStorageDomainDescription(String sdUUID, String description);

    StatusOnlyReturn extendStorageDomain(String sdUUID, String spUUID, String[] devlist, boolean force);

    ResizeStorageDomainPVMapReturn resizeStorageDomainPV(String sdUUID, String spUUID, String device);

    StoragePoolInfo getStoragePoolInfo(String spUUID);

    StatusOnlyReturn destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    OneUuidReturn deleteImage(String sdUUID, String spUUID, String imgGUID, String postZero, String force);

    OneUuidReturn deleteImage(String sdUUID, String spUUID, String imgGUID, String postZero, Boolean discard,
            String force);

    OneUuidReturn moveImage(String spUUID, String srcDomUUID, String dstDomUUID, String imgGUID,
            String vmGUID, int op, String postZero, String force);

    OneUuidReturn moveImage(String spUUID, String srcDomUUID, String dstDomUUID, String imgGUID,
            String vmGUID, int op, String postZero, Boolean discard, String force);

    OneUuidReturn cloneImageStructure(String spUUID, String srcDomUUID, String imgGUID, String dstDomUUID);

    OneUuidReturn syncImageData(String spUUID, String srcDomUUID, String imgGUID, String dstDomUUID, String syncType);

    StatusOnlyReturn updateVM(String spUUID, Map[] vms);

    StatusOnlyReturn removeVM(String spUUID, String vmGUID);

    StatusOnlyReturn updateVMInImportExport(String spUUID, Map[] vms, String StorageDomainId);

    StatusOnlyReturn removeVM(String spUUID, String vmGUID, String storageDomainId);

    GetVmsInfoReturn getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList);

    StatusOnlyReturn upgradeStoragePool(String storagePoolId, String targetVersion);

    ImagesListReturn getImagesList(String sdUUID);

    UUIDListReturn getVolumesList(String sdUUID, String spUUID, String imgUUID);

    OneUuidReturn extendVolumeSize(String spUUID, String sdUUID, String imageUUID,
                                            String volumeUUID, String newSize);

    StatusReturn setVolumeLegality(String spID, String sdID, String imageID, String volumeID, String legality);

    OneUuidReturn prepareMerge(String spUUID, Map<String, Object> subchainInfo);

    OneUuidReturn finalizeMerge(String spUUID, Map<String, Object> subchainInfo);

    OneUuidReturn reduceVolume(String spUUID, String sdUUID, String imageUUID,
                               String volumeUUID, boolean allowActive);

    LeaseTaskInfoReturn addLease(String leaseUUID, String sdUUID, Map<String, Object> leaseMetadata);

    LeaseTaskInfoReturn removeLease(String leaseUUID, String sdUUID);

    LeaseInfoReturn getLeaseInfo(String leaseUUID, String sdUUID);

    OneUuidReturn switchMaster(String spUUID, String oldMasterUUID, String newMasterUUID, int masterVersion);
}

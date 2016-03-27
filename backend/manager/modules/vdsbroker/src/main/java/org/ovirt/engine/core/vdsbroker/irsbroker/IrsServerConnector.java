package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public interface IrsServerConnector {

    public Map<String, Object> createVolume(String sdUUID, String spUUID, String imgGUID, String size, int volFormat,
            int volType, int diskType, String volUUID, String descr, String srcImgGUID, String srcVolUUID);

    public Map<String, Object> createVolume(String sdUUID, String spUUID, String imgGUID, String size, int volFormat,
            int volType, int diskType, String volUUID, String descr, String srcImgGUID, String srcVolUUID,
            String initialSize);

    public Map<String, Object> copyImage(String sdUUID, String spUUID, String vmGUID, String srcImgGUID,
            String srcVolUUID, String dstImgGUID, String dstVolUUID, String descr);

    public Map<String, Object> copyImage(String sdUUID, String spUUID, String vmGUID, String srcImgGUID,
            String srcVolUUID, String dstImgGUID, String dstVolUUID, String descr, String dstSdUUID, int volType,
            int volFormat, int preallocate, String postZero, String force);

    public Map<String, Object> downloadImage(Map methodInfo, String spUUID, String sdUUID, String dstImgGUID, String dstVolUUID);

    public Map<String, Object> uploadImage(Map methodInfo, String spUUID, String sdUUID, String srcImgGUID, String srcVolUUID);

    public Map<String, Object> mergeSnapshots(String sdUUID, String spUUID, String vmGUID, String imgGUID,
            String ancestorUUID, String successorUUID);

    public Map<String, Object> mergeSnapshots(String sdUUID, String spUUID, String vmGUID, String imgGUID,
            String ancestorUUID, String successorUUID, String postZero);

    public Map<String, Object> reconcileVolumeChain(String spUUID, String sdUUID, String imgGUID,
            String leafVolUUID);

    public Map<String, Object> deleteVolume(String sdUUID, String spUUID, String imgGUID, String[] volUUID,
            String postZero);

    public Map<String, Object> deleteVolume(String sdUUID, String spUUID, String imgGUID, String[] volUUID,
            String postZero, String force);

    public Map<String, Object> getVolumeInfo(String sdUUID, String spUUID, String imgGUID, String volUUID);

    public Map<String, Object> setVolumeDescription(String sdUUID, String spUUID, String imgGUID, String volUUID, String description);

    public Map<String, Object> getStats();

    public Map<String, Object> getIsoList(String spUUID);

    public Map<String, Object> getFloppyList(String spUUID);

    public Map<String, Object> getFileStats(String sdUUID, String pattern, boolean caseSensitive);

    public Map<String, Object> activateStorageDomain(String sdUUID, String spUUID);

    public Map<String, Object> deactivateStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    public Map<String, Object> detachStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    public Map<String, Object> forcedDetachStorageDomain(String sdUUID, String spUUID);

    public Map<String, Object> attachStorageDomain(String sdUUID, String spUUID);

    public Map<String, Object> setStorageDomainDescription(String sdUUID, String description);

    public Map<String, Object> extendStorageDomain(String sdUUID, String spUUID, String[] devlist, boolean force);

    public Map<String, Object> resizePV(String sdUUID, String spUUID, String device);

    public Map<String, Object> getStoragePoolInfo(String spUUID);

    public Map<String, Object> destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    public Map<String, Object> deleteImage(String sdUUID, String spUUID, String imgGUID, String postZero);

    public Map<String, Object> deleteImage(String sdUUID, String spUUID, String imgGUID, String postZero, String force);

    public Map<String, Object> moveImage(String spUUID, String srcDomUUID, String dstDomUUID, String imgGUID,
            String vmGUID, int op, String postZero, String force);

    public Map<String, Object> moveImage(String spUUID, String srcDomUUID, String dstDomUUID, String imgGUID,
            String vmGUID, int op);

    public Map<String, Object> cloneImageStructure(String spUUID, String srcDomUUID, String imgGUID, String dstDomUUID);

    public Map<String, Object> syncImageData(String spUUID, String srcDomUUID, String imgGUID, String dstDomUUID, String syncType);

    public Map<String, Object> getImageDomainsList(String spUUID, String imgUUID);

    public Map<String, Object> updateVM(String spUUID, Map[] vms);

    public Map<String, Object> removeVM(String spUUID, String vmGUID);

    public Map<String, Object> updateVM(String spUUID, Map[] vms, String StorageDomainId);

    public Map<String, Object> removeVM(String spUUID, String vmGUID, String storageDomainId);

    public Map<String, Object> getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList);

    public Map<String, Object> getVmsList(String storagePoolId, String storageDomainId);

    public Map<String, Object> upgradeStoragePool(String storagePoolId, String targetVersion);

    public Map<String, Object> getImagesList(String sdUUID);

    public Map<String, Object> getVolumesList(String sdUUID, String spUUID, String imgUUID);

    public Map<String, Object> extendVolumeSize(String spUUID, String sdUUID, String imageUUID,
                                                String volumeUUID, String newSize);

    public Map<String, Object> setVolumeLegality(String spID, String sdID, String imageID, String volumeID, String legality);
}

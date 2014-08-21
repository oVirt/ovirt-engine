package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StorageDomainListReturnForXmlRpc;

import java.util.Map;


public interface IIrsServer {

    OneUuidReturnForXmlRpc createVolume(String sdUUID, String spUUID, String imgGUID, String size, int volFormat,
            int volType, int diskType, String volUUID, String descr, String srcImgGUID, String srcVolUUID);

    OneUuidReturnForXmlRpc copyImage(String sdUUID, String spUUID, String vmGUID, String srcImgGUID, String srcVolUUID,
            String dstImgGUID, String dstVolUUID, String descr, String dstSdUUID, int volType, int volFormat,
            int preallocate, String postZero, String force);

    OneUuidReturnForXmlRpc downloadImage(Map methodInfo, String spUUID, String sdUUID, String dstImgGUID, String dstVolUUID);

    OneUuidReturnForXmlRpc uploadImage(Map methodInfo, String spUUID, String sdUUID, String srcImgGUID, String srcVolUUID);

    OneUuidReturnForXmlRpc mergeSnapshots(String sdUUID, String spUUID, String vmGUID, String imgGUID,
            String ancestorUUID, String successorUUID, String postZero);

    VolumeListReturnForXmlRpc reconcileVolumeChain(String spUUID, String sdUUID, String imgGUID,
            String leafVolUUID);

    OneUuidReturnForXmlRpc deleteVolume(String sdUUID, String spUUID, String imgGUID, String[] volUUID,
            String postZero, String force);

    OneImageInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgGUID, String volUUID);

    StatusOnlyReturnForXmlRpc setVolumeDescription(String sdUUID, String spUUID, String imgGUID, String volUUID, String description);

    IrsStatsAndStatusXmlRpc getIrsStats();

    OneUuidReturnForXmlRpc importCandidate(String sdUUID, String vmGUID, String templateGUID, String templateVolGUID,
            String path, String type, String force);

    FileStatsReturnForXmlRpc getIsoList(String spUUID);

    FileStatsReturnForXmlRpc getFloppyList(String spUUID);

    FileStatsReturnForXmlRpc getFileStats(String sdUUID, String pattern, boolean caseSensitive);

    StorageStatusReturnForXmlRpc activateStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturnForXmlRpc deactivateStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    StatusOnlyReturnForXmlRpc detachStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    StatusOnlyReturnForXmlRpc forcedDetachStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturnForXmlRpc attachStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturnForXmlRpc setStorageDomainDescription(String sdUUID, String description);

    StorageDomainListReturnForXmlRpc reconstructMaster(String spUUID, int hostSpmId, String msdUUID, String masterVersion);

    StatusOnlyReturnForXmlRpc extendStorageDomain(String sdUUID, String spUUID, String[] devlist);

    StatusOnlyReturnForXmlRpc extendStorageDomain(String sdUUID, String spUUID, String[] devlist, boolean force);

    StatusOnlyReturnForXmlRpc setStoragePoolDescription(String spUUID, String description);

    StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID);

    StatusOnlyReturnForXmlRpc destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    OneUuidReturnForXmlRpc deleteImage(String sdUUID, String spUUID, String imgGUID, String postZero, String force);

    OneUuidReturnForXmlRpc moveImage(String spUUID, String srcDomUUID, String dstDomUUID, String imgGUID,
            String vmGUID, int op, String postZero, String force);

    OneUuidReturnForXmlRpc cloneImageStructure(String spUUID, String srcDomUUID, String imgGUID, String dstDomUUID);

    OneUuidReturnForXmlRpc syncImageData(String spUUID, String srcDomUUID, String imgGUID, String dstDomUUID, String syncType);

    StatusOnlyReturnForXmlRpc setMaxHosts(int maxHosts);

    StatusOnlyReturnForXmlRpc updateVM(String spUUID, Map[] vms);

    StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID);

    StatusOnlyReturnForXmlRpc updateVMInImportExport(String spUUID, Map[] vms, String StorageDomainId);

    StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID, String storageDomainId);

    GetVmsInfoReturnForXmlRpc getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList);

    StatusOnlyReturnForXmlRpc upgradeStoragePool(String storagePoolId, String targetVersion);

    ImagesListReturnForXmlRpc getImagesList(String sdUUID);

    UUIDListReturnForXmlRpc getVolumesList(String sdUUID, String spUUID, String imgUUID);

    OneUuidReturnForXmlRpc extendVolumeSize(String spUUID, String sdUUID, String imageUUID,
                                            String volumeUUID, String newSize);
}

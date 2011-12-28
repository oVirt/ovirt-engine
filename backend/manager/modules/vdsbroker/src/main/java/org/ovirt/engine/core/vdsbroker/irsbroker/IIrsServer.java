package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StorageDomainListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;


public interface IIrsServer {

    OneUuidReturnForXmlRpc createVolume(String sdUUID, String spUUID, String imgGUID, String size, int volFormat,
            int volType, int diskType, String volUUID, String descr, String srcImgGUID, String srcVolUUID);

    OneUuidReturnForXmlRpc copyImage(String sdUUID, String spUUID, String vmGUID, String srcImgGUID, String srcVolUUID,
            String dstImgGUID, String dstVolUUID, String descr, String dstSdUUID, int volType, int volFormat,
            int preallocate, String postZero, String force);

    StatusOnlyReturnForXmlRpc setVolumeDescription(String sdUUID, String spUUID, String imgGUID, String volUUID,
            String description);

    OneUuidReturnForXmlRpc mergeSnapshots(String sdUUID, String spUUID, String vmGUID, String imgGUID,
            String ancestorUUID, String successorUUID, String postZero);

    OneUuidReturnForXmlRpc deleteVolume(String sdUUID, String spUUID, String imgGUID, String[] volUUID,
            String postZero, String force);

    OneImageInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgGUID, String volUUID);

    IrsStatsAndStatusXmlRpc getIrsStats();

    OneUuidReturnForXmlRpc exportCandidate(String sdUUID, String vmGUID, String[] volumesList, String vmMeta,
            String templateGUID, String templateVolGUID, String templateMeta, String expPath, String collapse,
            String force);

    IrsVMListReturnForXmlRpc getImportCandidates(String path, String type, String vmType);

    ImportCandidatesInfoReturnForXmlRpc getImportCandidatesInfo(String path, String type, String vmType);

    ImportCandidateInfoReturnForXmlRpc getCandidateInfo(String candidateGUID, String path, String type);

    OneUuidReturnForXmlRpc importCandidate(String sdUUID, String vmGUID, String templateGUID, String templateVolGUID,
            String path, String type, String force);

    IsoListReturnForXmlRpc getIsoList(String spUUID);

    IsoListReturnForXmlRpc getFloppyList(String spUUID);

    StatusOnlyReturnForXmlRpc extendVolume(String sdUUID, String spUUID, String imgGUID, String volUUID, int newSize);

    StorageStatusReturnForXmlRpc activateStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturnForXmlRpc deactivateStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    StatusOnlyReturnForXmlRpc detachStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion);

    StatusOnlyReturnForXmlRpc forcedDetachStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturnForXmlRpc attachStorageDomain(String sdUUID, String spUUID);

    StatusOnlyReturnForXmlRpc setStorageDomainDescription(String sdUUID, String description);

    StorageDomainListReturnForXmlRpc reconstructMaster(String spUUID, String msdUUID, String masterVersion);

    StatusOnlyReturnForXmlRpc extendStorageDomain(String sdUUID, String spUUID, String[] devlist);

    StatusOnlyReturnForXmlRpc setStoragePoolDescription(String spUUID, String description);

    StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID);

    StatusOnlyReturnForXmlRpc destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    OneUuidReturnForXmlRpc deleteImage(String sdUUID, String spUUID, String imgGUID, String postZero, String force);

    OneUuidReturnForXmlRpc moveImage(String spUUID, String srcDomUUID, String dstDomUUID, String imgGUID,
            String vmGUID, int op, String postZero, String force);

    OneUuidReturnForXmlRpc moveMultipleImages(String spUUID, String srcDomUUID, String dstDomUUID,
            XmlRpcStruct imgDict, String vmGUID);

    StorageDomainGuidListReturnForXmlRpc getImageDomainsList(String spUUID, String imgUUID);

    StatusOnlyReturnForXmlRpc setMaxHosts(int maxHosts);

    StatusOnlyReturnForXmlRpc updateVM(String spUUID, Map[] vms);

    StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID);

    StatusOnlyReturnForXmlRpc updateVMInImportExport(String spUUID, Map[] vms, String StorageDomainId);

    StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID, String storageDomainId);

    GetVmsInfoReturnForXmlRpc getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList);

    GetVmsListReturnForXmlRpc getVmsList(String storagePoolId, String storageDomainId);
}

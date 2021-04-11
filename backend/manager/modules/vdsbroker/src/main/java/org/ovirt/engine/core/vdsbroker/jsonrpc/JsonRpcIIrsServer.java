package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.GetVmsInfoReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.IIrsServer;
import org.ovirt.engine.core.vdsbroker.irsbroker.ImagesListReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.LeaseTaskInfoReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfo;
import org.ovirt.engine.core.vdsbroker.irsbroker.StorageStatusReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.UUIDListReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.VolumeListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.LeaseInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResizeStorageDomainPVMapReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturn;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.RequestBuilder;

public class JsonRpcIIrsServer implements IIrsServer {

    private JsonRpcClient client;

    public JsonRpcIIrsServer(JsonRpcClient client) {
        this.client = client;
    }

    @Override
    public void close() {
        this.client.close();
    }

    @Override
    public OneUuidReturn createVolume(String sdUUID,
            String spUUID,
            String imgGUID,
            String size,
            int volFormat,
            int volType,
            String diskType,
            String volUUID,
            String descr,
            String srcImgGUID,
            String srcVolUUID,
            String initialSize,
            boolean shouldAddBitmaps) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.create").withParameter("volumeID", volUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgGUID)
                        .withParameter("size", size)
                        .withParameter("volFormat", volFormat)
                        .withParameter("preallocate", volType)
                        .withParameter("diskType", diskType)
                        .withParameter("desc", descr)
                        .withParameter("srcImgUUID", srcImgGUID)
                        .withParameter("srcVolUUID", srcVolUUID)
                        .withOptionalParameter("initialSize", initialSize)
                        .withOptionalParameter("addBitmaps", shouldAddBitmaps)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public OneUuidReturn copyImage(String sdUUID,
            String spUUID,
            String vmGUID,
            String srcImgGUID,
            String srcVolUUID,
            String dstImgGUID,
            String dstVolUUID,
            String descr,
            String dstSdUUID,
            int volType,
            int volFormat,
            int preallocate,
            String postZero,
            String force) {
        // vmGUID is not needed and can be removed from the interface
        return copyImage(sdUUID, spUUID, vmGUID, srcImgGUID, srcVolUUID, dstImgGUID, dstVolUUID, descr, dstSdUUID,
                volType, volFormat, preallocate, postZero, null, force);
    }

    @Override
    public OneUuidReturn copyImage(String sdUUID,
            String spUUID,
            String vmGUID,
            String srcImgGUID,
            String srcVolUUID,
            String dstImgGUID,
            String dstVolUUID,
            String descr,
            String dstSdUUID,
            int volType,
            int volFormat,
            int preallocate,
            String postZero,
            Boolean discard,
            String force) {
        // vmGUID is not needed and can be removed from the interface
        JsonRpcRequest request =
                new RequestBuilder("Volume.copy").withParameter("volumeID", srcVolUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", srcImgGUID)
                        .withParameter("dstSdUUID", dstSdUUID)
                        .withParameter("dstImgUUID", dstImgGUID)
                        .withParameter("dstVolUUID", dstVolUUID)
                        .withParameter("desc", descr)
                        .withParameter("volType", volType)
                        .withParameter("volFormat", volFormat)
                        .withParameter("preallocate", preallocate)
                        .withParameter("postZero", postZero)
                        .withOptionalParameter("discard", discard)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OneUuidReturn downloadImage(Map methodInfo,
            String spUUID,
            String sdUUID,
            String dstImgGUID,
            String dstVolUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Image.download").withParameter("methodArgs", methodInfo)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", dstImgGUID)
                        .withParameter("volumeID", dstVolUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OneUuidReturn uploadImage(Map methodInfo,
            String spUUID,
            String sdUUID,
            String srcImgGUID,
            String srcVolUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Image.upload").withParameter("methodArgs", methodInfo)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", srcImgGUID)
                        .withParameter("volumeID", srcVolUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public VolumeListReturn reconcileVolumeChain(String spUUID, String sdUUID, String imgGUID,
            String leafVolUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Image.reconcileVolumeChain").withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgGUID)
                        .withParameter("leafVolID", leafVolUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("volumes")
                        .withResponseType(Object[].class);
        return new VolumeListReturn(response);
    }

    @Override
    public OneUuidReturn deleteVolume(String sdUUID,
            String spUUID,
            String imgGUID,
            String[] volUUID,
            String postZero,
            String force) {
        return deleteVolume(sdUUID, spUUID, imgGUID, volUUID, postZero, null, force);
    }

    @Override
    public OneUuidReturn deleteVolume(String sdUUID,
            String spUUID,
            String imgGUID,
            String[] volUUID,
            String postZero,
            Boolean discard,
            String force) {
        JsonRpcRequest request =
                new RequestBuilder("Image.deleteVolumes").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("volumeList", new ArrayList<>(Arrays.asList(volUUID)))
                        .withOptionalParameter("postZero", postZero)
                        .withOptionalParameter("discard", discard)
                        .withOptionalParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public FileStatsReturn getFileStats(String sdUUID, String pattern, boolean caseSensitive) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getFileStats").withParameter("storagedomainID", sdUUID)
                        .withParameter("pattern", pattern)
                        .withParameter("caseSensitive", caseSensitive)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("fileStats");
        return new FileStatsReturn(response);
    }

    @Override
    public StorageStatusReturn activateStorageDomain(String sdUUID, String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.activate").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("storageStatus")
                        .withResponseType(String.class);
        return new StorageStatusReturn(response);
    }

    @Override
    public StatusOnlyReturn deactivateStorageDomain(String sdUUID,
            String spUUID,
            String msdUUID,
            int masterVersion) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.deactivate").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("masterSdUUID", msdUUID)
                        .withParameter("masterVersion", masterVersion)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn detachStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.detach").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("masterSdUUID", msdUUID)
                        .withParameter("masterVersion", masterVersion)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn forcedDetachStorageDomain(String sdUUID, String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.detach").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("force", true)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn attachStorageDomain(String sdUUID, String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.attach").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn setStorageDomainDescription(String sdUUID, String description) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.setDescription").withParameter("storagedomainID", sdUUID)
                        .withParameter("description", description)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn extendStorageDomain(String sdUUID, String spUUID, String[] devlist, boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.extend").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("devlist", new ArrayList<>(Arrays.asList(devlist)))
                        // TODO: Change to withOptionalParameter when the API will allow to send primitives as
                        // optional parameters.
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public ResizeStorageDomainPVMapReturn resizeStorageDomainPV(String sdUUID, String spUUID, String device) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.resizePV").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("guid", device)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("size");
        return new ResizeStorageDomainPVMapReturn(response);
    }

    @Override
    public StoragePoolInfo getStoragePoolInfo(String spUUID) {
        // duplicated in IVdsServer#getStoragePoolInfo
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getInfo").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StoragePoolInfo(response);
    }

    @Override
    public StatusOnlyReturn destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.destroy").withParameter("storagepoolID", spUUID)
                        .withParameter("hostID", hostSpmId)
                        .withParameter("scsiKey", SCSIKey)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public OneUuidReturn deleteImage(String sdUUID,
            String spUUID,
            String imgGUID,
            String postZero,
            String force) {
        return deleteImage(sdUUID, spUUID, imgGUID, postZero, null, force);
    }

    @Override
    public OneUuidReturn deleteImage(String sdUUID,
            String spUUID,
            String imgGUID,
            String postZero,
            Boolean discard,
            String force) {
        JsonRpcRequest request =
                new RequestBuilder("Image.delete").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("postZero", postZero)
                        .withOptionalParameter("discard", discard)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public OneUuidReturn moveImage(String spUUID,
            String srcDomUUID,
            String dstDomUUID,
            String imgGUID,
            String vmGUID,
            int op,
            String postZero,
            String force) {
        return moveImage(spUUID, srcDomUUID, dstDomUUID, imgGUID, vmGUID, op, postZero, null, force);
    }

    @Override
    public OneUuidReturn moveImage(String spUUID,
            String srcDomUUID,
            String dstDomUUID,
            String imgGUID,
            String vmGUID,
            int op,
            String postZero,
            Boolean discard,
            String force) {
        JsonRpcRequest request =
                new RequestBuilder("Image.move").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", srcDomUUID)
                        .withParameter("dstSdUUID", dstDomUUID)
                        .withParameter("operation", op)
                        .withParameter("postZero", postZero)
                        .withOptionalParameter("discard", discard)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public OneUuidReturn cloneImageStructure(String spUUID,
            String srcDomUUID,
            String imgGUID,
            String dstDomUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Image.cloneStructure").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", srcDomUUID)
                        .withParameter("dstSdUUID", dstDomUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public OneUuidReturn syncImageData(String spUUID,
            String srcDomUUID,
            String imgGUID,
            String dstDomUUID,
            String syncType) {
        JsonRpcRequest request =
                new RequestBuilder("Image.syncData").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", srcDomUUID)
                        .withParameter("dstSdUUID", dstDomUUID)
                        .withParameter("syncType", syncType)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn updateVM(String spUUID, Map[] vms) {
        return updateVMInImportExport(spUUID, vms, null);
    }

    @Override
    public StatusOnlyReturn removeVM(String spUUID, String vmGUID) {
        return removeVM(spUUID, vmGUID, null);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn updateVMInImportExport(String spUUID, Map[] vms, String StorageDomainId) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.updateVMs").withParameter("storagepoolID", spUUID)
                        .withParameter("vmList", new ArrayList<>(Arrays.asList(vms)))
                        .withOptionalParameter("storagedomainID", StorageDomainId)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn removeVM(String spUUID, String vmGUID, String storageDomainId) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.removeVM").withParameter("storagepoolID", spUUID)
                        .withParameter("vmUUID", vmGUID).withOptionalParameter("storagedomainID", storageDomainId)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GetVmsInfoReturn getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getBackedUpVmsInfo").withParameter("storagepoolID", storagePoolId)
                        .withParameter("storagedomainID", storageDomainId)
                        .withParameter("vmList", new ArrayList<>(Arrays.asList(VMIDList)))
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmlist");
        return new GetVmsInfoReturn(response);
    }

    @Override
    public StatusOnlyReturn upgradeStoragePool(String storagePoolId, String targetVersion) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.upgrade").withParameter("storagepoolID", storagePoolId)
                        .withParameter("targetDomVersion", targetVersion)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public ImagesListReturn getImagesList(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getImages").withParameter("storagedomainID", sdUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("imageslist")
                        .withResponseType(Object[].class);
        return new ImagesListReturn(response);
    }

    @Override
    public UUIDListReturn getVolumesList(String sdUUID, String spUUID, String imgUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getVolumes").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("imageID", imgUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuidlist")
                        .withResponseType(Object[].class);
        return new UUIDListReturn(response);
    }

    @Override
    public OneUuidReturn extendVolumeSize(String spUUID,
            String sdUUID,
            String imageUUID,
            String volumeUUID,
            String newSize) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.extendSize").withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imageUUID)
                        .withParameter("volumeID", volumeUUID)
                        .withParameter("newSize", newSize)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public StatusOnlyReturn setVolumeDescription(String sdUUID, String spUUID, String imgGUID,
            String volUUID, String description) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.setDescription").withParameter("volumeID", volUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgGUID)
                        .withParameter("description", description)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusReturn setVolumeLegality(String spID, String sdID, String imageID, String volumeID, String legality) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.setLegality")
                        .withParameter("storagepoolID", spID)
                        .withParameter("storagedomainID", sdID)
                        .withParameter("imageID", imageID)
                        .withParameter("volumeID", volumeID)
                        .withParameter("legality", legality)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusReturn(response);
    }

    @Override
    public OneUuidReturn prepareMerge(String spUUID, Map<String, Object> subchainInfo) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.prepareMerge")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("subchainInfo", subchainInfo)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public OneUuidReturn finalizeMerge(String spUUID, Map<String, Object> subchainInfo) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.finalizeMerge")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("subchainInfo", subchainInfo)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public OneUuidReturn reduceVolume(String spUUID, String sdUUID, String imageUUID,
                               String volumeUUID, boolean allowActive) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.reduceVolume").withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imageUUID)
                        .withParameter("volumeID", volumeUUID)
                        .withParameter("allowActive", allowActive)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public LeaseTaskInfoReturn addLease(String leaseUUID, String sdUUID, Map<String, Object> leaseMetadata) {
        Map<String, Object> leaseDict = new HashMap<>();
        leaseDict.put("lease_id", leaseUUID);
        leaseDict.put("sd_id", sdUUID);

        JsonRpcRequest request =
                new RequestBuilder("Lease.create")
                          .withParameter("lease", leaseDict)
                          .withParameter("metadata", leaseMetadata)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new LeaseTaskInfoReturn(response);
    }

    @Override
    public LeaseTaskInfoReturn removeLease(String leaseUUID, String sdUUID) {
        Map<String, Object> leaseDict = new HashMap<>();
        leaseDict.put("lease_id", leaseUUID);
        leaseDict.put("sd_id", sdUUID);

        JsonRpcRequest request =
                new RequestBuilder("Lease.delete")
                        .withParameter("lease", leaseDict)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new LeaseTaskInfoReturn(response);
    }

    @Override
    public LeaseInfoReturn getLeaseInfo(String leaseUUID, String sdUUID) {
        Map<String, Object> leaseDict = new HashMap<>();
        leaseDict.put("lease_id", leaseUUID);
        leaseDict.put("sd_id", sdUUID);

        JsonRpcRequest request =
                new RequestBuilder("Lease.info")
                        .withParameter("lease", leaseDict)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new LeaseInfoReturn(response);
    }

    @Override
    public OneUuidReturn switchMaster(String spUUID, String oldMasterUUID, String newMasterUUID,
            int masterVersion) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.switchMaster")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("oldMasterUUID", oldMasterUUID)
                        .withParameter("newMasterUUID", newMasterUUID)
                        .withParameter("masterVersion", masterVersion)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }
}

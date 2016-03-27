package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.GetVmsInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.IIrsServer;
import org.ovirt.engine.core.vdsbroker.irsbroker.ImagesListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsStatsAndStatusXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneImageInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StorageStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.UUIDListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.VolumeListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResizeStorageDomainPVMapReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
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
    public OneUuidReturnForXmlRpc createVolume(String sdUUID,
            String spUUID,
            String imgGUID,
            String size,
            int volFormat,
            int volType,
            int diskType,
            String volUUID,
            String descr,
            String srcImgGUID,
            String srcVolUUID,
            String initialSize) {
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
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturnForXmlRpc(response);
    }


    @Override
    public OneUuidReturnForXmlRpc copyImage(String sdUUID,
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
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OneUuidReturnForXmlRpc downloadImage(Map methodInfo,
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
        return new OneUuidReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OneUuidReturnForXmlRpc uploadImage(Map methodInfo,
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
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc mergeSnapshots(String sdUUID,
            String spUUID,
            String vmGUID,
            String imgGUID,
            String ancestorUUID,
            String successorUUID,
            String postZero) {
        // vmGUID not used and can be removed from the interface
        JsonRpcRequest request =
                new RequestBuilder("Image.mergeSnapshots").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("ancestor", ancestorUUID)
                        .withParameter("successor", successorUUID)
                        .withParameter("postZero", postZero)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public VolumeListReturnForXmlRpc reconcileVolumeChain(String spUUID, String sdUUID, String imgGUID,
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
        return new VolumeListReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc deleteVolume(String sdUUID,
            String spUUID,
            String imgGUID,
            String[] volUUID,
            String postZero,
            String force) {
        JsonRpcRequest request =
                new RequestBuilder("Image.deleteVolumes").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("volumeList", new ArrayList<>(Arrays.asList(volUUID)))
                        .withOptionalParameter("postZero", postZero)
                        .withOptionalParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public OneImageInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgGUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.getInfo").withParameter("volumeID", volUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgGUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new OneImageInfoReturnForXmlRpc(response);
    }

    @Override
    public IrsStatsAndStatusXmlRpc getIrsStats() {
        JsonRpcRequest request = new RequestBuilder("Host.getStorageRepoStats").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("stats");
        return new IrsStatsAndStatusXmlRpc(response);
    }

    @Override
    public FileStatsReturnForXmlRpc getFileStats(String sdUUID, String pattern, boolean caseSensitive) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getFileStats").withParameter("storagedomainID", sdUUID)
                        .withParameter("pattern", pattern)
                        .withParameter("caseSensitive", caseSensitive)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("fileStats");
        return new FileStatsReturnForXmlRpc(response);
    }

    @Override
    public StorageStatusReturnForXmlRpc activateStorageDomain(String sdUUID, String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.activate").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("storageStatus")
                        .withResponseType(String.class);
        return new StorageStatusReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc deactivateStorageDomain(String sdUUID,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc detachStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.detach").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("masterSdUUID", msdUUID)
                        .withParameter("masterVersion", masterVersion)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc forcedDetachStorageDomain(String sdUUID, String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.detach").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("force", true)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc attachStorageDomain(String sdUUID, String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.attach").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc setStorageDomainDescription(String sdUUID, String description) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.setDescription").withParameter("storagedomainID", sdUUID)
                        .withParameter("description", description)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc extendStorageDomain(String sdUUID, String spUUID, String[] devlist, boolean force) {
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public ResizeStorageDomainPVMapReturnForXmlRpc resizeStorageDomainPV(String sdUUID, String spUUID, String device) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.resizePV").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("guid", device)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("size");
        return new ResizeStorageDomainPVMapReturnForXmlRpc(response);
    }

    @Override
    public StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID) {
        // duplicated in IVdsServer#getStoragePoolInfo
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getInfo").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StoragePoolInfoReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.destroy").withParameter("storagepoolID", spUUID)
                        .withParameter("hostID", hostSpmId)
                        .withParameter("scsiKey", SCSIKey)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc deleteImage(String sdUUID,
            String spUUID,
            String imgGUID,
            String postZero,
            String force) {
        JsonRpcRequest request =
                new RequestBuilder("Image.delete").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("postZero", postZero)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc moveImage(String spUUID,
            String srcDomUUID,
            String dstDomUUID,
            String imgGUID,
            String vmGUID,
            int op,
            String postZero,
            String force) {
        JsonRpcRequest request =
                new RequestBuilder("Image.move").withParameter("imageID", imgGUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", srcDomUUID)
                        .withParameter("dstSdUUID", dstDomUUID)
                        .withParameter("operation", op)
                        .withParameter("postZero", postZero)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc cloneImageStructure(String spUUID,
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
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc syncImageData(String spUUID,
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
        return new OneUuidReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc updateVM(String spUUID, Map[] vms) {
        return updateVMInImportExport(spUUID, vms, null);
    }

    @Override
    public StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID) {
        return removeVM(spUUID, vmGUID, null);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc updateVMInImportExport(String spUUID, Map[] vms, String StorageDomainId) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.updateVMs").withParameter("storagepoolID", spUUID)
                        .withParameter("vmList", new ArrayList<>(Arrays.asList(vms)))
                        .withOptionalParameter("storagedomainID", StorageDomainId)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID, String storageDomainId) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.removeVM").withParameter("storagepoolID", spUUID)
                        .withParameter("vmUUID", vmGUID).withOptionalParameter("storagedomainID", storageDomainId)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GetVmsInfoReturnForXmlRpc getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getBackedUpVmsInfo").withParameter("storagepoolID", storagePoolId)
                        .withParameter("storagedomainID", storageDomainId)
                        .withParameter("vmList", new ArrayList<>(Arrays.asList(VMIDList)))
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmlist");
        return new GetVmsInfoReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc upgradeStoragePool(String storagePoolId, String targetVersion) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.upgrade").withParameter("storagepoolID", storagePoolId)
                        .withParameter("targetDomVersion", targetVersion)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public ImagesListReturnForXmlRpc getImagesList(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getImages").withParameter("storagedomainID", sdUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("imageslist")
                        .withResponseType(Object[].class);
        return new ImagesListReturnForXmlRpc(response);
    }

    @Override
    public UUIDListReturnForXmlRpc getVolumesList(String sdUUID, String spUUID, String imgUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getVolumes").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("imageID", imgUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuidlist")
                        .withResponseType(Object[].class);
        return new UUIDListReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc extendVolumeSize(String spUUID,
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
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc setVolumeDescription(String sdUUID, String spUUID, String imgGUID,
            String volUUID, String description) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.setDescription").withParameter("volumeID", volUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgGUID)
                        .withParameter("description", description)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusReturnForXmlRpc setVolumeLegality(String spID, String sdID, String imageID, String volumeID, String legality) {
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
        return new StatusReturnForXmlRpc(response);
    }
}

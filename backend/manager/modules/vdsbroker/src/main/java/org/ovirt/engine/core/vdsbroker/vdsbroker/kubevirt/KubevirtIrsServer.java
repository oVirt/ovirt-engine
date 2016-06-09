package org.ovirt.engine.core.vdsbroker.vdsbroker.kubevirt;

import java.util.Map;

import javax.inject.Inject;

import org.apache.http.impl.client.CloseableHttpClient;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.GetVmsInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.IIrsServer;
import org.ovirt.engine.core.vdsbroker.irsbroker.ImagesListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsStatsAndStatusXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneImageInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StorageStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.UUIDListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.VolumeListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResizeStorageDomainPVMapReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;

public class KubevirtIrsServer implements IIrsServer {

    private final Guid vdsId;

    @Inject
    CloseableHttpClient httpClient;

    @Inject
    ServiceDiscovery serviceDiscovery;

    public KubevirtIrsServer(Guid vdsId) {
        this.vdsId = vdsId;

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
            String srcVolUUID) {
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc downloadImage(Map methodInfo,
            String spUUID,
            String sdUUID,
            String dstImgGUID,
            String dstVolUUID) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc uploadImage(Map methodInfo,
            String spUUID,
            String sdUUID,
            String srcImgGUID,
            String srcVolUUID) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc mergeSnapshots(String sdUUID,
            String spUUID,
            String vmGUID,
            String imgGUID,
            String ancestorUUID,
            String successorUUID,
            String postZero) {
        return null;
    }

    @Override
    public VolumeListReturnForXmlRpc reconcileVolumeChain(String spUUID,
            String sdUUID,
            String imgGUID,
            String leafVolUUID) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc deleteVolume(String sdUUID,
            String spUUID,
            String imgGUID,
            String[] volUUID,
            String postZero,
            String force) {
        return null;
    }

    @Override
    public OneImageInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgGUID, String volUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc setVolumeDescription(String sdUUID,
            String spUUID,
            String imgGUID,
            String volUUID,
            String description) {
        return null;
    }

    @Override
    public IrsStatsAndStatusXmlRpc getIrsStats() {
        return null;
    }

    @Override
    public FileStatsReturnForXmlRpc getIsoList(String spUUID) {
        return null;
    }

    @Override
    public FileStatsReturnForXmlRpc getFloppyList(String spUUID) {
        return null;
    }

    @Override
    public FileStatsReturnForXmlRpc getFileStats(String sdUUID, String pattern, boolean caseSensitive) {
        return null;
    }

    @Override
    public StorageStatusReturnForXmlRpc activateStorageDomain(String sdUUID, String spUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc deactivateStorageDomain(String sdUUID,
            String spUUID,
            String msdUUID,
            int masterVersion) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc detachStorageDomain(String sdUUID,
            String spUUID,
            String msdUUID,
            int masterVersion) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc forcedDetachStorageDomain(String sdUUID, String spUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc attachStorageDomain(String sdUUID, String spUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc setStorageDomainDescription(String sdUUID, String description) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc extendStorageDomain(String sdUUID, String spUUID, String[] devlist) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc extendStorageDomain(String sdUUID,
            String spUUID,
            String[] devlist,
            boolean force) {
        return null;
    }

    @Override
    public ResizeStorageDomainPVMapReturnForXmlRpc resizeStorageDomainPV(String sdUUID, String spUUID, String device) {
        return null;
    }

    @Override
    public StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc deleteImage(String sdUUID,
            String spUUID,
            String imgGUID,
            String postZero,
            String force) {
        return null;
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
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc cloneImageStructure(String spUUID,
            String srcDomUUID,
            String imgGUID,
            String dstDomUUID) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc syncImageData(String spUUID,
            String srcDomUUID,
            String imgGUID,
            String dstDomUUID,
            String syncType) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc updateVM(String spUUID, Map[] vms) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc updateVMInImportExport(String spUUID, Map[] vms, String StorageDomainId) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID, String storageDomainId) {
        return null;
    }

    @Override
    public GetVmsInfoReturnForXmlRpc getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc upgradeStoragePool(String storagePoolId, String targetVersion) {
        return null;
    }

    @Override
    public ImagesListReturnForXmlRpc getImagesList(String sdUUID) {
        return null;
    }

    @Override
    public UUIDListReturnForXmlRpc getVolumesList(String sdUUID, String spUUID, String imgUUID) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc extendVolumeSize(String spUUID,
            String sdUUID,
            String imageUUID,
            String volumeUUID,
            String newSize) {
        return null;
    }
}

package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.utils.PDIVMapBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ExtendVmDiskSizeVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private long newSize;
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;
    private String lunGuid;

    public ExtendVmDiskSizeVDSCommandParameters(Guid vdsId,
            Guid vmId,
            Guid storagePoolId,
            Guid storageDomainId,
            Guid imageId,
            Guid imageGroupId,
            long newSize) {
        super(vdsId, vmId);
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.imageId = imageId;
        this.imageGroupId = imageGroupId;
        this.newSize = newSize;
    }

    public ExtendVmDiskSizeVDSCommandParameters(Guid vdsId,
            Guid vmId,
            String lunGuid,
            long newSize) {
        super(vdsId, vmId);
        this.lunGuid = lunGuid;
        this.newSize = newSize;
    }

    public ExtendVmDiskSizeVDSCommandParameters() {
    }

    public long getNewSize() {
        return newSize;
    }

    public void setNewSize(long newSize) {
        this.newSize = newSize;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Map<String, String> getDriveSpecs() {
        if (lunGuid != null) {
            return Collections.singletonMap("GUID", lunGuid);
        }
        return PDIVMapBuilder.create()
                .setPoolId(getStoragePoolId())
                .setDomainId(getStorageDomainId())
                .setImageGroupId(getImageGroupId())
                .setVolumeId(getImageId()).build();
    }
}

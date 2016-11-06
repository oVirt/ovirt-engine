package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class VmTemplateImportExportParameters extends VmTemplateManagementParameters implements Serializable {
    private static final long serialVersionUID = -3811237640112907464L;
    private Guid privateStorageDomainId;

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    private Guid privateStoragePoolId;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    private List<DiskImage> privateImages;

    public List<DiskImage> getImages() {
        return privateImages == null ? new LinkedList<DiskImage>() : privateImages;
    }

    public void setImages(List<DiskImage> value) {
        privateImages = value;
    }

    public VmTemplateImportExportParameters(Guid vmTemplateId, Guid storageDomainId, Guid storagePoolId) {
        super(vmTemplateId);
        this.setStorageDomainId(storageDomainId);
        this.setStoragePoolId(storagePoolId);
    }

    public VmTemplateImportExportParameters() {
        privateStorageDomainId = Guid.Empty;
        privateStoragePoolId = Guid.Empty;
    }
}

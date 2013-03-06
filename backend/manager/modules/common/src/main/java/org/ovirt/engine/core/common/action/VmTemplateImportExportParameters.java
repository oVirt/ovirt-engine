package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

import java.util.LinkedList;
import java.util.List;

public class VmTemplateImportExportParameters extends VmTemplateParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -3811237640112907464L;
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    private Guid privateStoragePoolId = new Guid();

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
    }
}

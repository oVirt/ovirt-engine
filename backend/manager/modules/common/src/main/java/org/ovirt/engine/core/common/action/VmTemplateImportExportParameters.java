package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.*;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmTemplateImportExportParameters")
public class VmTemplateImportExportParameters extends VmTemplateParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -3811237640112907464L;
    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    @XmlElement(name = "Images")
    private List<DiskImage> privateImages;

    public List<DiskImage> getImages() {
        return privateImages == null ? new LinkedList<DiskImage>() : privateImages;
    }

    public void setImages(DiskImageList value) {
        privateImages = Arrays.asList(value.getDiskImages());
    }

    public VmTemplateImportExportParameters(Guid vmTemplateId, Guid storageDomainId, Guid storagePoolId) {
        super(vmTemplateId);
        this.setStorageDomainId(storageDomainId);
        this.setStoragePoolId(storagePoolId);
    }

    public VmTemplateImportExportParameters() {
    }
}

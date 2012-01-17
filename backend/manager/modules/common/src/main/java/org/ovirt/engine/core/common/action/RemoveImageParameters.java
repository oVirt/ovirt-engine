package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveImageParameters")
public class RemoveImageParameters extends ImagesContainterParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -7905125604587768041L;
    @XmlElement(name = "ImagesToRemoveListGuidArray")
    private java.util.ArrayList<Guid> privateImagesToRemoveList;

    public java.util.ArrayList<Guid> getImagesToRemoveList() {
        return privateImagesToRemoveList == null ? new ArrayList<Guid>() : privateImagesToRemoveList;
    }

    private void setImagesToRemoveList(java.util.ArrayList<Guid> value) {
        privateImagesToRemoveList = value;
    }

    @XmlElement(name = "DiskImage")
    private DiskImage privateDiskImage;

    public DiskImage getDiskImage() {
        return privateDiskImage;
    }

    public void setDiskImage(DiskImage value) {
        privateDiskImage = value;
    }

    @XmlElement
    private boolean privateForceDelete;

    public boolean getForceDelete() {
        return privateForceDelete;
    }

    public void setForceDelete(boolean value) {
        privateForceDelete = value;
    }

    public RemoveImageParameters(Guid imageId, java.util.ArrayList<Guid> imagesToRemoveList, Guid containerID) {
        super(imageId, "", containerID);
        setImagesToRemoveList(imagesToRemoveList);
        setForceDelete(false);
    }

    public RemoveImageParameters() {
    }
}

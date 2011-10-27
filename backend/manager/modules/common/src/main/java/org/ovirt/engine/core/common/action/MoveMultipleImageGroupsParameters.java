package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MoveMultipleImageGroupsParameters")
public class MoveMultipleImageGroupsParameters extends ImagesContainterParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 4749488355281258374L;

    public MoveMultipleImageGroupsParameters(Guid containerId, java.util.ArrayList<DiskImage> imagesList,
            Guid storageDomainId) {
        super(Guid.Empty, "", containerId);
        setStorageDomainId(storageDomainId);
        setImagesList(imagesList);
    }

    @XmlElement(name = "ImagesList")
    private java.util.ArrayList<DiskImage> privateImagesList;

    public java.util.ArrayList<DiskImage> getImagesList() {
        return privateImagesList;
    }

    private void setImagesList(java.util.ArrayList<DiskImage> value) {
        privateImagesList = value;
    }

    public MoveMultipleImageGroupsParameters() {
    }
}

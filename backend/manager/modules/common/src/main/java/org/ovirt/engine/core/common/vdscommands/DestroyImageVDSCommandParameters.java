package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DestroyImageVDSCommandParameters")
public class DestroyImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    public DestroyImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            java.util.ArrayList<Guid> imageList, boolean postZero, boolean force, String compatibilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId, Guid.Empty);
        setPostZero(postZero);
        setImageList(imageList);
        setForce(force);
        setCompatibilityVersion(compatibilityVersion);
    }

    @XmlElement(name = "ImageList")
    private java.util.ArrayList<Guid> privateImageList;

    public java.util.ArrayList<Guid> getImageList() {
        return privateImageList;
    }

    private void setImageList(java.util.ArrayList<Guid> value) {
        privateImageList = value;
    }

    @XmlElement(name = "PostZero")
    private boolean privatePostZero;

    public boolean getPostZero() {
        return privatePostZero;
    }

    protected void setPostZero(boolean value) {
        privatePostZero = value;
    }

    @XmlElement(name = "Force")
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    protected void setForce(boolean value) {
        privateForce = value;
    }

    public DestroyImageVDSCommandParameters() {
    }
    @Override
    public String toString() {
        return String.format("%s, imageList = %s, postZero = %s, force = %s",
                super.toString(),
                getImageList(),
                getPostZero(),
                getForce());
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllVmSnapshotsByDriveQueryReturnValue")
public class GetAllVmSnapshotsByDriveQueryReturnValue extends VdcQueryReturnValue {
    private static final long serialVersionUID = 3743404728664179142L;
    private Guid _tryingImage = new Guid();

    /**
     * Gets or sets the trying image.
     *
     * <value>The trying image.</value>
     */
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "TryingImage")
    public Guid getTryingImage() {
        return _tryingImage;
    }

    public void setTryingImage(Guid value) {
        _tryingImage = value;
    }

    public GetAllVmSnapshotsByDriveQueryReturnValue() {
    }
}

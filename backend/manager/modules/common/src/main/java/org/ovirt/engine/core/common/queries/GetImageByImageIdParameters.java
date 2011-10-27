package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetImageByImageIdParameters")
public class GetImageByImageIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8163118634744815433L;

    public GetImageByImageIdParameters(Guid imageId) {
        _imageId = imageId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ImageId")
    private Guid _imageId = new Guid();

    public Guid getImageId() {
        return _imageId;
    }

    public GetImageByImageIdParameters() {
    }
}

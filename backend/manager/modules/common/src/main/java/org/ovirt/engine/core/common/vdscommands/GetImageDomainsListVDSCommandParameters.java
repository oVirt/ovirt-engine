package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetImageDomainsListVDSCommandParameters")
public class GetImageDomainsListVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public GetImageDomainsListVDSCommandParameters(Guid storagePoolId, Guid imageGroupId) {
        super(storagePoolId);
        setImageGroupId(imageGroupId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ImageGroupId")
    private Guid privateImageGroupId = new Guid();

    public Guid getImageGroupId() {
        return privateImageGroupId;
    }

    private void setImageGroupId(Guid value) {
        privateImageGroupId = value;
    }

    public GetImageDomainsListVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, imageGroupId = %s", super.toString(), getImageGroupId());
    }
}

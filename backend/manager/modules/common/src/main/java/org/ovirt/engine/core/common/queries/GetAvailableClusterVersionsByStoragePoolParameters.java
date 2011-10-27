package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAvailableClusterVersionsByStoragePoolParameters")
public class GetAvailableClusterVersionsByStoragePoolParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6803980112895387178L;

    public GetAvailableClusterVersionsByStoragePoolParameters() {
    }

    public GetAvailableClusterVersionsByStoragePoolParameters(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "StoragePoolId")
    private NGuid privateStoragePoolId;

    public NGuid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(NGuid value) {
        privateStoragePoolId = value;
    }

}

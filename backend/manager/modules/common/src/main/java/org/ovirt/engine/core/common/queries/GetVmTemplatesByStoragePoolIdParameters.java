package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmTemplatesByStoragePoolIdParameters")
public class GetVmTemplatesByStoragePoolIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3999600118511670633L;

    public GetVmTemplatesByStoragePoolIdParameters(Guid storagePoolId) {
        _storagePoolId = storagePoolId;
    }

    @XmlElement(name = "StoragePoolId")
    private Guid _storagePoolId = new Guid();

    public Guid getStoragePoolId() {
        return _storagePoolId;
    }

    public GetVmTemplatesByStoragePoolIdParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllIsoImagesListParameters")
public class GetAllIsoImagesListParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6098440434536241071L;

    public GetAllIsoImagesListParameters(Guid storageDomainId) {
        sdId = storageDomainId;
    }

    @XmlElement(name = "StorageDomainId")
    private Guid sdId = new Guid();

    public Guid getStorageDomainId() {
        return sdId;
    }

    public void setStorageDomainId(Guid value) {
        sdId = value;
    }

    @XmlElement(name = "ForceRefresh", nillable = true)
    private boolean forceRefresh;

    public boolean getForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetAllIsoImagesListParameters() {
    }
}

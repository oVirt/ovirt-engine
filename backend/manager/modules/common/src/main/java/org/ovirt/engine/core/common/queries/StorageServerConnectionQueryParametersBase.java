package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageServerConnectionQueryParametersBase")
public class StorageServerConnectionQueryParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2686760857776133215L;

    @XmlElement(name = "ServerConnectionId")
    private String privateServerConnectionId;

    public String getServerConnectionId() {
        return privateServerConnectionId;
    }

    private void setServerConnectionId(String value) {
        privateServerConnectionId = value;
    }

    public StorageServerConnectionQueryParametersBase(String serverConnectionId) {
        setServerConnectionId(serverConnectionId);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public StorageServerConnectionQueryParametersBase() {
    }
}

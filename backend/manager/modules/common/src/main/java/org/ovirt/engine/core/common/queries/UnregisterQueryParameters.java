package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UnregisterQueryParameters", namespace = "http://service.engine.ovirt.org")
public class UnregisterQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1416061531708794625L;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "QueryID", required = true)
    private Guid privateQueryID = new Guid();

    public Guid getQueryID() {
        return privateQueryID;
    }

    protected void setQueryID(Guid value) {
        privateQueryID = value;
    }

    public UnregisterQueryParameters(Guid queryID) {
        setQueryID(queryID);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public UnregisterQueryParameters() {
    }
}

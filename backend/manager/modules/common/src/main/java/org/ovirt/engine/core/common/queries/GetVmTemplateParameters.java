package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmTemplateParameters")
public class GetVmTemplateParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8906662143775124331L;

    public GetVmTemplateParameters(Guid id) {
        _id = id;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Id")
    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        switch (queryType) {
        case GetVmTemplate:
            return RegisterableQueryReturnDataType.IQUERYABLE;
        case GetTemplateInterfacesByTemplateId:
            return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
        default:
            return RegisterableQueryReturnDataType.UNDEFINED;
        }
    }

    public GetVmTemplateParameters() {
    }
}

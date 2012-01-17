package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CanUpdateFieldGenericParameters")
public class CanUpdateFieldGenericParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4028592911318806041L;

    public CanUpdateFieldGenericParameters(Object fieldContainer, String fieldName, Enum<?> status) {
        _fieldName = fieldName;
        _fieldContainer = fieldContainer;
        _status = status;
    }

    @XmlElement
    private Object _fieldContainer;

    public Object getFieldContainer() {
        return _fieldContainer;
    }

    @XmlElement
    private Enum<?> _status;

    public Enum<?> getStatus() {
        return _status;
    }

    @XmlElement
    private String _fieldName;

    public String getFieldName() {
        return _fieldName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public CanUpdateFieldGenericParameters() {
    }
}

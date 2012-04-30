package org.ovirt.engine.core.common.queries;

public class CanUpdateFieldGenericParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4028592911318806041L;

    public CanUpdateFieldGenericParameters(Object fieldContainer, String fieldName, Enum<?> status) {
        _fieldName = fieldName;
        _fieldContainer = fieldContainer;
        _status = status;
    }

    private Object _fieldContainer;

    public Object getFieldContainer() {
        return _fieldContainer;
    }

    private Enum<?> _status;

    public Enum<?> getStatus() {
        return _status;
    }

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

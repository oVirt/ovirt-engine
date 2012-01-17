package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RegisterQueryParameters", namespace = "http://service.engine.ovirt.org")
public class RegisterQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -1794521524205136814L;

    @XmlElement(name = "QueryID", required = true)
    private Guid privateQueryID = new Guid();

    public Guid getQueryID() {
        return privateQueryID;
    }

    protected void setQueryID(Guid value) {
        privateQueryID = value;
    }

    @XmlElement(name = "QueryType", required = true)
    private VdcQueryType privateQueryType = VdcQueryType.forValue(0);

    public VdcQueryType getQueryType() {
        return privateQueryType;
    }

    protected void setQueryType(VdcQueryType value) {
        privateQueryType = value;
    }

    @XmlElement(name = "QueryParams", required = true)
    private VdcQueryParametersBase privateQueryParams;

    public VdcQueryParametersBase getQueryParams() {
        return privateQueryParams;
    }

    protected void setQueryParams(VdcQueryParametersBase value) {
        privateQueryParams = value;
    }

    public RegisterQueryParameters(Guid queryID, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        setQueryID(queryID);
        setQueryType(queryType);
        setQueryParams(queryParams);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public RegisterQueryParameters() {
    }
}

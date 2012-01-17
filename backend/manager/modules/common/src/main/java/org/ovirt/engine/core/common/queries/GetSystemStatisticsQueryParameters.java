package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetSystemStatisticsQueryParameters", namespace = "http://service.engine.ovirt.org")
public class GetSystemStatisticsQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -1449030042803469926L;
    @XmlElement(name = "Max", required = true)
    private int privateMax;

    public int getMax() {
        return privateMax;
    }

    protected void setMax(int value) {
        privateMax = value;
    }

    public GetSystemStatisticsQueryParameters(int max) {
        setMax(max);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetSystemStatisticsQueryParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVdsByHostParameters")
public class GetVdsByHostParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3121291409184710688L;

    public GetVdsByHostParameters(String hostname) {
        _hostName = hostname;
    }

    @XmlElement(name = "HostName")
    private String _hostName;

    public String getHostName() {
        return _hostName;
    }

    public GetVdsByHostParameters() {
    }
}

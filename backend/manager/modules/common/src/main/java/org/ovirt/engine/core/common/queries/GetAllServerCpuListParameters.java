package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllServerCpuListParameters")
public class GetAllServerCpuListParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6048741913142095068L;

    public GetAllServerCpuListParameters(Version version) {
        _version = version;
    }

    @XmlElement(name = "Version")
    private Version _version;

    public Version getVersion() {
        return _version;
    }

    public GetAllServerCpuListParameters() {
    }
}

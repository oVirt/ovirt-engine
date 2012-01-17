package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVGInfoVDSCommandParameters")
public class GetVGInfoVDSCommandParameters extends RemoveVGVDSCommandParameters {
    public GetVGInfoVDSCommandParameters(Guid vdsId, String vgId) {
        super(vdsId, vgId);
    }

    public GetVGInfoVDSCommandParameters() {
    }
}

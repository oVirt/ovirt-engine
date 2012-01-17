package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllVmSnapshotsParameters")
public class GetAllVmSnapshotsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -788238010985994689L;

    public GetAllVmSnapshotsParameters(Guid id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetAllVmSnapshotsParameters() {
    }
}

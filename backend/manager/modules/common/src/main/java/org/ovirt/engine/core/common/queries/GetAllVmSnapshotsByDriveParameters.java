package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllVmSnapshotsByDriveParameters")
public class GetAllVmSnapshotsByDriveParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3768508134295864787L;

    public GetAllVmSnapshotsByDriveParameters(Guid id, String drive) {
        _id = id;
        _drive = drive;
    }

    @XmlElement(name = "Id")
    private Guid _id = new Guid();
    @XmlElement(name = "Drive")
    private String _drive;

    public Guid getId() {
        return _id;
    }

    public String getDrive() {
        return _drive;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetAllVmSnapshotsByDriveParameters() {
    }
}

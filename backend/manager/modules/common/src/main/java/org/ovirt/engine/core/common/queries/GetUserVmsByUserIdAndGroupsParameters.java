package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetUserVmsByUserIdAndGroupsParameters")
public class GetUserVmsByUserIdAndGroupsParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 98112763182708327L;

    public GetUserVmsByUserIdAndGroupsParameters(Guid id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private Guid _id = new Guid();

    private boolean includeDiskData;

    @XmlElement(name = "IncludeDiskData", defaultValue = "false")
    public boolean getIncludeDiskData() {
        return includeDiskData;
    }

    public void setIncludeDiskData(boolean includeDiskData) {
        this.includeDiskData = includeDiskData;
    }

    public Guid getId() {
        return _id;
    }

    public GetUserVmsByUserIdAndGroupsParameters() {
    }
}

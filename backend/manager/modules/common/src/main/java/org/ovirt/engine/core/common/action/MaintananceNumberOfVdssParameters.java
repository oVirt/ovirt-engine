package org.ovirt.engine.core.common.action;

import java.util.LinkedList;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MaintananceNumberOfVdssParameters")
public class MaintananceNumberOfVdssParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 8806810521151850069L;

    @XmlElement
    private java.util.List<Guid> _vdsIdList;

    @XmlElement
    private boolean _isInternal;

    public MaintananceNumberOfVdssParameters(java.util.List<Guid> vdsIdList, boolean isInternal) {
        _vdsIdList = vdsIdList;
        _isInternal = isInternal;
    }

    public Iterable<Guid> getVdsIdList() {
        return _vdsIdList == null ? new LinkedList<Guid>() : _vdsIdList;
    }

    public void setVdsIdList(java.util.List<Guid> value) {
        _vdsIdList = value;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public MaintananceNumberOfVdssParameters() {
    }
}

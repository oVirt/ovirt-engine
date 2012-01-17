package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsGroupQueryParamenters")
public class VdsGroupQueryParamenters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1936229921452072377L;

    public VdsGroupQueryParamenters(Guid vdsgroupid) {
        _vdsgroupid = vdsgroupid;
    }

    @XmlElement(name = "Vdsgroupid")
    private Guid _vdsgroupid;

    public Guid getVdsGroupId() {
        return _vdsgroupid;
    }

    public VdsGroupQueryParamenters() {
    }
}

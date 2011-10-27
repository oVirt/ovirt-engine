package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ChangeVDSClusterParameters")
public class ChangeVDSClusterParameters extends VdsActionParameters {
    private static final long serialVersionUID = -4484499078098460017L;
    @XmlElement
    private Guid clusterId;

    public ChangeVDSClusterParameters() {
    }

    public ChangeVDSClusterParameters(Guid clusterId, Guid vdsId) {
        super(vdsId);
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

}

package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ChangeVMClusterParameters")
public class ChangeVMClusterParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 7078020632613403675L;
    @XmlElement
    private Guid clusterId;

    public ChangeVMClusterParameters() {
    }

    public ChangeVMClusterParameters(Guid clusterId, Guid vmId) {
        super(vmId);
        this.clusterId = clusterId;

    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

}

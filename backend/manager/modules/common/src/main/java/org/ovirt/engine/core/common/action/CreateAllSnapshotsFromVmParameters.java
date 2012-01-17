package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.validator.constraints.NotEmpty;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateAllSnapshotsFromVmParameters")
public class CreateAllSnapshotsFromVmParameters extends VmOperationParameterBase implements java.io.Serializable {

    private static final long serialVersionUID = 3456740034406494984L;
    @XmlElement
    @NotEmpty(groups = { CreateEntity.class },
            message = "VALIDATION.DISK_IMAGE.DESCRIPTION.NOT_EMPTY")
    private String _description;
    @XmlElement(name = "DisksListJport")
    public java.util.ArrayList<String> _disksList = new java.util.ArrayList<String>();

    public CreateAllSnapshotsFromVmParameters(Guid vmId, String description) {
        super(vmId);
        _description = description;
    }

    public String getDescription() {
        return _description;
    }

    public java.util.ArrayList<String> getDisksList() {
        return _disksList == null ? new ArrayList<String>() : _disksList;
    }

    public void setDisksList(java.util.ArrayList<String> value) {
        _disksList = value;
    }

    public CreateAllSnapshotsFromVmParameters() {
    }
}

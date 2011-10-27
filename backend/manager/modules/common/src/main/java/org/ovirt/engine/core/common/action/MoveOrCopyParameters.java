package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MoveOrCopyParameters")
public class MoveOrCopyParameters extends StorageDomainParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 1051590893103934441L;

    public MoveOrCopyParameters(Guid containerId, Guid storageDomainId) {
        super(storageDomainId);
        setContainerId(containerId);
        setTemplateMustExists(false);
        setForceOverride(false);
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "ContainerId")
    private Guid privateContainerId = new Guid();

    public Guid getContainerId() {
        return privateContainerId;
    }

    public void setContainerId(Guid value) {
        privateContainerId = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "CopyCollapse")
    private boolean privateCopyCollapse;

    public boolean getCopyCollapse() {
        return privateCopyCollapse;
    }

    public void setCopyCollapse(boolean value) {
        privateCopyCollapse = value;
    }

    private java.util.HashMap<String, DiskImageBase> privateDiskInfoList;

    public java.util.HashMap<String, DiskImageBase> getDiskInfoList() {
        return privateDiskInfoList;
    }

    public void setDiskInfoList(java.util.HashMap<String, DiskImageBase> value) {
        privateDiskInfoList = value;
    }

    @XmlElement(name = "DiskInfoValueObjectMap")
    public ValueObjectMap getDiskInfoValueObjectMap() {
        return new ValueObjectMap(privateDiskInfoList, false);
    }

    public void setDiskInfoValueObjectMap(ValueObjectMap value) {
        privateDiskInfoList = (value != null) ? new java.util.HashMap<String, DiskImageBase>(value.asMap()) : null;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "TemplateMustExists")
    private boolean privateTemplateMustExists;

    public boolean getTemplateMustExists() {
        return privateTemplateMustExists;
    }

    public void setTemplateMustExists(boolean value) {
        privateTemplateMustExists = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "ForceOverride")
    private boolean privateForceOverride;

    public boolean getForceOverride() {
        return privateForceOverride;
    }

    public void setForceOverride(boolean value) {
        privateForceOverride = value;
    }

    public MoveOrCopyParameters() {
    }
}

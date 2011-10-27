package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ExportVmParameters")
public class ExportVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -1727551658690191009L;

    public ExportVmParameters(Guid vmId, boolean collapse, boolean includeTemplate, boolean force, String path) {
        super(vmId);
        setCollapse(collapse);
        setIncludeTemplate(includeTemplate);
        setForce(force);
        setPath(path);
    }

    public ExportVmParameters(Guid vmId, String path) {
        this(vmId, false, true, false, path);
    }

    @XmlElement(name = "Collapse")
    private boolean privateCollapse;

    public boolean getCollapse() {
        return privateCollapse;
    }

    public void setCollapse(boolean value) {
        privateCollapse = value;
    }

    @XmlElement(name = "IncludeTemplate")
    private boolean privateIncludeTemplate;

    public boolean getIncludeTemplate() {
        return privateIncludeTemplate;
    }

    public void setIncludeTemplate(boolean value) {
        privateIncludeTemplate = value;
    }

    @XmlElement(name = "Force")
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

    @XmlElement(name = "Path")
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    public void setPath(String value) {
        privatePath = value;
    }

    public ExportVmParameters() {
    }
}

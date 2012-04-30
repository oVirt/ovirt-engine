package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

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

    private boolean privateCollapse;

    public boolean getCollapse() {
        return privateCollapse;
    }

    public void setCollapse(boolean value) {
        privateCollapse = value;
    }

    private boolean privateIncludeTemplate;

    public boolean getIncludeTemplate() {
        return privateIncludeTemplate;
    }

    public void setIncludeTemplate(boolean value) {
        privateIncludeTemplate = value;
    }

    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

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

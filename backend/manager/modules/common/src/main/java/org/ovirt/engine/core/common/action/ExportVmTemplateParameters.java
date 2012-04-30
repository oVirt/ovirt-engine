package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class ExportVmTemplateParameters extends VmTemplateParametersBase {
    private static final long serialVersionUID = -166265474793542917L;
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

    public ExportVmTemplateParameters(Guid vmTemplateId, boolean force, String path) {
        super(vmTemplateId);
        setForce(force);
        setPath(path);
    }

    public ExportVmTemplateParameters(Guid vmTemplateId, String path) {
        this(vmTemplateId, false, path);
    }

    public ExportVmTemplateParameters() {
    }
}

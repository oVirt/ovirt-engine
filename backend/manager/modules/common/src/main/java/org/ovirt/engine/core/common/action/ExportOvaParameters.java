package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Guid;

public class ExportOvaParameters extends ActionParametersBase {

    private static final long serialVersionUID = 4931085923357685965L;

    private VmEntityType entityType;
    private Guid entityId;
    private Guid proxyHostId;
    @NotNull
    private String directory;
    private String name;

    public ExportOvaParameters() {
    }

    public VmEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(VmEntityType entityType) {
        this.entityType = entityType;
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    public Guid getProxyHostId() {
        return proxyHostId;
    }

    public void setProxyHostId(Guid proxyHostId) {
        this.proxyHostId = proxyHostId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String path) {
        this.directory = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ExternalId;

public class DirectoryIdParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 3601116892296829414L;

    private String directory;
    private ExternalId id;

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    public void setId(ExternalId id) {
        this.id = id;
    }

    public ExternalId getId() {
        return id;
    }
}

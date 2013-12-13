package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class DirectoryIdParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 3601116892296829414L;

    private String directory;
    private Guid id;

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getId() {
        return id;
    }
}

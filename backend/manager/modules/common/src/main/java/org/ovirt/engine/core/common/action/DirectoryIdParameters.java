package org.ovirt.engine.core.common.action;

import java.io.Serializable;

public class DirectoryIdParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 3601116892296829414L;

    private String directory;
    private String id;

    private String namespace;

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}

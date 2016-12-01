package org.ovirt.engine.core.aaa;


public class DirectoryGroup extends DirectoryEntry {
    private static final long serialVersionUID = 7446478647138904658L;

    public DirectoryGroup() {
        super();
    }

    public DirectoryGroup(String directoryName, String namespace, String id, String name, String displayName) {
        super(directoryName, namespace, id, name, displayName);
    }
}

package org.ovirt.engine.core.aaa;

import org.ovirt.engine.core.common.utils.ExternalId;

public class DirectoryGroup extends DirectoryEntry {
    private static final long serialVersionUID = 7446478647138904658L;

    public DirectoryGroup(String directoryName, ExternalId id, String name) {
        super(directoryName, id, name);
    }
}

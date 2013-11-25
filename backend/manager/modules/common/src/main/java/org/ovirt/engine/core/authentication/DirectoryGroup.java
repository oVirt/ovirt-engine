package org.ovirt.engine.core.authentication;

import org.ovirt.engine.core.common.utils.ExternalId;

public class DirectoryGroup extends DirectoryEntry {
    private static final long serialVersionUID = 7446478647138904658L;

    public DirectoryGroup(Directory directory, ExternalId id, String name) {
        super(directory, id, name);
    }
}

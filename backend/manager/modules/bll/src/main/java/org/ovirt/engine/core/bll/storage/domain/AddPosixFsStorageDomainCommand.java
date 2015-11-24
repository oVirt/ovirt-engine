package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.compat.Guid;

/**
 * Adds POSIX storage domain
 */
public class AddPosixFsStorageDomainCommand<T extends StorageDomainManagementParameter> extends AddStorageDomainCommon<T> {

    protected AddPosixFsStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddPosixFsStorageDomainCommand(T parameters) {
        super(parameters);
    }

}

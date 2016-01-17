package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.compat.Guid;

/**
 * Adds Gluster storage domain
 */
public class AddGlusterFsStorageDomainCommand<T extends StorageDomainManagementParameter> extends AddStorageDomainCommon<T> {

    public AddGlusterFsStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddGlusterFsStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }
}

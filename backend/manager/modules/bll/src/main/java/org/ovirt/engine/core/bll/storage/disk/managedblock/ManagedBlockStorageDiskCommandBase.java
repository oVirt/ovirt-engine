package org.ovirt.engine.core.bll.storage.disk.managedblock;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.ManagedBlockStorageDomainStatsRefresher;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Base class for MBS commands that mutate domain capacity. Subclasses call
 * refreshAffectedDomain() at the end of executeCommand() once the adapter
 * operation has succeeded - end methods are not reliably invoked across
 * this command family.
 */
public abstract class ManagedBlockStorageDiskCommandBase<T extends ActionParametersBase> extends CommandBase<T> {

    @Inject
    private ManagedBlockStorageDomainStatsRefresher mbsStatsRefresher;

    protected ManagedBlockStorageDiskCommandBase(Guid commandId) {
        super(commandId);
    }

    protected ManagedBlockStorageDiskCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    /** Schedule an async capacity refresh for the domain this command mutated. */
    protected void refreshAffectedDomain(Guid storageDomainId) {
        mbsStatsRefresher.refresh(storageDomainId);
    }
}

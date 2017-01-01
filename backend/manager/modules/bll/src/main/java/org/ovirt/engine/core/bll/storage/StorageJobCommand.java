package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.StorageJobCallback;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.compat.Guid;

public abstract class StorageJobCommand<T extends StorageJobCommandParameters> extends CommandBase<T> {

    public StorageJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (getParameters().getStorageJobId() == null) {
            getParameters().setStorageJobId(Guid.newGuid());
        }
    }

    public StorageJobCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public CommandCallback getCallback() {
        return new StorageJobCallback();
    }

    /**
     * This method let the command inspect the job error and return a different status for the job according to the
     * error
     */
    public HostJobStatus handleJobError(EngineError error) {
        return HostJobStatus.failed;
    }

    /**
     * This method let the command immediately fail when the job status is unknown or couldn't be determined.
     * Useful in cases in which we don't want to wait and we don't care to fail the operation and let the user to retry.
     */
    public boolean failJobWithUndeterminedStatus() {
        return false;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}

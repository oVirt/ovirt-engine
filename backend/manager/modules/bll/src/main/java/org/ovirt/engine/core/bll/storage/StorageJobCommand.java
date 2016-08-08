package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.StorageJobCallback;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class StorageJobCommand<T extends StorageJobCommandParameters> extends CommandBase<T> {

    public StorageJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (getParameters().getStorageJobId() == null) {
            getParameters().setStorageJobId(Guid.newGuid());
        }
    }

    @Override
    public CommandCallback getCallback() {
        return new StorageJobCallback();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}

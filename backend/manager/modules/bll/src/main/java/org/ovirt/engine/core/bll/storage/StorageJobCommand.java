package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.StorageJobCallback;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class StorageJobCommand<T extends StorageJobCommandParameters> extends BaseImagesCommand<T> {

    public StorageJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        getParameters().setStorageJobId(Guid.newGuid());
    }

    @Override
    public CommandCallback getCallback() {
        return new StorageJobCallback();
    }
}

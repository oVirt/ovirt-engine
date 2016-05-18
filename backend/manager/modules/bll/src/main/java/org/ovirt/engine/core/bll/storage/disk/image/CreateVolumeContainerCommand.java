package org.ovirt.engine.core.bll.storage.disk.image;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.StorageJobCallback;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateVolumeContainerCommand<T extends CreateVolumeContainerCommandParameters> extends
        StorageJobCommand<T> {

    public CreateVolumeContainerCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public CreateVolumeContainerCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    public CommandCallback getCallback() {
        return new StorageJobCallback();
    }

    @Override
    protected void executeCommand() {
        VdsCommandsHelper.runVdsCommandWithFailover(
                VDSCommandType.CreateVolumeContainer,
                new CreateVolumeVDSCommandParameters(
                        getParameters().getStorageDomainId(),
                        getParameters().getStorageJobId(),
                        getParameters().getSize(),
                        getParameters().getInitialSize(),
                        getParameters().getImageGroupID(),
                        getParameters().getImageId(),
                        getParameters().getSrcImageGroupId(),
                        getParameters().getSrcImageId(),
                        getParameters().getVolumeFormat(),
                        getParameters().getDescription()),
                getParameters().getStoragePoolId(),
                this);
        setSucceeded(true);
    }
}


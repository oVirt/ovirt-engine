package org.ovirt.engine.core.bll.storage.disk.image;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RetrieveImageDataParameters;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.ImageHttpAccessVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RetrieveImageDataCommand<T extends RetrieveImageDataParameters> extends ImageSpmCommand<T> {

    public RetrieveImageDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected VDSReturnValue executeVdsCommand() {
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.RetrieveImageData,
                new ImageHttpAccessVDSCommandParameters(getVdsId(),
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupID(),
                        getParameters().getImageId(),
                        getParameters().getLength()));

        setActionReturnValue(vdsReturnValue.getReturnValue());
        return vdsReturnValue;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.uploadImageToStream;
    }
}

package org.ovirt.engine.core.bll.storage.disk.image;

import static org.ovirt.engine.core.common.constants.StorageConstants.ENTITY_FENCING_GENERATION_DIFF;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class FenceVolumeJobCommand<T extends FenceVolumeJobCommandParameters> extends StorageJobCommand<T> {

    public FenceVolumeJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        UpdateVolumeVDSCommandParameters p = new UpdateVolumeVDSCommandParameters(getParameters().getStorageJobId(),
                getParameters().getImageLocationInfo());
        p.setGeneration(getParameters().getImageLocationInfo().getGeneration() + ENTITY_FENCING_GENERATION_DIFF);
        VdsCommandsHelper.runVdsCommandWithoutFailover(VDSCommandType.UpdateVolume,
                p,
                getParameters().getStoragePoolId(),
                this);
        setSucceeded(true);
    }

    @Override
    public boolean failJobWithUndeterminedStatus() {
        // We are fine with failing this operation if the job status is undetermined due to any reason, on the worst
        // case another FenceVolumeJobCommand call will be executed and this job will silently fail because of an
        // unmatched generation.
        return true;
    }
}

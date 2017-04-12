package org.ovirt.engine.core.bll.storage.disk.image;

import static org.ovirt.engine.core.common.constants.StorageConstants.ENTITY_FENCING_GENERATION_DIFF;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class FenceVolumeJobCommand<T extends FenceVolumeJobCommandParameters> extends StorageJobCommand<T> {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public FenceVolumeJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        UpdateVolumeVDSCommandParameters p = new UpdateVolumeVDSCommandParameters(getParameters().getStorageJobId(),
                getParameters().getImageLocationInfo());
        p.setGeneration(getParameters().getImageLocationInfo().getGeneration() + ENTITY_FENCING_GENERATION_DIFF);
        vdsCommandsHelper.runVdsCommandWithoutFailover(VDSCommandType.UpdateVolume,
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

    @Override
    protected void endSuccessfully() {
        endActions();
    }

    @Override
    protected void endWithFailure() {
        endActions();
    }

    private void endActions() {
        // FenceVolumeJob is executed in order to fence operations that were submitted and are supposed
        // to be performed on the volume.
        // In case of failure to fence an operation, the engine may attempt to fence it again - when the fencing
        // fails constantly the number of commands will grow indefinitely. As we store a record for each command in
        // the commands table - that's something we should avoid.
        // The engine uses the command record just for polling (to avoid executing another fence operation before the
        // previous one ended) and not for determining if the fencing succeeded (it polls the entity to verify that),
        // therefore we are fine with deleting the command entity after the execution ends.
        commandCoordinatorUtil.removeAllCommandsInHierarchy(getCommandId());
    }
}

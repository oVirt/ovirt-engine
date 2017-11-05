package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class RemoveSnapshotSingleDiskParameters extends ImagesContainterParametersBase {

    private static final long serialVersionUID = -7100810356843417368L;

    // Members used to persist data during command execution
    private RemoveSnapshotSingleDiskStep commandStep;
    private RemoveSnapshotSingleDiskStep nextCommandStep;
    private Map<RemoveSnapshotSingleDiskStep, Guid> childCommands;
    private MergeStatusReturnValue mergeStatusReturnValue;

    public RemoveSnapshotSingleDiskParameters() {
        super();
    }

    public RemoveSnapshotSingleDiskParameters(Guid imageId) {
        super(imageId);
    }

    public RemoveSnapshotSingleDiskParameters(Guid imageId, Guid containerId) {
        super(imageId, containerId);
    }

    public RemoveSnapshotSingleDiskStep getCommandStep() {
        return commandStep;
    }

    public void setCommandStep(RemoveSnapshotSingleDiskStep commandStep) {
        this.commandStep = commandStep;
    }

    public RemoveSnapshotSingleDiskStep getNextCommandStep() {
        return nextCommandStep;
    }

    public void setNextCommandStep(RemoveSnapshotSingleDiskStep nextCommandStep) {
        this.nextCommandStep = nextCommandStep;
    }

    public Map<RemoveSnapshotSingleDiskStep, Guid> getChildCommands() {
        return childCommands;
    }

    public void setChildCommands(Map<RemoveSnapshotSingleDiskStep, Guid> childCommands) {
        this.childCommands = childCommands;
    }

    public MergeStatusReturnValue getMergeStatusReturnValue() {
        return mergeStatusReturnValue;
    }

    public void setMergeStatusReturnValue(MergeStatusReturnValue mergeStatusReturnValue) {
        this.mergeStatusReturnValue = mergeStatusReturnValue;
    }
}

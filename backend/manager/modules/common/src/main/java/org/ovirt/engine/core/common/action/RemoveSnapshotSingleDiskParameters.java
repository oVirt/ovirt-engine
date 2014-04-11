package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class RemoveSnapshotSingleDiskParameters extends ImagesContainterParametersBase {

    private static final long serialVersionUID = -7100810356843417368L;

    // Members used to persist data during command execution
    private RemoveSnapshotSingleDiskLiveStep commandStep;
    private RemoveSnapshotSingleDiskLiveStep nextCommandStep;
    private Map<RemoveSnapshotSingleDiskLiveStep, Guid> childCommands;
    private boolean mergeCommandComplete;
    private MergeStatusReturnValue mergeStatusReturnValue;
    private boolean destroyImageCommandComplete;

    public RemoveSnapshotSingleDiskParameters() {
        super();
    }

    public RemoveSnapshotSingleDiskParameters(Guid imageId) {
        super(imageId);
    }

    public RemoveSnapshotSingleDiskParameters(Guid imageId, Guid containerId) {
        super(imageId, containerId);
    }

    public RemoveSnapshotSingleDiskLiveStep getCommandStep() {
        return commandStep;
    }

    public void setCommandStep(RemoveSnapshotSingleDiskLiveStep commandStep) {
        this.commandStep = commandStep;
    }

    public RemoveSnapshotSingleDiskLiveStep getNextCommandStep() {
        return nextCommandStep;
    }

    public void setNextCommandStep(RemoveSnapshotSingleDiskLiveStep nextCommandStep) {
        this.nextCommandStep = nextCommandStep;
    }

    public Map<RemoveSnapshotSingleDiskLiveStep, Guid> getChildCommands() {
        return childCommands;
    }

    public void setChildCommands(Map<RemoveSnapshotSingleDiskLiveStep, Guid> childCommands) {
        this.childCommands = childCommands;
    }

    public boolean isMergeCommandComplete() {
        return mergeCommandComplete;
    }

    public void setMergeCommandComplete(boolean mergeCommandComplete) {
        this.mergeCommandComplete = mergeCommandComplete;
    }

    public MergeStatusReturnValue getMergeStatusReturnValue() {
        return mergeStatusReturnValue;
    }

    public void setMergeStatusReturnValue(MergeStatusReturnValue mergeStatusReturnValue) {
        this.mergeStatusReturnValue = mergeStatusReturnValue;
    }

    public boolean isDestroyImageCommandComplete() {
        return destroyImageCommandComplete;
    }

    public void setDestroyImageCommandComplete(boolean destroyImageCommandComplete) {
        this.destroyImageCommandComplete = destroyImageCommandComplete;
    }
}

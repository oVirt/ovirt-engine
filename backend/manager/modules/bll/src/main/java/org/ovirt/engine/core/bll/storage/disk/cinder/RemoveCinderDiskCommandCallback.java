package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveCinderDiskCommandCallback<T extends RemoveCinderVolumeParentCommand<? extends RemoveCinderDiskParameters>> extends AbstractCinderDiskCommandCallback<T> {
    private static final Logger log = LoggerFactory.getLogger(RemoveCinderDiskCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        super.doPolling(cmdId, childCmdIds);
        boolean anyFailed = false;
        for (Guid childCmdId : childCmdIds) {
            CommandStatus commandStatus = CommandCoordinatorUtil.getCommandStatus(childCmdId);
            switch (commandStatus) {
            case NOT_STARTED:
                break;
            case ACTIVE:
                log.info("Waiting on RemoveCinderDiskVolumeCommandCallback child commands to complete");
                return;
            case ENDED_SUCCESSFULLY:
            case SUCCEEDED:
                if (!getCommand().getParameters().getFinishedChildCmdIds().contains(childCmdId)) {
                    int removedVolumeIndex = getCommand().getParameters().getRemovedVolumeIndex();
                    final CinderDisk cinderVolume =
                            getCommand().getParameters()
                                    .getChildCommandsParameters()
                                    .get(removedVolumeIndex)
                                    .getRemovedVolume();
                    getCommand().removeDiskFromDbCallBack(cinderVolume);
                    getCommand().getParameters().getFinishedChildCmdIds().add(childCmdId);
                }

                break;
            case ENDED_WITH_FAILURE:
            case FAILED:
            case FAILED_RESTARTED:
            case UNKNOWN:
                anyFailed = true;
                if (!getCommand().getParameters().getFinishedChildCmdIds().contains(childCmdId)) {
                    getCommand().getParameters().getFinishedChildCmdIds().add(childCmdId);
                }
                break;
            default:
                log.error("Invalid command status: '{}", commandStatus);
                break;
            }
        }

        if (allChildCommandWereExecuted()) {
            getCommand().setCommandStatus(anyFailed ? CommandStatus.FAILED : CommandStatus.SUCCEEDED);
        } else if (allChildCmdIdsFinished(childCmdIds)) {
            if (anyFailed) {
                getCommand().setCommandStatus(CommandStatus.FAILED);
            } else {
                int removedVolumeIndex = getCommand().getParameters().getRemovedVolumeIndex();
                removedVolumeIndex++;
                getCommand().getParameters().setRemovedVolumeIndex(removedVolumeIndex);
                getCommand().removeCinderVolume(removedVolumeIndex, getDisk().getStorageIds().get(0));
            }
        }
    }

    private boolean allChildCmdIdsFinished(List<Guid> childCmdIds) {
        return getCommand().getParameters().getFinishedChildCmdIds().size() == childCmdIds.size();
    }

    private boolean allChildCommandWereExecuted() {
        return getCommand().getParameters().getFinishedChildCmdIds().size() == getCommand().getParameters()
                .getChildCommandsParameters()
                .size();
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        super.onFailed(cmdId, childCmdIds);
        getCommand().getParameters().setTaskGroupSuccess(false);
        log.error("Failed deleting volume/snapshot from Cinder. ID: {}", getDiskId());
        getCommand().endAction();
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);
        log.info("Volume/Snapshot has been successfully deleted from Cinder. ID: {}", getDiskId());
        getCommand().endAction();
    }

    @Override
    protected CinderDisk getDisk() {
        return getCommand().getParameters().getRemovedVolume();
    }

    @Override
    protected Guid getDiskId() {
        return getCommand().getParameters().getRemovedVolume().getImageId();
    }

    @Override
    protected CinderBroker getCinderBroker() {
        return getCommand().getCinderBroker();
    }
}

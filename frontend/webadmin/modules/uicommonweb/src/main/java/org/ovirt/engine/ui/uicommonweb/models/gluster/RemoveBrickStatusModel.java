package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

public class RemoveBrickStatusModel extends VolumeRebalanceStatusModel {

    private List<GlusterBrickEntity> bricks;

    private UICommand stopRemoveBricksCommand;

    private UICommand commitRemoveBricksCommand;

    private UICommand retainBricksCommand;

    public RemoveBrickStatusModel(GlusterVolumeEntity volumeEntity, List<GlusterBrickEntity> bricks) {
        super(volumeEntity);
        setBricks(bricks);
    }

    public void setBricks(List<GlusterBrickEntity> bricks) {
        this.bricks = bricks;
    }

    public List<GlusterBrickEntity> getBricks() {
        return this.bricks;
    }

    public void addStopRemoveBricksCommand(UICommand command) {
        getCommands().add(command);
        this.stopRemoveBricksCommand = command;
    }

    public UICommand getStopRemoveBricksCommand() {
        return this.stopRemoveBricksCommand;
    }

    public void addRetainBricksCommand(UICommand command) {
        getCommands().add(command);
        this.retainBricksCommand = command;
    }

    public UICommand getRetainBricksCommand() {
        return this.retainBricksCommand;
    }

    public void addCommitRemoveBricksCommand(UICommand command) {
        getCommands().add(command);
        this.commitRemoveBricksCommand = command;
    }

    public UICommand getCommitRemoveBricksCommand() {
        return this.commitRemoveBricksCommand;
    }

    @Override
    public void showStatus(GlusterVolumeTaskStatusEntity statusEntity) {
        super.showStatus(statusEntity);
        getStopRemoveBricksCommand().setIsExecutionAllowed(statusEntity.getStatusSummary().getStatus() == JobExecutionStatus.STARTED);
        getCommitRemoveBricksCommand().setIsExecutionAllowed(statusEntity.getStatusSummary()
                .getStatus() == JobExecutionStatus.FINISHED);
        getRetainBricksCommand().setIsExecutionAllowed(statusEntity.getStatusSummary()
                .getStatus() == JobExecutionStatus.FINISHED);
    }

    @Override
    public void refreshDetails(GlusterVolumeEntity volumeEntity) {
        AsyncDataProvider.getInstance().getGlusterRemoveBricksStatus(new AsyncQuery<>(returnValue -> {
            GlusterVolumeTaskStatusEntity statusEntity = returnValue.getReturnValue();
            if (statusEntity != null) {
                showStatus(statusEntity);
            }
        }), volumeEntity.getClusterId(), volumeEntity.getId(), getBricks());
    }
}

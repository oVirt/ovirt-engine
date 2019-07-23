package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeResetBrickActionParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class VolumeBrickListModel extends SearchableListModel<GlusterVolumeEntity, GlusterBrickEntity> {
    private String glusterMetaVolumeName;

    @Override
    protected String getListName() {
        return "VolumeBrickListModel"; //$NON-NLS-1$
    }

    public VolumeBrickListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().bricksTitle());
        setHelpTag(HelpTag.bricks);
        setHashName("bricks"); //$NON-NLS-1$
        setAddBricksCommand(new UICommand("Add Bricks", this)); //$NON-NLS-1$
        setRemoveBricksCommand(new UICommand("Remove Bricks", this)); //$NON-NLS-1$
        setStopRemoveBricksCommand(new UICommand("StopRemoveBricks", this)); //$NON-NLS-1$
        setCommitRemoveBricksCommand(new UICommand("CommitRemoveBricks", this)); //$NON-NLS-1$
        setStatusRemoveBricksCommand(new UICommand("StatusRemoveBricks", this)); //$NON-NLS-1$
        setRetainBricksCommand(new UICommand("RetainBricks", this)); //$NON-NLS-1$
        setReplaceBrickCommand(new UICommand("Replace Brick", this)); //$NON-NLS-1$
        setBrickAdvancedDetailsCommand(new UICommand("Brick Advanced Details", this)); //$NON-NLS-1$
        setResetBrickCommand(new UICommand("Reset Brick", this)); //$NON-NLS-1$
        getReplaceBrickCommand().setIsAvailable(true);

        // Get the meta volume name
        AsyncDataProvider.getInstance()
                .getConfigFromCache(new GetConfigurationValueParameters(ConfigValues.GlusterMetaVolumeName,
                        AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                        new AsyncQuery<String>(returnValue -> glusterMetaVolumeName = returnValue));
    }

    private GlusterVolumeEntity volumeEntity;

    public void setVolumeEntity(GlusterVolumeEntity volumeEntity) {
        this.volumeEntity = volumeEntity;
        updateRemoveBrickActionsAvailability(volumeEntity);
    }

    public GlusterVolumeEntity getVolumeEntity() {
        return volumeEntity;
    }

    private UICommand addBricksCommand;

    public UICommand getAddBricksCommand() {
        return addBricksCommand;
    }

    private void setAddBricksCommand(UICommand value) {
        addBricksCommand = value;
    }

    private UICommand removeBricksCommand;

    public UICommand getRemoveBricksCommand() {
        return removeBricksCommand;
    }

    private void setRemoveBricksCommand(UICommand value) {
        removeBricksCommand = value;
    }

    private UICommand stopRemoveBricksCommand;

    public UICommand getStopRemoveBricksCommand() {
        return stopRemoveBricksCommand;
    }

    private void setStopRemoveBricksCommand(UICommand value) {
        stopRemoveBricksCommand = value;
    }

    private UICommand commitRemoveBricksCommand;

    public UICommand getCommitRemoveBricksCommand() {
        return commitRemoveBricksCommand;
    }

    private void setCommitRemoveBricksCommand(UICommand value) {
        commitRemoveBricksCommand = value;
    }

    private UICommand statusRemoveBricksCommand;

    public UICommand getStatusRemoveBricksCommand() {
        return statusRemoveBricksCommand;
    }

    private void setStatusRemoveBricksCommand(UICommand value) {
        statusRemoveBricksCommand = value;
    }

    private UICommand retainBricksCommand;

    private void setRetainBricksCommand(UICommand value) {
        retainBricksCommand = value;
    }

    public UICommand getRetainBricksCommand() {
        return retainBricksCommand;
    }

    private UICommand replaceBrickCommand;

    public UICommand getReplaceBrickCommand() {
        return replaceBrickCommand;
    }

    private void setReplaceBrickCommand(UICommand value) {
        replaceBrickCommand = value;
    }

    private UICommand brickAdvancedDetailsCommand;

    public UICommand getBrickAdvancedDetailsCommand() {
        return brickAdvancedDetailsCommand;
    }

    private void setBrickAdvancedDetailsCommand(UICommand value) {
        brickAdvancedDetailsCommand = value;
    }

    private UICommand resetBrickCommand;

    public UICommand getResetBrickCommand() {
        return resetBrickCommand;
    }

    private void setResetBrickCommand(UICommand value) {
        resetBrickCommand = value;
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        GlusterVolumeEntity volumeEntity = getEntity();

        boolean allowRemove = true;
        boolean allowReplace = true;
        boolean allowAdvanced = true;
        boolean allowAdd = true;
        boolean allowReset = true;

        if (volumeEntity == null || volumeEntity.getVolumeType().isDispersedType()
                || !volumeEntity.getVolumeType().isSupported()) {
            allowRemove = false;
            allowAdd = false;
        }

        if (volumeEntity == null || getSelectedItems() == null || getSelectedItems().size() == 0) {
            allowRemove = false;
            allowReplace = false;
            allowAdvanced = false;
            allowReset = false;
        } else {
            if (getSelectedItems().size() == 1) {
                allowAdvanced = volumeEntity.isOnline() && getSelectedItems().get(0).isOnline();
            } else {
                allowReplace = false;
                allowAdvanced = false;
                allowReset = false;
            }
            GlusterAsyncTask volumeTask = volumeEntity.getAsyncTask();
            if (volumeTask != null
                    && (volumeTask.getStatus() == JobExecutionStatus.STARTED
                            || volumeTask.getType() == GlusterTaskType.REMOVE_BRICK
                                    && volumeTask.getStatus() == JobExecutionStatus.FINISHED)) {
                allowRemove = false;
                allowReplace = false;
                allowReset = false;
            } else if (volumeEntity.getVolumeType() == GlusterVolumeType.STRIPE
                    || getSelectedItems().size() == volumeEntity.getBricks().size()) {
                allowRemove = false;
            } else if (volumeEntity.getVolumeType() == GlusterVolumeType.REPLICATE
                    && (volumeEntity.getBricks().size() == VolumeListModel.REPLICATE_COUNT_DEFAULT
                            || getSelectedItems().size() > 1)) {
                allowRemove = false;
            }
        }

        getRemoveBricksCommand().setIsExecutionAllowed(allowRemove);
        getReplaceBrickCommand().setIsExecutionAllowed(allowReplace);
        getBrickAdvancedDetailsCommand().setIsExecutionAllowed(allowAdvanced);
        getAddBricksCommand().setIsExecutionAllowed(allowAdd);
        getResetBrickCommand().setIsExecutionAllowed(allowReset);
    }

    public void updateRemoveBrickActionsAvailability(GlusterVolumeEntity volumeEntity) {
        boolean allowStopRemove = true;
        boolean allowCommitRemove = true;
        boolean allowStatusRemove = true;
        boolean allowRetain = true;

        // Stop/Commit/Retain brick removal can be invoked from the Volume(tab) Activities menu as well
        // So no need to check if there are any bricks selected or not, command availability
        // will be decided based on the task on the volume
        allowStopRemove =
                volumeEntity != null && volumeEntity.getAsyncTask() != null
                        && volumeEntity.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK
                        && volumeEntity.getAsyncTask().getStatus() == JobExecutionStatus.STARTED;

        allowCommitRemove =
                volumeEntity != null && volumeEntity.getAsyncTask() != null
                        && volumeEntity.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK
                        && volumeEntity.getAsyncTask().getStatus() == JobExecutionStatus.FINISHED;
        allowRetain =
                volumeEntity != null && volumeEntity.getAsyncTask() != null
                        && volumeEntity.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK
                        && volumeEntity.getAsyncTask().getStatus() == JobExecutionStatus.FINISHED;

        allowStatusRemove =
                volumeEntity != null && volumeEntity.getAsyncTask() != null
                        && volumeEntity.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK;

        getStopRemoveBricksCommand().setIsExecutionAllowed(allowStopRemove);
        getCommitRemoveBricksCommand().setIsExecutionAllowed(allowCommitRemove);
        getStatusRemoveBricksCommand().setIsExecutionAllowed(allowStatusRemove);
        getRetainBricksCommand().setIsExecutionAllowed(allowRetain);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void syncSearch() {
        if (getEntity() != null) {
            GlusterVolumeEntity glusterVolumeEntity = getEntity();
            // If the items are same, just fire the item changed event to make sure that items are displayed
            if (getItems() == glusterVolumeEntity.getBricks()) {
                getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            } else {
                setItems(glusterVolumeEntity.getBricks());
            }
        } else {
            setItems(null);
        }
    }

    private void checkUpServerAndAddBricks() {
        if (getWindow() != null) {
            return;
        }

        final GlusterVolumeEntity volumeEntity = getEntity();

        if (volumeEntity == null) {
            return;
        }

        AsyncDataProvider.getInstance().isAnyHostUpInCluster(new AsyncQuery<>(clusterHasUpHost -> {
            if (clusterHasUpHost) {
                addBricks(volumeEntity);
            } else {
                ConfirmationModel model = new ConfirmationModel();
                setWindow(model);
                model.setTitle(ConstantsManager.getInstance().getConstants().addBricksTitle());
                model.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .cannotAddBricksNoUpServerFound());
                model.setHelpTag(HelpTag.cannot_add_bricks);
                model.setHashName("cannot_add_bricks"); //$NON-NLS-1$

                UICommand command = new UICommand("Cancel", VolumeBrickListModel.this); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().close());
                command.setIsCancel(true);
                model.getCommands().add(command);
                return;
            }
        }), volumeEntity.getClusterName());
    }

    private void addBricks(final GlusterVolumeEntity volumeEntity) {
        final VolumeBrickModel volumeBrickModel = new VolumeBrickModel();
        volumeBrickModel.getReplicaCount().setEntity(volumeEntity.getReplicaCount());
        volumeBrickModel.getReplicaCount().setIsChangeable(true);
        volumeBrickModel.getReplicaCount().setIsAvailable(volumeEntity.getVolumeType().isReplicatedType());

        volumeBrickModel.getStripeCount().setEntity(volumeEntity.getStripeCount());
        volumeBrickModel.getStripeCount().setIsChangeable(true);
        volumeBrickModel.getStripeCount().setIsAvailable(volumeEntity.getVolumeType().isStripedType());

        volumeBrickModel.setTitle(ConstantsManager.getInstance().getConstants().addBricksTitle());
        volumeBrickModel.setHelpTag(HelpTag.add_bricks);
        volumeBrickModel.setHashName("add_bricks"); //$NON-NLS-1$
        volumeBrickModel.getVolumeType().setEntity(volumeEntity.getVolumeType());

        setWindow(volumeBrickModel);

        AsyncDataProvider.getInstance().getClusterById(volumeBrickModel.asyncQuery(cluster -> {
            volumeBrickModel.getForce().setIsAvailable(true);
            volumeBrickModel.setIsBrickProvisioningSupported();
            AsyncDataProvider.getInstance().getHostListByCluster(volumeBrickModel.asyncQuery(hostList -> {
                Iterator<VDS> iterator = hostList.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getStatus() != VDSStatus.Up) {
                        iterator.remove();
                    }
                }

                volumeBrickModel.setHostList(hostList);
            }), cluster.getName());
        }), volumeEntity.getClusterId());

        // TODO: fetch the mount points to display
        volumeBrickModel.getBricks().setItems(new ArrayList<EntityModel<GlusterBrickEntity>>());

        UICommand command = UICommand.createDefaultOkUiCommand("OnAddBricks", this); //$NON-NLS-1$
        volumeBrickModel.getCommands().add(command);

        volumeBrickModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onAddBricks() {
        VolumeBrickModel volumeBrickModel = (VolumeBrickModel) getWindow();
        if (volumeBrickModel == null) {
            return;
        }

        if (!volumeBrickModel.validate()) {
            return;
        }

        GlusterVolumeEntity volumeEntity = getEntity();
        if (volumeEntity == null) {
            return;
        }

        ArrayList<GlusterBrickEntity> brickList = new ArrayList<>();
        for (Object model : volumeBrickModel.getBricks().getItems()) {
            GlusterBrickEntity brickEntity = (GlusterBrickEntity) ((EntityModel) model).getEntity();
            brickEntity.setVolumeId(volumeEntity.getId());
            brickList.add(brickEntity);
        }

        volumeBrickModel.setMessage(null);

        if (!validateReplicaStripeCount(volumeEntity, volumeBrickModel)) {
            return;
        }

        if (brickList.size() == 0) {
            volumeBrickModel.setMessage(ConstantsManager.getInstance().getConstants().emptyAddBricksMsg());
            return;
        }

        if (!VolumeBrickModel.validateBrickCount(volumeEntity.getVolumeType(),
                volumeEntity.getBricks().size()
                        + brickList.size(),
                volumeBrickModel.getReplicaCountValue(),
                volumeBrickModel.getStripeCountValue(),
                false)) {
            volumeBrickModel.setMessage(VolumeBrickModel.getValidationFailedMsg(volumeEntity.getVolumeType(), false));
            return;
        }

        if ((volumeEntity.getVolumeType() == GlusterVolumeType.REPLICATE
                || volumeEntity.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE)
                && !volumeBrickModel.validateReplicateBricks(volumeEntity.getReplicaCount(),
                        volumeEntity.getBricks())) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .addBricksReplicateConfirmationTitle());
            confirmModel.setHelpTag(HelpTag.add_bricks_confirmation);
            confirmModel.setHashName("add_bricks_confirmation"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .addBricksToReplicateVolumeFromSameServerMsg());

            UICommand okCommand = new UICommand("OnAddBricksInternal", this); //$NON-NLS-1$
            okCommand.setTitle(ConstantsManager.getInstance().getConstants().yes());
            okCommand.setIsDefault(true);
            getConfirmWindow().getCommands().add(okCommand);

            UICommand cancelCommand = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().no());
            cancelCommand.setIsCancel(true);
            getConfirmWindow().getCommands().add(cancelCommand);
        } else {
            onAddBricksInternal();
        }
    }

    private void onAddBricksInternal() {

        cancelConfirmation();

        VolumeBrickModel volumeBrickModel = (VolumeBrickModel) getWindow();
        GlusterVolumeEntity volumeEntity = getEntity();

        ArrayList<GlusterBrickEntity> brickList = new ArrayList<>();
        for (Object model : volumeBrickModel.getBricks().getItems()) {
            GlusterBrickEntity brickEntity = (GlusterBrickEntity) ((EntityModel) model).getEntity();
            brickEntity.setVolumeId(volumeEntity.getId());
            brickList.add(brickEntity);
        }

        volumeBrickModel.startProgress();

        GlusterVolumeBricksActionParameters parameter = new GlusterVolumeBricksActionParameters(volumeEntity.getId(),
                brickList,
                volumeBrickModel.getReplicaCountValue(),
                volumeBrickModel.getStripeCountValue(),
                volumeBrickModel.getForce().getEntity());

        Frontend.getInstance().runAction(ActionType.AddBricksToGlusterVolume, parameter, result -> {
            VolumeBrickListModel localModel = (VolumeBrickListModel) result.getState();
            localModel.postOnAddBricks(result.getReturnValue());

        }, this);
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    public void postOnAddBricks(ActionReturnValue returnValue) {
        VolumeBrickModel model = (VolumeBrickModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
        }
    }

    public void cancel() {
        setWindow(null);
    }

    private boolean validateReplicaStripeCount(GlusterVolumeEntity volumeEntity, VolumeBrickModel volumeBrickModel) {
        if (volumeEntity.getVolumeType().isReplicatedType()) {
            int newReplicaCount = volumeBrickModel.getReplicaCountValue();
            if (newReplicaCount > (volumeEntity.getReplicaCount() + 1)) {
                volumeBrickModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .addBricksReplicaCountIncreaseValidationMsg());
                return false;
            }
        }
        if (volumeEntity.getVolumeType().isStripedType()) {
            int newStripeCount = volumeBrickModel.getStripeCountValue();
            if (newStripeCount > (volumeEntity.getStripeCount() + 1)) {
                volumeBrickModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .addBricksStripeCountIncreaseValidationMsg());
                return false;
            }
        }
        return true;
    }

    private void removeBricks() {
        if (getSelectedItems() == null || getSelectedItems().isEmpty()) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        GlusterVolumeEntity volumeEntity = getEntity();

        RemoveBrickModel removeBrickModel = new RemoveBrickModel();
        removeBrickModel.setHelpTag(HelpTag.volume_remove_bricks);
        removeBrickModel.setHashName("volume_remove_bricks"); //$NON-NLS-1$
        removeBrickModel.setTitle(ConstantsManager.getInstance().getConstants().removeBricksTitle());
        setWindow(removeBrickModel);

        removeBrickModel.setReplicaCount(volumeEntity.getReplicaCount());
        removeBrickModel.setStripeCount(volumeEntity.getStripeCount());

        ArrayList<String> list = new ArrayList<>();
        for (GlusterBrickEntity item : getSelectedItems()) {
            list.add(item.getQualifiedName());
        }
        removeBrickModel.setItems(list);

        if (!validateRemoveBricks(volumeEntity.getVolumeType(),
                getSelectedItems(),
                volumeEntity.getBricks(),
                removeBrickModel)) {
            removeBrickModel.setMigrationSupported(false);
            removeBrickModel.setMessage(removeBrickModel.getValidationMessage());
        } else {
            removeBrickModel.setMigrationSupported(volumeEntity.getVolumeType().isDistributedType());
            removeBrickModel.getMigrateData().setEntity(removeBrickModel.isMigrationSupported());

            if (removeBrickModel.isReduceReplica()) {
                if (volumeEntity.getName().equals(glusterMetaVolumeName) && volumeEntity.getReplicaCount() <= 3) {
                    removeBrickModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .removeMetaVolumeBricksMessage());
                    removeBrickModel.setNote(ConstantsManager.getInstance()
                            .getConstants()
                            .removeMetaVolumeBricksWarning());
                } else {
                    removeBrickModel.setMessage(ConstantsManager.getInstance()
                            .getMessages()
                            .removeBricksReplicateVolumeMessage(volumeEntity.getReplicaCount(),
                                    volumeEntity.getReplicaCount() - 1));
                    removeBrickModel.setMigrationSupported(false);
                    removeBrickModel.getMigrateData().setEntity(false);
                }
            } else {
                removeBrickModel.setMessage(ConstantsManager.getInstance().getConstants().removeBricksMessage());
            }

            UICommand command = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
            removeBrickModel.getCommands().add(command);
        }

        removeBrickModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    public boolean validateRemoveBricks(GlusterVolumeType volumeType,
            List<GlusterBrickEntity> selectedBricks,
            List<GlusterBrickEntity> brickList,
            RemoveBrickModel removeBrickModel) {
        boolean valid = true;

        switch (volumeType) {
        case REPLICATE:
            if (selectedBricks.size() > 1) {
                valid = false;
                removeBrickModel.setValidationMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .cannotRemoveBricksReplicateVolume());
            }
            removeBrickModel.setReplicaCount(removeBrickModel.getReplicaCount() - 1);
            removeBrickModel.setReduceReplica(true);
            break;

        case DISTRIBUTED_REPLICATE:
            valid = validateDistriputedReplicateRemove(volumeType, selectedBricks, brickList, removeBrickModel);
            if (!valid) {
                removeBrickModel.setValidationMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .cannotRemoveBricksDistributedReplicateVolume());
            }
            break;

        case DISTRIBUTED_STRIPE:
            valid = validateDistriputedStripeRemove(volumeType, selectedBricks, brickList, removeBrickModel);
            if (!valid) {
                removeBrickModel.setValidationMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .cannotRemoveBricksDistributedStripeVolume());
            }
            break;

        case STRIPED_REPLICATE:
            valid = validateStripedReplicateRemove(volumeType, selectedBricks, brickList, removeBrickModel);
            if (!valid) {
                removeBrickModel.setValidationMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .cannotRemoveBricksStripedReplicateVolume());
            }
            break;

        case DISTRIBUTED_STRIPED_REPLICATE:
            valid = validateDistributedStripedReplicateRemove(volumeType, selectedBricks, brickList, removeBrickModel);
            if (!valid) {
                removeBrickModel.setValidationMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .cannotRemoveBricksDistributedStripedReplicateVolume());
            }
            break;

        default:
            break;
        }

        return valid;
    }

    public boolean validateStripedReplicateRemove(GlusterVolumeType volumeType,
            List<GlusterBrickEntity> selectedBricks,
            List<GlusterBrickEntity> brickList,
            RemoveBrickModel removeBrickModel) {
        // validate only count in the UI
        int stripeCount = removeBrickModel.getStripeCount();
        int replicaCount = removeBrickModel.getReplicaCount();

        if ((brickList.size() - selectedBricks.size()) != stripeCount * replicaCount) {
            return false;
        }

        return true;
    }

    public boolean validateDistributedStripedReplicateRemove(GlusterVolumeType volumeType,
            List<GlusterBrickEntity> selectedBricks,
            List<GlusterBrickEntity> brickList,
            RemoveBrickModel removeBrickModel) {
        int stripeCount = removeBrickModel.getStripeCount();
        int replicaCount = removeBrickModel.getReplicaCount();

        if (selectedBricks.size() % (stripeCount * replicaCount) != 0) {
            return false;
        }

        return true;
    }

    public boolean validateDistriputedReplicateRemove(GlusterVolumeType volumeType,
            List<GlusterBrickEntity> selectedBricks,
            List<GlusterBrickEntity> brickList,
            RemoveBrickModel removeBrickModel) {
        int replicaCount = removeBrickModel.getReplicaCount();
        int distributions = brickList.size() / replicaCount;

        // Key - No.of.bricks selected in sub-volume
        // Value - No.of sub-volumes which has 'Key' no.of bricks selected
        Map<Integer, Integer> selectedBricksToSubVolumesMap = new HashMap<>();

        for (int distIndex = 0; distIndex < distributions; distIndex++) {

            List<GlusterBrickEntity> bricksInSubVolumeList =
                    brickList.subList(distIndex * replicaCount, (distIndex * replicaCount) + replicaCount);

            int selectedBricksInSubVolume = 0;
            for (GlusterBrickEntity brick : bricksInSubVolumeList) {
                if (selectedBricks.contains(brick)) {
                    selectedBricksInSubVolume++;
                }
            }
            if (selectedBricksInSubVolume > 0) {
                if (!selectedBricksToSubVolumesMap.containsKey(selectedBricksInSubVolume)) {
                    selectedBricksToSubVolumesMap.put(selectedBricksInSubVolume, 0);
                }
                selectedBricksToSubVolumesMap.put(selectedBricksInSubVolume,
                        selectedBricksToSubVolumesMap.get(selectedBricksInSubVolume) + 1);
            }
        }

        // If the size of the map is more than 1, then the user has selected different no.of bricks from different
        // sub-volumes, hence not valid for removal.
        if (selectedBricksToSubVolumesMap.size() == 1) {
            // If the user has selected once brick from each sub-volume, then replica count needs to be reduced
            if (selectedBricksToSubVolumesMap.containsKey(1) && selectedBricksToSubVolumesMap.get(1) == distributions) {
                removeBrickModel.setReplicaCount(removeBrickModel.getReplicaCount() - 1);
                removeBrickModel.setReduceReplica(true);
                return true;
            } else if (selectedBricksToSubVolumesMap.containsKey(replicaCount)) {
                return true;
            }
            return false;
        }

        return false;
    }

    public boolean validateDistriputedStripeRemove(GlusterVolumeType volumeType,
            List<GlusterBrickEntity> selectedBricks,
            List<GlusterBrickEntity> brickList,
            RemoveBrickModel removeBrickModel) {
        int stripeCount = removeBrickModel.getStripeCount();
        int distributions = brickList.size() / stripeCount;

        if (selectedBricks.size() != stripeCount) {
            return false;
        }

        for (int i = 0; i < distributions; i++) {
            List<GlusterBrickEntity> subBrickList =
                    brickList.subList(i * stripeCount, (i * stripeCount) + stripeCount);
            if (subBrickList.containsAll(selectedBricks)) {
                return true;
            }
        }

        return false;
    }

    private void onRemoveBricks() {
        if (getWindow() == null) {
            return;
        }

        RemoveBrickModel model = (RemoveBrickModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (getSelectedItems() == null || getSelectedItems().isEmpty()) {
            return;
        }

        GlusterVolumeEntity volumeEntity = getEntity();

        GlusterVolumeRemoveBricksParameters parameter =
                new GlusterVolumeRemoveBricksParameters(volumeEntity.getId(), getSelectedItems());

        if (volumeEntity.getVolumeType() == GlusterVolumeType.REPLICATE) {
            parameter.setReplicaCount(volumeEntity.getReplicaCount() - 1);
        } else if (volumeEntity.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
            if (model.isReduceReplica()) {
                parameter.setReplicaCount(volumeEntity.getReplicaCount() - 1);
            } else {
                parameter.setReplicaCount(volumeEntity.getReplicaCount());
            }
        }

        model.startProgress();

        boolean isMigrate = model.getMigrateData().getEntity();

        Frontend.getInstance().runAction(isMigrate ? ActionType.StartRemoveGlusterVolumeBricks
                : ActionType.GlusterVolumeRemoveBricks, parameter, result -> {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    setWindow(null);
                }, model);
    }

    private void stopRemoveBricks() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().stopRemoveBricksTitle());
        model.setMessage(ConstantsManager.getInstance().getConstants().stopRemoveBricksMessage());
        model.setHelpTag(HelpTag.volume_remove_bricks_stop);
        model.setHashName("volume_remove_bricks_stop"); //$NON-NLS-1$

        GlusterVolumeEntity volumeEntity = getVolumeEntity();
        GlusterAsyncTask volumeTask = volumeEntity.getAsyncTask();
        ArrayList<String> list = new ArrayList<>();
        for (GlusterBrickEntity brick : volumeEntity.getBricks()) {
            if (brick.getAsyncTask() != null && volumeTask != null && brick.getAsyncTask().getTaskId() != null
                    && brick.getAsyncTask().getTaskId().equals(volumeTask.getTaskId())
                    && volumeTask.getStatus() == JobExecutionStatus.STARTED) {
                list.add(brick.getQualifiedName());
            }
        }
        model.setItems(list);

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnStopRemoveBricks", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);

        UICommand cancelCommand = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onStopRemoveBricks() {
        if (getConfirmWindow() == null) {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        GlusterVolumeEntity volumeEntity = getVolumeEntity();

        ArrayList<GlusterBrickEntity> list = new ArrayList<>();
        for (Object brickName : model.getItems()) {
            GlusterBrickEntity brick = volumeEntity.getBrickWithQualifiedName((String) brickName);
            if (brick != null) {
                list.add(brick);
            }
        }

        GlusterVolumeRemoveBricksParameters parameter =
                new GlusterVolumeRemoveBricksParameters(volumeEntity.getId(), list);
        model.startProgress();

        Frontend.getInstance().runAction(ActionType.StopRemoveGlusterVolumeBricks, parameter, result -> {
            ConfirmationModel localModel = (ConfirmationModel) result.getState();
            localModel.stopProgress();
            setConfirmWindow(null);
            if (result.getReturnValue().getSucceeded()) {
                showRemoveBricksStatus();
            }
        }, model);
    }

    private void commitRemoveBricks() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().commitRemoveBricksTitle());
        model.setMessage(ConstantsManager.getInstance().getConstants().commitRemoveBricksMessage());
        model.setHelpTag(HelpTag.volume_remove_bricks_commit);
        model.setHashName("volume_remove_bricks_commit"); //$NON-NLS-1$

        GlusterVolumeEntity volumeEntity = getVolumeEntity();
        GlusterAsyncTask volumeTask = volumeEntity.getAsyncTask();
        ArrayList<String> list = new ArrayList<>();
        for (GlusterBrickEntity brick : volumeEntity.getBricks()) {
            if (brick.getAsyncTask() != null && volumeTask != null && brick.getAsyncTask().getTaskId() != null
                    && brick.getAsyncTask().getTaskId().equals(volumeTask.getTaskId())
                    && volumeTask.getStatus() == JobExecutionStatus.FINISHED) {
                list.add(brick.getQualifiedName());
            }
        }
        model.setItems(list);

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnCommitRemoveBricks", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onCommitRemoveBricks() {
        if (getConfirmWindow() == null) {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        GlusterVolumeEntity volumeEntity = getVolumeEntity();
        ArrayList<GlusterBrickEntity> list = new ArrayList<>();
        for (Object brickName : model.getItems()) {
            GlusterBrickEntity brick = volumeEntity.getBrickWithQualifiedName((String) brickName);
            if (brick != null) {
                list.add(brick);
            }
        }

        GlusterVolumeRemoveBricksParameters parameter =
                new GlusterVolumeRemoveBricksParameters(volumeEntity.getId(), list);
        model.startProgress();

        Frontend.getInstance().runAction(ActionType.CommitRemoveGlusterVolumeBricks,
                parameter,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    setConfirmWindow(null);
                    if (result.getReturnValue().getSucceeded()) {
                        disableRemoveBrickStatusPopUpActions();
                    }
                },
                model);
    }

    private void showRemoveBricksStatus() {
        final GlusterVolumeEntity volumeEntity = getVolumeEntity();
        final ArrayList<GlusterBrickEntity> bricks = new ArrayList<>();
        for (GlusterBrickEntity brick : volumeEntity.getBricks()) {
            if (brick.getAsyncTask() != null && brick.getAsyncTask().getTaskId() != null) {
                bricks.add(brick);
            }
        }
        final ConfirmationModel cModel = new ConfirmationModel();
        cModel.setHelpTag(HelpTag.volume_remove_bricks_status);
        cModel.setHashName("volume_remove_bricks_status"); ////$NON-NLS-1$

        UICommand removeBrickStatusOk = new UICommand("CancelConfirmation", VolumeBrickListModel.this);//$NON-NLS-1$
        removeBrickStatusOk.setTitle(ConstantsManager.getInstance().getConstants().ok());
        removeBrickStatusOk.setIsCancel(true);
        cModel.startProgress(ConstantsManager.getInstance().getConstants().fetchingDataMessage());
        cModel.getCommands().add(removeBrickStatusOk);
        cModel.setTitle(ConstantsManager.getInstance().getConstants().removeBricksStatusTitle());
        setConfirmWindow(cModel);

        final UICommand stopRemoveBrickFromStatus = new UICommand("StopRemoveBricksOnStatus", this);//$NON-NLS-1$
        stopRemoveBrickFromStatus.setTitle(ConstantsManager.getInstance().getConstants().stopRemoveBricksButton());
        stopRemoveBrickFromStatus.setIsExecutionAllowed(false);

        final UICommand commitRemoveBrickFromStatus = new UICommand("CommitRemoveBricksOnStatus", this);//$NON-NLS-1$
        commitRemoveBrickFromStatus.setTitle(ConstantsManager.getInstance().getConstants().commitRemoveBricksButton());
        commitRemoveBrickFromStatus.setIsExecutionAllowed(false);

        final UICommand retainBricksFromStatus = new UICommand("RetainBricksOnStatus", this);//$NON-NLS-1$
        retainBricksFromStatus.setTitle(ConstantsManager.getInstance().getConstants().retainBricksButton());
        retainBricksFromStatus.setIsExecutionAllowed(false);

        final UICommand cancelCommand = new UICommand("CancelRemoveBricksStatus", this);//$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        cancelCommand.setIsCancel(true);

        AsyncDataProvider.getInstance().getGlusterRemoveBricksStatus(new AsyncQuery<>(returnValue -> {
            cModel.stopProgress();
            if (returnValue.getSucceeded() && returnValue.getReturnValue() != null) {
                cancelConfirmation();

                RemoveBrickStatusModel removeBrickStatusModel;
                GlusterVolumeTaskStatusEntity removeBrickStatusEntity = returnValue.getReturnValue();

                if (getWindow() == null) {
                    removeBrickStatusModel =
                            new RemoveBrickStatusModel(volumeEntity, bricks);
                    removeBrickStatusModel.setTitle(ConstantsManager.getInstance()
                            .getConstants()
                            .removeBricksStatusTitle());
                    removeBrickStatusModel.setHelpTag(HelpTag.volume_remove_bricks_status);
                    removeBrickStatusModel.setHashName("volume_remove_bricks_status"); ////$NON-NLS-1$

                    setWindow(removeBrickStatusModel);

                    removeBrickStatusModel.getVolume().setEntity(volumeEntity.getName());
                    removeBrickStatusModel.getCluster().setEntity(volumeEntity.getClusterName());

                    removeBrickStatusModel.addStopRemoveBricksCommand(stopRemoveBrickFromStatus);
                    removeBrickStatusModel.addCommitRemoveBricksCommand(commitRemoveBrickFromStatus);
                    removeBrickStatusModel.addRetainBricksCommand(retainBricksFromStatus);
                    removeBrickStatusModel.getCommands().add(cancelCommand);
                } else {
                    removeBrickStatusModel = (RemoveBrickStatusModel) getWindow();
                }

                removeBrickStatusModel.showStatus(removeBrickStatusEntity);

            } else {
                cModel.setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .removeBrickStatusFailed(volumeEntity.getName()));
            }
        }),
                volumeEntity.getClusterId(),
                volumeEntity.getId(),
                bricks);
    }

    private void cancelRemoveBrickStatus() {
        if (getWindow() == null) {
            return;
        }
        ((RemoveBrickStatusModel) getWindow()).cancelRefresh();
        cancel();

    }

    private void retainBricks() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().retainBricksTitle());
        model.setMessage(ConstantsManager.getInstance().getConstants().retainBricksMessage());
        model.setHelpTag(HelpTag.volume_retain_brick);
        model.setHashName("volume_retain_brick"); //$NON-NLS-1$

        GlusterVolumeEntity volumeEntity = getVolumeEntity();
        GlusterAsyncTask volumeTask = volumeEntity.getAsyncTask();
        ArrayList<String> list = new ArrayList<>();
        for (GlusterBrickEntity brick : volumeEntity.getBricks()) {
            if (brick.getAsyncTask() != null && volumeTask != null && brick.getAsyncTask().getTaskId() != null
                    && brick.getAsyncTask().getTaskId().equals(volumeTask.getTaskId())
                    && volumeTask.getStatus() == JobExecutionStatus.FINISHED) {
                list.add(brick.getQualifiedName());
            }
        }
        model.setItems(list);

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnRetainBricks", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);

        UICommand cancelCommand = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onRetainBricks() {
        if (getConfirmWindow() == null) {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        GlusterVolumeEntity volumeEntity = getVolumeEntity();
        ArrayList<GlusterBrickEntity> list = new ArrayList<>();
        for (Object brickName : model.getItems()) {
            GlusterBrickEntity brick = volumeEntity.getBrickWithQualifiedName((String) brickName);
            if (brick != null) {
                list.add(brick);
            }
        }

        GlusterVolumeRemoveBricksParameters parameter =
                new GlusterVolumeRemoveBricksParameters(volumeEntity.getId(), list);
        model.startProgress();

        Frontend.getInstance().runAction(ActionType.StopRemoveGlusterVolumeBricks,
                parameter,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    setConfirmWindow(null);
                    if (result.getReturnValue().getSucceeded()) {
                        showRemoveBricksStatus();
                        disableRemoveBrickStatusPopUpActions();
                    }
                },
                model);
    }

    private void disableRemoveBrickStatusPopUpActions() {
        if (getWindow() != null && getWindow() instanceof RemoveBrickStatusModel) {
            RemoveBrickStatusModel statusModel = (RemoveBrickStatusModel) getWindow();
            statusModel.getCommitRemoveBricksCommand().setIsExecutionAllowed(false);
            statusModel.getRetainBricksCommand().setIsExecutionAllowed(false);
            statusModel.getStopRemoveBricksCommand().setIsExecutionAllowed(false);
        }
    }

    private void replaceBrick() {
        if (getWindow() != null) {
            return;
        }

        GlusterVolumeEntity volumeEntity = getEntity();

        if (volumeEntity == null) {
            return;
        }

        final ReplaceBrickModel brickModel = new ReplaceBrickModel();

        setWindow(brickModel);
        brickModel.setTitle(ConstantsManager.getInstance().getConstants().replaceBrickTitle());
        brickModel.setHelpTag(HelpTag.replace_brick);
        brickModel.setHashName("replace_brick"); //$NON-NLS-1$

        AsyncDataProvider.getInstance()
                .getClusterById(brickModel.asyncQuery(cluster -> AsyncDataProvider.getInstance().getHostListByCluster(
                        brickModel.asyncQuery(hostList -> brickModel.getServers().setItems(hostList)),
                        cluster.getName())), volumeEntity.getClusterId());

        UICommand command = UICommand.createDefaultOkUiCommand("OnReplace", this); //$NON-NLS-1$
        brickModel.getCommands().add(command);

        brickModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onReplaceBrick() {
        GlusterVolumeEntity volumeEntity = getEntity();
        ReplaceBrickModel replaceBrickModel = (ReplaceBrickModel) getWindow();
        GlusterBrickEntity existingBrick = getSelectedItem();
        if (volumeEntity == null || replaceBrickModel == null || existingBrick == null) {
            return;
        }
        VDS server = replaceBrickModel.getServers().getSelectedItem();
        if (server == null) {
            return;
        }
        String selectedHost = server.getHostName();
        boolean isMultipleBricks = volumeEntity.getBrickDirectories()
                .stream().anyMatch(brick -> brick.contains(selectedHost) && !selectedHost.equals(existingBrick.getServerName()));

        if (volumeEntity.getVolumeType().isReplicatedType() && isMultipleBricks) {
            ConfirmationModel model = new ConfirmationModel();
            setConfirmWindow(model);
            model.setTitle(ConstantsManager.getInstance().getConstants().replaceBrickTitle());
            model.setMessage(ConstantsManager.getInstance().getConstants().replaceBrickWarning());
            model.setHelpTag(HelpTag.replace_brick);
            model.setHashName("replace brick"); //$NON-NLS-1$

            UICommand yesCommand = new UICommand("OnReplaceConfirmation", VolumeBrickListModel.this); //$NON-NLS-1$
            yesCommand.setTitle(ConstantsManager.getInstance().getConstants().yes());
            model.getCommands().add(yesCommand);

            UICommand cancelCommand = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().no());
            cancelCommand.setIsCancel(true);
            getConfirmWindow().getCommands().add(cancelCommand);
        } else {
            onReplaceConfirmation();
        }
    }

    private void onReplaceConfirmation() {
        cancelConfirmation();
        ReplaceBrickModel replaceBrickModel = (ReplaceBrickModel) getWindow();
        GlusterVolumeEntity volumeEntity = getEntity();
        GlusterBrickEntity existingBrick = getSelectedItem();
        if (replaceBrickModel == null || volumeEntity == null || existingBrick == null) {
            return;
        }

        if (!replaceBrickModel.validate()) {
            return;
        }

        VDS server = replaceBrickModel.getServers().getSelectedItem();
        if (server == null) {
            return;
        }
        GlusterBrickEntity newBrick = new GlusterBrickEntity();
        newBrick.setVolumeId(volumeEntity.getId());
        newBrick.setServerId(server.getId());
        newBrick.setServerName(server.getHostName());
        newBrick.setBrickDirectory(replaceBrickModel.getBrickDirectory().getEntity());
        replaceBrickModel.startProgress();
        GlusterVolumeReplaceBrickActionParameters parameter =
                new GlusterVolumeReplaceBrickActionParameters(volumeEntity.getId(),
                        existingBrick,
                        newBrick);

        Frontend.getInstance().runAction(ActionType.ReplaceGlusterVolumeBrick, parameter, result -> {
            ReplaceBrickModel localModel = (ReplaceBrickModel) result.getState();
            localModel.stopProgress();
            setWindow(null);
        }, replaceBrickModel);

    }

    private void showBrickAdvancedDetails() {
        final GlusterVolumeEntity volumeEntity = getEntity();
        AsyncDataProvider.getInstance().getClusterById(
                new AsyncQuery<>(returnValue -> onShowBrickAdvancedDetails(volumeEntity)),
                volumeEntity.getClusterId());
    }

    private void onShowBrickAdvancedDetails(GlusterVolumeEntity volumeEntity) {
        final GlusterBrickEntity brickEntity = getSelectedItem();

        final BrickAdvancedDetailsModel brickModel = new BrickAdvancedDetailsModel();
        setWindow(brickModel);
        brickModel.setTitle(ConstantsManager.getInstance().getConstants().advancedDetailsBrickTitle());
        brickModel.setHelpTag(HelpTag.brick_advanced);
        brickModel.setHashName("brick_advanced"); //$NON-NLS-1$
        brickModel.startProgress();

        AsyncDataProvider.getInstance().getGlusterVolumeBrickDetails(new AsyncQuery<QueryReturnValue>(returnValue -> {
            brickModel.stopProgress();

            if (returnValue == null || !returnValue.getSucceeded()) {
                brickModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .errorInFetchingBrickAdvancedDetails());
                return;
            }

            GlusterVolumeAdvancedDetails advDetails = returnValue.getReturnValue();
            brickModel.getBrick().setEntity(brickEntity.getQualifiedName());
            if (advDetails != null && advDetails.getBrickDetails() != null
                    && advDetails.getBrickDetails().size() == 1) {
                BrickDetails brickDetails = advDetails.getBrickDetails().get(0);
                brickModel.getBrickProperties().setProperties(brickDetails.getBrickProperties());

                ArrayList<EntityModel<GlusterClientInfo>> clients = new ArrayList<>();
                for (GlusterClientInfo client : brickDetails.getClients()) {
                    clients.add(new EntityModel<>(client));
                }
                brickModel.getClients().setItems(clients);

                brickModel.getMemoryStatistics().updateMemoryStatistics(brickDetails.getMemoryStatus()
                        .getMallInfo());

                ArrayList<EntityModel<Mempool>> memoryPools = new ArrayList<>();
                for (Mempool mempool : brickDetails.getMemoryStatus().getMemPools()) {
                    memoryPools.add(new EntityModel<>(mempool));
                }
                brickModel.getMemoryPools().setItems(memoryPools);
            }
        }, true), volumeEntity.getClusterId(), volumeEntity.getId(), brickEntity.getId());

        UICommand command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().close());
        command.setIsCancel(true);
        brickModel.getCommands().add(command);
    }

    private void resetBrick() {
        if (getSelectedItems() == null || getSelectedItems().isEmpty()) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        final ResetBrickModel brickModel = new ResetBrickModel();

        setWindow(brickModel);
        brickModel.setTitle(ConstantsManager.getInstance().getConstants().resetBrickTitle());
        brickModel.setHelpTag(HelpTag.reset_brick);
        brickModel.setHashName("reset_brick"); //$NON-NLS-1$
        brickModel.setMessage(ConstantsManager.getInstance().getConstants().resetBrickMessage());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnReset", this); //$NON-NLS-1$
        brickModel.getCommands().add(okCommand);

        brickModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onResetBrick() {
        ResetBrickModel resetBrickModel = (ResetBrickModel) getWindow();
        GlusterVolumeEntity volumeEntity = getEntity();
        GlusterBrickEntity existingBrick = getSelectedItem();
        if (resetBrickModel == null || volumeEntity == null || existingBrick == null || !resetBrickModel.validate()) {
            return;
        }
        resetBrickModel.startProgress();

        GlusterVolumeResetBrickActionParameters parameter =
                new GlusterVolumeResetBrickActionParameters(volumeEntity.getId(),
                        existingBrick);

        Frontend.getInstance().runAction(ActionType.ResetGlusterVolumeBrick, parameter, result -> {
            ResetBrickModel localModel = (ResetBrickModel) result.getState();
            localModel.stopProgress();
            setWindow(null);
        }, resetBrickModel);
    }


    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getAddBricksCommand())) {
            checkUpServerAndAddBricks();
        } else if (command.getName().equals("OnAddBricks")) { //$NON-NLS-1$
            onAddBricks();
        } else if (command.getName().equals("OnAddBricksInternal")) { //$NON-NLS-1$
            onAddBricksInternal();
        } else if (command.getName().equals("CancelConfirmation")) { //$NON-NLS-1$
            cancelConfirmation();
        } else if (command.equals(getRemoveBricksCommand())) {
            removeBricks();
        } else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemoveBricks();
        } else if (command.equals(getStopRemoveBricksCommand())) {
            stopRemoveBricks();
        } else if (command.getName().equals("OnStopRemoveBricks")) { //$NON-NLS-1$
            onStopRemoveBricks();
        } else if (command.equals(getCommitRemoveBricksCommand())) {
            commitRemoveBricks();
        } else if (command.getName().equals("OnCommitRemoveBricks")) { //$NON-NLS-1$
            onCommitRemoveBricks();
        } else if (command.equals(getStatusRemoveBricksCommand())) {
            showRemoveBricksStatus();
        } else if (command.getName().equals("StopRemoveBricksOnStatus")) { //$NON-NLS-1$
            getStopRemoveBricksCommand().execute();
        } else if (command.getName().equals("CommitRemoveBricksOnStatus")) { //$NON-NLS-1$
            getCommitRemoveBricksCommand().execute();
        } else if (command.getName().equals("CancelRemoveBricksStatus")) { //$NON-NLS-1$
            cancelRemoveBrickStatus();
        } else if (command.equals(getRetainBricksCommand())) {
            retainBricks();
        } else if (command.getName().equals("OnRetainBricks")) { //$NON-NLS-1$
            onRetainBricks();
        } else if (command.getName().equals("RetainBricksOnStatus")) { //$NON-NLS-1$
            getRetainBricksCommand().execute();
        } else if (command.equals(getReplaceBrickCommand())) {
            replaceBrick();
        } else if (command.getName().equals("OnReplace")) { //$NON-NLS-1$
            onReplaceBrick();
        }  else if (command.equals(getBrickAdvancedDetailsCommand())) {
            showBrickAdvancedDetails();
        } else if (command.equals(getResetBrickCommand())) {
            resetBrick();
        } else if (command.getName().equals("OnReset")) { //$NON-NLS-1$
            onResetBrick();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            setWindow(null);
        } else if (command.getName().equals("OnReplaceConfirmation")) { //$NON-NLS-1$
            onReplaceConfirmation();
        }

    }
}

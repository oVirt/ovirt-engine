package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class GlusterVolumeSnapshotListModel extends SearchableListModel<GlusterVolumeEntity, GlusterVolumeSnapshotEntity> {

    @Override
    public String getListName() {
        return "GlusterVolumeSnapshotListModel"; //$NON-NLS-1$
    }

    public GlusterVolumeSnapshotListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().snapshotsTitle());
        setHelpTag(HelpTag.volume_snapshots);
        setHashName("volume_snapshots");//$NON-NLS-1$

        setRestoreSnapshotCommand(new UICommand("restore", this)); //$NON-NLS-1$
        setDeleteSnapshotCommand(new UICommand("delete", this)); //$NON-NLS-1$
        setDeleteAllSnapshotsCommand(new UICommand("deleteAll", this)); //$NON-NLS-1$
        setActivateSnapshotCommand(new UICommand("activate", this)); //$NON-NLS-1$
        setDeactivateSnapshotCommand(new UICommand("deactivate", this)); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
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

    private UICommand restoreSnapshotCommand;
    private UICommand deleteSnapshotCommand;
    private UICommand deleteAllSnapshotsCommand;
    private UICommand activateSnapshotCommand;
    private UICommand deactivateSnapshotCommand;
    private UICommand createSnapshotCommand;
    private UICommand editSnapshotScheduleCommand;

    public UICommand getRestoreSnapshotCommand() {
        return restoreSnapshotCommand;
    }

    public void setRestoreSnapshotCommand(UICommand restoreSnapshotCommand) {
        this.restoreSnapshotCommand = restoreSnapshotCommand;
    }

    public UICommand getDeleteSnapshotCommand() {
        return deleteSnapshotCommand;
    }

    public void setDeleteSnapshotCommand(UICommand deleteSnapshotCommand) {
        this.deleteSnapshotCommand = deleteSnapshotCommand;
    }

    public UICommand getDeleteAllSnapshotsCommand() {
        return deleteAllSnapshotsCommand;
    }

    public void setDeleteAllSnapshotsCommand(UICommand deleteAllSnapshotsCommand) {
        this.deleteAllSnapshotsCommand = deleteAllSnapshotsCommand;
    }

    public UICommand getActivateSnapshotCommand() {
        return activateSnapshotCommand;
    }

    public void setActivateSnapshotCommand(UICommand activateSnapshotCommand) {
        this.activateSnapshotCommand = activateSnapshotCommand;
    }

    public UICommand getDeactivateSnapshotCommand() {
        return deactivateSnapshotCommand;
    }

    public void setDeactivateSnapshotCommand(UICommand deactivateSnapshotCommand) {
        this.deactivateSnapshotCommand = deactivateSnapshotCommand;
    }

    public UICommand getCreateSnapshotCommand() {
        return this.createSnapshotCommand;
    }

    public void setCreateSnapshotCommand(UICommand command) {
        this.createSnapshotCommand = command;
    }

    public UICommand getEditSnapshotScheduleCommand() {
        return this.editSnapshotScheduleCommand;
    }

    public void setEditSnapshotScheduleCommand(UICommand command) {
        this.editSnapshotScheduleCommand = command;
    }

    private void updateActionAvailability() {
        boolean allowRestore = false;
        boolean allowDelete = true;
        boolean allowDeleteAll = getItems() == null ? false : getItems().size() > 0;
        boolean allowActivate = false;
        boolean allowDeactivate = false;

        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            allowDelete = false;
        } else {
            List<GlusterVolumeSnapshotEntity> snapshots = getSelectedItems();

            if (snapshots.size() == 1) {
                allowRestore = true;
                allowActivate = snapshots.get(0).getStatus() == GlusterSnapshotStatus.DEACTIVATED;
                allowDeactivate = snapshots.get(0).getStatus() == GlusterSnapshotStatus.ACTIVATED;
            }
        }

        getRestoreSnapshotCommand().setIsExecutionAllowed(allowRestore);
        getDeleteSnapshotCommand().setIsExecutionAllowed(allowDelete);
        getDeleteAllSnapshotsCommand().setIsExecutionAllowed(allowDeleteAll);
        getActivateSnapshotCommand().setIsExecutionAllowed(allowActivate);
        getDeactivateSnapshotCommand().setIsExecutionAllowed(allowDeactivate);
    }


    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getGlusterVolumeSnapshotsForVolume(new AsyncQuery<>(
                snapshots -> {
                    Collections.sort(snapshots, Comparator.comparing(GlusterVolumeSnapshotEntity::getSnapshotName));
                    setItems(snapshots);
                }), getEntity().getId());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getRestoreSnapshotCommand())) {
            restoreSnapshot();
        } else if (command.equals(getDeleteSnapshotCommand())) {
            deleteSnapshot();
        } else if (command.equals(getDeleteAllSnapshotsCommand())) {
            deleteAllSnapshots();
        } else if (command.equals(getActivateSnapshotCommand())) {
            activateSnapshot();
        } else if (command.equals(getDeactivateSnapshotCommand())) {
            deactivateSnapshot();
        } else if (command.getName().equals("onRestoreSnapshot")) { //$NON-NLS-1$
            onRestoreSnapshot();
        } else if (command.getName().equals("onDeleteSnapshot")) { //$NON-NLS-1$
            onDeleteSnapshot();
        } else if (command.getName().equals("onDeleteAllSnapshots")) { //$NON-NLS-1$
            onDeleteAllSnapshots();
        } else if (command.getName().equals("onActivateSnapshot")) { //$NON-NLS-1$
            onActivateSnapshot();
        } else if (command.getName().equals("onDeactivateSnapshot")) { //$NON-NLS-1$
            onDeactivateSnapshot();
        } else if (command.getName().equals("cancelConfirmation")) { //$NON-NLS-1$
            setConfirmWindow(null);
        }
    }

    private void restoreSnapshot() {
        if (getSelectedItem() == null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getMessages().confirmRestoreSnapshot(getEntity().getName()));
        model.setHelpTag(HelpTag.volume_restore_snapshot_confirmation);
        model.setHashName("volume_restore_snapshot_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().confirmVolumeSnapshotRestoreWithStopMessage());
        UICommand okCommand = UICommand.createDefaultOkUiCommand("onRestoreSnapshot", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onRestoreSnapshot() {
        runAction(ActionType.RestoreGlusterVolumeSnapshot,
                new GlusterVolumeSnapshotActionParameters(getEntity().getId(),
                        getSelectedItem().getSnapshotName(),
                        true));
    }

    private void deleteSnapshot() {
        if (getSelectedItems() == null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        List<GlusterVolumeSnapshotEntity> snapshots = getSelectedItems();
        StringBuilder snapshotNames = new StringBuilder();
        for (GlusterVolumeSnapshotEntity snapshot : snapshots) {
            snapshotNames.append(snapshot.getSnapshotName());
            snapshotNames.append("\n"); //$NON-NLS-1$
        }

        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getMessages().confirmRemoveSnapshot(getEntity().getName()));
        model.setHelpTag(HelpTag.volume_delete_snapshot_confirmation);
        model.setHashName("volume_delete_snapshot_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance()
                .getMessages()
                .confirmVolumeSnapshotDeleteMessage(snapshotNames.toString()));

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onDeleteSnapshot", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onDeleteSnapshot() {
        if (getConfirmWindow() == null) {
            return;
        }

        final ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        List<ActionParametersBase> paramsList = new ArrayList<>();
        for (GlusterVolumeSnapshotEntity snapshot : getSelectedItems()) {
            GlusterVolumeSnapshotActionParameters param =
                    new GlusterVolumeSnapshotActionParameters(getEntity().getId(), snapshot.getSnapshotName(), true);
            paramsList.add(param);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.DeleteGlusterVolumeSnapshot,
                paramsList,
                result -> {
                    model.stopProgress();
                    setConfirmWindow(null);
                }, model);
    }

    private void deleteAllSnapshots() {
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getMessages().confirmRemoveAllSnapshots(getEntity().getName()));
        model.setHelpTag(HelpTag.volume_delete_all_snapshot_confirmation);
        model.setHashName("volume_delete_all_snapshot_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().confirmVolumeSnapshotDeleteAllMessage());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onDeleteAllSnapshots", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onDeleteAllSnapshots() {
        runAction(ActionType.DeleteAllGlusterVolumeSnapshots, new GlusterVolumeParameters(getEntity().getId()));
    }

    private void activateSnapshot() {
        if (getSelectedItem() == null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getMessages().confirmActivateSnapshot(getEntity().getName()));
        model.setHelpTag(HelpTag.volume_activate_snapshot_confirmation);
        model.setHashName("volume_activate_snapshot_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().confirmVolumeSnapshotActivateMessage());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onActivateSnapshot", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onActivateSnapshot() {
        runAction(ActionType.ActivateGlusterVolumeSnapshot,
                new GlusterVolumeSnapshotActionParameters(getEntity().getId(),
                        getSelectedItem().getSnapshotName(),
                        true));
    }

    private void deactivateSnapshot() {
        if (getSelectedItem() == null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getMessages().confirmDeactivateSnapshot(getEntity().getName()));
        model.setHelpTag(HelpTag.volume_deactivate_snapshot_confirmation);
        model.setHashName("volume_deactivate_snapshot_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().confirmVolumeSnapshotDeactivateMessage());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onDeactivateSnapshot", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onDeactivateSnapshot() {
        runAction(ActionType.DeactivateGlusterVolumeSnapshot,
                new GlusterVolumeSnapshotActionParameters(getEntity().getId(),
                        getSelectedItem().getSnapshotName(),
                        true));
    }

    private void runAction(ActionType action, ActionParametersBase param) {
        if (getConfirmWindow() == null) {
            return;
        }

        final ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        model.startProgress();

        Frontend.getInstance().runAction(action, param, result -> {
            model.stopProgress();
            setConfirmWindow(null);
        });
    }
}

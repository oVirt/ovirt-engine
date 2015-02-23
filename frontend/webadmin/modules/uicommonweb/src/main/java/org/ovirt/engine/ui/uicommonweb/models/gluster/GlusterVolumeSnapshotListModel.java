package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class GlusterVolumeSnapshotListModel extends SearchableListModel {
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
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private UICommand restoreSnapshotCommand;
    private UICommand deleteSnapshotCommand;
    private UICommand deleteAllSnapshotsCommand;
    private UICommand activateSnapshotCommand;
    private UICommand deactivateSnapshotCommand;

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

    private void updateActionAvailability() {
        boolean allowRestore = false;
        boolean allowDelete = true;
        boolean allowDeleteAll = true;
        boolean allowActivate = false;
        boolean allowDeactivate = false;

        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            allowDelete = false;
        } else {
            List<GlusterVolumeSnapshotEntity> snapshots = Linq.<GlusterVolumeSnapshotEntity> cast(getSelectedItems());

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

        AsyncDataProvider.getGlusterVolumeSnapshotsForVolume(new AsyncQuery(this,
                new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<GlusterVolumeSnapshotEntity> snapshots =
                                (ArrayList<GlusterVolumeSnapshotEntity>) returnValue;
                        Collections.sort(snapshots, new Linq.GlusterVolumeSnapshotComparer());
                        setItems(snapshots);
                    }
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
        GlusterVolumeSnapshotEntity snapshot = (GlusterVolumeSnapshotEntity) getSelectedItem();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getMessages().confirmRestoreSnapshot(getEntity().getName()));
        model.setHelpTag(HelpTag.volume_restore_snapshot_confirmation);
        model.setHashName("volume_restore_snapshot_confirmation"); //$NON-NLS-1$
        if (snapshot.getStatus() == GlusterSnapshotStatus.ACTIVATED) {
            model.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .confirmVolumeSnapshotRestoreWithStopMessage());
        } else {
            model.setMessage(ConstantsManager.getInstance().getConstants().confirmVolumeSnapshotRestoreMesage());
        }

        UICommand okCommand = new UICommand("onRestoreSnapshot", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getConfirmWindow().getCommands().add(okCommand);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("cancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onRestoreSnapshot() {
        runAction(VdcActionType.RestoreGlusterVolumeSnapshot,
                new GlusterVolumeSnapshotActionParameters(getEntity().getId(),
                        ((GlusterVolumeSnapshotEntity) getSelectedItem()).getSnapshotName(),
                        true));
    }

    private void deleteSnapshot() {
        if (getSelectedItems() == null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        List<GlusterVolumeSnapshotEntity> snapshots = (List<GlusterVolumeSnapshotEntity>) getSelectedItems();
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

        UICommand okCommand = new UICommand("onDeleteSnapshot", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getConfirmWindow().getCommands().add(okCommand);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("cancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onDeleteSnapshot() {
        if (getConfirmWindow() == null) {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        List<VdcActionParametersBase> paramsList = new ArrayList<VdcActionParametersBase>();
        for (GlusterVolumeSnapshotEntity snapshot : (List<GlusterVolumeSnapshotEntity>) getSelectedItems()) {
            GlusterVolumeSnapshotActionParameters param =
                    new GlusterVolumeSnapshotActionParameters(getEntity().getId(), snapshot.getSnapshotName(), true);
            paramsList.add(param);
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.DeleteGlusterVolumeSnapshot,
                paramsList, true,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) getConfirmWindow();
                        localModel.stopProgress();
                        setConfirmWindow(null);
                    }
                },
                model);
    }

    private void deleteAllSnapshots() {
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getMessages().confirmRemoveAllSnapshots(getEntity().getName()));
        model.setHelpTag(HelpTag.volume_delete_all_snapshot_confirmation);
        model.setHashName("volume_delete_all_snapshot_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().confirmVolumeSnapshotDeleteAllMessage());

        UICommand okCommand = new UICommand("onDeleteAllSnapshots", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getConfirmWindow().getCommands().add(okCommand);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("cancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onDeleteAllSnapshots() {
        runAction(VdcActionType.DeleteAllGlusterVolumeSnapshots, new GlusterVolumeParameters(getEntity().getId()));
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

        UICommand okCommand = new UICommand("onActivateSnapshot", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getConfirmWindow().getCommands().add(okCommand);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("cancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onActivateSnapshot() {
        runAction(VdcActionType.ActivateGlusterVolumeSnapshot,
                new GlusterVolumeSnapshotActionParameters(getEntity().getId(),
                        ((GlusterVolumeSnapshotEntity) getSelectedItem()).getSnapshotName(),
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

        UICommand okCommand = new UICommand("onDeactivateSnapshot", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getConfirmWindow().getCommands().add(okCommand);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("cancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onDeactivateSnapshot() {
        runAction(VdcActionType.DeactivateGlusterVolumeSnapshot,
                new GlusterVolumeSnapshotActionParameters(getEntity().getId(),
                        ((GlusterVolumeSnapshotEntity) getSelectedItem()).getSnapshotName(),
                        true));
    }

    private void runAction(VdcActionType action, VdcActionParametersBase param) {
        if (getConfirmWindow() == null) {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        model.startProgress(null);

        Frontend.getInstance().runAction(action, param, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                ConfirmationModel localModel = (ConfirmationModel) getConfirmWindow();
                localModel.stopProgress();
                setConfirmWindow(null);
            }
        });
    }

    @Override
    public GlusterVolumeEntity getEntity() {
        return (GlusterVolumeEntity) super.getEntity();
    }

    public void setEntity(GlusterVolumeEntity value) {
        super.setEntity(value);
    }
}

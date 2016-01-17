package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IPredicate;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel.EndDateOptions;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class GlusterVolumeSnapshotListModel extends SearchableListModel<GlusterVolumeEntity, GlusterVolumeSnapshotEntity> {

    private UICommand createSnapshotCommand;

    public UICommand getCreateSnapshotCommand() {
        return createSnapshotCommand;
    }

    public void setCreateSnapshotCommand(UICommand value) {
        this.createSnapshotCommand = value;
    }

    private UICommand editSnapshotScheduleCommand;

    public UICommand getEditSnapshotScheduleCommand() {
        return this.editSnapshotScheduleCommand;
    }

    public void setEditSnapshotScheduleCommand(UICommand command) {
        this.editSnapshotScheduleCommand = command;
    }

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
        setCreateSnapshotCommand(new UICommand("createSnapshot", this));//$NON-NLS-1$
        setEditSnapshotScheduleCommand(new UICommand("editSnapshotSchedule", this));//$NON-NLS-1$
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
        boolean allowDeleteAll = getItems() == null ? false : getItems().size() > 0;
        boolean allowActivate = false;
        boolean allowDeactivate = false;
        boolean allowCreateSnapshot = true;
        boolean allowEditSnapshotSchedule = false;

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

        if (getEntity() == null || getEntity().getStatus() == GlusterStatus.DOWN) {
            allowCreateSnapshot = false;
        }

        if (getEntity() != null && getEntity().getStatus() == GlusterStatus.UP && getEntity().getSnapshotScheduled()) {
            allowEditSnapshotSchedule = true;
        }

        getRestoreSnapshotCommand().setIsExecutionAllowed(allowRestore);
        getDeleteSnapshotCommand().setIsExecutionAllowed(allowDelete);
        getDeleteAllSnapshotsCommand().setIsExecutionAllowed(allowDeleteAll);
        getActivateSnapshotCommand().setIsExecutionAllowed(allowActivate);
        getDeactivateSnapshotCommand().setIsExecutionAllowed(allowDeactivate);
        getCreateSnapshotCommand().setIsExecutionAllowed(allowCreateSnapshot);
        getEditSnapshotScheduleCommand().setIsExecutionAllowed(allowEditSnapshotSchedule);
    }


    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getGlusterVolumeSnapshotsForVolume(new AsyncQuery(this,
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
        } else if (command.equals(getCreateSnapshotCommand())) {
            createSnapshot();
        } else if (command.getName().equalsIgnoreCase("onCreateSnapshot")) {//$NON-NLS-1$
            onCreateSnapshot();
        } else if (command.getName().equalsIgnoreCase("cancel")) {//$NON-NLS-1$
            setWindow(null);
        } else if (command.equals(getEditSnapshotScheduleCommand())) {
            editSnapshotSchedule();
        } else if (command.getName().equalsIgnoreCase("onEditSnapshotSchedule")) {//$NON-NLS-1$
            onEditSnapshotSchedule();
        } else if (command.getName().equalsIgnoreCase("onEditSnapshotScheduleInternal")) {//$NON-NLS-1$
            onEditSnapshotScheduleInternal();
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
        runAction(VdcActionType.RestoreGlusterVolumeSnapshot,
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

        List<VdcActionParametersBase> paramsList = new ArrayList<>();
        for (GlusterVolumeSnapshotEntity snapshot : getSelectedItems()) {
            GlusterVolumeSnapshotActionParameters param =
                    new GlusterVolumeSnapshotActionParameters(getEntity().getId(), snapshot.getSnapshotName(), true);
            paramsList.add(param);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.DeleteGlusterVolumeSnapshot,
                paramsList,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        model.stopProgress();
                        setConfirmWindow(null);
                    }
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

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onActivateSnapshot", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onActivateSnapshot() {
        runAction(VdcActionType.ActivateGlusterVolumeSnapshot,
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
        runAction(VdcActionType.DeactivateGlusterVolumeSnapshot,
                new GlusterVolumeSnapshotActionParameters(getEntity().getId(),
                        getSelectedItem().getSnapshotName(),
                        true));
    }

    private void runAction(VdcActionType action, VdcActionParametersBase param) {
        if (getConfirmWindow() == null) {
            return;
        }

        final ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        model.startProgress();

        Frontend.getInstance().runAction(action, param, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                model.stopProgress();
                setConfirmWindow(null);
            }
        });
    }

    @Override
    public GlusterVolumeEntity getEntity() {
        return super.getEntity();
    }

    public void setEntity(GlusterVolumeEntity value) {
        super.setEntity(value);
    }

    private void createSnapshot() {
        if (getWindow() != null) {
            return;
        }

        GlusterVolumeEntity volumeEntity = getEntity();
        final GlusterVolumeSnapshotModel snapshotModel =
                new GlusterVolumeSnapshotModel(true, !volumeEntity.getSnapshotScheduled());

        snapshotModel.setHelpTag(HelpTag.new_volume_snapshot);
        snapshotModel.setHashName("new_volume_snapshot"); //$NON-NLS-1$
        snapshotModel.setTitle(ConstantsManager.getInstance().getConstants().createScheduleVolumeSnapshotTitle());
        setWindow(snapshotModel);

        snapshotModel.startProgress();

        snapshotModel.getClusterName().setEntity(volumeEntity.getClusterName());
        snapshotModel.getVolumeName().setEntity(volumeEntity.getName());

        AsyncDataProvider.getInstance().getIsGlusterVolumeSnapshotCliScheduleEnabled(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                Boolean isCliScheduleEnabled = (Boolean) returnValue;
                snapshotModel.getDisableCliSchedule().setEntity(isCliScheduleEnabled);
                snapshotModel.stopProgress();
            }
        }), volumeEntity.getClusterId());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onCreateSnapshot", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("cancel", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(cancelCommand);
    }

    private void onCreateSnapshot() {
        final GlusterVolumeSnapshotModel snapshotModel = (GlusterVolumeSnapshotModel) getWindow();

        if (!snapshotModel.validate(false)) {
            return;
        }

        if (!snapshotModel.isScheduleTabVisible()
                || snapshotModel.getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN) {
            createNewSnapshot(snapshotModel);
        } else {
            scheduleSnapshot(snapshotModel, false);
        }
    }

    private Time getExecutionTime(GlusterVolumeSnapshotModel model) {
        int hours = model.getExecutionTime().getEntity().getHours();
        int minutes = model.getExecutionTime().getEntity().getMinutes();

        return new Time(hours, minutes, 0);
    }

    private void scheduleSnapshot(final GlusterVolumeSnapshotModel snapshotModel, boolean reschedule) {
        GlusterVolumeEntity volumeEntity = getEntity();

        final GlusterVolumeSnapshotSchedule schedule = new GlusterVolumeSnapshotSchedule();
        schedule.setSnapshotNamePrefix(snapshotModel.getSnapshotName().getEntity());
        schedule.setSnapshotDescription(snapshotModel.getDescription().getEntity());
        schedule.setClusterId(volumeEntity.getClusterId());
        schedule.setVolumeId(volumeEntity.getId());
        switch (snapshotModel.getRecurrence().getSelectedItem()) {
        case INTERVAL:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.INTERVAL);
            schedule.setInterval(Integer.valueOf(snapshotModel.getInterval().getSelectedItem()));
            break;
        case HOURLY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.HOURLY);
            break;
        case DAILY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.DAILY);
            schedule.setExecutionTime(getExecutionTime(snapshotModel));
            break;
        case WEEKLY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.WEEKLY);
            schedule.setExecutionTime(getExecutionTime(snapshotModel));
            StringBuilder sb = new StringBuilder();
            for (DayOfWeek day : snapshotModel.getDaysOfTheWeek().getSelectedItem()) {
                sb.append(day.name().substring(0, 3));
                sb.append(',');//$NON-NLS-1$
            }
            schedule.setDays(sb.toString());
            break;
        case MONTHLY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.MONTHLY);
            schedule.setExecutionTime(getExecutionTime(snapshotModel));
            schedule.setDays(snapshotModel.getDaysOfMonth().getSelectedItem());
            break;
        }

        Date startAt = snapshotModel.getStartAt().getEntity();
        schedule.setStartDate(startAt);
        schedule.setTimeZone(snapshotModel.getTimeZones().getSelectedItem().getKey());

        if (snapshotModel.getEndByOptions().getSelectedItem() == EndDateOptions.NoEndDate) {
            schedule.setEndByDate(null);
        } else {
            schedule.setEndByDate(snapshotModel.getEndDate().getEntity());
        }

        ScheduleGlusterVolumeSnapshotParameters params =
                new ScheduleGlusterVolumeSnapshotParameters(schedule, snapshotModel.getDisableCliSchedule().getEntity());
        snapshotModel.startProgress();

        VdcActionType actionType = null;
        if (reschedule) {
            actionType = VdcActionType.RescheduleGlusterVolumeSnapshot;
        } else {
            actionType = VdcActionType.ScheduleGlusterVolumeSnapshot;
        }

        Frontend.getInstance().runAction(actionType,
                params,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        GlusterVolumeSnapshotListModel localModel =
                                (GlusterVolumeSnapshotListModel) result.getState();
                        snapshotModel.stopProgress();
                        localModel.postSnapshotAction(result.getReturnValue());
                    }
                },
                this, snapshotModel.getDisableCliSchedule().getEntity());
    }

    private void createNewSnapshot(final GlusterVolumeSnapshotModel snapshotModel) {
        GlusterVolumeEntity volumeEntity = getEntity();

        final GlusterVolumeSnapshotEntity snapshot = new GlusterVolumeSnapshotEntity();
        snapshot.setClusterId(volumeEntity.getClusterId());
        snapshot.setSnapshotName(snapshotModel.getSnapshotName().getEntity());
        snapshot.setVolumeId(volumeEntity.getId());
        snapshot.setDescription(snapshotModel.getDescription().getEntity());

        CreateGlusterVolumeSnapshotParameters parameter =
                new CreateGlusterVolumeSnapshotParameters(snapshot, false);

        snapshotModel.startProgress();
        Frontend.getInstance().runAction(VdcActionType.CreateGlusterVolumeSnapshot,
                parameter,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        GlusterVolumeSnapshotListModel localModel =
                                (GlusterVolumeSnapshotListModel) result.getState();
                        snapshotModel.stopProgress();
                        localModel.postSnapshotAction(result.getReturnValue());
                    }
                },
                this);
    }

    public void postSnapshotAction(VdcReturnValueBase returnValue) {
        if (returnValue != null && returnValue.getSucceeded()) {
            setWindow(null);
        }
    }

    public void editSnapshotSchedule() {
        if (getWindow() != null) {
            return;
        }

        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        final GlusterVolumeSnapshotModel snapshotModel =
                new GlusterVolumeSnapshotModel(true, true);
        snapshotModel.setHelpTag(HelpTag.edit_volume_snapshot_schedule);
        snapshotModel.setHashName("edit_volume_snapshot_schedule"); //$NON-NLS-1$
        snapshotModel.setTitle(constants.editVolumeSnapshotScheduleTitle());
        setWindow(snapshotModel);

        snapshotModel.startProgress();

        AsyncDataProvider.getInstance().getVolumeSnapshotSchedule(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                if (returnValue == null) {
                    snapshotModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .unableToFetchVolumeSnapshotSchedule());
                    return;
                }
                final GlusterVolumeSnapshotSchedule schedule = (GlusterVolumeSnapshotSchedule) returnValue;
                snapshotModel.getSnapshotName().setEntity(schedule.getSnapshotNamePrefix());
                snapshotModel.getDescription().setEntity(schedule.getSnapshotDescription());
                snapshotModel.getRecurrence().setSelectedItem(schedule.getRecurrence());
                if (schedule.getEndByDate() == null) {
                    snapshotModel.getEndByOptions().setSelectedItem(EndDateOptions.NoEndDate);
                } else {
                    snapshotModel.getEndByOptions().setSelectedItem(EndDateOptions.HasEndDate);
                    snapshotModel.getEndDate().setEntity(schedule.getEndByDate());
                }

                if (schedule.getRecurrence() != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN) {
                    Map<String, String> timeZones = TimeZoneType.GENERAL_TIMEZONE.getTimeZoneList();
                    snapshotModel.getTimeZones().setSelectedItem(Linq.firstOrNull(timeZones.entrySet(),
                            new IPredicate<Map.Entry<String, String>>() {
                                @Override
                                public boolean match(Map.Entry<String, String> item) {
                                    return item.getKey().startsWith(schedule.getTimeZone()); //$NON-NLS-1$
                                }
                            }));
                }
                switch (schedule.getRecurrence()) {
                case INTERVAL:
                    snapshotModel.getInterval().setSelectedItem(String.valueOf(schedule.getInterval()));
                    break;
                case HOURLY:
                    break;
                case DAILY:
                    snapshotModel.getExecutionTime().setEntity(getExecutionTimeValue(schedule));
                    break;
                case WEEKLY:
                    List<DayOfWeek> daysList = new ArrayList<>();
                    for (String day : schedule.getDays().split(",")) {//$NON-NLS-1$
                        daysList.add(getDayOfWeek(day));
                    }
                    snapshotModel.getDaysOfTheWeek().setSelectedItem(daysList);
                    snapshotModel.getExecutionTime().setEntity(getExecutionTimeValue(schedule));
                    break;
                case MONTHLY:
                    snapshotModel.getDaysOfMonth().setSelectedItem(schedule.getDays());
                    snapshotModel.getExecutionTime().setEntity(getExecutionTimeValue(schedule));
                    break;
                }

                snapshotModel.getStartAt().setEntity(schedule.getStartDate());
                snapshotModel.stopProgress();
            }

            private DayOfWeek getDayOfWeek(String day) {
                switch (day) {
                case "Sun"://$NON-NLS-1$
                    return DayOfWeek.Sunday;
                case "Mon"://$NON-NLS-1$
                    return DayOfWeek.Monday;
                case "Tue"://$NON-NLS-1$
                    return DayOfWeek.Tuesday;
                case "Wed"://$NON-NLS-1$
                    return DayOfWeek.Wednesday;
                case "Thu"://$NON-NLS-1$
                    return DayOfWeek.Thursday;
                case "Fri"://$NON-NLS-1$
                    return DayOfWeek.Friday;
                case "Sat"://$NON-NLS-1$
                    return DayOfWeek.Saturday;
                default:
                    return null;
                }
            }

            private Date getExecutionTimeValue(GlusterVolumeSnapshotSchedule schedule) {
                Date dt = new Date();
                dt.setHours(schedule.getExecutionTime().getHours());
                dt.setMinutes(schedule.getExecutionTime().getMinutes());

                return dt;
            }
        }),
                getEntity().getId());

        snapshotModel.getClusterName().setEntity(getEntity().getClusterName());
        snapshotModel.getVolumeName().setEntity(getEntity().getName());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onEditSnapshotSchedule", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("cancel", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(cancelCommand);
    }

    private void confirmDeleteVolumeSnapshotSchedule() {
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .removeGlusterVolumeSnapshotScheduleConfirmationTitle());
        model.setHelpTag(HelpTag.remove_volume_snapshot_schedule_confirmation);
        model.setHashName("remove_volume_snapshot_schedule_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().youAreAboutToRemoveSnapshotScheduleMsg());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onEditSnapshotScheduleInternal", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    public void onEditSnapshotSchedule() {
        final GlusterVolumeSnapshotModel snapshotModel = (GlusterVolumeSnapshotModel) getWindow();

        if (snapshotModel.getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN) {
            confirmDeleteVolumeSnapshotSchedule();
        } else {
            onEditSnapshotScheduleInternal();
        }
    }

    private void onEditSnapshotScheduleInternal() {
        final GlusterVolumeSnapshotModel snapshotModel = (GlusterVolumeSnapshotModel) getWindow();

        if (!snapshotModel.validate(false)) {
            return;
        }

        setConfirmWindow(null);

        scheduleSnapshot(snapshotModel, true);
    }
}

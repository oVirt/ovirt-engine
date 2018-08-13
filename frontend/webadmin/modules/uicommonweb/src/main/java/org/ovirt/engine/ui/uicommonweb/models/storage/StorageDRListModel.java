package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageSyncScheduleParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class StorageDRListModel extends SearchableListModel<StorageDomain, StorageDomainDR> {

    private Map<Guid, GlusterGeoRepSession> geoRepSessionsMap = new HashMap<>();

    private UICommand newCommand;

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
    }

    private UICommand editCommand;

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand value) {
        editCommand = value;
    }

    public StorageDRListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().storageDomainDRTitle());
        setHelpTag(HelpTag.disks);
        setHashName("storagedr"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        updateActionAvailability();
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

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        } else {
            setItems(null);
        }
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch();

        IdQueryParameters parameters = new IdQueryParameters(getEntity().getId());
        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetStorageDomainDR, parameters, new AsyncQuery<QueryReturnValue>(returnValue -> {
            List<StorageDomainDR> resultList = returnValue.getReturnValue();
            setItems(resultList);
            populateSessionsMap(resultList);
        }));
    }

    private void populateSessionsMap(List<StorageDomainDR> storageDRs) {
        storageDRs.stream().filter(storageDR -> !geoRepSessionsMap.containsKey(storageDR.getGeoRepSessionId()))
                .forEach(storageDR -> {
                    AsyncDataProvider.getInstance()
                            .getGlusterVolumeGeoRepSessionById(new AsyncQuery<>(geoRepSession -> {
                                if (geoRepSession != null) {
                                    geoRepSessionsMap.put(storageDR.getGeoRepSessionId(), geoRepSession);
                                }
                            }), storageDR.getGeoRepSessionId());
                });
    }

    public Map<Guid, GlusterGeoRepSession> getGeoRepSessionsMap() {
        return geoRepSessionsMap;
    }

    private void updateActionAvailability() {
        List<StorageDomainDR> domainDRs = getSelectedItems() != null ?
                getSelectedItems() : new ArrayList<>();

        getEditCommand().setIsExecutionAllowed(domainDRs.size() == 1);
    }

    private void newDR() {
        if (getWindow() != null) {
            return;
        }

        final StorageDomain storageDomain = getEntity();

        if (storageDomain == null) {
            return;
        }
        final StorageDRModel model = new StorageDRModel();
        model.setHelpTag(HelpTag.new_storage_dr);
        model.setHashName("new_storage_dr"); //$NON-NLS-1$
        model.setTitle(ConstantsManager.getInstance().getConstants().newDRSetup());
        setWindow(model);

        model.getStorageDomain().setEntity(storageDomain);

        model.startProgress();
        AsyncDataProvider.getInstance().getGlusterGeoRepSessionsForStorageDomain(new AsyncQuery<>(geoRepSessions -> {
            model.getGeoRepSession().setItems(geoRepSessions);
            //show error if there are no associated geoRepSessions
            if (geoRepSessions == null || geoRepSessions.isEmpty()) {
                model.setMessage(ConstantsManager.getInstance().getConstants().noGeoRepSessionForGlusterVolume());
            }
            model.stopProgress();
        }), storageDomain.getId());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);

    }


    private void onSave() {
        final StorageDRModel model = (StorageDRModel) getWindow();

        if (!model.validate()) {
            return;
        }
        StorageDomain storageDomain = getEntity();
        final StorageSyncSchedule syncSchedule = new StorageSyncSchedule();
        syncSchedule.setFrequency(model.getFrequency().getSelectedItem());
        syncSchedule.setHour(model.getHour().getSelectedItem());
        syncSchedule.setMins(model.getMins().getSelectedItem());

        Guid georepId = model.getGeoRepSession().getSelectedItem() != null ? model.getGeoRepSession().getSelectedItem().getId() : null;
        StorageSyncScheduleParameters parameter =
                new StorageSyncScheduleParameters(syncSchedule, storageDomain.getId(), georepId);

        model.startProgress();
        Frontend.getInstance().runAction(ActionType.ScheduleGlusterStorageSync,
                parameter,
                result -> {
                    StorageDRListModel localModel =
                            (StorageDRListModel) result.getState();
                    model.stopProgress();
                    localModel.postSaveAction(result.getReturnValue());
                },
                this);
    }

    private void editDR() {
        if (getWindow() != null) {
            return;
        }

        final StorageDomain storageDomain = getEntity();
        if (storageDomain == null) {
            return;
        }
        final StorageDomainDR selectedDR = getSelectedItem();
        StorageSyncSchedule schedule = new StorageSyncSchedule(selectedDR.getScheduleCronExpression());
        final StorageDRModel model = new StorageDRModel();
        model.setHelpTag(HelpTag.new_storage_dr);
        model.setTitle(ConstantsManager.getInstance().getConstants().edit());
        model.setHashName("edit_dr"); //$NON-NLS-1$
        model.getStorageDomain().setEntity(storageDomain);
        setWindow(model);
        model.startProgress();

        model.getFrequency().setSelectedItem(schedule.getFrequency());
        model.getHour().setSelectedItem(schedule.getHour());
        model.getMins().setSelectedItem(schedule.getMins());

        AsyncDataProvider.getInstance().getGlusterGeoRepSessionsForStorageDomain(new AsyncQuery<>(geoRepSessions -> {
            model.getGeoRepSession().setItems(geoRepSessions);
            model.getGeoRepSession().setSelectedItem(Linq.firstOrNull(geoRepSessions,
                    new Linq.IdPredicate<>(selectedDR.getGeoRepSessionId())));
            model.stopProgress();
        }), storageDomain.getId());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    public void postSaveAction(ActionReturnValue returnValue) {
        if (returnValue != null && returnValue.getSucceeded()) {
            setWindow(null);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newDR();
        } else if (command == getEditCommand()) {
            editDR();
        } else if (command.getName().equalsIgnoreCase("OnSave")) {//$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageDRListModel"; //$NON-NLS-1$
    }
}

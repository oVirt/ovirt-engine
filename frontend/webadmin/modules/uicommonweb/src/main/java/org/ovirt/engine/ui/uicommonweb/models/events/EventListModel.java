package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.communication.RefreshActiveModelEvent;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class EventListModel<E> extends ListWithSimpleDetailsModel<E, AuditLog> implements HasDismissCommand {
    private UICommand privateRefreshCommand;

    public UICommand getRefreshCommand() {
        return privateRefreshCommand;
    }

    private void setRefreshCommand(UICommand value) {
        privateRefreshCommand = value;
    }

    private UICommand detailsCommand;

    public UICommand getDetailsCommand() {
        return detailsCommand;
    }

    private UICommand dismissCommand;

    @Override
    public UICommand getDismissCommand() {
        return dismissCommand;
    }

    private UICommand clearAllCommand;

    public UICommand getClearAllCommand() {
        return clearAllCommand;
    }

    private UICommand displayAllCommand;

    public UICommand getDisplayAllCommand() {
        return displayAllCommand;
    }

    private void setDetailsCommand(UICommand value) {
        detailsCommand = value;
    }

    private AuditLog lastEvent;

    public AuditLog getLastEvent() {
        return lastEvent;
    }

    private void setLastEvent(AuditLog value) {
        if (lastEvent != value) {
            lastEvent = value;
            onPropertyChanged(new PropertyChangedEventArgs("LastEvent")); //$NON-NLS-1$
        }
    }

    private boolean isAdvancedView;

    public boolean getIsAdvancedView() {
        return isAdvancedView;
    }

    public void setIsAdvancedView(boolean value) {
        if (isAdvancedView != value) {
            isAdvancedView = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsAdvancedView")); //$NON-NLS-1$
        }
    }

    private boolean displayEventsOnly;

    public boolean isDisplayEventsOnly() {
        return displayEventsOnly;
    }

    public void setDisplayEventsOnly(boolean displayEventsOnly) {
        this.displayEventsOnly = displayEventsOnly;
    }

    public EventListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().eventsTitle());
        setHelpTag(HelpTag.events);
        setApplicationPlace(WebAdminApplicationPlaces.eventMainTabPlace);
        setHashName("events"); //$NON-NLS-1$

        setRefreshCommand(new UICommand("Refresh", this)); //$NON-NLS-1$
        setDetailsCommand(new UICommand("Details", this)); //$NON-NLS-1$
        dismissCommand = new UICommand("Dismiss", this); //$NON-NLS-1$
        clearAllCommand = new UICommand("Clear All", this); //$NON-NLS-1$
        displayAllCommand = new UICommand("Display All", this); //$NON-NLS-1$

        setDefaultSearchString("Events:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.AUDIT_OBJ_NAME, SearchObjects.AUDIT_PLU_OBJ_NAME });

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    @Override
    protected void doGridTimerExecute() {
        getRefreshCommand().execute();
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("event"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        refreshModel();
    }

    protected void forceRefreshWithoutTimers() {
        getRefreshCommand().execute();
    }

    @Override
    protected boolean handleRefreshActiveModel(RefreshActiveModelEvent event) {
        return false;
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    protected void refreshModel() {
        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object outerObject, Object returnValue) {
                List<AuditLog> newEvents = ((VdcQueryReturnValue) returnValue).getReturnValue();
                List<AuditLog> currentEvents = (List<AuditLog>) getItems();
                if (isDisplayEventsOnly()) {
                    newEvents = new ArrayList<>(Linq.filterAudidLogsByExcludingSeverity(newEvents, AuditLogSeverity.ALERT));
                }
                if (!newEvents.isEmpty() &&
                        currentEvents != null &&
                        (currentEvents.isEmpty() || !currentEvents.get(0).equals(newEvents.get(0)))) {
                    //We received some new events, tell the active models to update.
                    RefreshActiveModelEvent.fire(EventListModel.this, false);
                }
                EventListModel.this.setItems(newEvents);
                EventListModel.this.setLastEvent(Linq.firstOrNull(newEvents));
            }
        });

        SearchParameters params = new SearchParameters(applySortOptions(getSearchString()), SearchType.AuditLog,
                isCaseSensitiveSearch());
        params.setMaxCount(getSearchPageSize());
        params.setRefresh(false);

        Frontend.getInstance().runQuery(VdcQueryType.Search, params, query);
    }

    private void details() {

        AuditLog event = getSelectedItem();

        if (getWindow() != null || event == null) {
            return;
        }

        EventModel model = new EventModel();
        model.setEvent(event);
        model.setTitle(ConstantsManager.getInstance().getConstants().eventDetailsTitle());
        model.setHelpTag(HelpTag.event_details);
        model.setHashName("event_details"); //$NON-NLS-1$
        setWindow(model);

        UICommand command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().close());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRefreshCommand()) {
            syncSearch();
        } else if (command == getDetailsCommand()) {
            details();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if (command == getDismissCommand()) {
            dismissEvent();
        } else if (command == getClearAllCommand()) {
            clearAllDismissedEvents();
        } else if (command == getDisplayAllCommand()) {
            displayAllDismissedEvents();
        }
    }

    public void dismissEvent() {
        dismissEvent(getSelectedItem());
    }

    public void dismissEvent(AuditLog auditLog) {
        if (auditLog == null) {
            return;
        }
        RemoveAuditLogByIdParameters params = new RemoveAuditLogByIdParameters(auditLog.getAuditLogId());
        Frontend.getInstance().runAction(VdcActionType.RemoveAuditLogById, params, new IFrontendActionAsyncCallback() {
            @Override public void executed(FrontendActionAsyncResult result) {
                EventListModel.this.refresh();
            }
        });
    }

    public void clearAllDismissedEvents() {
        Frontend.getInstance().runAction(VdcActionType.ClearAllAuditLogEvents, new VdcActionParametersBase(),
                new IFrontendActionAsyncCallback() {
            @Override public void executed(FrontendActionAsyncResult result) {
                EventListModel.this.refresh();
            }
        });
    }

    public void displayAllDismissedEvents() {
        Frontend.getInstance().runAction(VdcActionType.DisplayAllAuditLogEvents, new VdcActionParametersBase(),
                new IFrontendActionAsyncCallback() {
                    @Override public void executed(FrontendActionAsyncResult result) {
                        EventListModel.this.refresh();
                    }
                });
    }

    @Override
    public UICommand getDoubleClickCommand() {
        return getDetailsCommand();
    }

    private boolean entitiesChanged = true;

    @Override
    protected void entityChanging(E newValue, E oldValue) {
        super.entityChanging(newValue, oldValue);
        entitiesChanged = calculateEntitiesChanged(newValue, oldValue);
    }

    /**
     * Returns true if and only if the two entities: <li>are not null <li>implement the IVdcQueryable.getQueryableId()
     * method <li>the old.getQueryableId().equals(new.getQueryableId())
     */
    private boolean calculateEntitiesChanged(Object newValue, Object oldValue) {
        if (newValue == null || oldValue == null) {
            return true;
        }

        if (!(newValue instanceof IVdcQueryable && oldValue instanceof IVdcQueryable)) {
            return true;
        }

        Object oldValueQueriable = null;
        Object newValueQueriable = null;
        try {
            oldValueQueriable = ((IVdcQueryable) oldValue).getQueryableId();
            newValueQueriable = ((IVdcQueryable) newValue).getQueryableId();
        } catch (UnsupportedOperationException e) {
            return true;
        }

        if (oldValueQueriable == null || newValueQueriable == null) {
            return true;
        }

        return !oldValueQueriable.equals(newValueQueriable);
    }

    /**
     * Runs the onEntityContentChanged() only when the calculateEntitiesChanged(new, old) returns true
     */
    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (entitiesChanged) {
            onEntityContentChanged();
        }
    }

    /**
     * Called when the OnEntityChanged() ensures, that the new entity is different than the old one (based on the
     * IVdcQueryable). Override it in child classes to refresh your model.
     */
    protected void onEntityContentChanged() {
    }

    @Override
    protected String getListName() {
        return "EventListModel"; //$NON-NLS-1$
    }

}

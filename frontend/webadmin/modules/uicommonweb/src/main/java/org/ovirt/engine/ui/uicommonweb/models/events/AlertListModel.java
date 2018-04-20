package org.ovirt.engine.ui.uicommonweb.models.events;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class AlertListModel extends SearchableListModel<Void, AuditLog> implements HasDismissCommand {
    private UICommand dismissCommand;

    public UICommand getDismissCommand() {
        return dismissCommand;
    }

    public void setDismissCommand(UICommand value) {
        dismissCommand = value;
    }

    private UICommand displayAllCommand;

    public UICommand getDisplayAllCommand() {
        return displayAllCommand;
    }

    public void setDisplayAllCommand(UICommand value) {
        displayAllCommand = value;
    }

    private UICommand clearAllCommand;

    public UICommand getClearAllCommand() {
        return clearAllCommand;
    }

    public void setClearAllCommand(UICommand value) {
        clearAllCommand = value;
    }

    public AlertListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().alertsTitle());
        setIsTimerDisabled(false);
        setDismissCommand(new UICommand("Dismiss Alert", this)); //$NON-NLS-1$
        setDisplayAllCommand(new UICommand("Display All", this)); //$NON-NLS-1$
        setClearAllCommand(new UICommand("Clear All", this)); //$NON-NLS-1$
        getSearchCommand().execute();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command == getDismissCommand()) {
            dismissAlert();
        } else if (command == getDisplayAllCommand()) {
             displayAllAlerts();
        } else if (command == getClearAllCommand()) {
            clearAllAlerts();
        }
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters("Events: severity=alert", SearchType.AuditLog); //$NON-NLS-1$
        tempVar.setMaxCount(getSearchPageSize());
        tempVar.setRefresh(false);
        SearchParameters searchParameters = tempVar;

        Frontend.getInstance().runQuery(QueryType.Search, searchParameters, new SetItemsAsyncQuery());
    }

    public void dismissAlert() {
        dismissAlert(getSelectedItem());
    }

    public void dismissAlert(AuditLog auditLog) {
        RemoveAuditLogByIdParameters params = new RemoveAuditLogByIdParameters(auditLog.getAuditLogId());
        Frontend.getInstance().runAction(ActionType.RemoveAuditLogById, params);
    }

    public void clearAllAlerts() {
        Frontend.getInstance().runAction(ActionType.ClearAllAuditLogAlerts, new ActionParametersBase());
    }

    public void displayAllAlerts() {
        Frontend.getInstance().runAction(ActionType.DisplayAllAuditLogAlerts, new ActionParametersBase());
    }

    @Override
    protected String getListName() {
        return "AlertListModel"; //$NON-NLS-1$
    }

    @Override
    public boolean useDefaultItemComparator() {
        // We want alerts to be rendered in the same order as received from backend.
        return false;
    }

}

package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class AlertListModel extends SearchableListModel
{
    private UICommand dismissCommand;

    public UICommand getDismissCommand() {
        return dismissCommand;
    }

    public void setDismissCommand(UICommand value) {
        dismissCommand = value;
    }

    private UICommand clearAllCommand;

    public UICommand getClearAllCommand() {
        return clearAllCommand;
    }

    public void setClearAllCommand(UICommand value) {
        clearAllCommand = value;
    }

    public AlertListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().alertsTitle());
        setIsTimerDisabled(false);
        setDismissCommand(new UICommand("Dismiss Alert", this)); //$NON-NLS-1$
        setClearAllCommand(new UICommand("Clear All", this)); //$NON-NLS-1$
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command == getDismissCommand())
        {
            dismissAlert();
        }
        else if (command == getClearAllCommand())
        {
             clearAllDismissedAlerts();
        }
    }

    @Override
    protected void syncSearch()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                AlertListModel alertListModel = (AlertListModel) model;
                ArrayList<AuditLog> list =
                        (ArrayList<AuditLog>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                alertListModel.setItems(list);
            }
        };

        SearchParameters tempVar = new SearchParameters("Events: severity=alert", SearchType.AuditLog); //$NON-NLS-1$
        tempVar.setMaxCount(getSearchPageSize());
        tempVar.setRefresh(false);
        SearchParameters searchParameters = tempVar;

        Frontend.getInstance().runQuery(VdcQueryType.Search, searchParameters, _asyncQuery);
    }

    public void dismissAlert() {
        AuditLog auditLog = (AuditLog) getSelectedItem();
        RemoveAuditLogByIdParameters params = new RemoveAuditLogByIdParameters(auditLog.getAuditLogId());
        Frontend.getInstance().runAction(VdcActionType.RemoveAuditLogById, params);
    }

    public void clearAllDismissedAlerts() {
        VdcActionParametersBase params = new VdcActionParametersBase();
        Frontend.getInstance().runAction(VdcActionType.ClearAllDismissedAuditLogs, params);
    }

    @Override
    protected String getListName() {
        return "AlertListModel"; //$NON-NLS-1$
    }
}

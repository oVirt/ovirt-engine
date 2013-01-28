package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class AlertListModel extends SearchableListModel
{
    public AlertListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().alertsTitle());
        setIsTimerDisabled(false);
    }

    @Override
    protected void SyncSearch()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
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

        Frontend.RunQuery(VdcQueryType.Search, searchParameters, _asyncQuery);
    }

    @Override
    protected String getListName() {
        return "AlertListModel"; //$NON-NLS-1$
    }
}

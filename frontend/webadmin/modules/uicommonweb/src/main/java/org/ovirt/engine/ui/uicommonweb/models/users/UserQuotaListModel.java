package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class UserQuotaListModel extends SearchableListModel
{
    public UserQuotaListModel() {
        setTitle("Quotas");
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch() {
        if (getEntity() == null) {
            return;
        }
        super.SyncSearch();
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.Model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                UserQuotaListModel quotaListModel = (UserQuotaListModel) model;
                quotaListModel.setItems((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        GetQuotasByAdElementIdQueryParameters parameters = new GetQuotasByAdElementIdQueryParameters();
        parameters.setAdElementId(((DbUser) getEntity()).getuser_id());

        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetQuotasByAdElementId,
                parameters,
                asyncQuery);
    }

    @Override
    protected String getListName() {
        return "UserQuotaListModel";
    }
}

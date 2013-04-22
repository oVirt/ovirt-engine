package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetQuotaVdsGroupByQuotaIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class QuotaClusterListModel extends SearchableListModel {

    public QuotaClusterListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().clustersTitle());
        setHashName("clusters"); // $//$NON-NLS-1$
    }

    @Override
    protected void SyncSearch() {
        super.SyncSearch();
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.Model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                QuotaClusterListModel quotaClusterListModel = (QuotaClusterListModel) model;
                quotaClusterListModel.setItems((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        GetQuotaVdsGroupByQuotaIdQueryParameters params = new GetQuotaVdsGroupByQuotaIdQueryParameters();
        params.setQuotaId(((Quota) getEntity()).getId());
        Frontend.RunQuery(VdcQueryType.GetQuotaVdsGroupByQuotaId,
                params,
                asyncQuery);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        if (getEntity() == null) {
            return;
        }
        getSearchCommand().Execute();
    }

    @Override
    protected String getListName() {
        return "QuotaClusterListModel"; //$NON-NLS-1$
    }

}

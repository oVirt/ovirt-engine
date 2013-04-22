package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetQuotaStorageByQuotaIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class QuotaStorageListModel extends SearchableListModel {

    public QuotaStorageListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setHashName("storage"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch() {
        super.SyncSearch();
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.Model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                QuotaStorageListModel quotaStorageListModel = (QuotaStorageListModel) model;
                quotaStorageListModel.setItems((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        GetQuotaStorageByQuotaIdQueryParameters params = new GetQuotaStorageByQuotaIdQueryParameters();
        params.setQuotaId(((Quota) getEntity()).getId());
        Frontend.RunQuery(VdcQueryType.GetQuotaStorageByQuotaId,
                params,
                asyncQuery);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        if (getEntity() == null) {
            return;
        }
        getSearchCommand().Execute();
    }

    @Override
    protected String getListName() {
        return "QuotaStorageListModel"; //$NON-NLS-1$
    }

}

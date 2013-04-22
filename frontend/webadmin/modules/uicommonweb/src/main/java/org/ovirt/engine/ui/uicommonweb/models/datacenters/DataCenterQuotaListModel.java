package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetQuotaByStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class DataCenterQuotaListModel extends QuotaListModel
{
    public DataCenterQuotaListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().quotaTitle());
        setHashName("quota"); //$NON-NLS-1$
    }

    @Override
    public storage_pool getEntity()
    {
        return (storage_pool) ((super.getEntity() instanceof storage_pool) ? super.getEntity() : null);
    }

    public void setEntity(storage_pool value)
    {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    protected void SyncSearch() {
        if (getEntity() == null) {
            return;
        }
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.Model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                DataCenterQuotaListModel quotaListModel = (DataCenterQuotaListModel) model;
                quotaListModel.setItems((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        GetQuotaByStoragePoolIdQueryParameters parameters = new GetQuotaByStoragePoolIdQueryParameters();
        parameters.setRefresh(getIsQueryFirstTime());
        parameters.setStoragePoolId(getEntity().getId());
        Frontend.RunQuery(VdcQueryType.GetQuotaByStoragePoolId,
                parameters,
                asyncQuery);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected String getListName() {
        return "DataCenterQuotaListModel"; //$NON-NLS-1$
    }

    @Override
    protected void createQuota() {
        super.createQuota(false);

        QuotaModel quotaModel = (QuotaModel) getWindow();


        quotaModel.getDataCenter().setItems(Arrays.asList(getEntity()));
        quotaModel.getDataCenter().setSelectedItem(getEntity());
        quotaModel.getDataCenter().setIsChangable(false);
    }
}

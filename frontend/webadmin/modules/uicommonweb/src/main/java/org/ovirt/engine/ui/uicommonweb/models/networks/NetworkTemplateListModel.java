package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NetworkTemplateListModel extends SearchableListModel
{

    @Override
    public Network getEntity()
    {
        return (Network) super.getEntity();
    }

    public void setEntity(Network value)
    {
        super.setEntity(value);
    }

    public NetworkTemplateListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setHashName("templates"); // $//$NON-NLS-1$
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        getSearchCommand().Execute();
    }

    @Override
    public void setEntity(Object value)
    {
        if (value == null || !value.equals(getEntity())) {
            super.setEntity(value);
        }
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
//        _asyncQuery.asyncCallback = new INewAsyncCallback() {
//            @Override
//            public void OnSuccess(Object model, Object ReturnValue)
//            {
//                StorageTemplateListModel templateModel = (StorageTemplateListModel) model;
//                templateModel.setItems((ArrayList<VmTemplate>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
//                templateModel.setIsEmpty(((List) templateModel.getItems()).size() == 0);
//            }
//        };
//
//        StorageDomainQueryParametersBase tempVar = new StorageDomainQueryParametersBase(getEntity().getId());
//        tempVar.setRefresh(getIsQueryFirstTime());
//        Frontend.RunQuery(VdcQueryType.GetVmTemplatesFromStorageDomain, tempVar, _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    protected String getListName() {
        return "NetworkTemplateListModel"; //$NON-NLS-1$
    }
}


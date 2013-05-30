package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class StorageVmListModel extends SearchableListModel
{

    @Override
    public StorageDomain getEntity()
    {
        return (StorageDomain) super.getEntity();
    }

    public void setEntity(StorageDomain value)
    {
        super.setEntity(value);
    }

    public StorageVmListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHashName("virtual_machines"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        getSearchCommand().execute();
    }

    @Override
    public void setEntity(Object value)
    {
        if (value == null || !value.equals(getEntity())) {
            super.setEntity(value);
        }
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                StorageVmListModel vmModel = (StorageVmListModel) model;
                vmModel.setItems((ArrayList<VM>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                vmModel.setIsEmpty(((List) vmModel.getItems()).size() == 0);
            }
        };

        StorageDomainQueryParametersBase tempVar =
                new StorageDomainQueryParametersBase(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetVmsByStorageDomain, tempVar, _asyncQuery);
    }

    @Override
    protected String getListName() {
        return "StorageVmListModel"; //$NON-NLS-1$
    }
}

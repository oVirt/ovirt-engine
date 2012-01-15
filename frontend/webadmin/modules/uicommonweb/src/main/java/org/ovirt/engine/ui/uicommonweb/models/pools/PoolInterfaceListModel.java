package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class PoolInterfaceListModel extends SearchableListModel
{
    public PoolInterfaceListModel()
    {
        setTitle("Network Interfaces");
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        vm_pools pool = (vm_pools) getEntity();
        if (pool != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    VM vm = (VM) result;
                    if (vm != null)
                    {
                        PoolInterfaceListModel poolInterfaceListModel = (PoolInterfaceListModel) model;
                        poolInterfaceListModel.SyncSearch(VdcQueryType.GetVmInterfacesByVmId,
                                new GetVmByVmIdParameters(vm.getvm_guid()));
                    }
                }
            };
            AsyncDataProvider.GetAnyVm(_asyncQuery, pool.getvm_pool_name());
        }
    }

    @Override
    protected String getListName() {
        return "PoolInterfaceListModel";
    }
}

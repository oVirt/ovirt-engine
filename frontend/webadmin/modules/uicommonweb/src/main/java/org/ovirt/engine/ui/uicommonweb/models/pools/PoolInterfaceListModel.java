package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class PoolInterfaceListModel extends SearchableListModel
{
    public PoolInterfaceListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHashName("network_interfaces"); //$NON-NLS-1$
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        VmPool pool = (VmPool) getEntity();
        if (pool != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    if (result != null)
                    {
                        VM vm = (VM) ((VdcQueryReturnValue) result).getReturnValue();
                        if (vm == null) {
                           return;
                        }
                        PoolInterfaceListModel poolInterfaceListModel = (PoolInterfaceListModel) model;
                        poolInterfaceListModel.SyncSearch(VdcQueryType.GetVmInterfacesByVmId,
                                new IdQueryParameters(vm.getId()));
                    }
                }
            };
            Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                    new GetVmdataByPoolIdParameters(pool.getVmPoolId()),
                    _asyncQuery);
        }
    }

    @Override
    protected String getListName() {
        return "PoolInterfaceListModel"; //$NON-NLS-1$
    }
}

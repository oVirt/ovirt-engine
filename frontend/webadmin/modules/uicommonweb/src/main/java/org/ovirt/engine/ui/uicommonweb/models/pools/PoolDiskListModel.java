package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class PoolDiskListModel extends SearchableListModel
{
    public PoolDiskListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().virtualDisksTitle());
        setHashName("virtual_disks"); //$NON-NLS-1$
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
                    if (result != null)
                    {
                        VM vm = (VM) ((VdcQueryReturnValue) result).getReturnValue();
                        if (vm == null) {
                            return;
                        }
                        PoolDiskListModel poolDiskListModel = (PoolDiskListModel) model;
                        poolDiskListModel.SyncSearch(VdcQueryType.GetAllDisksByVmId,
                                new GetAllDisksByVmIdParameters(vm.getId()));
                    }
                }
            };
            Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                    new GetVmdataByPoolIdParameters(pool.getvm_pool_id()),
                    _asyncQuery);
        }
    }

    @Override
    protected String getListName() {
        return "PoolDiskListModel"; //$NON-NLS-1$
    }
}

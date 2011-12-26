package org.ovirt.engine.ui.uicommonweb.models.pools;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.*;

@SuppressWarnings("unused")
public class PoolDiskListModel extends SearchableListModel
{
	public PoolDiskListModel()
	{
		setTitle("Virtual Disks");
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		vm_pools pool = (vm_pools)getEntity();
		if (pool != null)
		{
			AsyncQuery _asyncQuery = new AsyncQuery();
			_asyncQuery.setModel(this);
			_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
											{
												VM vm = (VM)result;
												if (vm != null)
												{
													PoolDiskListModel poolDiskListModel = (PoolDiskListModel)model;
													poolDiskListModel.SyncSearch(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getvm_guid()));
												}
											}};
			AsyncDataProvider.GetAnyVm(_asyncQuery, pool.getvm_pool_name());
		}
	}

    @Override
    protected String getListName() {
        return "PoolDiskListModel";
    }
}
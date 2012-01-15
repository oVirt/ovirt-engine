package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;

@SuppressWarnings("unused")
public class VmBasicDiskListModel extends SearchableListModel
{
    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        if (getEntity() instanceof VM)
        {
            VM vm = (VM) getEntity();

            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object ReturnValue)
                {
                    java.util.List<DiskImage> disks =
                            (java.util.List<DiskImage>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                    java.util.ArrayList<DiskImage> diskList = new java.util.ArrayList<DiskImage>();
                    diskList.addAll(disks);
                    Collections.sort(diskList, new Linq.DiskByInternalDriveMappingComparer());

                    SearchableListModel searchableListModel = (SearchableListModel) model;
                    searchableListModel.setItems(diskList);
                }
            };

            Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId,
                    new GetAllDisksByVmIdParameters(vm.getvm_guid()),
                    _asyncQuery);
        }
        else if (getEntity() instanceof vm_pools)
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
                        VmBasicDiskListModel poolDiskListModel = (VmBasicDiskListModel) model;

                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(poolDiskListModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object model1, Object ReturnValue)
                            {
                                java.util.List<DiskImage> disks =
                                        (java.util.List<DiskImage>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                                java.util.ArrayList<DiskImage> diskList = new java.util.ArrayList<DiskImage>();
                                diskList.addAll(disks);
                                Collections.sort(diskList, new Linq.DiskByInternalDriveMappingComparer());

                                SearchableListModel searchableListModel = (SearchableListModel) model1;
                                searchableListModel.setItems(diskList);
                            }
                        };
                        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId,
                                new GetAllDisksByVmIdParameters(vm.getvm_guid()),
                                _asyncQuery1);
                    }
                }
            };

            vm_pools pool = (vm_pools) getEntity();
            AsyncDataProvider.GetAnyVm(_asyncQuery, pool.getvm_pool_name());
        }
    }

    public void OnSuccess(FrontendActionAsyncResult result)
    {
    }

    public void OnFailure(FrontendActionAsyncResult result)
    {
    }

    @Override
    protected String getListName() {
        return "VmBasicDiskListModel";
    }

}

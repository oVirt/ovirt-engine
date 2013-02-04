package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
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
                    List<DiskImage> disks =
                            (List<DiskImage>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                    ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();
                    diskList.addAll(disks);
                    Collections.sort(diskList, new Linq.DiskByAliasComparer());

                    SearchableListModel searchableListModel = (SearchableListModel) model;
                    searchableListModel.setItems(diskList);
                }
            };

            Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId,
                    new GetAllDisksByVmIdParameters(vm.getId()),
                    _asyncQuery);
        }
        else if (getEntity() instanceof VmPool)
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
                        VmBasicDiskListModel poolDiskListModel = (VmBasicDiskListModel) model;

                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(poolDiskListModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object model1, Object ReturnValue)
                            {
                                List<DiskImage> disks =
                                        (List<DiskImage>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                                ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();
                                diskList.addAll(disks);
                                Collections.sort(diskList, new Linq.DiskByAliasComparer());

                                SearchableListModel searchableListModel = (SearchableListModel) model1;
                                searchableListModel.setItems(diskList);
                            }
                        };
                        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId,
                                new GetAllDisksByVmIdParameters(vm.getId()),
                                _asyncQuery1);
                    }
                }
            };

            VmPool pool = (VmPool) getEntity();
            Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                    new GetVmdataByPoolIdParameters(pool.getVmPoolId()),
                    _asyncQuery);
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
        return "VmBasicDiskListModel"; //$NON-NLS-1$
    }

}

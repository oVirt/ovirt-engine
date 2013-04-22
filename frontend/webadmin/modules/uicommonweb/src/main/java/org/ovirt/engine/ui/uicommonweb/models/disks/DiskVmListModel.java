package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmsByDiskGuidParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskVmListModel extends SearchableListModel
{
    private HashMap diskVmMap;

    public DiskVmListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHashName("virtual_machines"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch()
    {
        Disk disk = (Disk) getEntity();
        if (disk == null)
        {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                diskVmMap = (HashMap) ((VdcQueryReturnValue) ReturnValue).getReturnValue();

                ArrayList<VM> vmList = new ArrayList<VM>();
                ArrayList<VM> pluggedList = (ArrayList<VM>) diskVmMap.get(true);
                ArrayList<VM> unPluggedList = (ArrayList<VM>) diskVmMap.get(false);

                if (pluggedList != null)
                    vmList.addAll(pluggedList);
                if (unPluggedList != null)
                    vmList.addAll(unPluggedList);

                searchableListModel.setItems(vmList);
            }
        };

        GetVmsByDiskGuidParameters getVmsByDiskGuidParameters = new GetVmsByDiskGuidParameters(disk.getId());
        getVmsByDiskGuidParameters.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetVmsByDiskGuid, getVmsByDiskGuidParameters, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        DiskImage disk = (DiskImage) getEntity();
    }

    @Override
    protected String getListName() {
        return "DiskVmListModel"; //$NON-NLS-1$

    }

    public boolean isDiskPluggedToVm(VM vm) {
        ArrayList<VM> pluggedList = (ArrayList<VM>) diskVmMap.get(true);
        return pluggedList != null && pluggedList.contains(vm);
    }
}

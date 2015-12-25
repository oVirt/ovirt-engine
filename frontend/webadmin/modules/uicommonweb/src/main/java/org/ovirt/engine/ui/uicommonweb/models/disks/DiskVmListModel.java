package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskVmListModel extends SearchableListModel<Disk, VM> {
    private HashMap diskVmMap;

    public DiskVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHelpTag(HelpTag.virtual_machines);
        setHashName("virtual_machines"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        Disk disk = getEntity();
        if (disk == null) {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                diskVmMap = ((VdcQueryReturnValue) ReturnValue).getReturnValue();

                ArrayList<VM> vmList = new ArrayList<>();
                ArrayList<VM> pluggedList = (ArrayList<VM>) diskVmMap.get(true);
                ArrayList<VM> unPluggedList = (ArrayList<VM>) diskVmMap.get(false);

                if (pluggedList != null) {
                    vmList.addAll(pluggedList);
                }
                if (unPluggedList != null) {
                    vmList.addAll(unPluggedList);
                }

                setItems(vmList);
            }
        };

        IdQueryParameters getVmsByDiskGuidParameters = new IdQueryParameters(disk.getId());
        getVmsByDiskGuidParameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(VdcQueryType.GetVmsByDiskGuid, getVmsByDiskGuidParameters, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
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

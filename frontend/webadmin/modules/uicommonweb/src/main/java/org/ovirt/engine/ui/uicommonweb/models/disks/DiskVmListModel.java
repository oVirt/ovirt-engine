package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskVmListModel extends SearchableListModel<Disk, VM> {
    private Map<Boolean, List<VM>> diskVmMap;

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

        IdQueryParameters getVmsByDiskGuidParameters = new IdQueryParameters(disk.getId());
        getVmsByDiskGuidParameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetVmsByDiskGuid, getVmsByDiskGuidParameters, new AsyncQuery<QueryReturnValue>(returnValue -> {
            diskVmMap = returnValue.getReturnValue();

            List<VM> vmList = new ArrayList<>();
            List<VM> pluggedList = diskVmMap.get(true);
            List<VM> unPluggedList = diskVmMap.get(false);

            if (pluggedList != null) {
                vmList.addAll(pluggedList);
            }
            if (unPluggedList != null) {
                vmList.addAll(unPluggedList);
            }

            setItems(vmList);
        }));

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
        List<VM> pluggedList = diskVmMap.get(true);
        return pluggedList != null && pluggedList.contains(vm);
    }
}

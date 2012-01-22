package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;

@SuppressWarnings("unused")
public class VmImportInterfaceListModel extends VmInterfaceListModel
{
    public VmImportInterfaceListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            VM vm = (VM) getEntity();
            setItems(vm.getInterfaces());
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    protected void SyncSearch() {

    }
}

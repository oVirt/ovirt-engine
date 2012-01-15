package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;

@SuppressWarnings("unused")
public class VmEventListModel extends EventListModel
{
    @Override
    protected void onEntityContentChanged()
    {
        super.onEntityContentChanged();

        // Deal with pool as Entity without failing.
        if (getEntity() != null && !(getEntity() instanceof vm_pools))
        {
            getSearchCommand().Execute();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    public void Search()
    {
        VM vm = (VM) getEntity();

        if (getEntity() != null)
        {
            setSearchString("events: vm.name=" + vm.getvm_name());
            super.Search();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("vm_name"))
        {
            getSearchCommand().Execute();
        }
    }
}

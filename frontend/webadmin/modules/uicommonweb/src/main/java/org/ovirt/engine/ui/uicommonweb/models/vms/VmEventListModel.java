package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmEventListModel extends SubTabEventListModel
{
    @Override
    protected void onEntityContentChanged()
    {
        super.onEntityContentChanged();

        // Deal with pool as Entity without failing.
        if (getEntity() != null && !(getEntity() instanceof VmPool))
        {
            getSearchCommand().Execute();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    public void search()
    {
        VM vm = (VM) getEntity();

        if (getEntity() != null)
        {
            preSearchCalled(vm);
            super.search();
        }
    }

    protected void preSearchCalled(VM vm) {
        setSearchString("events: vm.name=" + vm.getName()); //$NON-NLS-1$
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("vm_name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }
}

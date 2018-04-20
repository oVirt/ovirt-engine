package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmEventListModel extends SubTabEventListModel<VM> {
    @Override
    protected void onEntityContentChanged() {
        super.onEntityContentChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        } else {
            setItems(null);
        }
    }

    @Override
    public void search() {
        VM vm = getEntity();

        if (getEntity() != null) {
            preSearchCalled(vm);
            super.search();
        }
    }

    protected void preSearchCalled(VM vm) {
        setSearchString("events: vm.name=" + vm.getName()); //$NON-NLS-1$
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("vm_name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }
}

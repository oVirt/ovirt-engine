package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class HostVmListModel extends VmListModel
{
    @Override
    public VDS getEntity()
    {
        return (VDS) super.getEntity();
    }

    public void setEntity(VDS value)
    {
        super.setEntity(value);
    }

    @Override
    protected void syncSearch()
    {
        search();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        search();
    }

    @Override
    public void search()
    {
        // Override standard search query mechanism.
        // During the migration, the VM should be visible on source host (Migrating From), and also
        // on destination host (Migrating To)
        if (getEntity() != null)
        {
            AsyncDataProvider.getInstance().getVmsRunningOnOrMigratingToVds(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    @SuppressWarnings("unchecked")
                    final ArrayList<VM> list = (ArrayList<VM>) returnValue;
                    final HostVmListModel model = (HostVmListModel) target;
                    model.setItems(list);
                }
            }), getEntity().getId());
        } else {
            setItems(new ArrayList<VM>());
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("vds_name")) //$NON-NLS-1$
        {
            search();
        }
    }
}

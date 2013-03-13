package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmAppListModel extends SearchableListModel
{

    @Override
    public Iterable getItems()
    {
        return items;
    }

    @Override
    public void setItems(Iterable value)
    {
        if (items != value)
        {
            ItemsChanging(value, items);
            items = value;
            ItemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            OnPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    public VmAppListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().applicationsTitle());
        setHashName("applications"); //$NON-NLS-1$
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);
        if (e.PropertyName.equals("appList")) //$NON-NLS-1$
        {
            updateAppList();
        }
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        updateAppList();
    }

    protected void updateAppList() {
        if (getEntity() instanceof VM) {
            updateAppListFromVm((VM) getEntity());
        } else {
            VmPool pool = (VmPool) getEntity();
            if (pool != null)
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
                            if (vm != null) {
                                updateAppListFromVm(vm);
                            }
                        }
                    }
                };
                Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                        new GetVmdataByPoolIdParameters(pool.getVmPoolId()),
                        _asyncQuery);
            }
        }
    }

    private void updateAppListFromVm(VM vm) {
        setItems(null);
        if (vm != null && vm.getAppList() != null)
        {
            ArrayList<String> list = new ArrayList<String>();

            String[] array = vm.getAppList().split("[,]", -1); //$NON-NLS-1$
            for (String item : array)
            {
                list.add(item);
            }
            Collections.sort(list);

            setItems(list);
        } else {
            setItems(new ArrayList<String>());
        }
    }

    @Override
    protected void SyncSearch()
    {
        updateAppList();
        setIsQueryFirstTime(false);
    }

    @Override
    protected String getListName() {
        return "VmAppListModel"; //$NON-NLS-1$
    }
}

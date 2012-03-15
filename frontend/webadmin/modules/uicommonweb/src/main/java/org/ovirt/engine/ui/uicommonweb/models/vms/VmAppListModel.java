package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

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
            OnPropertyChanged(new PropertyChangedEventArgs("Items"));
        }
    }

    public VmAppListModel()
    {
        setTitle("Applications");
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);
        if (e.PropertyName.equals("app_list"))
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
            vm_pools pool = (vm_pools) getEntity();
            if (pool != null)
            {
                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object result)
                    {
                        VM vm = (VM) result;
                        if (vm != null)
                        {
                            updateAppListFromVm(vm);
                        }
                    }
                };
                AsyncDataProvider.GetAnyVm(_asyncQuery, pool.getvm_pool_name());
            }
        }
    }

    private void updateAppListFromVm(VM vm) {
        setItems(null);
        if (vm != null && vm.getapp_list() != null)
        {
            java.util.ArrayList<String> list = new java.util.ArrayList<String>();

            String[] array = vm.getapp_list().split("[,]", -1);
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
        return "VmAppListModel";
    }
}

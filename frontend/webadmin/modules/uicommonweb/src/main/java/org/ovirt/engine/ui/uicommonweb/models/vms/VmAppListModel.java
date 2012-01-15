package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
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
            UpdateAppList();
        }
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        // Deal with pool as Entity without failing.
        if (!(getEntity() instanceof vm_pools))
        {
            UpdateAppList();
        }
    }

    private void UpdateAppList()
    {
        VM vm = (VM) getEntity();

        // Items = (Entity != null && Entity.app_list != null)
        // ? Entity.app_list.Split(',').OrderBy(a => a)
        // : null;

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
        }
    }

    @Override
    protected void SyncSearch()
    {
        UpdateAppList();
        setIsQueryFirstTime(false);
    }

    @Override
    protected String getListName() {
        return "VmAppListModel";
    }
}

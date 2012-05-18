package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;

import java.util.ArrayList;
import java.util.List;

public abstract class StorageModelBehavior
{
    private StorageModel privateModel;

    public StorageModel getModel()
    {
        return privateModel;
    }

    public void setModel(StorageModel value)
    {
        privateModel = value;
    }

    public List<storage_pool> FilterDataCenter(List<storage_pool> source)
    {
        return source;
    }

    public void UpdateItemsAvailability()
    {
    }

    public void FilterUnSelectableModels()
    {
        // Filter UnSelectable models from AvailableStorageItems list
        ArrayList<Object> filterredItems = new ArrayList<Object>();
        ArrayList<IStorageModel> items = Linq.<IStorageModel> Cast(getModel().getItems());
        for (IStorageModel model : items)
        {
            if (((Model) model).getIsSelectable())
            {
                filterredItems.add(model);
            }
        }

        getModel().getAvailableStorageItems().setItems(filterredItems);
    }

    public void OnStorageModelUpdated(IStorageModel model)
    {
        // Update models list (the list is used for checking update completion)
        getModel().UpdatedStorageModels.add(model);

        // Filter UnSelectable model from AvailableStorageItems list
        if (getModel().UpdatedStorageModels.size() == Linq.<IStorageModel> Cast(getModel().getItems()).size())
        {
            getModel().getHost().setItems(new ArrayList<HostModel>());
            getModel().getHost().setSelectedItem(null);

            FilterUnSelectableModels();
            getModel().UpdatedStorageModels.clear();
            getModel().ChooseFirstItem();

            if (getModel().getSelectedItem() != null) {
                getModel().UpdateFormat();
                getModel().UpdateHost();
            }
        }
    }
}

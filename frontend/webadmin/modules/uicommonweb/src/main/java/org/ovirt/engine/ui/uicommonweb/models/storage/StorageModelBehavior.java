package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
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

    public java.util.List<storage_pool> FilterDataCenter(java.util.List<storage_pool> source)
    {
        return source;
    }

    public void UpdateItemsAvailability()
    {
    }

    public void FilterUnSelectableModels()
    {
        // Filter UnSelectable models from AvailableStorageItems list
        java.util.ArrayList<Object> filterredItems = new java.util.ArrayList<Object>();
        java.util.ArrayList<IStorageModel> items = Linq.<IStorageModel> Cast(getModel().getItems());
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
            FilterUnSelectableModels();
            getModel().UpdatedStorageModels.clear();
            getModel().ChooseFirstItem();
        }
    }
}

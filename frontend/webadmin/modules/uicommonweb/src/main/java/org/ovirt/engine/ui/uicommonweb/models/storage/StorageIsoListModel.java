package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class StorageIsoListModel extends SearchableListModel
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
            EntityModel lastSelectedItem = (EntityModel) getSelectedItem();
            ArrayList<EntityModel> lastSelectedItems = (ArrayList<EntityModel>) getSelectedItems();

            ItemsChanging(value, items);
            items = value;
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$

            selectedItem = null;
            if (getSelectedItems() != null)
            {
                getSelectedItems().clear();
            }

            if (lastSelectedItem != null)
            {
                EntityModel newSelectedItem = null;
                ArrayList<EntityModel> newItems = (ArrayList<EntityModel>) value;

                if (newItems != null)
                {
                    for (EntityModel newItem : newItems)
                    {
                        // Search for selected item
                        if (newItem.getHashName().equals(lastSelectedItem.getHashName()))
                        {
                            newSelectedItem = newItem;
                            break;
                        }
                        else
                        {
                            // Search for selected items
                            for (EntityModel item : lastSelectedItems)
                            {
                                if (newItem.getHashName().equals(item.getHashName()))
                                {
                                    selectedItems.add(newItem);
                                }
                            }
                        }
                    }
                }

                if (newSelectedItem != null)
                {
                    selectedItem = newSelectedItem;
                    selectedItems.add(newSelectedItem);
                }
            }
            onSelectedItemChanged();
        }
    }

    public StorageIsoListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().imagesTitle());
        setHashName("images"); // $//$NON-NLS-1$

        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getIsAvailable())
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    public void setEntity(Object value)
    {
        if (value == null || !value.equals(getEntity())) {
            super.setEntity(value);
        }
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.syncSearch();

        if (getProgress() != null)
        {
            return;
        }

        StorageDomain storageDomain = (StorageDomain) getEntity();

        GetImagesListParameters imagesListParams = new GetImagesListParameters(storageDomain.getId(), ImageFileType.All);
        imagesListParams.setForceRefresh(true);
        imagesListParams.setRefresh(getIsQueryFirstTime());

        StartProgress(null);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnObject)
            {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnObject;

                StopProgress();

                if (!returnValue.getSucceeded()) {
                    return;
                }

                ArrayList<RepoFileMetaData> repoFileList = (ArrayList<RepoFileMetaData>)
                        returnValue.getReturnValue();

                Collections.sort(repoFileList, new Comparator<RepoFileMetaData>() {
                    @Override
                    public int compare(RepoFileMetaData a, RepoFileMetaData b) {
                        return a.getRepoImageId().compareToIgnoreCase(b.getRepoImageId());
                    }
                });

                ArrayList<EntityModel> entityList = new ArrayList<EntityModel>();

                for (RepoFileMetaData repoFileItem : repoFileList) {
                    EntityModel entityItem = new EntityModel();
                    entityItem.setHashName(repoFileItem.getRepoImageId());
                    entityItem.setTitle(repoFileItem.getRepoImageTitle());
                    entityItem.setEntity(repoFileItem.getFileType());
                    entityList.add(entityItem);
                }

                UpdateIsoModels(entityList);
                setIsEmpty(entityList.isEmpty());
            }
        };

        Frontend.RunQuery(VdcQueryType.GetImagesList, imagesListParams, _asyncQuery);
    }

    @Override
    protected void asyncSearch()
    {
        super.asyncSearch();
        syncSearch();
    }

    private void UpdateIsoModels(ArrayList<EntityModel> items)
    {
        ArrayList<EntityModel> newItems = new ArrayList<EntityModel>();

        if (getItems() != null)
        {
            ArrayList<EntityModel> oldItems = Linq.ToList((Iterable<EntityModel>) getItems());

            for (EntityModel newItem : items)
            {
                boolean isItemUpdated = false;
                for (EntityModel item : oldItems)
                {
                    if (newItem.getHashName().equals(item.getHashName()))
                    {
                        item.setTitle(newItem.getTitle());
                        item.setEntity(newItem.getEntity());
                        newItems.add(item);
                        isItemUpdated = true;
                        break;
                    }
                }

                if (!isItemUpdated)
                {
                    newItems.add(newItem);
                }
            }
        }

        setItems(newItems.isEmpty() ? items : newItems);
    }

    @Override
    protected String getListName() {
        return "StorageIsoListModel"; //$NON-NLS-1$
    }
}

package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class StorageIsoListModel extends SearchableListModel implements IFrontendMultipleQueryAsyncCallback
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
            OnPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$

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
            OnSelectedItemChanged();
        }
    }

    public StorageIsoListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().imagesTitle());
        setHashName("images"); // $//$NON-NLS-1$

        setIsTimerDisabled(true);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

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
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch();

        if (getProgress() != null)
        {
            return;
        }

        StorageDomain storageDomain = (StorageDomain) getEntity();

        GetImagesListParameters isoListParams = new GetImagesListParameters(
                storageDomain.getId(), ImageFileType.ISO);
        isoListParams.setForceRefresh(true);
        isoListParams.setRefresh(getIsQueryFirstTime());

        GetImagesListParameters floppyListParams = new GetImagesListParameters(
                storageDomain.getId(), ImageFileType.Floppy);
        floppyListParams.setForceRefresh(true);
        floppyListParams.setRefresh(getIsQueryFirstTime());

        StartProgress(null);

        Frontend.RunMultipleQueries(new ArrayList<VdcQueryType>(Arrays.asList(new VdcQueryType[] {
                VdcQueryType.GetImagesList, VdcQueryType.GetImagesList })),
                new ArrayList<VdcQueryParametersBase>(Arrays.asList(new VdcQueryParametersBase[] {
                        isoListParams, floppyListParams })),
                this);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    public void Executed(FrontendMultipleQueryAsyncResult result)
    {
        StopProgress();

        ArrayList<EntityModel> items = new ArrayList<EntityModel>();

        VdcQueryReturnValue isoReturnValue = result.getReturnValues().get(0);

        ArrayList<RepoFileMetaData> isoImages =
                isoReturnValue.getSucceeded() ? (ArrayList<RepoFileMetaData>) isoReturnValue.getReturnValue()
                        : new ArrayList<RepoFileMetaData>();

        ArrayList<String> fileNameList = new ArrayList<String>();
        for (RepoFileMetaData RepoFileMetaData : isoImages) {
            fileNameList.add(RepoFileMetaData.getRepoFileName());
        }
        Collections.sort(fileNameList, new Linq.CaseInsensitiveComparer());

        for (String item : fileNameList) {
            EntityModel model = new EntityModel();
            model.setHashName(item);
            model.setTitle(item);
            model.setEntity("CD/DVD"); //$NON-NLS-1$
            items.add(model);
        }

        VdcQueryReturnValue floppyReturnValue = result.getReturnValues().get(1);

        ArrayList<RepoFileMetaData> floppyImages =
                floppyReturnValue.getSucceeded() ? (ArrayList<RepoFileMetaData>) floppyReturnValue.getReturnValue()
                        : new ArrayList<RepoFileMetaData>();

        ArrayList<String> floppyNameList = new ArrayList<String>();
        for (RepoFileMetaData RepoFileMetaData : floppyImages) {
            floppyNameList.add(RepoFileMetaData.getRepoFileName());
        }
        Collections.sort(floppyNameList, new Linq.CaseInsensitiveComparer());

        for (String item : floppyNameList) {
            EntityModel model = new EntityModel();
            model.setHashName(item);
            model.setTitle(item);
            model.setEntity("Floppy"); //$NON-NLS-1$
            items.add(model);
        }

        UpdateIsoModels(items);
        setIsEmpty(items.isEmpty());
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

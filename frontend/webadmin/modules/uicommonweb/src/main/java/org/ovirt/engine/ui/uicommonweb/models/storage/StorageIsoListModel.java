package org.ovirt.engine.ui.uicommonweb.models.storage;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.queries.*;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class StorageIsoListModel extends SearchableListModel implements IFrontendMultipleQueryAsyncCallback
{
	public Iterable getItems()
	{
		return items;
	}
	public void setItems(Iterable value)
	{
		if (items != value)
		{
			EntityModel lastSelectedItem = (EntityModel)getSelectedItem();
			java.util.ArrayList<EntityModel> lastSelectedItems = (java.util.ArrayList<EntityModel>)getSelectedItems();

			ItemsChanging(value, items);
			items = value;
			getItemsChangedEvent().raise(this, EventArgs.Empty);
			OnPropertyChanged(new PropertyChangedEventArgs("Items"));

			selectedItem = null;
			if (getSelectedItems() != null)
			{
				getSelectedItems().clear();
			}

			if (lastSelectedItem != null)
			{
				EntityModel newSelectedItem = null;
				java.util.ArrayList<EntityModel> newItems = (java.util.ArrayList<EntityModel>)value;

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
		setTitle("Images");
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

		storage_domains storageDomain = (storage_domains)getEntity();

		GetAllIsoImagesListParameters tempVar = new GetAllIsoImagesListParameters();
		tempVar.setStorageDomainId(storageDomain.getid());
		tempVar.setForceRefresh(true);
		tempVar.setRefresh(getIsQueryFirstTime());
		GetAllIsoImagesListParameters parameters = tempVar;

		StartProgress(null);

		Frontend.RunMultipleQueries(new java.util.ArrayList<VdcQueryType>(java.util.Arrays.asList(new VdcQueryType[] { VdcQueryType.GetAllIsoImagesList, VdcQueryType.GetAllFloppyImagesList })), new java.util.ArrayList<VdcQueryParametersBase>(java.util.Arrays.asList(new VdcQueryParametersBase[] { parameters, parameters })), this);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}

	public void Executed(FrontendMultipleQueryAsyncResult result)
	{
		StopProgress();

		java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();

		VdcQueryReturnValue isoReturnValue = result.getReturnValues().get(0);

		java.util.ArrayList<RepoFileMetaData> isoImages = isoReturnValue.getSucceeded() ? (java.util.ArrayList<RepoFileMetaData>)isoReturnValue.getReturnValue() : new java.util.ArrayList<RepoFileMetaData>();

		for (RepoFileMetaData item : isoImages)
		{
			EntityModel model = new EntityModel();
			model.setHashName(item.getRepoFileName());
			model.setTitle(item.getRepoFileName());
			model.setEntity("CD/DVD");
			items.add(model);
		}


		VdcQueryReturnValue floppyReturnValue = result.getReturnValues().get(1);

		java.util.ArrayList<RepoFileMetaData> floppyImages = floppyReturnValue.getSucceeded() ? (java.util.ArrayList<RepoFileMetaData>)floppyReturnValue.getReturnValue() : new java.util.ArrayList<RepoFileMetaData>();

		for (RepoFileMetaData item : floppyImages)
		{
			EntityModel model = new EntityModel();
			model.setHashName(item.getRepoFileName());
			model.setTitle(item.getRepoFileName());
			model.setEntity("Floppy");
			items.add(model);
		}

		UpdateIsoModels(items);
		setIsEmpty(items.isEmpty());
	}

	private void UpdateIsoModels(java.util.ArrayList<EntityModel> items)
	{
		java.util.ArrayList<EntityModel> newItems = new java.util.ArrayList<EntityModel>();

		if (getItems() != null)
		{
			java.util.ArrayList<EntityModel> oldItems = Linq.ToList((Iterable<EntityModel>)getItems());

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

		setItems(newItems);
	}
}
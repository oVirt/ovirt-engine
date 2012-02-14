package org.ovirt.engine.ui.uicommon.models.storage;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.queries.*;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class StorageIsoListModel extends SearchableListModel implements IFrontendMultipleQueryAsyncCallback
{
	public StorageIsoListModel()
	{
		setTitle("Images");
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		getSearchCommand().Execute();
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
		super.SyncSearch();

		if (getProgress() != null)
		{
			return;
		}

		storage_domains storageDomain = (storage_domains)getEntity();

		GetAllIsoImagesListParameters tempVar = new GetAllIsoImagesListParameters();
		tempVar.setStorageDomainId(storageDomain.getId());
		tempVar.setForceRefresh(true);
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
			model.setTitle(item.getRepoFileName());
			model.setEntity("CD/DVD");
			items.add(model);
		}


		VdcQueryReturnValue floppyReturnValue = result.getReturnValues().get(1);

		java.util.ArrayList<RepoFileMetaData> floppyImages = floppyReturnValue.getSucceeded() ? (java.util.ArrayList<RepoFileMetaData>)floppyReturnValue.getReturnValue() : new java.util.ArrayList<RepoFileMetaData>();

		for (RepoFileMetaData item : floppyImages)
		{
			EntityModel model = new EntityModel();
			model.setTitle(item.getRepoFileName());
			model.setEntity("Floppy");
			items.add(model);
		}

		setItems(items);
		setIsEmpty(items.isEmpty());
	}
}
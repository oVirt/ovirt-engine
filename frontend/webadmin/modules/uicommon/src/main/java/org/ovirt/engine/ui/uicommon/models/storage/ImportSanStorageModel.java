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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public abstract class ImportSanStorageModel extends SanStorageModelBase
{

	private java.util.List<storage_domains> candidates;
	public java.util.List<storage_domains> getCandidates()
	{
		return candidates;
	}
	public void setCandidates(java.util.List<storage_domains> value)
	{
		if (candidates != value)
		{
			candidates = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Candidates"));
		}
	}

	private String error;
	public String getError()
	{
		return error;
	}
	public void setError(String value)
	{
		if (!StringHelper.stringsEqual(error, value))
		{
			error = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Error"));
		}
	}


	private VDS oldHost;

	protected ImportSanStorageModel()
	{
		InitializeItems(null);
	}

	@Override
	protected void PostDiscoverTargets(java.util.ArrayList<SanTargetModel> newItems)
	{
		super.PostDiscoverTargets(newItems);

		InitializeItems(newItems);
	}

	@Override
	protected void UpdateInternal()
	{
		super.UpdateInternal();

		if (getContainer().getProgress() != null)
		{
			return;
		}

		VDS host = (VDS)getContainer().getHost().getSelectedItem();
		if (host == null)
		{
			return;
		}

		if (host != oldHost)
		{
			setItems(null);
			oldHost = host;
		}

		InitializeItems(null);

		setError(null);
		getContainer().StartProgress(null);

		Frontend.RunQuery(VdcQueryType.GetExistingStorageDomainList, new GetExistingStorageDomainListParameters(host.getvds_id(), getType(), getRole(), ""), new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			ImportSanStorageModel model = (ImportSanStorageModel)target;
			Object result = ((VdcQueryReturnValue)returnValue).getReturnValue();
			if (result != null)
			{
				model.setCandidates((java.util.ArrayList<storage_domains>)result);
			}
			else
			{
				setError("Error while retrieving list of domains. Please consult your Storage Administrator.");
			}
			model.getContainer().StopProgress();

			}
		}, true));
	}

	private void InitializeItems(java.util.List<SanTargetModel> newItems)
	{
		if (getItems() == null)
		{
			setItems(new ObservableCollection<SanTargetModel>());
		}
		else
		{
			java.util.List<SanTargetModel> items = (java.util.List<SanTargetModel>)getItems();

			//Add new targets.
			if (newItems != null)
			{
				for (SanTargetModel newItem : newItems)
				{
					if (Linq.FirstOrDefault(items, new Linq.TargetPredicate(newItem)) == null)
					{
						items.add(newItem);
					}
				}
			}

			UpdateLoginAllAvailability();
		}
	}

	@Override
	public boolean Validate()
	{
		setIsValid(getSelectedItem() != null);

		return super.Validate() && getIsValid();
	}
}
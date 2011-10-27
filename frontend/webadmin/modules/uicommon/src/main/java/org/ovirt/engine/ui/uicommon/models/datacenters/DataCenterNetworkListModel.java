package org.ovirt.engine.ui.uicommon.models.datacenters;
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

import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class DataCenterNetworkListModel extends SearchableListModel implements IFrontendMultipleQueryAsyncCallback
{
	private static final String ENGINE_NETWORK = "engine";


	private UICommand privateNewCommand;
	public UICommand getNewCommand()
	{
		return privateNewCommand;
	}
	private void setNewCommand(UICommand value)
	{
		privateNewCommand = value;
	}
	private UICommand privateEditCommand;
	public UICommand getEditCommand()
	{
		return privateEditCommand;
	}
	private void setEditCommand(UICommand value)
	{
		privateEditCommand = value;
	}
	private UICommand privateRemoveCommand;
	public UICommand getRemoveCommand()
	{
		return privateRemoveCommand;
	}
	private void setRemoveCommand(UICommand value)
	{
		privateRemoveCommand = value;
	}



	public storage_pool getEntity()
	{
		return (storage_pool)super.getEntity();
	}
	public void setEntity(storage_pool value)
	{
		super.setEntity(value);
	}

	private Model window;
	public Model getWindow()
	{
		return window;
	}
	public void setWindow(Model value)
	{
		if (window != value)
		{
			window = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Window"));
		}
	}

	private Model confirmWindow;
	public Model getConfirmWindow()
	{
		return confirmWindow;
	}
	public void setConfirmWindow(Model value)
	{
		if (confirmWindow != value)
		{
			confirmWindow = value;
			OnPropertyChanged(new PropertyChangedEventArgs("ConfirmWindow"));
		}
	}

	private java.util.ArrayList<VDSGroup> privateClusterList;
	public java.util.ArrayList<VDSGroup> getClusterList()
	{
		return privateClusterList;
	}
	public void setClusterList(java.util.ArrayList<VDSGroup> value)
	{
		privateClusterList = value;
	}
	private java.util.ArrayList<SelectionTreeNodeModel> privateSelectionNodeList;
	public java.util.ArrayList<SelectionTreeNodeModel> getSelectionNodeList()
	{
		return privateSelectionNodeList;
	}
	public void setSelectionNodeList(java.util.ArrayList<SelectionTreeNodeModel> value)
	{
		privateSelectionNodeList = value;
	}


	public DataCenterNetworkListModel()
	{
		setTitle("Logical Networks");

		setNewCommand(new UICommand("New", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));

		UpdateActionAvailability();
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
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			SearchableListModel searchableListModel = (SearchableListModel)model;
			searchableListModel.setItems((java.util.ArrayList<network>)((VdcQueryReturnValue)ReturnValue).getReturnValue());
		}};

		Frontend.RunQuery(VdcQueryType.GetAllNetworks, new GetAllNetworkQueryParamenters(getEntity().getId()), _asyncQuery);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllNetworks, new GetAllNetworkQueryParamenters(getEntity().getId())));
		setItems(getAsyncResult().getData());
	}

	public void remove()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Logical Network(s)");
		model.setHashName("remove_logical_network");
		model.setMessage("Logical Network(s)");

		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		for (network a : Linq.<network>Cast(getSelectedItems()))
		{
			list.add(a.getname());
		}
		model.setItems(list);


		UICommand tempVar = new UICommand("OnRemove", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnRemove()
	{
		java.util.ArrayList<VdcActionParametersBase> pb = new java.util.ArrayList<VdcActionParametersBase>();
		for (network a : Linq.<network>Cast(getSelectedItems()))
		{
			pb.add((VdcActionParametersBase)new AddNetworkStoragePoolParameters(getEntity().getId(), a));
		}
		Frontend.RunMultipleAction(VdcActionType.RemoveNetwork, pb);

		Cancel();
	}

	public void Edit()
	{
		network network = (network)getSelectedItem();

		if (getWindow() != null)
		{
			return;
		}

		DataCenterNetworkModel model = new DataCenterNetworkModel();
		setWindow(model);
		model.setTitle("Edit Logical Network");
		model.setHashName("edit_logical_network");
		model.getName().setEntity(network.getname());
		model.getDescription().setEntity(network.getdescription());
		model.setIsStpEnabled(network.getstp());
		model.setHasVLanTag(network.getvlan_id() != null);
		model.getVLanTag().setEntity((network.getvlan_id() == null ? 0 : network.getvlan_id()));

		setClusterList(DataProvider.GetClusterList(getEntity().getId()));
		setSelectionNodeList(new java.util.ArrayList<SelectionTreeNodeModel>());
		java.util.ArrayList<VdcQueryParametersBase> parametersList = new java.util.ArrayList<VdcQueryParametersBase>();
		java.util.ArrayList<VdcQueryType> queryTypeList = new java.util.ArrayList<VdcQueryType>();
		for (VDSGroup vdsGroup : getClusterList())
		{
			queryTypeList.add(VdcQueryType.GetAllNetworksByClusterId);
			parametersList.add(new VdsGroupQueryParamenters(vdsGroup.getID()));
			SelectionTreeNodeModel tempVar = new SelectionTreeNodeModel();
			tempVar.setIsSelectedNullable(false);
			tempVar.setEntity(vdsGroup);
			tempVar.setDescription(vdsGroup.getname());
			getSelectionNodeList().add(tempVar);
		}
		Frontend.RunMultipleQueries(queryTypeList, parametersList, this);
		model.setDetachAllCommand(new UICommand("DetachClusters", this));
		//cannot detach engine networks from clusters
		if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
		{
			for (SelectionTreeNodeModel nodeModel : getSelectionNodeList())
			{
				nodeModel.setIsChangable(false);
			}
			model.getDetachAllCommand().setIsAvailable(false);
			model.getName().setIsChangable(false);
			model.setMessage("Cannot detach Management Network from Clusters");
		}
	}

	public void New()
	{
		if (getWindow() != null)
		{
			return;
		}

		DataCenterNetworkModel model = new DataCenterNetworkModel();
		setWindow(model);
		model.setTitle("New Logical Network");
		model.setHashName("new_logical_network");

		model.setIsNew(true);
		model.setClusters(DataProvider.GetClusterList(getEntity().getId()));


		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		model.setDetachAllCommand(new UICommand("DetachClusters", this));
		model.getDetachAllAvailable().setEntity(false);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnSave()
	{
		DataCenterNetworkModel model = (DataCenterNetworkModel)getWindow();

		if (getEntity() == null || (!model.getIsNew() && getSelectedItem() == null))
		{
			Cancel();
			return;
		}

		model.setcurrentNetwork(model.getIsNew() ? new network() : (network)Cloner.clone(getSelectedItem()));

		if (!model.Validate())
		{
			return;
		}

		//Save changes.
		model.getcurrentNetwork().setstorage_pool_id(getEntity().getId());
		model.getcurrentNetwork().setname((String)model.getName().getEntity());
		model.getcurrentNetwork().setstp(model.getIsStpEnabled());
		model.getcurrentNetwork().setdescription((String)model.getDescription().getEntity());
		model.getcurrentNetwork().setvlan_id(null);

		if (model.getHasVLanTag())
		{
			model.getcurrentNetwork().setvlan_id(Integer.parseInt(model.getVLanTag().getEntity().toString()));
		}

		model.setnewClusters(new java.util.ArrayList<VDSGroup>());

		for (SelectionTreeNodeModel selectionTreeNodeModel : model.getClusterTreeNodes())
		{
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
			if (selectionTreeNodeModel.getIsSelectedNullable() != null && selectionTreeNodeModel.getIsSelectedNullable().equals(true))
			{
				model.getnewClusters().add((VDSGroup)selectionTreeNodeModel.getEntity());
			}
		}
		java.util.ArrayList<VDSGroup> detachNetworkFromClusters = Linq.Except(model.getOriginalClusters(), model.getnewClusters());
		java.util.ArrayList<VdcActionParametersBase> actionParameters = new java.util.ArrayList<VdcActionParametersBase>();

		for (VDSGroup detachNetworkFromCluster : detachNetworkFromClusters)
		{
			actionParameters.add((VdcActionParametersBase) new AttachNetworkToVdsGroupParameter(detachNetworkFromCluster, model.getcurrentNetwork()));
		}

		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			DataCenterNetworkModel networkModel = (DataCenterNetworkModel)result.getState();
			network network = networkModel.getcurrentNetwork();
			VdcReturnValueBase returnValue;
			if (networkModel.getIsNew())
			{
				returnValue = Frontend.RunAction(VdcActionType.AddNetwork, new AddNetworkStoragePoolParameters(getEntity().getId(), network));
			}
			else
			{
				if ((Boolean)networkModel.getIsEnabled().getEntity())
				{
					returnValue = Frontend.RunAction(VdcActionType.UpdateNetwork, new AddNetworkStoragePoolParameters(getEntity().getId(), network));
				}
				else
				{
					VdcReturnValueBase tempVar = new VdcReturnValueBase();
					tempVar.setSucceeded(true);
					returnValue = tempVar;
				}
			}
			if (returnValue != null && returnValue.getSucceeded())
			{
				Guid networkId = networkModel.getIsNew() ? (Guid)returnValue.getActionReturnValue() : network.getId();
				java.util.ArrayList<VDSGroup> attachNetworkToClusters = Linq.Except(networkModel.getnewClusters(), networkModel.getOriginalClusters());
				java.util.ArrayList<VdcActionParametersBase> actionParameters1 = new java.util.ArrayList<VdcActionParametersBase>();
				for (VDSGroup attachNetworkToCluster : attachNetworkToClusters)
				{
					network tempVar2 = new network();
					tempVar2.setId(networkId);
					tempVar2.setname(network.getname());
					actionParameters1.add((VdcActionParametersBase) new AttachNetworkToVdsGroupParameter(attachNetworkToCluster, tempVar2));
				}
				Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, actionParameters1);
			}
			if (returnValue != null && returnValue.getSucceeded())
			{
				Cancel();
			}
			networkModel.StopProgress();

			}
		}, model);
	}

	public void DetachClusters()
	{
		ConfirmationModel confirmModel = new ConfirmationModel();
		setConfirmWindow(confirmModel);
		confirmModel.setTitle("Detach Network from ALL Clusters");
		confirmModel.setHashName("detach_network_from_all_clusters");
		confirmModel.setMessage("You are about to detach the Network from all of the Clusters to which it is currentlyattached.\nAs a result, the Clusters' Hosts might become unreachable.\n\nAre you sure you want to continue?");
		confirmModel.getLatch().setIsAvailable(true);
		UICommand tempVar = new UICommand("OnDetachClusters", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		confirmModel.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("CancelConfirmation", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		confirmModel.getCommands().add(tempVar2);
	}

	public void CancelConfirmation()
	{
		setConfirmWindow(null);
	}

	public void OnDetachClusters()
	{
		ConfirmationModel confirmationModel = (ConfirmationModel)getConfirmWindow();

		if (!confirmationModel.Validate())
		{
			return;
		}
		DataCenterNetworkModel model = (DataCenterNetworkModel)getWindow();
		network network = (network)getSelectedItem();

		java.util.ArrayList<VdcActionParametersBase> actionParameters = new java.util.ArrayList<VdcActionParametersBase>();

		for (SelectionTreeNodeModel selectionTreeNodeModel : model.getClusterTreeNodes())
		{
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
			if (selectionTreeNodeModel.getIsSelectedNullable() != null && selectionTreeNodeModel.getIsSelectedNullable().equals(true))
			{
				selectionTreeNodeModel.setIsSelectedNullable(false);
				actionParameters.add((VdcActionParametersBase)new AttachNetworkToVdsGroupParameter((VDSGroup)selectionTreeNodeModel.getEntity(), network));
			}
		}

		java.util.ArrayList<VdcReturnValueBase> returnValueList = Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters);
		boolean isSucceded = true;
		for (VdcReturnValueBase vdcReturnValueBase : returnValueList)
		{
			isSucceded &= vdcReturnValueBase.getSucceeded();
		}

		CancelConfirmation();

		if (isSucceded)
		{
			model.setOriginalClusters(new java.util.ArrayList<VDSGroup>());
			model.getIsEnabled().setEntity(true);
			model.getDetachAllAvailable().setEntity(!(Boolean)model.getIsEnabled().getEntity());
		}
		else
		{
			Cancel();
		}
	}

	public void Cancel()
	{
		setWindow(null);
	}

	@Override
	protected void OnSelectedItemChanged()
	{
		super.OnSelectedItemChanged();
		UpdateActionAvailability();
	}

	@Override
	protected void SelectedItemsChanged()
	{
		super.SelectedItemsChanged();
		UpdateActionAvailability();
	}

	private void UpdateActionAvailability()
	{
		java.util.List tempVar = getSelectedItems();
		java.util.ArrayList selectedItems = (java.util.ArrayList)((tempVar != null) ? tempVar : new java.util.ArrayList());

		boolean anyEngine = false;
		for (Object item : selectedItems)
		{
			network network = (network)item;
			if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
			{
				anyEngine = true;
				break;
			}
		}

		getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
		getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0 && !anyEngine);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getNewCommand())
		{
			New();
		}
		else if (command == getEditCommand())
		{
			Edit();
		}
		else if (command == getRemoveCommand())
		{
			remove();
		}

		else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
		else if(StringHelper.stringsEqual(command.getName(), "DetachClusters"))
		{
			DetachClusters();
		}
		else if(StringHelper.stringsEqual(command.getName(), "OnDetachClusters"))
		{
			OnDetachClusters();
		}
		else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation"))
		{
			CancelConfirmation();
		}
	}


	public void Executed(FrontendMultipleQueryAsyncResult result)
	{
		network network = (network)getSelectedItem();
		java.util.List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
		DataCenterNetworkModel model = (DataCenterNetworkModel)getWindow();
		java.util.ArrayList<network> clusterNetworkList = null;
		boolean networkHasAttachedClusters = false;
		for (int i = 0; i < returnValueList.size(); i++)
		{
			VdcQueryReturnValue returnValue = returnValueList.get(i);
			if (returnValue.getSucceeded() && returnValue.getReturnValue() != null)
			{
				clusterNetworkList = (java.util.ArrayList<network>)returnValue.getReturnValue();
				for (network clusterNetwork : clusterNetworkList)
				{
					if (clusterNetwork.getId().equals(network.getId()))
					{
						model.getOriginalClusters().add((VDSGroup)getSelectionNodeList().get(i).getEntity());
						getSelectionNodeList().get(i).setIsSelectedNullable(true);
						networkHasAttachedClusters = true;
						break;
					}
				}
			}
		}
		if (networkHasAttachedClusters)
		{
			model.getIsEnabled().setEntity(false);
			if (!StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
			{
				model.getDetachAllAvailable().setEntity(!(Boolean)model.getIsEnabled().getEntity());
			}
		}

		model.setClusterTreeNodes(getSelectionNodeList());
		if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK) && getSelectionNodeList().size() > 0)
		{
			UICommand tempVar = new UICommand("Cancel", this);
			tempVar.setTitle("Close");
			tempVar.setIsDefault(true);
			tempVar.setIsCancel(true);
			model.getCommands().add(tempVar);
		}
		else
		{
			UICommand tempVar2 = new UICommand("OnSave", this);
			tempVar2.setTitle("OK");
			tempVar2.setIsDefault(true);
			model.getCommands().add(tempVar2);
			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Cancel");
			tempVar3.setIsCancel(true);
			model.getCommands().add(tempVar3);
		}
	}

}
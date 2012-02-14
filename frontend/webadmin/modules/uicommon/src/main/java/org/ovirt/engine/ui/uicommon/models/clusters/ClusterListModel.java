package org.ovirt.engine.ui.uicommon.models.clusters;
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

import org.ovirt.engine.ui.uicommon.models.configure.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ClusterListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{

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
	private UICommand privateGuideCommand;
	public UICommand getGuideCommand()
	{
		return privateGuideCommand;
	}
	private void setGuideCommand(UICommand value)
	{
		privateGuideCommand = value;
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

		//			get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<VDSGroup>().Select(a => a.ID).Cast<object>().ToArray(); }
	protected Object[] getSelectedKeys()
	{
		if (getSelectedItems() == null)
		{
			return new Object[0];
		}
		else
		{
			java.util.ArrayList<Object> items = new java.util.ArrayList<Object>();
			for (Object i : getSelectedItems())
			{
				items.add(((VDSGroup)i).getId());
			}
			return items.toArray(new Object[]{});
		}
	}

	private Object privateGuideContext;
	public Object getGuideContext()
	{
		return privateGuideContext;
	}
	public void setGuideContext(Object value)
	{
		privateGuideContext = value;
	}



	public ClusterListModel()
	{
		setTitle("Clusters");

		setDefaultSearchString("Cluster:");
		setSearchString(getDefaultSearchString());

		setNewCommand(new UICommand("New", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));
		setGuideCommand(new UICommand("Guide", this));

		UpdateActionAvailability();

		getSearchNextPageCommand().setIsAvailable(true);
		getSearchPreviousPageCommand().setIsAvailable(true);
	}

	public void Guide()
	{
		ClusterGuideModel model = new ClusterGuideModel();
		setWindow(model);
		model.setTitle("New Cluster - Guide Me");
		model.setHashName("new_cluster_-_guide_me");

		model.setEntity(getGuideContext() != null ? DataProvider.GetClusterById(getGuideContext() instanceof Guid ? (Guid)getGuideContext() : (Guid)getGuideContext()) : null);

		UICommand tempVar = new UICommand("Cancel", this);
		tempVar.setTitle("Configure Later");
		tempVar.setIsDefault(true);
		tempVar.setIsCancel(true);
		model.getCommands().add(tempVar);
	}

	@Override
	protected void InitDetailModels()
	{
		super.InitDetailModels();

		ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
		list.add(new ClusterPolicyModel());
		list.add(new ClusterHostListModel());
		list.add(new ClusterVmListModel());
		list.add(new ClusterNetworkListModel());
		list.add(new PermissionListModel());
		setDetailModels(list);
	}

	@Override
	public boolean IsSearchStringMatch(String searchString)
	{
		return searchString.trim().toLowerCase().startsWith("cluster");
	}

	@Override
	protected void SyncSearch()
	{
		SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Cluster);
		tempVar.setMaxCount(getSearchPageSize());
		super.SyncSearch(VdcQueryType.Search, tempVar);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.Cluster, getSearchPageSize()));
		setItems(getAsyncResult().getData());
	}

	public void New()
	{
		if (getWindow() != null)
		{
			return;
		}

		ClusterModel model = new ClusterModel();
		setWindow(model);
		model.setTitle("New Cluster");
		model.setHashName("new_cluster");
		model.setIsNew(true);


		java.util.ArrayList<storage_pool> dataCenters = DataProvider.GetDataCenterList();
		model.getDataCenter().setItems(dataCenters);

		//Be aware of system tree selection.
		//Strict data center as neccessary.
		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
		{
			storage_pool selectDataCenter = (storage_pool)getSystemTreeSelectedItem().getEntity();

			model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters, new Linq.DataCenterPredicate(selectDataCenter.getId())));
			model.getDataCenter().setIsChangable(false);
		}
		else
		{
			model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
		}


		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void Edit()
	{
		VDSGroup cluster = (VDSGroup)getSelectedItem();

		if (getWindow() != null)
		{
			return;
		}

		ClusterModel model = new ClusterModel();
		setWindow(model);
		model.setTitle("Edit Cluster");
		model.setHashName("edit_cluster");
		model.setOriginalName(cluster.getname());
		model.getName().setEntity(cluster.getname());

		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster)
		{
			model.getName().setIsChangable(false);
			model.getName().setInfo("Cannot edit Cluster's Name in tree context");
			;
		}

		model.getDescription().setEntity(cluster.getdescription());
		model.setMemoryOverCommit(cluster.getmax_vds_memory_over_commit());


		java.util.ArrayList<storage_pool> storagePools = DataProvider.GetDataCenterList();
		model.getDataCenter().setItems(storagePools);

		model.getDataCenter().setSelectedItem(null);
		for (storage_pool a : storagePools)
		{
			if (cluster.getstorage_pool_id() != null && a.getId().equals(cluster.getstorage_pool_id()))
			{
				model.getDataCenter().setSelectedItem(a);
				break;
			}
		}
		model.getDataCenter().setIsChangable(model.getDataCenter().getSelectedItem() == null);

		// When editing, the possible version values should be retrieved by the cluster.
		model.getVersion().setItems(DataProvider.GetClusterVersions(cluster.getId()));
		model.getVersion().setSelectedItem((Version)cluster.getcompatibility_version());

		model.setMigrateOnErrorOption(cluster.getMigrateOnError());

		model.getCPU().setSelectedItem(null);
		for (ServerCpu a : (java.util.ArrayList<ServerCpu>)model.getCPU().getItems())
		{
			if (StringHelper.stringsEqual(a.getCpuName(), cluster.getcpu_name()))
			{
				model.getCPU().setSelectedItem(a);
				break;
			}
		}

		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void remove()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Cluster(s)");
		model.setHashName("remove_cluster");
		model.setMessage("Cluster(s)");

		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		for (VDSGroup a : Linq.<VDSGroup>Cast(getSelectedItems()))
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
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object a : getSelectedItems())
		{
			prms.add(new VdsGroupParametersBase(((VDSGroup)a).getId()));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.RemoveVdsGroup, prms,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	public void OnSave()
	{
		ClusterModel model = (ClusterModel)getWindow();
		if (!model.Validate())
		{
			return;
		}

		if (!model.getIsNew() && (Version)model.getVersion().getSelectedItem() != ((VDSGroup)getSelectedItem()).getcompatibility_version())
		{
			ConfirmationModel confirmModel = new ConfirmationModel();
			setConfirmWindow(confirmModel);
			confirmModel.setTitle("Change Cluster Compatibility Version");
			confirmModel.setHashName("change_cluster_compatibility_version");
			confirmModel.setMessage("You are about to change the Cluster Compatibility Version. Are you sure you want to continue?");

			UICommand tempVar = new UICommand("OnSaveInternal", this);
			tempVar.setTitle("OK");
			tempVar.setIsDefault(true);
			getConfirmWindow().getCommands().add(tempVar);
			UICommand tempVar2 = new UICommand("CancelConfirmation", this);
			tempVar2.setTitle("Cancel");
			tempVar2.setIsCancel(true);
			getConfirmWindow().getCommands().add(tempVar2);
		}
		else
		{
			OnSaveInternal();
		}
	}

	public void OnSaveInternal()
	{
		ClusterModel model = (ClusterModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		//cancel confirm window if there is
		CancelConfirmation();

		VDSGroup cluster = model.getIsNew() ? new VDSGroup() : (VDSGroup)Cloner.clone(getSelectedItem());

		Version version = (Version)model.getVersion().getSelectedItem();

		cluster.setname((String)model.getName().getEntity());
		cluster.setdescription((String)model.getDescription().getEntity());
		cluster.setstorage_pool_id(((storage_pool)model.getDataCenter().getSelectedItem()).getId());
		cluster.setcpu_name(((ServerCpu)model.getCPU().getSelectedItem()).getCpuName());
		cluster.setmax_vds_memory_over_commit(model.getMemoryOverCommit());
		cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0);
		cluster.setcompatibility_version(version);
		cluster.setMigrateOnError(model.getMigrateOnErrorOption());


		model.StartProgress(null);

		Frontend.RunAction(model.getIsNew() ? VdcActionType.AddVdsGroup : VdcActionType.UpdateVdsGroup, new VdsGroupOperationParameters(cluster),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			ClusterListModel localModel = (ClusterListModel)result.getState();
			localModel.PostOnSaveInternal(result.getReturnValue());

			}
		}, this);
	}

	public void PostOnSaveInternal(VdcReturnValueBase returnValue)
	{
		ClusterModel model = (ClusterModel)getWindow();

		model.StopProgress();

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();

			if (model.getIsNew())
			{
				setGuideContext(returnValue.getActionReturnValue());
				UpdateActionAvailability();
				getGuideCommand().Execute();
			}
		}
	}

	public void Cancel()
	{
		CancelConfirmation();

		setGuideContext(null);
		setWindow(null);

		UpdateActionAvailability();
	}

	public void CancelConfirmation()
	{
		setConfirmWindow(null);
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

	@Override
	protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
	{
		super.ItemsCollectionChanged(sender, e);

		//Try to select an item corresponding to the system tree selection.
		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster)
		{
			VDSGroup cluster = (VDSGroup)getSystemTreeSelectedItem().getEntity();

			setSelectedItem(Linq.FirstOrDefault(Linq.<VDSGroup>Cast(getItems()), new Linq.ClusterPredicate(cluster.getId())));
		}
	}

	private void UpdateActionAvailability()
	{
		getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1);

		getGuideCommand().setIsExecutionAllowed(getGuideContext() != null || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));

		getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);


		//System tree dependent actions.
		boolean isAvailable = !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster);

		getNewCommand().setIsAvailable(isAvailable);
		getRemoveCommand().setIsAvailable(isAvailable);
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
		else if (command == getGuideCommand())
		{
			Guide();
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
		else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternal"))
		{
			OnSaveInternal();
		}
		else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation"))
		{
			CancelConfirmation();
		}
	}


	private SystemTreeItemModel systemTreeSelectedItem;
	public SystemTreeItemModel getSystemTreeSelectedItem()
	{
		return systemTreeSelectedItem;
	}
	public void setSystemTreeSelectedItem(SystemTreeItemModel value)
	{
		if (systemTreeSelectedItem != value)
		{
			systemTreeSelectedItem = value;
			OnSystemTreeSelectedItemChanged();
		}
	}

	private void OnSystemTreeSelectedItemChanged()
	{
		UpdateActionAvailability();
	}
}
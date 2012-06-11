package org.ovirt.engine.ui.uicommon.models.hosts;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.AttachVdsToTagParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.MaintananceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommon.Cloner;
import org.ovirt.engine.ui.uicommon.DataProvider;
import org.ovirt.engine.ui.uicommon.Linq;
import org.ovirt.engine.ui.uicommon.TagsEqualityComparer;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommon.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommon.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommon.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommon.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommon.models.tags.TagModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ITaskTarget;
import org.ovirt.engine.ui.uicompat.Task;
import org.ovirt.engine.ui.uicompat.TaskContext;

@SuppressWarnings("unused")
public class HostListModel extends ListWithDetailsModel implements ISupportSystemTreeContext//, ITaskTarget
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
	private UICommand privateActivateCommand;
	public UICommand getActivateCommand()
	{
		return privateActivateCommand;
	}
	private void setActivateCommand(UICommand value)
	{
		privateActivateCommand = value;
	}
	private UICommand privateMaintenanceCommand;
	public UICommand getMaintenanceCommand()
	{
		return privateMaintenanceCommand;
	}
	private void setMaintenanceCommand(UICommand value)
	{
		privateMaintenanceCommand = value;
	}
	private UICommand privateApproveCommand;
	public UICommand getApproveCommand()
	{
		return privateApproveCommand;
	}
	private void setApproveCommand(UICommand value)
	{
		privateApproveCommand = value;
	}
	private UICommand privateRestartCommand;
	public UICommand getRestartCommand()
	{
		return privateRestartCommand;
	}
	private void setRestartCommand(UICommand value)
	{
		privateRestartCommand = value;
	}
	private UICommand privateStartCommand;
	public UICommand getStartCommand()
	{
		return privateStartCommand;
	}
	private void setStartCommand(UICommand value)
	{
		privateStartCommand = value;
	}
	private UICommand privateStopCommand;
	public UICommand getStopCommand()
	{
		return privateStopCommand;
	}
	private void setStopCommand(UICommand value)
	{
		privateStopCommand = value;
	}
	private UICommand privateManualFenceCommand;
	public UICommand getManualFenceCommand()
	{
		return privateManualFenceCommand;
	}
	private void setManualFenceCommand(UICommand value)
	{
		privateManualFenceCommand = value;
	}
	private UICommand privateAssignTagsCommand;
	public UICommand getAssignTagsCommand()
	{
		return privateAssignTagsCommand;
	}
	private void setAssignTagsCommand(UICommand value)
	{
		privateAssignTagsCommand = value;
	}
	private UICommand privateConfigureLocalStorageCommand;
	public UICommand getConfigureLocalStorageCommand()
	{
		return privateConfigureLocalStorageCommand;
	}
	private void setConfigureLocalStorageCommand(UICommand value)
	{
		privateConfigureLocalStorageCommand = value;
	}



	private HostEventListModel privateHostEventListModel;
	private HostEventListModel getHostEventListModel()
	{
		return privateHostEventListModel;
	}
	private void setHostEventListModel(HostEventListModel value)
	{
		privateHostEventListModel = value;
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

	private boolean isPowerManagementEnabled;
	public boolean getIsPowerManagementEnabled()
	{
		return isPowerManagementEnabled;
	}
	public void setIsPowerManagementEnabled(boolean value)
	{
		if (isPowerManagementEnabled != value)
		{
			isPowerManagementEnabled = value;
			OnPropertyChanged(new PropertyChangedEventArgs("isPowerManagementEnabled"));
		}
	}

	protected Object[] getSelectedKeys()
	{
		if (getSelectedItems() == null)
		{
			return new Object[0];
		}
		else
		{
			Object[] keys = new Object[getSelectedItems().size()];
			for (int i = 0; i < getSelectedItems().size(); i++)
			{
				keys[i] = ((VDS)getSelectedItems().get(i)).getId();
			}
			return keys;
		}
	}


	public HostListModel()
	{
		setTitle("Hosts");

		setDefaultSearchString("Host:");
		setSearchString(getDefaultSearchString());

		setNewCommand(new UICommand("New", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));
		setActivateCommand(new UICommand("Activate", this));
		setMaintenanceCommand(new UICommand("Maintenance", this));
		setApproveCommand(new UICommand("Approve", this));
		setRestartCommand(new UICommand("Restart", this));
		setStartCommand(new UICommand("Start", this));
		setStopCommand(new UICommand("Stop", this));
		setManualFenceCommand(new UICommand("ManualFence", this));
		setAssignTagsCommand(new UICommand("AssignTags", this));
		setConfigureLocalStorageCommand(new UICommand("ConfigureLocalStorage", this));

		UpdateActionAvailability();

		getSearchNextPageCommand().setIsAvailable(true);
		getSearchPreviousPageCommand().setIsAvailable(true);
	}

	public void AssignTags()
	{
		if (getWindow() != null)
		{
			return;
		}

		TagListModel model = new TagListModel();
		setWindow(model);
		model.setTitle("Assign Tags");
		model.setHashName("assign_tags_hosts");

		model.setAttachedTagsToEntities(GetAttachedTagsToSelectedHosts());
		java.util.ArrayList<TagModel> tags = (java.util.ArrayList<TagModel>)model.getItems();

		UICommand tempVar = new UICommand("OnAssignTags", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private java.util.Map<Guid, Boolean> GetAttachedTagsToSelectedHosts()
	{
		java.util.HashMap<Guid, Boolean> tags = new java.util.HashMap<Guid, Boolean>();

		//          var hostIds = SelectedItems
		//              .Cast<VDS>()
		//              .Select(a => a.vds_id)
		//              .ToList();

		//          var allAttachedTags = hostIds.SelectMany(a => DataProvider.GetAttachedTagsToHost(a)).ToList();

		//          var attachedTags = allAttachedTags
		//              .Distinct(new TagsEqualityComparer())
		//              .ToList();

		//          attachedTags.Each(a => { tags.Add(a.tag_id, allAttachedTags.Count(b => b.tag_id == a.tag_id) == hostIds.Count() ? true : false ); });

		java.util.ArrayList<Guid> hostIds = new java.util.ArrayList<Guid>();

		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			hostIds.add(vds.getId());
		}

		java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags> allAttachedTags = new java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags>();

		for (Guid hostId : hostIds)
		{
			allAttachedTags.addAll(DataProvider.GetAttachedTagsToHost(hostId));
		}

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
		java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags> attachedTags = Linq.Distinct(allAttachedTags, new TagsEqualityComparer());

		for (org.ovirt.engine.core.common.businessentities.tags tag : attachedTags)
		{
			int count = 0;
			for (org.ovirt.engine.core.common.businessentities.tags tag2 : allAttachedTags)
			{
				if (tag2.gettag_id().equals(tag.gettag_id()))
				{
					count++;
				}
			}
			tags.put(tag.gettag_id(), count == hostIds.size() ? true : false);
		}

		return tags;
	}

	public void OnAssignTags()
	{
		TagListModel model = (TagListModel)getWindow();

		//          var hostIds = SelectedItems
		//              .Cast<VDS>()
		//              .Select(a => a.vds_id)
		//              .ToList();
		java.util.ArrayList<Guid> hostIds = new java.util.ArrayList<Guid>();

		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			hostIds.add(vds.getId());
		}

		//          var attachedTags = GetAttachedTagsToSelectedHosts();
		java.util.Map<Guid, Boolean> attachedTags = GetAttachedTagsToSelectedHosts();

		//prepare attach/detach lists
		java.util.ArrayList<Guid> tagsToAttach = new java.util.ArrayList<Guid>();
		java.util.ArrayList<Guid> tagsToDetach = new java.util.ArrayList<Guid>();

		//          model.Items
		//              .Cast<TagModel>()
		//              .First()
		//              .EachRecursive(a => a.Children, (a, b) => {
		//                  if(a.Selection == true && (!attachedTags.ContainsKey(a.Id) || attachedTags[a.Id] == false))
		//                  {
		//                      tagsToAttach.Add(a.Id);
		//                  }
		//                  else if (a.Selection == false && attachedTags.ContainsKey(a.Id))
		//                  {
		//                      tagsToDetach.Add(a.Id);
		//                  }
		//              });
		if (model.getItems() != null && ((java.util.ArrayList<TagModel>)model.getItems()).size() > 0)
		{
			java.util.ArrayList<TagModel> tags = (java.util.ArrayList<TagModel>)model.getItems();
			TagModel rootTag = tags.get(0);
			TagModel.RecursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
		}
		//Attach tags.

		//          Frontend.RunMultipleActions(VdcActionType.AttachVdsToTag,
		//              tagsToAttach.Select(a =>
		//                  (VdcActionParametersBase)new AttachVdsToTagParameters(a, hostIds)
		//              )
		//              .ToList()
		//          );

		java.util.ArrayList<VdcActionParametersBase> prmsToAttach = new java.util.ArrayList<VdcActionParametersBase>();
		for (Guid tag_id : tagsToAttach)
		{
			prmsToAttach.add(new AttachVdsToTagParameters(tag_id, hostIds));
		}
		Frontend.RunMultipleAction(VdcActionType.AttachVdsToTag, prmsToAttach);

		//          Frontend.RunMultipleActions(VdcActionType.DetachVdsFromTag,
		//              tagsToDetach.Select(a =>
		//                  (VdcActionParametersBase)new AttachVdsToTagParameters(a, hostIds)
		//              )
		//              .ToList()
		//);
		java.util.ArrayList<VdcActionParametersBase> prmsToDetach = new java.util.ArrayList<VdcActionParametersBase>();
		for (Guid tag_id : tagsToDetach)
		{
			prmsToDetach.add(new AttachVdsToTagParameters(tag_id, hostIds));
		}
		Frontend.RunMultipleAction(VdcActionType.DetachVdsFromTag, prmsToDetach);


		Cancel();
	}

	public void ManualFence()
	{
		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Are you sure?");
		model.setHashName("manual_fence_are_you_sure");
		java.util.ArrayList<VDS> items = new java.util.ArrayList<VDS>();
		items.add((VDS)getSelectedItem());
		model.setItems(items);

		model.getLatch().setIsAvailable(true);
		model.getLatch().setIsChangable(true);


		UICommand tempVar = new UICommand("OnManualFence", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnManualFence()
	{
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			FenceVdsManualyParameters parameters = new FenceVdsManualyParameters(true);
			parameters.setStoragePoolId(vds.getstorage_pool_id());
			parameters.setVdsId(vds.getId());
			list.add(parameters);
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.FenceVdsManualy, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	public void New()
	{
		if (getWindow() != null)
		{
			return;
		}

		HostModel model = new HostModel();
		setWindow(model);
		model.setTitle("New Host");
		model.setHashName("new_host");
		model.getPort().setEntity(54321);
		model.getPmType().setSelectedItem(null);
		model.getOverrideIpTables().setEntity(true);

		java.util.ArrayList<storage_pool> dataCenter = DataProvider.GetDataCenterList();
		model.getDataCenter().setItems(dataCenter);
		model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenter));

		if (getSystemTreeSelectedItem() != null)
		{
			switch (getSystemTreeSelectedItem().getType())
			{
				case Host:
					model.getName().setIsChangable(false);
					model.getName().setInfo("Cannot edit Host's Name in this tree context");
					break;
				case Hosts:
				case Cluster:
					VDSGroup cluster = (VDSGroup)getSystemTreeSelectedItem().getEntity();
					for (storage_pool dc : (java.util.ArrayList<storage_pool>)model.getDataCenter().getItems())
					{
						if (dc.getId().equals(cluster.getstorage_pool_id()))
						{
							model.getDataCenter().setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { dc })));
							model.getDataCenter().setSelectedItem(dc);
							break;
						}
					}
					model.getDataCenter().setIsChangable(false);
					model.getDataCenter().setInfo("Cannot choose Host's Data Center in tree context");
					model.getCluster().setIsChangable(false);
					model.getCluster().setInfo("Cannot choose Host's Cluster in tree context");
					break;
				case DataCenter:
					storage_pool selectDataCenter = (storage_pool)getSystemTreeSelectedItem().getEntity();
					model.getDataCenter().setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { selectDataCenter })));
					model.getDataCenter().setSelectedItem(selectDataCenter);
					model.getDataCenter().setIsChangable(false);
					model.getDataCenter().setInfo("Cannot choose Host's Data Center in tree context");
					break;
				default:
					break;
			}
		}

		UICommand tempVar = new UICommand("OnSaveFalse", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void GoToEventsTab()
	{
		setActiveDetailModel(getHostEventListModel());
	}

	public void EditWithPMemphasis()
	{
		getEditCommand().Execute();

		HostModel model = (HostModel)getWindow();
		model.setIsPowerManagementSelected(true);
		model.getIsPm().setEntity(true);
		model.getIsPm().setIsChangable(false);
	}

    public void Edit()
    {
        if (getWindow() != null)
        {
            return;
        }

        VDS host = (VDS) getSelectedItem();
        HostModel model = PrepareModelForApproveEdit(host);
        setWindow(model);
        model.setTitle("Edit Host");
        model.setHashName("edit_host");

        UICommand tempVar = new UICommand("OnSaveFalse", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

	public void OnSaveFalse()
	{
		OnSave(false);
	}

	public void OnSave(boolean approveInitiated)
	{
		HostModel model = (HostModel)getWindow();

		if (!model.Validate())
		{
			return;
		}

		if (!((Boolean)model.getIsPm().getEntity()))
		{
			ConfirmationModel confirmModel = new ConfirmationModel();
			setConfirmWindow(confirmModel);
			confirmModel.setTitle("Power Management Configuration");
			confirmModel.setHashName("power_management_configuration");
			confirmModel.setMessage("You haven't configured Power Management for this Host. Are you sure you want to continue?");

			UICommand tempVar = new UICommand(approveInitiated ? "OnSaveInternalFromApprove" : "OnSaveInternalNotFromApprove", this);
			tempVar.setTitle("OK");
			tempVar.setIsDefault(true);
			confirmModel.getCommands().add(tempVar);
			UICommand tempVar2 = new UICommand("CancelConfirmFocusPM", this);
			tempVar2.setTitle("Cancel");
			tempVar2.setIsCancel(true);
			confirmModel.getCommands().add(tempVar2);
		}
		else
		{
			OnSaveInternal(approveInitiated);
		}

	}

	public void CancelConfirmFocusPM()
	{
		HostModel hostModel = (HostModel) getWindow();
		hostModel.setIsPowerManagementSelected(true);
		hostModel.getIsPm().setEntity(true);

		setConfirmWindow(null);
	}

	public void OnSaveInternalNotFromApprove()
	{
		OnSaveInternal(false);
	}

	public void OnSaveInternalFromApprove()
	{
		OnSaveInternal(true);
	}

	public void OnSaveInternal(boolean approveInitiated)
	{
		HostModel model = (HostModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		VDS host = model.getIsNew() ? new VDS() : (VDS)Cloner.clone(getSelectedItem());

		//Save changes.
		host.setvds_name((String)model.getName().getEntity());
		host.sethost_name((String)model.getHost().getEntity());
		host.setManagmentIp((String)model.getManagementIp().getEntity());
		host.setport(Integer.parseInt(model.getPort().getEntity().toString()));

		Guid oldClusterId = host.getvds_group_id();
		Guid newClusterId = ((VDSGroup)model.getCluster().getSelectedItem()).getId();
		host.setvds_group_id(newClusterId);
		host.setpm_enabled((Boolean)model.getIsPm().getEntity());
		host.setpm_user((String)model.getPmUserName().getEntity());
		host.setpm_password((String)model.getPmPassword().getEntity());
		host.setpm_type((String)model.getPmType().getSelectedItem());
		host.setPmOptionsMap(new ValueObjectMap(model.getPmOptionsMap(), false));


		CancelConfirm();
		model.StartProgress(null);

		if (model.getIsNew())
		{
			AddVdsActionParameters parameters = new AddVdsActionParameters();
			parameters.setVdsId(host.getId());
			parameters.setvds(host);
			parameters.setRootPassword((String)model.getRootPassword().getEntity());
			parameters.setOverrideFirewall((Boolean)model.getOverrideIpTables().getEntity());

			Frontend.RunAction(VdcActionType.AddVds, parameters,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

				Object[] array = (Object[])result.getState();
				HostListModel localModel = (HostListModel)array[0];
				boolean localApproveInitiated = (Boolean)array[1];
				localModel.PostOnSaveInternal(result.getReturnValue(), localApproveInitiated);

			}
		}, new Object[] { this, approveInitiated });
		}
		else // Update VDS -> consists of changing VDS cluster first and then updating rest of VDS properties:
		{
			UpdateVdsActionParameters parameters = new UpdateVdsActionParameters();
			parameters.setvds(host);
			parameters.setVdsId(host.getId());
			parameters.setRootPassword("");
			parameters.setInstallVds(false);

			if (!oldClusterId.equals(newClusterId))
			{
				Frontend.RunAction(VdcActionType.ChangeVDSCluster, new ChangeVDSClusterParameters(newClusterId, host.getId()),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

					Object[] array = (Object[])result.getState();
					HostListModel localModel = (HostListModel)array[0];
					UpdateVdsActionParameters localParameters = (UpdateVdsActionParameters)array[1];
					boolean localApproveInitiated = (Boolean)array[2];
					VdcReturnValueBase localReturnValue = result.getReturnValue();
					if (localReturnValue != null && localReturnValue.getSucceeded())
					{
						localModel.PostOnSaveInternalChangeCluster(localParameters, localApproveInitiated);
					}
					else
					{
						localModel.getWindow().StopProgress();
					}

			}
		}, new Object[] { this, parameters, approveInitiated });
			}
			else
			{
				PostOnSaveInternalChangeCluster(parameters, approveInitiated);
			}
		}
	}

	public void PostOnSaveInternalChangeCluster(UpdateVdsActionParameters parameters, boolean approveInitiated)
	{
		Frontend.RunAction(VdcActionType.UpdateVds, parameters,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			Object[] array = (Object[])result.getState();
			HostListModel localModel = (HostListModel)array[0];
			boolean localApproveInitiated = (Boolean)array[1];
			localModel.PostOnSaveInternal(result.getReturnValue(), localApproveInitiated);

			}
		}, new Object[] { this, approveInitiated });
	}

	public void PostOnSaveInternal(VdcReturnValueBase returnValue, boolean approveInitiated)
	{
		HostModel model = (HostModel)getWindow();

		model.StopProgress();

		if (returnValue != null && returnValue.getSucceeded())
		{
			if (approveInitiated)
			{
				OnApproveInternal();
			}
			Cancel();
		}
	}

	private void OnApproveInternal()
	{
		VDS vds = (VDS)getSelectedItem();

		Frontend.RunMultipleAction(VdcActionType.ApproveVds, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new ApproveVdsParameters(vds.getId()) })),
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	public void remove()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Host(s)");
		model.setHashName("remove_host");
		model.setMessage("Host(s)");

		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		for (VDS item : Linq.<VDS>Cast(getSelectedItems()))
		{
			list.add(item.getvds_name());
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

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			list.add(new VdsActionParameters(vds.getId()));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.RemoveVds, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	public void Activate()
	{
		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			list.add(new VdsActionParameters(vds.getId()));
		}

		Frontend.RunMultipleAction(VdcActionType.ActivateVds, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	public void Maintenance()
	{
		if (getConfirmWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setConfirmWindow(model);
		model.setTitle("Maintenance Host(s)");
		model.setHashName("maintenance_host");
		model.setMessage("Are you sure you want to place the following host(s) into maintenance mode?");
		//          model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
		java.util.ArrayList<String> vdss = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			vdss.add(vds.getvds_name());
		}
		model.setItems(vdss);


		UICommand tempVar = new UICommand("OnMaintenance", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("CancelConfirm", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnMaintenance()
	{
		ConfirmationModel model = (ConfirmationModel)getConfirmWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		java.util.ArrayList<Guid> vdss = new java.util.ArrayList<Guid>();

		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			vdss.add(vds.getId());
		}
		list.add(new MaintananceNumberOfVdssParameters(vdss, false));


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.MaintananceNumberOfVdss, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			CancelConfirm();

			}
		}, model);
	}

    public void Approve()
    {
        VDS host = (VDS) getSelectedItem();
        HostModel model = PrepareModelForApproveEdit(host);
        setWindow(model);
        model.setTitle("Edit and Approve Host");
        model.setHashName("edit_and_approve_host");

        UICommand tempVar = new UICommand("OnApprove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

	private HostModel PrepareModelForApproveEdit(VDS vds)
    {
        HostModel returnVal = new HostModel();
        returnVal.setHostId(vds.getId());
        returnVal.getRootPassword().setIsAvailable(false);
        returnVal.getOverrideIpTables().setIsAvailable(false);
        returnVal.setOriginalName(vds.getvds_name());
        returnVal.getName().setEntity(vds.getvds_name());
        returnVal.getHost().setEntity(vds.gethost_name());
        returnVal.getPort().setEntity(vds.getport());
        returnVal.getIsPm().setEntity(vds.getpm_enabled());
        returnVal.getManagementIp().setEntity(vds.getManagmentIp());
        returnVal.getPmType().setSelectedItem(vds.getpm_type());
        returnVal.getPmUserName().setEntity(vds.getpm_user());
        returnVal.getPmPassword().setEntity(vds.getpm_password());
        /*
         * --- JUICOMMENT_BEGIN // * TODO: Need to find a solution for casting ValueObjectMap to Dictionary<string,
         * string> // in Java, and conform the C# code to do that when a solution is found
         * returnVal.setPmOptionsMap(vds.getPmOptionsMap()); JUICOMMENT_END ---
         */
        java.util.ArrayList<storage_pool> dataCenters = DataProvider.GetDataCenterList();
        returnVal.getDataCenter().setItems(dataCenters);
        returnVal.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters,
                new Linq.DataCenterPredicate(vds.getstorage_pool_id())));
        if (returnVal.getDataCenter().getSelectedItem() == null)
        {
            Linq.FirstOrDefault(dataCenters);
        }

        java.util.ArrayList<VDSGroup> clusters;
        if (returnVal.getCluster().getItems() == null)
        {
            VDSGroup tempVar = new VDSGroup();
            tempVar.setname(vds.getvds_group_name());
            tempVar.setId(vds.getvds_group_id());
            tempVar.setcompatibility_version(vds.getvds_group_compatibility_version());
            returnVal.getCluster()
                    .setItems(new java.util.ArrayList<VDSGroup>(java.util.Arrays.asList(new VDSGroup[] { tempVar })));
        }
        clusters = (java.util.ArrayList<VDSGroup>) returnVal.getCluster().getItems();
        returnVal.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters,
                new Linq.ClusterPredicate(vds.getvds_group_id())));
        if (returnVal.getCluster().getSelectedItem() == null)
        {
            Linq.FirstOrDefault(clusters);
        }

        if (vds.getstatus() != VDSStatus.Maintenance && vds.getstatus() != VDSStatus.PendingApproval)
        {
            returnVal.getDataCenter().setIsChangable(false);
            returnVal.getDataCenter()
                    .getChangeProhibitionReasons()
                    .add("Data Center can be changed only when the Host is in Maintenance mode.");
            returnVal.getCluster().setIsChangable(false);
            returnVal.getCluster()
                    .getChangeProhibitionReasons()
                    .add("Cluster can be changed only when the Host is in Maintenance mode.");
        }
        else if (getSystemTreeSelectedItem() != null)
        {
            switch (getSystemTreeSelectedItem().getType())
            {
            case Host:
                returnVal.getName().setIsChangable(false);
                returnVal.getName().setInfo("Cannot edit Host's Name in this tree context");
                break;
            case Hosts:
            case Cluster:
                returnVal.getCluster().setIsChangable(false);
                returnVal.getCluster().setInfo("Cannot change Host's Cluster in tree context");
                returnVal.getDataCenter().setIsChangable(false);
                break;
            case DataCenter:
                storage_pool selectDataCenter = (storage_pool) getSystemTreeSelectedItem().getEntity();
                returnVal.getDataCenter()
                        .setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { selectDataCenter })));
                returnVal.getDataCenter().setSelectedItem(selectDataCenter);
                returnVal.getDataCenter().setIsChangable(false);
                break;
            default:
                break;
            }
        }
        return returnVal;
    }

	public void OnApprove()
	{
		OnSave(true);
	}

	public void Restart()
	{
		ConfirmationModel model = new ConfirmationModel();
		setConfirmWindow(model);
		model.setTitle("Restart Host(s)");
		model.setHashName("restart_host");
		model.setMessage("Are you sure you want to Restart the following Host(s)?");
		//          model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			items.add(vds.getvds_name());
		}
		model.setItems(items);


		UICommand tempVar = new UICommand("OnRestart", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnRestart()
	{
		ConfirmationModel model = (ConfirmationModel)getConfirmWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			list.add(new FenceVdsActionParameters(vds.getId(), FenceActionType.Restart));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.RestartVds, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			CancelConfirm();

			}
		}, model);
	}

	public void start()
	{
		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			list.add(new FenceVdsActionParameters(vds.getId(), FenceActionType.Start));
		}

		Frontend.RunMultipleAction(VdcActionType.StartVds, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	public void stop()
	{
		ConfirmationModel model = new ConfirmationModel();
		setConfirmWindow(model);
		model.setTitle("Stop Host(s)");
		model.setHashName("stop_host");
		model.setMessage("Are you sure you want to Stop the following Host(s)?");
		//          model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			items.add(vds.getvds_name());
		}
		model.setItems(items);


		UICommand tempVar = new UICommand("OnStop", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnStop()
	{
		ConfirmationModel model = (ConfirmationModel)getConfirmWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VDS vds = (VDS)item;
			list.add(new FenceVdsActionParameters(vds.getId(), FenceActionType.Stop));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.StopVds, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			CancelConfirm();

			}
		}, model);
	}

	private void ConfigureLocalStorage()
	{
		VDS host = (VDS)getSelectedItem();

		if (getWindow() != null)
		{
			return;
		}

		ConfigureLocalStorageModel model = new ConfigureLocalStorageModel();
		setWindow(model);
		model.setTitle("Configure Local Storage");
		model.setHashName("configure_local_storage");

		if (host.getvds_type() == VDSType.oVirtNode)
		{
			String prefix = DataProvider.GetLocalFSPath();
			if (!StringHelper.isNullOrEmpty(prefix))
			{
				EntityModel pathModel = model.getStorage().getPath();
				pathModel.setEntity(prefix);
				pathModel.setIsChangable(false);
			}
		}

		boolean hostSupportLocalStorage = false;
		Version version3_0 = new Version(3,0);
		if (host.getsupported_cluster_levels() != null)
		{
			String[] array = host.getsupported_cluster_levels().split("[,]", -1);
			for (int i = 0; i < array.length; i++)
			{
				if (version3_0.compareTo(new Version(array[i])) <= 0)
				{
					hostSupportLocalStorage = true;
					break;
				}
			}
		}

		if (hostSupportLocalStorage)
		{
			String modelMessage = null;
			RefObject<String> tempRef_modelMessage = new RefObject<String>(modelMessage);
			model.SetDefaultNames(host, tempRef_modelMessage);
			modelMessage = tempRef_modelMessage.argvalue;
			model.setMessage(modelMessage);

			UICommand tempVar = new UICommand("OnConfigureLocalStorage", this);
			tempVar.setTitle("OK");
			tempVar.setIsDefault(true);
			model.getCommands().add(tempVar);
			UICommand tempVar2 = new UICommand("Cancel", this);
			tempVar2.setTitle("Cancel");
			tempVar2.setIsCancel(true);
			model.getCommands().add(tempVar2);
		}
		else
		{
			model.setMessage("Host doesn't support Local Storage configuration");
			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Close");
			tempVar3.setIsCancel(true);
			tempVar3.setIsDefault(true);
			model.getCommands().add(tempVar3);
		}
	}

	private void OnConfigureLocalStorage()
	{
//		ConfigureLocalStorageModel model = (ConfigureLocalStorageModel)getWindow();
//
//		if (model.getProgress() != null)
//		{
//			return;
//		}
//
//		if (!model.Validate())
//		{
//			return;
//		}
//
//		model.StartProgress("Configuring Local Storage...");
//
//		Task.Create(this, 1).Run();
	}

	@Override
	protected void InitDetailModels()
	{
		super.InitDetailModels();

		HostGeneralModel generalModel = new HostGeneralModel();
		generalModel.getRequestEditEvent().addListener(this);
		generalModel.getRequestGOToEventsTabEvent().addListener(this);
		ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
		list.add(generalModel);
		list.add(new HostVmListModel());
		list.add(new HostInterfaceListModel());
		setHostEventListModel(new HostEventListModel());
		list.add(getHostEventListModel());
		list.add(new HostHooksListModel());
		list.add(new PermissionListModel());
		setDetailModels(list);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(HostGeneralModel.RequestEditEventDefinition))
		{
			EditWithPMemphasis();
		}
		if (ev.equals(HostGeneralModel.RequestGOToEventsTabEventDefinition))
		{
			GoToEventsTab();
		}
	}

	@Override
	public boolean IsSearchStringMatch(String searchString)
	{
		return searchString.trim().toLowerCase().startsWith("host");
	}

	@Override
	protected void SyncSearch()
	{
		SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VDS);
		tempVar.setMaxCount(getSearchPageSize());
		super.SyncSearch(VdcQueryType.Search, tempVar);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.VDS, getSearchPageSize()));
		setItems(getAsyncResult().getData());
	}

	public void Cancel()
	{
		CancelConfirm();
		setWindow(null);
	}

	public void CancelConfirm()
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
		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host)
		{
			VDS host = (VDS)getSystemTreeSelectedItem().getEntity();

			setSelectedItem(Linq.FirstOrDefault(Linq.<VDS>Cast(getItems()), new Linq.HostPredicate(host.getId())));
		}
	}

	@Override
	protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.SelectedItemPropertyChanged(sender, e);

		if (e.PropertyName.equals("status") || e.PropertyName.equals("pm_enabled"))
		{
			UpdateActionAvailability();
		}
	}

	private void UpdateActionAvailability()
	{
		java.util.ArrayList<VDS> items = getSelectedItems() != null ? Linq.<VDS>Cast(getSelectedItems()) : new java.util.ArrayList<VDS>();
		boolean isAllPMEnabled = Linq.FindAllVDSByPmEnabled(items).size() == items.size();


		getEditCommand().setIsExecutionAllowed(items.size() == 1 && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.UpdateVds));

		getRemoveCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.RemoveVds));

		getActivateCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.ActivateVds));

		//or special case where its installation failed but its oVirt node
		boolean approveAvailability = items.size() == 1 && (VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.ApproveVds) || (items.get(0).getstatus() == VDSStatus.InstallFailed && items.get(0).getvds_type() == VDSType.oVirtNode));
		getApproveCommand().setIsExecutionAllowed(approveAvailability);
		getApproveCommand().setIsAvailable(approveAvailability);

		getMaintenanceCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.MaintananceVds));

		getRestartCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.RestartVds) && isAllPMEnabled);

		getStartCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.StartVds) && isAllPMEnabled);


		getStopCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.StopVds) && isAllPMEnabled);

		setIsPowerManagementEnabled(getRestartCommand().getIsExecutionAllowed() || getStartCommand().getIsExecutionAllowed() || getStopCommand().getIsExecutionAllowed());

		getManualFenceCommand().setIsExecutionAllowed(items.size() == 1);

		getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);

		getConfigureLocalStorageCommand().setIsExecutionAllowed(items.size() == 1 && items.get(0).getstatus() == VDSStatus.Maintenance);
		if (!DataProvider.HasAdminSystemPermission() && getConfigureLocalStorageCommand().getIsExecutionAllowed())
		{
			getConfigureLocalStorageCommand().setIsExecutionAllowed(false);
			getConfigureLocalStorageCommand().getExecuteProhibitionReasons().add("Configuring local Storage is permitted only to Administrators with System-level permissions");
		}

		//System tree dependent actions.
		boolean isAvailable = !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host);

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
		else if (command == getActivateCommand())
		{
			Activate();
		}
		else if (command == getMaintenanceCommand())
		{
			Maintenance();
		}
		else if (command == getApproveCommand())
		{
			Approve();
		}
		else if (command == getRestartCommand())
		{
			Restart();
		}
		else if (command == getStartCommand())
		{
			start();
		}
		else if (command == getStopCommand())
		{
			stop();
		}
		else if (command == getManualFenceCommand())
		{
			ManualFence();
		}
		else if (command == getAssignTagsCommand())
		{
			AssignTags();
		}
		else if (command == getConfigureLocalStorageCommand())
		{
			ConfigureLocalStorage();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnAssignTags"))
		{
			OnAssignTags();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnManualFence"))
		{
			OnManualFence();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnSaveFalse"))
		{
			OnSaveFalse();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternalFromApprove"))
		{
			OnSaveInternalFromApprove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternalNotFromApprove"))
		{
			OnSaveInternalNotFromApprove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm"))
		{
			CancelConfirm();
		}
		else if(StringHelper.stringsEqual(command.getName(), "CancelConfirmFocusPM"))
		{
			CancelConfirmFocusPM();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnMaintenance"))
		{
			OnMaintenance();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnApprove"))
		{
			OnApprove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRestart"))
		{
			OnRestart();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnStop"))
		{
			OnStop();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnConfigureLocalStorage"))
		{
			OnConfigureLocalStorage();
		}
	}

	private SystemTreeItemModel systemTreeSelectedItem;
	@Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
	{
		return systemTreeSelectedItem;
	}
	@Override
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

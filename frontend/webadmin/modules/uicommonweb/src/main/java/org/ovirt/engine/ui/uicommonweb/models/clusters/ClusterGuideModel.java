package org.ovirt.engine.ui.uicommonweb.models.clusters;
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

import org.ovirt.engine.ui.uicommonweb.models.hosts.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class ClusterGuideModel extends GuideModel
{

	public final String ClusterConfigureHostsAction = "Configure Host";
	public final String ClusterAddAnotherHostAction = "Add another Host";
	public final String SelectHostsAction = "Select Hosts";


	public VDSGroup getEntity()
	{
		return (VDSGroup)((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
	}
	public void setEntity(VDSGroup value)
	{
		super.setEntity(value);
	}



	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		UpdateOptions();
	}

	private void UpdateOptions()
	{
		getCompulsoryActions().clear();
		getOptionalActions().clear();

		if (getEntity() != null)
		{
			storage_pool dataCenter = null;
			if (getEntity().getstorage_pool_id() != null)
			{
				dataCenter = DataProvider.GetDataCenterById(getEntity().getstorage_pool_id().getValue());
			}
			if (dataCenter == null || dataCenter.getstorage_pool_type() != StorageType.LOCALFS)
			{
				//Add host action.
				UICommand addHostAction = new UICommand("AddHost", this);

				// 				var hosts = DataProvider.GetHostListByCluster(Entity.name)
				// 					.Skip(1)
				// 					.ToList();
				java.util.ArrayList<VDS> hosts = DataProvider.GetHostListByCluster(getEntity().getname());
				if (hosts.size() > 1)
				{
					hosts.remove(0);
				}

				if (hosts.isEmpty())
				{
					addHostAction.setTitle(ClusterConfigureHostsAction);
					getCompulsoryActions().add(addHostAction);
				}
				else
				{
					addHostAction.setTitle(ClusterAddAnotherHostAction);
					getOptionalActions().add(addHostAction);
				}
				if (getEntity().getstorage_pool_id() == null)
				{
					addHostAction.setIsExecutionAllowed(false);
					addHostAction.getExecuteProhibitionReasons().add("The Cluster isn't attached to a Data Center");
					return;
				}
				java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterList((Guid)getEntity().getstorage_pool_id());
				Version minimalClusterVersion = Linq.GetMinVersionByClusters(clusters);
				java.util.ArrayList<VDS> availableHosts = new java.util.ArrayList<VDS>();
				for (VDS vds : DataProvider.GetHostList())
				{
					if ((!Linq.IsHostBelongsToAnyOfClusters(clusters, vds)) && (vds.getstatus() == VDSStatus.Maintenance || vds.getstatus() == VDSStatus.PendingApproval) && (vds.getVersion().getFullVersion() == null || Extensions.GetFriendlyVersion(vds.getVersion().getFullVersion()).compareTo(minimalClusterVersion) >= 0))
					{
						availableHosts.add(vds);
					}
				}
				//Select host action.
				UICommand selectHostAction = new UICommand("SelectHost", this);

				if (availableHosts.size() > 0 && clusters.size() > 0)
				{
					if (hosts.isEmpty())
					{
						selectHostAction.setTitle(SelectHostsAction);
						getCompulsoryActions().add(selectHostAction);
					}
					else
					{
						selectHostAction.setTitle(SelectHostsAction);
						getOptionalActions().add(selectHostAction);
					}
				}
			}
			else
			{
				UICommand tempVar = new UICommand("AddHost", this);
				tempVar.setTitle(ClusterAddAnotherHostAction);
				UICommand addHostAction = tempVar;
				UICommand tempVar2 = new UICommand("SelectHost", this);
				tempVar2.setTitle(SelectHostsAction);
				UICommand selectHost = tempVar2;
				VDS host = DataProvider.GetLocalStorageHost(dataCenter.getname());
				if (host != null)
				{
					addHostAction.setIsExecutionAllowed(false);
					selectHost.setIsExecutionAllowed(false);
					String hasHostReason = "This Cluster belongs to a Local Data Center which already contain a Host";
					addHostAction.getExecuteProhibitionReasons().add(hasHostReason);
					selectHost.getExecuteProhibitionReasons().add(hasHostReason);
				}
				getCompulsoryActions().add(addHostAction);
				getOptionalActions().add(selectHost);
			}
		}
	}

	public void SelectHost()
	{
		java.util.ArrayList<VDSGroup> clusters = new java.util.ArrayList<VDSGroup>();
		clusters.add(getEntity());

		MoveHost model = new MoveHost();
		model.setTitle("Select Host");
		model.setHashName("select_host");
		setWindow(model);
		model.getCluster().setItems(clusters);
		model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
		model.getCluster().setIsAvailable(false);

		UICommand tempVar = new UICommand("OnSelectHost", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnSelectHost()
	{
		MoveHost model = (MoveHost)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		model.setSelectedHosts(new java.util.ArrayList<VDS>());
		for (EntityModel a : Linq.<EntityModel>Cast(model.getItems()))
		{
			if (a.getIsSelected())
			{
				model.getSelectedHosts().add((VDS)a.getEntity());
			}
		}

		VDSGroup cluster = (VDSGroup)model.getCluster().getSelectedItem();

		java.util.ArrayList<VdcActionParametersBase> paramerterList = new java.util.ArrayList<VdcActionParametersBase>();
		for (VDS host : model.getSelectedHosts())
		{
			//Try to change host's cluster as neccessary.
			if (host.getvds_group_id() != null && !host.getvds_group_id().equals(cluster.getID()))
			{
				paramerterList.add(new ChangeVDSClusterParameters(cluster.getID(), host.getvds_id()));

			}
		}
		model.StartProgress(null);
		Frontend.RunMultipleAction(VdcActionType.ChangeVDSCluster, paramerterList,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ClusterGuideModel clusterGuideModel = (ClusterGuideModel)result.getState();
			java.util.ArrayList<VDS> hosts = ((MoveHost) clusterGuideModel.getWindow()).getSelectedHosts();
			java.util.ArrayList<VdcReturnValueBase> retVals = (java.util.ArrayList<VdcReturnValueBase>) result.getReturnValue();
			if (retVals != null && hosts.size() == retVals.size())
			{
				int i = 0;
				for (VDS selectedHost : hosts)
				{
					if (selectedHost.getstatus() == VDSStatus.PendingApproval && retVals.get(i) != null && retVals.get(i).getSucceeded())
					{
						Frontend.RunAction(VdcActionType.ApproveVds, new ApproveVdsParameters(selectedHost.getvds_id()));
					}
				}
				i++;
			}
			clusterGuideModel.getWindow().StopProgress();
			clusterGuideModel.Cancel();
			clusterGuideModel.PostAction();

			}
		}, this);
	}

	public void AddHost()
	{
		HostModel model = new HostModel();
		setWindow(model);
		model.setTitle("New Host");
		model.setHashName("new_host");
		model.getPort().setEntity(54321);
		model.getOverrideIpTables().setEntity(true);

		model.getCluster().setSelectedItem(getEntity());
		model.getCluster().setIsChangable(false);

		java.util.ArrayList<storage_pool> dataCenters = DataProvider.GetDataCenterList();
		model.getDataCenter().setItems(dataCenters);
		if (getEntity().getstorage_pool_id() != null)
		{
			model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters, new Linq.DataCenterPredicate((Guid)getEntity().getstorage_pool_id())));
		}
		model.getDataCenter().setIsChangable(false);


		UICommand tempVar = new UICommand("OnConfirmPMHost", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnConfirmPMHost()
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

			UICommand tempVar = new UICommand("OnAddHost", this);
			tempVar.setTitle("OK");
			tempVar.setIsDefault(true);
			confirmModel.getCommands().add(tempVar);
			UICommand tempVar2 = new UICommand("CancelConfirmWithFocus", this);
			tempVar2.setTitle("Cancel");
			tempVar2.setIsCancel(true);
			confirmModel.getCommands().add(tempVar2);
		}
		else
		{
			OnAddHost();
		}
	}

	public void OnAddHost()
	{
		CancelConfirm();

		HostModel model = (HostModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		//Save changes.
		VDS host = new VDS();
		host.setvds_name((String)model.getName().getEntity());
		host.sethost_name((String)model.getHost().getEntity());
		host.setManagmentIp((String)model.getManagementIp().getEntity());
		host.setport((Integer)model.getPort().getEntity());
		host.setvds_group_id(((VDSGroup)model.getCluster().getSelectedItem()).getID());
		host.setpm_enabled((Boolean)model.getIsPm().getEntity());
		host.setpm_user((Boolean)model.getIsPm().getEntity() ? (String)model.getPmUserName().getEntity() : null);
		host.setpm_password((Boolean)model.getIsPm().getEntity() ? (String)model.getPmPassword().getEntity() : null);
		host.setpm_type((Boolean)model.getIsPm().getEntity() ? (String)model.getPmType().getSelectedItem() : null);
		host.setPmOptionsMap((Boolean)model.getIsPm().getEntity() ? new ValueObjectMap(model.getPmOptionsMap(), false) : null);

		AddVdsActionParameters vdsActionParams = new AddVdsActionParameters();
		vdsActionParams.setvds(host);
		vdsActionParams.setVdsId(host.getvds_id());
		vdsActionParams.setRootPassword((String)model.getRootPassword().getEntity());

		model.StartProgress(null);

		Frontend.RunAction(VdcActionType.AddVds, vdsActionParams,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			ClusterGuideModel localModel = (ClusterGuideModel)result.getState();
			localModel.PostOnAddHost(result.getReturnValue());

			}
		}, this);
	}

	public void PostOnAddHost(VdcReturnValueBase returnValue)
	{
		HostModel model = (HostModel)getWindow();

		model.StopProgress();

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();
			PostAction();
		}
	}

	private void PostAction()
	{
		UpdateOptions();
	}

	public void Cancel()
	{
		setWindow(null);
	}

	public void CancelConfirm()
	{
		setConfirmWindow(null);
	}

	public void CancelConfirmWithFocus()
	{
		setConfirmWindow(null);

		HostModel hostModel = (HostModel)getWindow();
		hostModel.setIsPowerManagementSelected(true);
		hostModel.getIsPm().setEntity(true);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (StringHelper.stringsEqual(command.getName(), "AddHost"))
		{
			AddHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnConfirmPMHost"))
		{
			OnConfirmPMHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAddHost"))
		{
			OnAddHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "SelectHost"))
		{
			SelectHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnSelectHost"))
		{
			OnSelectHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		if (StringHelper.stringsEqual(command.getName(), "CancelConfirm"))
		{
			CancelConfirm();
		}
		if (StringHelper.stringsEqual(command.getName(), "CancelConfirmWithFocus"))
		{
			CancelConfirmWithFocus();
		}
	}
}
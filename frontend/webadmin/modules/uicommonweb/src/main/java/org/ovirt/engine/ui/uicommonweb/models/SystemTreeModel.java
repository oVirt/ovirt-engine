package org.ovirt.engine.ui.uicommonweb.models;
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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.*;

@SuppressWarnings("unused")
public class SystemTreeModel extends SearchableListModel implements IFrontendMultipleQueryAsyncCallback
{

	public static EventDefinition ResetRequestedEventDefinition;
	private Event privateResetRequestedEvent;
	public Event getResetRequestedEvent()
	{
		return privateResetRequestedEvent;
	}
	private void setResetRequestedEvent(Event value)
	{
		privateResetRequestedEvent = value;
	}



	private UICommand privateResetCommand;
	public UICommand getResetCommand()
	{
		return privateResetCommand;
	}
	private void setResetCommand(UICommand value)
	{
		privateResetCommand = value;
	}
	private UICommand privateExpandAllCommand;
	public UICommand getExpandAllCommand()
	{
		return privateExpandAllCommand;
	}
	private void setExpandAllCommand(UICommand value)
	{
		privateExpandAllCommand = value;
	}
	private UICommand privateCollapseAllCommand;
	public UICommand getCollapseAllCommand()
	{
		return privateCollapseAllCommand;
	}
	private void setCollapseAllCommand(UICommand value)
	{
		privateCollapseAllCommand = value;
	}



	public java.util.ArrayList<SystemTreeItemModel> getItems()
	{
		return (java.util.ArrayList<SystemTreeItemModel>)super.getItems();
	}
	public void setItems(java.util.ArrayList<SystemTreeItemModel> value)
	{
		if (items != value)
		{
			ItemsChanging(value, items);
			items = value;
			ItemsChanged();
			getItemsChangedEvent().raise(this, EventArgs.Empty);
			OnPropertyChanged(new PropertyChangedEventArgs("Items"));
		}
	}

	private java.util.ArrayList<storage_pool> privateDataCenters;
	public java.util.ArrayList<storage_pool> getDataCenters()
	{
		return privateDataCenters;
	}
	public void setDataCenters(java.util.ArrayList<storage_pool> value)
	{
		privateDataCenters = value;
	}
	private java.util.HashMap<Guid, java.util.ArrayList<VDSGroup>> privateClusterMap;
	public java.util.HashMap<Guid, java.util.ArrayList<VDSGroup>> getClusterMap()
	{
		return privateClusterMap;
	}
	public void setClusterMap(java.util.HashMap<Guid, java.util.ArrayList<VDSGroup>> value)
	{
		privateClusterMap = value;
	}
	private java.util.HashMap<Guid, java.util.ArrayList<VDS>> privateHostMap;
	public java.util.HashMap<Guid, java.util.ArrayList<VDS>> getHostMap()
	{
		return privateHostMap;
	}
	public void setHostMap(java.util.HashMap<Guid, java.util.ArrayList<VDS>> value)
	{
		privateHostMap = value;
	}



	static
	{
		ResetRequestedEventDefinition = new EventDefinition("ResetRequested", SystemTreeModel.class);
	}

	public SystemTreeModel()
	{
		setResetRequestedEvent(new Event(ResetRequestedEventDefinition));

		setResetCommand(new UICommand("Reset", this));
		setExpandAllCommand(new UICommand("ExpandAll", this));
		setCollapseAllCommand(new UICommand("CollapseAll", this));

		setIsTimerDisabled(true);

		setItems(new java.util.ArrayList<SystemTreeItemModel>());
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
			{
				SystemTreeModel systemTreeModel = (SystemTreeModel)model;
				systemTreeModel.setDataCenters((java.util.ArrayList<storage_pool>)result);

				AsyncQuery _asyncQuery1 = new AsyncQuery();
				_asyncQuery1.setModel(systemTreeModel);
				_asyncQuery1.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model1, Object result1)
					{
						SystemTreeModel systemTreeModel1 = (SystemTreeModel)model1;
						java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>)result1;

						systemTreeModel1.setClusterMap(new java.util.HashMap<Guid, java.util.ArrayList<VDSGroup>>());
						for (VDSGroup cluster : clusters)
						{
							if (cluster.getstorage_pool_id() != null)
							{
								Guid key = cluster.getstorage_pool_id().getValue();
								if (!systemTreeModel1.getClusterMap().containsKey(key))
								{
									systemTreeModel1.getClusterMap().put(key, new java.util.ArrayList<VDSGroup>());
								}
								java.util.ArrayList<VDSGroup> list1 = systemTreeModel1.getClusterMap().get(key);
								list1.add(cluster);
							}
						}
						AsyncQuery _asyncQuery2 = new AsyncQuery();
						_asyncQuery2.setModel(systemTreeModel1);
						_asyncQuery2.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model2, Object result2)
						{
							SystemTreeModel systemTreeModel2 = (SystemTreeModel)model2;
							java.util.ArrayList<VDS> hosts = (java.util.ArrayList<VDS>)result2;
							systemTreeModel2.setHostMap(new java.util.HashMap<Guid, java.util.ArrayList<VDS>>());
							for (VDS host : hosts)
							{
								Guid key = host.getvds_group_id();
								if (!systemTreeModel2.getHostMap().containsKey(key))
								{
									systemTreeModel2.getHostMap().put(key, new java.util.ArrayList<VDS>());
								}
								java.util.ArrayList<VDS> list = systemTreeModel2.getHostMap().get(key);
								list.add(host);
							}
							java.util.ArrayList<VdcQueryType> queryTypeList = new java.util.ArrayList<VdcQueryType>();
							java.util.ArrayList<VdcQueryParametersBase> queryParamList = new java.util.ArrayList<VdcQueryParametersBase>();
							for (storage_pool dataCenter : systemTreeModel2.getDataCenters())
							{
								queryTypeList.add(VdcQueryType.GetStorageDomainsByStoragePoolId);
								queryParamList.add(new StoragePoolQueryParametersBase(dataCenter.getId()));
							}
							Frontend.RunMultipleQueries(queryTypeList, queryParamList, systemTreeModel2);
						}};
						AsyncDataProvider.GetHostList(_asyncQuery2);
					}};
				AsyncDataProvider.GetClusterList(_asyncQuery1);
			}};
		AsyncDataProvider.GetDataCenterList(_asyncQuery);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getResetCommand())
		{
			Reset();
		}
		else if (command == getExpandAllCommand())
		{
			ExpandAll();
		}
		else if (command == getCollapseAllCommand())
		{
			CollapseAll();
		}
	}

	private void CollapseAll()
	{
		SetIsExpandedRecursively(false, getItems().get(0));
	}

	private void ExpandAll()
	{
		SetIsExpandedRecursively(true, getItems().get(0));
	}

	private void SetIsExpandedRecursively(boolean value, SystemTreeItemModel root)
	{
		root.setIsExpanded(value);

		for (SystemTreeItemModel model : root.getChildren())
		{
			SetIsExpandedRecursively(value, model);
		}
	}

	private void Reset()
	{
		getResetRequestedEvent().raise(this, EventArgs.Empty);
	}


	public void Executed(FrontendMultipleQueryAsyncResult result)
	{
		java.util.ArrayList<storage_domains> storages;
		int count = -1;

		//Build tree items.
		SystemTreeItemModel systemItem = new SystemTreeItemModel();
		systemItem.setType(SystemTreeItemType.System);
		systemItem.setIsSelected(true);
		systemItem.setTitle("System");

		for (VdcQueryReturnValue returnValue : result.getReturnValues())
		{
			++count;
			if (!returnValue.getSucceeded())
			{
				continue;
			}
			storages = (java.util.ArrayList<storage_domains>)returnValue.getReturnValue();

			SystemTreeItemModel dataCenterItem = new SystemTreeItemModel();
			dataCenterItem.setType(SystemTreeItemType.DataCenter);
			dataCenterItem.setTitle(getDataCenters().get(count).getname());
			dataCenterItem.setEntity(getDataCenters().get(count));
			systemItem.getChildren().add(dataCenterItem);

			SystemTreeItemModel storagesItem = new SystemTreeItemModel();
			storagesItem.setType(SystemTreeItemType.Storages);
			storagesItem.setTitle("Storage");
			storagesItem.setParent(dataCenterItem);
			storagesItem.setEntity(getDataCenters().get(count));
			dataCenterItem.getChildren().add(storagesItem);

			if (storages.size() > 0)
			{
				for (storage_domains storage : storages)
				{
					SystemTreeItemModel storageItem = new SystemTreeItemModel();
					storageItem.setType(SystemTreeItemType.Storage);
					storageItem.setTitle(storage.getstorage_name());
					storageItem.setParent(dataCenterItem);
					storageItem.setEntity(storage);
					storagesItem.getChildren().add(storageItem);
				}
			}

			SystemTreeItemModel templatesItem = new SystemTreeItemModel();
			templatesItem.setType(SystemTreeItemType.Templates);
			templatesItem.setTitle("Templates");
			templatesItem.setParent(dataCenterItem);
			templatesItem.setEntity(getDataCenters().get(count));
			dataCenterItem.getChildren().add(templatesItem);


			SystemTreeItemModel clustersItem = new SystemTreeItemModel();
			clustersItem.setType(SystemTreeItemType.Clusters);
			clustersItem.setTitle("Clusters");
			clustersItem.setParent(dataCenterItem);
			clustersItem.setEntity(getDataCenters().get(count));
			dataCenterItem.getChildren().add(clustersItem);

			if (getClusterMap().containsKey(getDataCenters().get(count).getId()))
			{
				for (VDSGroup cluster : getClusterMap().get(getDataCenters().get(count).getId()))
				{
					SystemTreeItemModel clusterItem = new SystemTreeItemModel();
					clusterItem.setType(SystemTreeItemType.Cluster);
					clusterItem.setTitle(cluster.getname());
					clusterItem.setParent(dataCenterItem);
					clusterItem.setEntity(cluster);
					clustersItem.getChildren().add(clusterItem);


					SystemTreeItemModel hostsItem = new SystemTreeItemModel();
					hostsItem.setType(SystemTreeItemType.Hosts);
					hostsItem.setTitle("Hosts");
					hostsItem.setParent(clusterItem);
					hostsItem.setEntity(cluster);
					clusterItem.getChildren().add(hostsItem);

					if (getHostMap().containsKey(cluster.getID()))
					{
						for (VDS host : getHostMap().get(cluster.getID()))
						{
							SystemTreeItemModel hostItem = new SystemTreeItemModel();
							hostItem.setType(SystemTreeItemType.Host);
							hostItem.setTitle(host.getvds_name());
							hostItem.setParent(clusterItem);
							hostItem.setEntity(host);
							hostsItem.getChildren().add(hostItem);
						}
					}

					SystemTreeItemModel vmsItem = new SystemTreeItemModel();
					vmsItem.setType(SystemTreeItemType.VMs);
					vmsItem.setTitle("VMs");
					vmsItem.setParent(clusterItem);
					vmsItem.setEntity(cluster);
					clusterItem.getChildren().add(vmsItem);
				}
			}
		}
		setItems(new java.util.ArrayList<SystemTreeItemModel>(java.util.Arrays.asList(new SystemTreeItemModel[] { systemItem })));
	}
    @Override
    protected String getListName() {
        return "SystemTreeModel";
    }

}
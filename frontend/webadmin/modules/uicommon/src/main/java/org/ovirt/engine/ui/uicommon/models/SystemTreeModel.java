package org.ovirt.engine.ui.uicommon.models;
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
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;

@SuppressWarnings("unused")
public class SystemTreeModel extends SearchableListModel
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
		super.setItems(value);
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


		//Build maps.
		java.util.ArrayList<storage_pool> dataCenters = DataProvider.GetDataCenterList();

		//Cluster by data center map.
		java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterList();
		java.util.HashMap<Guid, java.util.ArrayList<VDSGroup>> clusterMap = new java.util.HashMap<Guid, java.util.ArrayList<VDSGroup>>();
		for (VDSGroup cluster : clusters)
		{
			if (cluster.getstorage_pool_id() != null)
			{
				Guid key = cluster.getstorage_pool_id().getValue();
				if (!clusterMap.containsKey(key))
				{
					clusterMap.put(key, new java.util.ArrayList<VDSGroup>());
				}
				java.util.ArrayList<VDSGroup> list = clusterMap.get(key);
				list.add(cluster);
			}
		}

		//Host by cluster map.
		java.util.ArrayList<VDS> hosts = DataProvider.GetHostList();
		java.util.HashMap<Guid, java.util.ArrayList<VDS>> hostMap = new java.util.HashMap<Guid, java.util.ArrayList<VDS>>();
		for (VDS host : hosts)
		{
			Guid key = host.getvds_group_id();
			if (!hostMap.containsKey(key))
			{
				hostMap.put(key, new java.util.ArrayList<VDS>());
			}
			java.util.ArrayList<VDS> list = hostMap.get(key);
			list.add(host);
		}


		//Build tree items.
		SystemTreeItemModel systemItem = new SystemTreeItemModel();
		systemItem.setType(SystemTreeItemType.System);
		systemItem.setIsSelected(true);
		systemItem.setTitle("System");

		for (storage_pool dataCenter : dataCenters)
		{
			SystemTreeItemModel dataCenterItem = new SystemTreeItemModel();
			dataCenterItem.setType(SystemTreeItemType.DataCenter);
			dataCenterItem.setTitle(dataCenter.getname());
			dataCenterItem.setEntity(dataCenter);
			systemItem.getChildren().add(dataCenterItem);

			SystemTreeItemModel storagesItem = new SystemTreeItemModel();
			storagesItem.setType(SystemTreeItemType.Storages);
			storagesItem.setTitle("Storage");
			storagesItem.setParent(dataCenterItem);
			storagesItem.setEntity(dataCenter);
			dataCenterItem.getChildren().add(storagesItem);

			java.util.ArrayList<storage_domains> storages = DataProvider.GetStorageDomainList(dataCenter.getId());
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
			templatesItem.setEntity(dataCenter);
			dataCenterItem.getChildren().add(templatesItem);


			SystemTreeItemModel clustersItem = new SystemTreeItemModel();
			clustersItem.setType(SystemTreeItemType.Clusters);
			clustersItem.setTitle("Clusters");
			clustersItem.setParent(dataCenterItem);
			clustersItem.setEntity(dataCenter);
			dataCenterItem.getChildren().add(clustersItem);

			if (clusterMap.containsKey(dataCenter.getId()))
			{
				for (VDSGroup cluster : clusterMap.get(dataCenter.getId()))
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

					if (hostMap.containsKey(cluster.getId()))
					{
						for (VDS host : hostMap.get(cluster.getId()))
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
}
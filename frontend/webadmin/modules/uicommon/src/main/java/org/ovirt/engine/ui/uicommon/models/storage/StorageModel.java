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

import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.interfaces.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class StorageModel extends ListModel implements ISupportSystemTreeContext
{
	public static final Guid UnassignedDataCenterId = Guid.Empty;
	private StorageModelBehavior behavior;


	public IStorageModel getSelectedItem()
	{
		return (IStorageModel)super.getSelectedItem();
	}
	public void setSelectedItem(IStorageModel value)
	{
		super.setSelectedItem(value);
	}

	/**
	 Gets or sets the storage being edited. Null if it's a new one.
	*/
	private storage_domains privateStorage;
	public storage_domains getStorage()
	{
		return privateStorage;
	}
	public void setStorage(storage_domains value)
	{
		privateStorage = value;
	}

	private String privateOriginalName;
	public String getOriginalName()
	{
		return privateOriginalName;
	}
	public void setOriginalName(String value)
	{
		privateOriginalName = value;
	}

	private EntityModel privateName;
	public EntityModel getName()
	{
		return privateName;
	}
	private void setName(EntityModel value)
	{
		privateName = value;
	}
	private ListModel privateDataCenter;
	public ListModel getDataCenter()
	{
		return privateDataCenter;
	}
	private void setDataCenter(ListModel value)
	{
		privateDataCenter = value;
	}
	private ListModel privateHost;
	public ListModel getHost()
	{
		return privateHost;
	}
	private void setHost(ListModel value)
	{
		privateHost = value;
	}
	private ListModel privateFormat;
	public ListModel getFormat()
	{
		return privateFormat;
	}
	private void setFormat(ListModel value)
	{
		privateFormat = value;
	}



	public StorageModel(StorageModelBehavior behavior)
	{
		this.behavior = behavior;
		this.behavior.setModel(this);

		setName(new EntityModel());
		setDataCenter(new ListModel());
		getDataCenter().getSelectedItemChangedEvent().addListener(this);
		setHost(new ListModel());
		getHost().getSelectedItemChangedEvent().addListener(this);
		setFormat(new ListModel());
	}

	@Override
	public void Initialize()
	{
		super.Initialize();

		InitDataCenter();
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(SelectedItemChangedEventDefinition))
		{
			if (sender == getDataCenter())
			{
				DataCenter_SelectedItemChanged();
			}
			else if (sender == getHost())
			{
				Host_SelectedItemChanged();
			}
		}
		else if (ev.equals(NfsStorageModel.PathChangedEventDefinition))
		{
			NfsStorageModel_PathChanged(sender, args);
		}
	}

	private void NfsStorageModel_PathChanged(Object sender, EventArgs args)
	{
		NfsStorageModel senderModel = (NfsStorageModel)sender;

		for (Object item : getItems())
		{
			if (item instanceof NfsStorageModel && item != sender)
			{
				NfsStorageModel model = (NfsStorageModel)item;
				model.getPath().setEntity(senderModel.getPath().getEntity());
			}
		}
	}

	@Override
	protected void OnSelectedItemChanged()
	{
		super.OnSelectedItemChanged();

		if (getSelectedItem() != null)
		{
			UpdateFormat();
			UpdateHost();
			getSelectedItem().getUpdateCommand().Execute();
		}
	}

	@Override
	protected void ItemsChanged()
	{
		super.ItemsChanged();

		if (getItems() != null)
		{
			for (Object item : getItems())
			{
				IStorageModel model = (IStorageModel)item;
				model.setContainer(this);

				if (item instanceof NfsStorageModel)
				{
					NfsStorageModel nfsModel = (NfsStorageModel)item;
					nfsModel.getPathChangedEvent().addListener(this);
				}
			}
		}
	}

	private void DataCenter_SelectedItemChanged()
	{
		UpdateItemsAvailability();
		UpdateFormat();
		UpdateHost();
	}

	private void Host_SelectedItemChanged()
	{
		VDS host = (VDS)getHost().getSelectedItem();
		if (getSelectedItem() != null)
		{
			//When changing host clear items for san storage model.
			if (getSelectedItem() instanceof SanStorageModelBase && getStorage() == null)
			{
				SanStorageModelBase sanStorageModel = (SanStorageModelBase)getSelectedItem();
				sanStorageModel.setItems(null);
			}

			if (host != null)
			{
				getSelectedItem().getUpdateCommand().Execute();

				//Update path prefix for local storage.
				String prefix = host.getvds_type() == VDSType.oVirtNode ? DataProvider.GetLocalFSPath() : "";

				for (Object item : getItems())
				{
					if (item instanceof LocalStorageModel)
					{
						LocalStorageModel model = (LocalStorageModel)item;
						model.getPath().setEntity(prefix);
						model.getPath().setIsChangable(StringHelper.isNullOrEmpty(prefix));
					}
				}
			}
		}
	}

	private void InitDataCenter()
	{
		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
		{
			switch (getSystemTreeSelectedItem().getType())
			{
				case DataCenter:
				case Cluster:
				case Storages:
				case Storage:
					{
						SystemTreeItemModel dataCenterItem = SystemTreeItemModel.FindAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
						storage_pool dc = (storage_pool)dataCenterItem.getEntity();

						getDataCenter().setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { dc })));
						getDataCenter().setSelectedItem(dc);
						getDataCenter().setIsChangable(false);
						getDataCenter().setInfo("Cannot choose Storage's Data Center in tree context");
					}
					break;

				case Host:
					{
						VDS host = (VDS)getSystemTreeSelectedItem().getEntity();

						getHost().setIsChangable(false);
						getHost().setInfo("Cannot choose Storage's Host in tree context");
						getHost().setSelectedItem(host);

						SystemTreeItemModel dataCenterItem = SystemTreeItemModel.FindAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
						storage_pool dc = (storage_pool)dataCenterItem.getEntity();

						getDataCenter().setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { dc })));
						getDataCenter().setSelectedItem(dc);
						getDataCenter().setIsChangable(false);
						getDataCenter().setInfo("Cannot choose Storage's Data Center in tree context");
					}
					break;
			}
		}
		else
		{
			java.util.List<storage_pool> dataCenters = new java.util.ArrayList<storage_pool>();

			if (getStorage() == null || getStorage().getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
			// We are either adding a new storage or editing an unattached storage
			// -> fill DataCenters drop-down with all possible Data-Centers, choose the empty one:
			// [TODO: In case of an Unattached SD, choose only DCs of the same type]
			{
				dataCenters = behavior.FilterDataCenter(DataProvider.GetDataCenterList());
				AddEmptyDataCenterToList(dataCenters);

				storage_pool oldSelectedItem = (storage_pool)getDataCenter().getSelectedItem();
				getDataCenter().setItems(dataCenters);

				if (oldSelectedItem != null)
				{
					getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters, new Linq.DataCenterPredicate(oldSelectedItem.getId())));
				}
				else
				{
					getDataCenter().setSelectedItem(getStorage() == null ? Linq.FirstOrDefault(dataCenters) : Linq.FirstOrDefault(dataCenters, new Linq.DataCenterPredicate(UnassignedDataCenterId))); // Edit -> select empty DC -  New -> select first DC (should be non-empty)
				}
			}

			else // "Edit Storage" mode:
			{
				java.util.List<storage_pool> dataCentersWithStorage = behavior.FilterDataCenter(DataProvider.GetDataCentersByStorageDomain(getStorage().getid()));
				if (dataCentersWithStorage.size() < 1 || dataCentersWithStorage.get(0) == null)
				{
					// [*** shouldn't happen - this is just to prevent unexpected crashes/behavior ***]
					// SD isn't attached to any DC -> just add Unassigned item:
					AddEmptyDataCenterToList(dataCenters);
				}
				else
				// storage is in (at least) one (real) DC
				//-> set this DC as the only item in the list:
				{
					dataCenters = new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { dataCentersWithStorage.get(0) }));
				}

				getDataCenter().setItems(dataCenters);
				getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
			}
		}
	}

	private static void AddEmptyDataCenterToList(java.util.List<storage_pool> dataCenters)
	{
		storage_pool tempVar = new storage_pool();
		tempVar.setId(UnassignedDataCenterId);
		tempVar.setname("(none)");
		dataCenters.add(tempVar);
	}

	private void UpdateHost()
	{
		if (getDataCenter().getItems() == null)
		{
			return;
		}


		storage_pool dataCenter = (storage_pool)getDataCenter().getSelectedItem();
		Iterable<VDS> hosts = new java.util.ArrayList<VDS>();

		if (getSelectedItem() instanceof LocalStorageModel && (dataCenter == null || dataCenter.getId().equals(UnassignedDataCenterId)))
		{
			java.util.ArrayList<storage_pool> dataCenterList = (java.util.ArrayList<storage_pool>)getDataCenter().getItems();
			java.util.ArrayList<storage_pool> localDCList = new java.util.ArrayList<storage_pool>();
			String dataCenterQueryLine = "";

			for (storage_pool storagePool : dataCenterList)
			{
				if (storagePool.getstorage_pool_type() == StorageType.LOCALFS)
				{
					localDCList.add(storagePool);
				}
			}

			if (localDCList.size() > 0)
			{
				int i = 0;
				for (; i < localDCList.size() - 1; i++)
				{
					dataCenterQueryLine += "datacenter=" + localDCList.get(i).getname() + " or ";
				}
				dataCenterQueryLine += "datacenter=" + localDCList.get(i).getname();

				VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Hosts: status=Up " + dataCenterQueryLine, SearchType.VDS));
				if (returnValue != null && returnValue.getSucceeded())
				{
					hosts = (java.util.ArrayList<VDS>)returnValue.getReturnValue();
				}
			}
		}
		else
		{
			hosts = dataCenter == null || dataCenter.getId().equals(UnassignedDataCenterId) ? DataProvider.GetHostList() : DataProvider.GetHostListByDataCenter(dataCenter.getname());

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
			hosts = Linq.Where(hosts, new Linq.HostStatusPredicate(VDSStatus.Up));
		}

		//Allow only hosts with version above 2.2 for export storage.
		java.util.ArrayList<VDS> list = new java.util.ArrayList<VDS>();
		if (getSelectedItem() != null && getSelectedItem().getRole() == StorageDomainType.ImportExport)
		{
			for (VDS host : hosts)
			{
				if (host.getvds_group_compatibility_version().compareTo(new Version("2.2")) >= 0)
				{
					list.add(host);
				}
			}
			hosts = list;
		}


		VDS oldSelectedItem = (VDS)getHost().getSelectedItem();
		getHost().setItems(hosts);

		//Try to select previously selected host.
		if (oldSelectedItem != null)
		{
			getHost().setSelectedItem(Linq.FirstOrDefault(hosts, new Linq.HostPredicate(oldSelectedItem.getvds_id())));
		}

		//Try to select an SPM host when edit storage.
		if (getHost().getSelectedItem() == null && getStorage() != null)
		{
			for (VDS host : hosts)
			{
				if (host.getspm_status() == VdsSpmStatus.SPM)
				{
					getHost().setSelectedItem(host);
					break;
				}
			}
		}

		//Select a default - first host in the list.
		if (getHost().getSelectedItem() == null)
		{
			getHost().setSelectedItem(Linq.FirstOrDefault(hosts));
		}
	}

	private void UpdateFormat()
	{
		storage_pool dataCenter = (storage_pool)getDataCenter().getSelectedItem();

		StorageFormatType selectItem = StorageFormatType.V1;

		java.util.ArrayList<StorageFormatType> formats = new java.util.ArrayList<StorageFormatType>();

		if (dataCenter != null && getSelectedItem() != null)
		{
			if (!dataCenter.getId().equals(UnassignedDataCenterId))
			{
				getFormat().setIsChangable(false);

				// If data center has format defined and the selected-item role is Data, choose it.
				if (dataCenter.getStoragePoolFormatType() != null && getSelectedItem().getRole() == StorageDomainType.Data)
				{
					formats.add(dataCenter.getStoragePoolFormatType());
					selectItem = dataCenter.getStoragePoolFormatType();
				}
				// If selected-item role is ISO or Export, add only the 'V1' option.
				// (*** Note that currently both ISO and Export can be only NFS, so theoretically they are covered by
				// the next "else if..." condition; however, just in case we will support non-NFS ISO/Export in the future
				// and in order to make the code more explicit, it is here. ***)
				else if (getSelectedItem().getRole() == StorageDomainType.ISO || getSelectedItem().getRole() == StorageDomainType.ImportExport)
				{
					formats.add(StorageFormatType.V1);
				}
				else if (getSelectedItem().getType() == StorageType.NFS || getSelectedItem().getType() == StorageType.LOCALFS)
				{
					formats.add(StorageFormatType.V1);
				}
				else if ((getSelectedItem().getType() == StorageType.ISCSI || getSelectedItem().getType() == StorageType.FCP) && dataCenter.getcompatibility_version().compareTo(new Version("3.0")) < 0)
				{
					formats.add(StorageFormatType.V1);
				}
				else if ((getSelectedItem().getType() == StorageType.ISCSI || getSelectedItem().getType() == StorageType.FCP) && dataCenter.getcompatibility_version().compareTo(new Version("3.0")) >= 0)
				{
					formats.add(StorageFormatType.V2);
					selectItem = StorageFormatType.V2;
				}
			}
			else // Unassigned DC:
			{
				getFormat().setIsChangable(true);

				formats.add(StorageFormatType.V1);

				if ((getSelectedItem().getType() == StorageType.FCP || getSelectedItem().getType() == StorageType.ISCSI) && getSelectedItem().getRole() == StorageDomainType.Data)
				{
					formats.add(StorageFormatType.V2);
					selectItem = StorageFormatType.V2;
				}
			}
		}

		getFormat().setItems(formats);
		getFormat().setSelectedItem(selectItem);
	}

	private void UpdateItemsAvailability()
	{
		if (getItems() == null)
		{
			return;
		}

		behavior.UpdateItemsAvailability();

		boolean chooseFirst = false;
		if (getSelectedItem() != null)
		{
			Model selectedModel = (Model)getSelectedItem();
			if (!selectedModel.getIsSelectable())
			{
				chooseFirst = true;
			}
		}
		else
		{
			chooseFirst = true;
		}

		if (chooseFirst)
		{
			// Choose first allowed type (it will be data role in case of
			// New Domain and ISO role in case of Import Domain).
			for (IStorageModel item : Linq.<IStorageModel>Cast(getItems()))
			{
				Model model = (Model)item;
				if (model.getIsSelectable())
				{
					setSelectedItem(item);
					break;
				}
			}
		}
	}

	public boolean Validate()
	{
		int nameMaxLength = DataProvider.GetStorageDomainMaxNameLength();

		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$");
		tempVar.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters, max length: " + nameMaxLength);
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

		String name = (String)getName().getEntity();

		if (name.compareToIgnoreCase(getOriginalName()) != 0 && !DataProvider.IsStorageDomainNameUnique(name))
		{
			getName().setIsValid(false);
			getName().getInvalidityReasons().add("Name must be unique.");
		}


		getHost().ValidateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });
		ValidateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });


		return getName().getIsValid() && getHost().getIsValid() && getIsValid() && getSelectedItem().Validate();
	}


	private SystemTreeItemModel privateSystemTreeSelectedItem;
	public SystemTreeItemModel getSystemTreeSelectedItem()
	{
		return privateSystemTreeSelectedItem;
	}
	public void setSystemTreeSelectedItem(SystemTreeItemModel value)
	{
		privateSystemTreeSelectedItem = value;
	}
}
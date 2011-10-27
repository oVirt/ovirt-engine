package org.ovirt.engine.ui.uicommon.models.vms;
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
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ImportVmModel extends ListWithDetailsModel
{

	private storage_domain_static privateSourceStorage;
	public storage_domain_static getSourceStorage()
	{
		return privateSourceStorage;
	}
	public void setSourceStorage(storage_domain_static value)
	{
		privateSourceStorage = value;
	}
	private storage_pool privateStoragePool;
	public storage_pool getStoragePool()
	{
		return privateStoragePool;
	}
	public void setStoragePool(storage_pool value)
	{
		privateStoragePool = value;
	}

	private ListModel privateDestinationStorage;
	public ListModel getDestinationStorage()
	{
		return privateDestinationStorage;
	}
	private void setDestinationStorage(ListModel value)
	{
		privateDestinationStorage = value;
	}
	private ListModel privateCluster;
	public ListModel getCluster()
	{
		return privateCluster;
	}
	private void setCluster(ListModel value)
	{
		privateCluster = value;
	}
	private ListModel privateSystemDiskFormat;
	public ListModel getSystemDiskFormat()
	{
		return privateSystemDiskFormat;
	}
	private void setSystemDiskFormat(ListModel value)
	{
		privateSystemDiskFormat = value;
	}
	private ListModel privateDataDiskFormat;
	public ListModel getDataDiskFormat()
	{
		return privateDataDiskFormat;
	}
	private void setDataDiskFormat(ListModel value)
	{
		privateDataDiskFormat = value;
	}

	private boolean collapseSnapshots;
	public boolean getCollapseSnapshots()
	{
		return collapseSnapshots;
	}
	public void setCollapseSnapshots(boolean value)
	{
		if (collapseSnapshots != value)
		{
			collapseSnapshots = value;
			OnCollapseSnapshotsChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("CollapseSnapshots"));
		}
	}

	private String nameAndDescription;
	public String getNameAndDescription()
	{
		return nameAndDescription;
	}
	public void setNameAndDescription(String value)
	{
		if (!StringHelper.stringsEqual(nameAndDescription, value))
		{
			nameAndDescription = value;
			OnPropertyChanged(new PropertyChangedEventArgs("NameAndDescription"));
		}
	}


	public ImportVmModel()
	{
		setCollapseSnapshots(false);
		setDestinationStorage(new ListModel());
		setCluster(new ListModel());
		setSystemDiskFormat(new ListModel());
		setDataDiskFormat(new ListModel());
	}

	public void OnCollapseSnapshotsChanged()
	{
		if (this.getItems() == null)
		{
			return;
		}
		storage_domains selectedDestinationStorage = null;
		boolean sameSelectedDestinationStorage = false;
		if(getDestinationStorage().getSelectedItem() != null)
		{
			selectedDestinationStorage = (storage_domains) getDestinationStorage().getSelectedItem();
		}
		java.util.ArrayList<storage_domains> destStorages = new java.util.ArrayList<storage_domains>();
		java.util.HashMap<Guid, java.util.ArrayList<storage_domains>> templateGuidStorageDomainDic = new java.util.HashMap<Guid, java.util.ArrayList<storage_domains>>();
		for (Object item : getItems())
		{
			VM vm = (VM)item;
			Guid Guid = vm.getvmt_guid();
			if (templateGuidStorageDomainDic.containsKey(Guid))
			{
				continue;
			}
			if (Guid.equals(Guid.Empty))
			{
				templateGuidStorageDomainDic.put(Guid, null);
			}
			else
			{
				templateGuidStorageDomainDic.put(Guid, DataProvider.GetStorageDomainListByTemplate(Guid));
			}
		}
		for (storage_domains domain : DataProvider.GetDataDomainsListByDomain(this.getSourceStorage().getId()))
		{
			boolean addStorage = false;
			if ((domain.getstorage_domain_type() == StorageDomainType.Data || domain.getstorage_domain_type() == StorageDomainType.Master) && domain.getstatus() != null && domain.getstatus() == StorageDomainStatus.Active)
			{
				for (java.util.Map.Entry<Guid, java.util.ArrayList<storage_domains>> keyValuePair : templateGuidStorageDomainDic.entrySet())
				{
					if (Guid.Empty.equals(keyValuePair.getKey()))
					{
						addStorage = true;
					}
					else
					{
						addStorage = false;
						for (storage_domains storageDomain : keyValuePair.getValue())
						{
							if (storageDomain.getid().equals(domain.getid()) || getCollapseSnapshots() == true)
							{
								addStorage = true;
								break;
							}
						}
					}
					if (addStorage == false)
					{
						break;
					}
				}
			}
			if (addStorage)
			{
				destStorages.add(domain);
				if(sameSelectedDestinationStorage == false && domain.equals(selectedDestinationStorage))
				{
					sameSelectedDestinationStorage = true;
					selectedDestinationStorage = domain;
				}
			}
		}
		getDestinationStorage().setItems(destStorages);
		if(sameSelectedDestinationStorage)
		{
			getDestinationStorage().setSelectedItem(selectedDestinationStorage);
		}
		else
		{
			getDestinationStorage().setSelectedItem(Linq.FirstOrDefault(destStorages));
		}

		if (getDetailModels() != null && getActiveDetailModel() instanceof VmImportDiskListModel)
		{
			VmImportDiskListModel detailModel = (VmImportDiskListModel)getActiveDetailModel();
			detailModel.setCollapseSnapshots(getCollapseSnapshots());
		}
	}

	@Override
	protected void ActiveDetailModelChanged()
	{
		super.ActiveDetailModelChanged();
		OnCollapseSnapshotsChanged();
	}

	@Override
	protected void InitDetailModels()
	{
		super.InitDetailModels();

		VmImportDiskListModel importDiskListModel = new VmImportDiskListModel();

		ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
		list.add(new VmGeneralModel());
		list.add(new VmImportInterfaceListModel());
		list.add(importDiskListModel);
		list.add(new VmAppListModel());
		setDetailModels(list);

		importDiskListModel.setSelectedVMsCount(((java.util.List)getItems()).size());
	}

	public boolean Validate()
	{
		getDestinationStorage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		return getDestinationStorage().getIsValid() && getCluster().getIsValid();
	}

	@Override
	protected void OnSelectedItemChanged()
	{
		super.OnSelectedItemChanged();

		if (getSelectedItem() != null)
		{
			VM vm = (VM)getSelectedItem();
			setNameAndDescription(StringFormat.format("%1$s%2$s", vm.getvm_name(), !StringHelper.isNullOrEmpty(vm.getvm_description()) ? " [" + vm.getvm_description() + "]" : ""));
		}
		else
		{
			setNameAndDescription("");
		}

	}
}
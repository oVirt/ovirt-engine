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
import org.ovirt.engine.ui.uicompat.*;
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
	private EntityModel privateCollapseSnapshots;
	public EntityModel getCollapseSnapshots()
	{
		return privateCollapseSnapshots;
	}
	private void setCollapseSnapshots(EntityModel value)
	{
		privateCollapseSnapshots = value;
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

	private java.util.List<VM> problematicItems;
	public java.util.List<VM> getProblematicItems()
	{
		return problematicItems;
	}
	public void setProblematicItems(java.util.List<VM> value)
	{
		if (problematicItems != value)
		{
			problematicItems = value;
			OnPropertyChanged(new PropertyChangedEventArgs("ProblematicItems"));
		}
	}

	private boolean hasCollapseSnapshotsWarning;
	public boolean getHasCollapseSnapshotsWarning()
	{
		return hasCollapseSnapshotsWarning;
	}
	public void setHasCollapseSnapshotsWarning(boolean value)
	{
		if (hasCollapseSnapshotsWarning != value)
		{
			hasCollapseSnapshotsWarning = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasCollapseSnapshotsWarning"));
		}
	}


	public ImportVmModel()
	{
		EntityModel tempVar = new EntityModel();
		tempVar.setEntity(false);
		setCollapseSnapshots(tempVar);
		getCollapseSnapshots().getEntityChangedEvent().addListener(this);
		setDestinationStorage(new ListModel());
		getDestinationStorage().getSelectedItemChangedEvent().addListener(this);
		setCluster(new ListModel());
		setSystemDiskFormat(new ListModel());
		setDataDiskFormat(new ListModel());
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(EntityChangedEventDefinition) && sender == getCollapseSnapshots())
		{
			CollapseSnapshots_EntityChanged();
		}
		else if (ev.equals(getSelectedItemChangedEvent()) && sender == getDestinationStorage())
		{
			DestinationStorage_SelectedItemChanged();
		}
	}

	private void DestinationStorage_SelectedItemChanged()
	{
		UpdateImportWarnings();
	}

	@Override
	protected void ItemsChanged()
	{
		super.ItemsChanged();
		UpdateImportWarnings();
	}

	private void UpdateImportWarnings()
	{
		//Clear problematic state.
		setProblematicItems(null);

		if (getItems() == null)
		{
			return;
		}


		storage_domains destinationStorage = (storage_domains)getDestinationStorage().getSelectedItem();

		//Determine which items are problematic.
		java.util.ArrayList<VM> problematicItems = new java.util.ArrayList<VM>();

		for (Object item : getItems())
		{
			VM vm = (VM)item;

			if (vm.getDiskMap() != null)
			{
				for (java.util.Map.Entry<String, Disk> pair : vm.getDiskMap().entrySet())
				{
					DiskImage disk = (DiskImage)pair.getValue();

					if (disk.getvolume_type() == VolumeType.Sparse && disk.getvolume_format() == VolumeFormat.RAW && destinationStorage != null && (destinationStorage.getstorage_type() == StorageType.ISCSI || destinationStorage.getstorage_type() == StorageType.FCP))
					{
						problematicItems.add(vm);
					}
				}
			}
		}

		//Decide what to do with the CollapseSnapshots option.
		if (problematicItems.size() > 0)
	{
			if (problematicItems.size() == Linq.Count(getItems()))
			{
				//All items are problematic.
				getCollapseSnapshots().setIsChangable(false);
				getCollapseSnapshots().setEntity(true);
				getCollapseSnapshots().setMessage("Note that all snapshots will be collapsed due to different storage types");
				setHasCollapseSnapshotsWarning(true);
			}
			else
			{
				//Some items are problematic.
				getCollapseSnapshots().setMessage("Use a separate import operation for the marked VMs or\nApply \"Collapse Snapshots\" for all VMs");
				setHasCollapseSnapshotsWarning(!(Boolean)getCollapseSnapshots().getEntity());
				setProblematicItems((Boolean)getCollapseSnapshots().getEntity() ? null : problematicItems);
			}
		}
		else
		{
			//No problematic items.
			getCollapseSnapshots().setIsChangable(true);
			getCollapseSnapshots().setMessage(null);
			setHasCollapseSnapshotsWarning(false);
		}
	}

	public void CollapseSnapshots_EntityChanged()
	{
		if (getItems() == null)
		{
			return;
		}

		storage_domains selectedDestinationStorage = null;
		boolean sameSelectedDestinationStorage = false;
		if (getDestinationStorage().getSelectedItem() != null)
		{
			selectedDestinationStorage = (storage_domains)getDestinationStorage().getSelectedItem();
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
							if (storageDomain.getId().equals(domain.getId()) || (Boolean)getCollapseSnapshots().getEntity())
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
				if (sameSelectedDestinationStorage == false && domain.equals(selectedDestinationStorage))
				{
					sameSelectedDestinationStorage = true;
					selectedDestinationStorage = domain;
				}
			}
		}
		getDestinationStorage().setItems(destStorages);
		if (sameSelectedDestinationStorage)
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
			detailModel.setCollapseSnapshots((Boolean)getCollapseSnapshots().getEntity());
		}


		UpdateImportWarnings();
	}

	@Override
	protected void ActiveDetailModelChanged()
	{
		super.ActiveDetailModelChanged();
		CollapseSnapshots_EntityChanged();
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
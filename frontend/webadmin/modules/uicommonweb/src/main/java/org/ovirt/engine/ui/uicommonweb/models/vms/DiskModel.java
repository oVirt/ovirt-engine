package org.ovirt.engine.ui.uicommonweb.models.vms;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class DiskModel extends Model
{
    static int maxDiskSize = 2047;
    static boolean maxDiskSizeInited = false;

	private boolean privateIsNew;
	public boolean getIsNew()
	{
		return privateIsNew;
	}
	public void setIsNew(boolean value)
	{
		privateIsNew = value;
	}
	private String privateName;
	public String getName()
	{
		return privateName;
	}
	public void setName(String value)
	{
		privateName = value;
	}
	private VolumeFormat privateVolumeFormat = getVolumeFormat().values()[0];
	public VolumeFormat getVolumeFormat()
	{
		return privateVolumeFormat;
	}
	public void setVolumeFormat(VolumeFormat value)
	{
		privateVolumeFormat = value;
	}
	private java.util.Date privateCreationDate = new java.util.Date(0);
	public java.util.Date getCreationDate()
	{
		return privateCreationDate;
	}
	public void setCreationDate(java.util.Date value)
	{
		privateCreationDate = value;
	}
	private int privateActualSize;
	public int getActualSize()
	{
		return privateActualSize;
	}
	public void setActualSize(int value)
	{
		privateActualSize = value;
	}
	private DiskType privateDiskType = getDiskType().values()[0];
	public DiskType getDiskType()
	{
		return privateDiskType;
	}
	public void setDiskType(DiskType value)
	{
		privateDiskType = value;
	}
	private EntityModel privateSize;
	public EntityModel getSize()
	{
		return privateSize;
	}
	public void setSize(EntityModel value)
	{
		privateSize = value;
	}
	private ListModel privatePreset;
	public ListModel getPreset()
	{
		return privatePreset;
	}
	public void setPreset(ListModel value)
	{
		privatePreset = value;
	}
	private ListModel privateVolumeType;
	public ListModel getVolumeType()
	{
		return privateVolumeType;
	}
	public void setVolumeType(ListModel value)
	{
		privateVolumeType = value;
	}
	private ListModel privateInterface;
	public ListModel getInterface()
	{
		return privateInterface;
	}
	public void setInterface(ListModel value)
	{
		privateInterface = value;
	}
	private ListModel privateStorageDomain;
	public ListModel getStorageDomain()
	{
		return privateStorageDomain;
	}
	public void setStorageDomain(ListModel value)
	{
		privateStorageDomain = value;
	}
	private EntityModel privateWipeAfterDelete;
	public EntityModel getWipeAfterDelete()
	{
		return privateWipeAfterDelete;
	}
	public void setWipeAfterDelete(EntityModel value)
	{
		privateWipeAfterDelete = value;
	}
	private EntityModel privateIsBootable;
	public EntityModel getIsBootable()
	{
		return privateIsBootable;
	}
	public void setIsBootable(EntityModel value)
	{
		privateIsBootable = value;
	}


	public DiskModel()
	{
		setSize(new EntityModel());
		getSize().setIsValid(true);

		setInterface(new ListModel());
		setStorageDomain(new ListModel());

		setPreset(new ListModel());
		getPreset().getSelectedItemChangedEvent().addListener(this);

		setVolumeType(new ListModel());
		getVolumeType().setItems(DataProvider.GetVolumeTypeList());
		getVolumeType().getSelectedItemChangedEvent().addListener(this);

		setWipeAfterDelete(new EntityModel());
		getWipeAfterDelete().setEntity(false);
		getWipeAfterDelete().getEntityChangedEvent().addListener(this);

		setIsBootable(new EntityModel());
		getIsBootable().setEntity(false);

		AsyncDataProvider.GetDiskMaxSize(new AsyncQuery(this,
	            new INewAsyncCallback() {
	                @Override
	                public void OnSuccess(Object target, Object returnValue) {
	                    if (!DiskModel.maxDiskSizeInited){
	                        DiskModel.maxDiskSizeInited = true;
	                        DiskModel.maxDiskSize = ((Integer)returnValue);
	                    }
	                }
	            }));
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getWipeAfterDelete())
		{
			WipeAfterDelete_EntityChanged(args);
		}
		else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getPreset())
		{
			Preset_SelectedItemChanged();
		}
		else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getVolumeType())
		{
			VolumeType_SelectedItemChanged();
		}
	}

	private void Preset_SelectedItemChanged()
	{
		DiskImageBase preset = (DiskImageBase)getPreset().getSelectedItem();
		setVolumeFormat(preset.getvolume_format());
		getVolumeType().setSelectedItem(preset.getvolume_type());
	}

	private void VolumeType_SelectedItemChanged()
	{
		UpdateVolumeFormat();
	}

	private void UpdateVolumeFormat()
	{
		VolumeType volumeType = getVolumeType().getSelectedItem() == null ? org.ovirt.engine.core.common.businessentities.VolumeType.Unassigned : (VolumeType)getVolumeType().getSelectedItem();

		StorageType storageType = getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN : ((storage_domains)getStorageDomain().getSelectedItem()).getstorage_type();

		setVolumeFormat(DataProvider.GetDiskVolumeFormat(volumeType, storageType));
	}

	private void WipeAfterDelete_EntityChanged(EventArgs e)
	{
		if (!getWipeAfterDelete().getIsChangable() && (Boolean)getWipeAfterDelete().getEntity())
		{
			getWipeAfterDelete().setEntity(false);
		}
	}

	public boolean Validate()
	{
		IntegerValidation tempVar = new IntegerValidation();
		tempVar.setMinimum(1);
		tempVar.setMaximum(maxDiskSize);
		IntegerValidation intValidation = tempVar;
		getSize().ValidateEntity(new IValidation[] { new NotEmptyValidation(), intValidation });

		getStorageDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		return getSize().getIsValid() && getStorageDomain().getIsValid();
	}
}
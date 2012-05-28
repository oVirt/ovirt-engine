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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.queries.*;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class VmImportDiskListModel extends VmDiskListModel
{
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

	private int privateSelectedVMsCount;
	public int getSelectedVMsCount()
	{
		return privateSelectedVMsCount;
	}
	public void setSelectedVMsCount(int value)
	{
		privateSelectedVMsCount = value;
	}

	private void OnCollapseSnapshotsChanged()
	{
		SetDisksVolumeTypeAvailability();
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		VM vm = (VM)getEntity();
		if (vm != null && vm.getDiskMap() != null)
		{
			java.util.ArrayList<DiskModel> list = new java.util.ArrayList<DiskModel>();
			for (Disk disk : vm.getDiskMap().values())
			{
			    DiskImage img = (DiskImage)disk;
				DiskModel model = new DiskModel();
				model.setName(img.getDiskAlias());
				EntityModel tempVar = new EntityModel();
				tempVar.setEntity(img.getSizeInGigabytes());
				model.setSize(tempVar);
				model.getVolumeType().setSelectedItem(img.getvolume_type());
				model.getVolumeType().getSelectedItemChangedEvent().addListener(this);
				model.setVolumeFormat(img.getvolume_format());
				model.setCreationDate(img.getcreation_date());
				//NOTE: The following code won't pass conversion to Java.
				//model.ActualSize = Convert.ToInt32(img.ActualDiskWithSnapshotsSize);
				list.add(model);
			}
			setItems(list);

		}
		else
		{
			setItems(null);
		}

		SetDisksVolumeTypeAvailability();
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(SelectedItemChangedEventDefinition) && sender instanceof ListModel)
		{
			VolumeType_SelectedItemChanged(args);
		}
	}

	private void VolumeType_SelectedItemChanged(EventArgs e)
	{
		VM vm = (VM)getEntity();
		if (vm != null)
		{
			for (Object item : getItems())
			{
				DiskModel model = (DiskModel)item;

				for (java.util.Map.Entry<Guid, Disk> kvp : vm.getDiskMap().entrySet())
				{
					DiskImage disk = (DiskImage)kvp.getValue();
					ListModel volumeType = model.getVolumeType();
				}
			}
		}
	}

	private void SetDisksVolumeTypeAvailability()
	{
		java.util.ArrayList<DiskModel> list = (java.util.ArrayList<DiskModel>)getItems();
		if (list != null && list.size() > 0)
		{
			if (getSelectedVMsCount() == 1)
			{
				if (!getCollapseSnapshots())
				{
					for (DiskModel a : list)
					{
						a.getVolumeType().getChangeProhibitionReasons().add("Allocation can be modified only when 'Collapse Snapshots' is check");
						a.getVolumeType().setIsChangable(false);
					}
				}
				else
				{
					for (DiskModel a : list)
					{
						a.getVolumeType().setIsChangable(true);
					}
				}
			}
			else
			{
				for (DiskModel a : list)
				{
					a.getVolumeType().getChangeProhibitionReasons().add("Allocation can be modified only when importing a single VM");
					a.getVolumeType().setIsChangable(false);
				}
			}
		}
	}

	@Override
	public void Search()
	{
	}


}
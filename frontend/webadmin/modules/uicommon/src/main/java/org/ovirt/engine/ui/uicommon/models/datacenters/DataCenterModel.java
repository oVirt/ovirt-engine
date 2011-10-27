package org.ovirt.engine.ui.uicommon.models.datacenters;
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
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class DataCenterModel extends Model
{

	private NGuid privateDataCenterId;
	public NGuid getDataCenterId()
	{
		return privateDataCenterId;
	}
	public void setDataCenterId(NGuid value)
	{
		privateDataCenterId = value;
	}
	private boolean privateIsNew;
	public boolean getIsNew()
	{
		return privateIsNew;
	}
	public void setIsNew(boolean value)
	{
		privateIsNew = value;
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
	public void setName(EntityModel value)
	{
		privateName = value;
	}
	private EntityModel privateDescription;
	public EntityModel getDescription()
	{
		return privateDescription;
	}
	public void setDescription(EntityModel value)
	{
		privateDescription = value;
	}
	private ListModel privateStorageTypeList;
	public ListModel getStorageTypeList()
	{
		return privateStorageTypeList;
	}
	public void setStorageTypeList(ListModel value)
	{
		privateStorageTypeList = value;
	}
	private ListModel privateVersion;
	public ListModel getVersion()
	{
		return privateVersion;
	}
	public void setVersion(ListModel value)
	{
		privateVersion = value;
	}


	public DataCenterModel()
	{
		setName(new EntityModel());
		setDescription(new EntityModel());
		setVersion(new ListModel());

		setStorageTypeList(new ListModel());
		getStorageTypeList().getSelectedItemChangedEvent().addListener(this);
		getStorageTypeList().setItems(DataProvider.GetStoragePoolTypeList());
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getStorageTypeList())
		{
			StorageType_SelectedItemChanged();
		}
	}

	private void StorageType_SelectedItemChanged()
	{
		StorageType type = (StorageType)getStorageTypeList().getSelectedItem();

		//Rebuild version items.
		java.util.ArrayList<Version> list = new java.util.ArrayList<Version>();
		for (Version item : DataProvider.GetDataCenterVersions(getDataCenterId()))
		{
			if (DataProvider.IsVersionMatchStorageType(item, type))
			{
				list.add(item);
			}
		}

		if(type == StorageType.LOCALFS)
		{
			java.util.ArrayList<Version> tempList = new java.util.ArrayList<Version>();
			for (Version version : list)
			{
				Version version3_0 = new Version(3, 0);
				if(version.compareTo(version3_0) >= 0)
				{
					tempList.add(version);
				}
			}
			list = tempList;
		}

		Version selectedVersion = null;
		if (getVersion().getSelectedItem() != null)
		{
			selectedVersion = (Version)getVersion().getSelectedItem();
			boolean hasSelectedVersion = false;
			for (Version version : list)
			{
				if (selectedVersion.equals(version))
				{
					selectedVersion = version;
					hasSelectedVersion = true;
					break;
				}
			}
			if (!hasSelectedVersion)
			{
				selectedVersion = null;
			}
		}

		getVersion().setItems(list);

		if (selectedVersion == null)
		{
			getVersion().setSelectedItem(Linq.SelectHighestVersion(list));
		}
		else
		{
			getVersion().setSelectedItem(selectedVersion);
		}
	}

	public boolean Validate()
	{
		int nameMaxLength = DataProvider.GetDataCenterMaxNameLength();
		String nameRegex = StringFormat.format("^[A-Za-z0-9_-]{1,%1$s}$", nameMaxLength);
		String nameMessage = StringFormat.format("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters, max length: %1$s", nameMaxLength);

		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression(nameRegex);
		tempVar.setMessage(nameMessage);
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });
		getStorageTypeList().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		getVersion().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		String name = (String)getName().getEntity();

		if (name.compareToIgnoreCase(getOriginalName()) != 0 && !DataProvider.IsDataCenterNameUnique(name))
		{
			getName().setIsValid(false);
			getName().getInvalidityReasons().add("Name must be unique.");
		}


		return getName().getIsValid() && getDescription().getIsValid() && getStorageTypeList().getIsValid() && getVersion().getIsValid();
	}
}
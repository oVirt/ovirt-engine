package org.ovirt.engine.ui.uicommonweb.models.storage;
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

import org.ovirt.engine.ui.uicommonweb.models.events.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class StorageEventListModel extends EventListModel
{

		//			get { return base.Entity as storage_domains; }
	public storage_domains getEntity()
	{
		return (storage_domains)super.getEntity();
	}
	public void setEntity(storage_domains value)
	{
		super.setEntity(value);
	}


	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (getEntity() != null)
		{
			getSearchCommand().Execute();
		}
		else
		{
			setItems(null);
		}
	}

	@Override
	public void Search()
	{
		if (getEntity() != null)
		{
			setSearchString(StringFormat.format("events: event_storage=%1$s", getEntity().getstorage_name()));
			super.Search();
		}
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("storage_name"))
		{
			getSearchCommand().Execute();
		}
	}
}
package org.ovirt.engine.ui.uicommon.models.users;
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

import org.ovirt.engine.ui.uicommon.models.events.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class UserEventListModel extends EventListModel
{

	public DbUser getEntity()
	{
		return (DbUser)((super.getEntity() instanceof DbUser) ? super.getEntity() : null);
	}
	public void setEntity(DbUser value)
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
			setSearchString(StringFormat.format("events: usrname=%1$s", getEntity().getname()));
			super.Search();
		}
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("name"))
		{
			getSearchCommand().Execute();
		}
	}
}
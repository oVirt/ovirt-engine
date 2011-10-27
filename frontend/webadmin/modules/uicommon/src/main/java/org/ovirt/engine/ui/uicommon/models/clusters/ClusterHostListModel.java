package org.ovirt.engine.ui.uicommon.models.clusters;
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

import org.ovirt.engine.ui.uicommon.models.hosts.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ClusterHostListModel extends HostListModel
{

	public VDSGroup getEntity()
	{
		return (VDSGroup)((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
	}
	public void setEntity(VDSGroup value)
	{
		super.setEntity(value);
	}



	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		getSearchCommand().Execute();
	}

	@Override
	public void Search()
	{
		if (getEntity() != null)
		{
			setSearchString(StringFormat.format("hosts: cluster=%1$s", getEntity().getname()));
			super.Search();
		}
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch(VdcQueryType.Search, new SearchParameters(getSearchString(), SearchType.VDS));
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
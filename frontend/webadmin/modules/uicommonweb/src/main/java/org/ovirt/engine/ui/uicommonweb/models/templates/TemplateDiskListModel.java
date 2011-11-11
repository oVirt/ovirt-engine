package org.ovirt.engine.ui.uicommonweb.models.templates;
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

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class TemplateDiskListModel extends SearchableListModel
{

	private VmTemplate getEntityStronglyTyped()
	{
		return (VmTemplate)((super.getEntity() instanceof VmTemplate) ? super.getEntity() : null);
	}


	public TemplateDiskListModel()
	{
		setTitle("Virtual Disks");
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
		if (getEntityStronglyTyped() != null)
		{
			super.Search();
		}
	}

	@Override
	protected void SyncSearch()
	{
		if (getEntity() == null)
		{
			return;
		}

		super.SyncSearch(VdcQueryType.GetVmTemplatesDisks, new GetVmTemplatesDisksParameters(getEntityStronglyTyped().getId()));
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetVmTemplatesDisks, new GetVmTemplatesDisksParameters(getEntityStronglyTyped().getId())));
		setItems(getAsyncResult().getData());
	}
}
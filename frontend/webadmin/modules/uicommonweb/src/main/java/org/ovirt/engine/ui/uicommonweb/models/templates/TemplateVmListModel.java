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

import org.ovirt.engine.ui.uicommonweb.models.vms.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class TemplateVmListModel extends VmListModel
{

	public VmTemplate getEntity()
	{
		return (VmTemplate)((super.getEntity() instanceof VmTemplate) ? super.getEntity() : null);
	}
	public void setEntity(VmTemplate value)
	{
		super.setEntity(value);
	}



	public TemplateVmListModel()
	{
		setTitle("Virtual Machines");
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
			setSearchString(StringFormat.format("Vms: template.name=%1$s", getEntity().getname()));
			super.Search();
		}
	}
}
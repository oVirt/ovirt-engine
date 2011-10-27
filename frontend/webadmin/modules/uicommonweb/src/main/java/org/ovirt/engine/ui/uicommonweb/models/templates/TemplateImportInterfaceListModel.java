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
public class TemplateImportInterfaceListModel extends TemplateInterfaceListModel
{
	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (getEntity() != null)
		{
			java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> pair = (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)getEntity();
			VmTemplate template = pair.getKey();
			setItems(template.getInterfaces());
		}
		else
		{
			setItems(null);
		}
	}
}
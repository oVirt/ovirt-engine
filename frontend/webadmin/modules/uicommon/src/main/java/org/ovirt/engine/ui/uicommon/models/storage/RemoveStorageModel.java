package org.ovirt.engine.ui.uicommon.models.storage;
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


import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class RemoveStorageModel extends Model
{

	private ListModel privateHostList;
	public ListModel getHostList()
	{
		return privateHostList;
	}
	private void setHostList(ListModel value)
	{
		privateHostList = value;
	}
	private EntityModel privateFormat;
	public EntityModel getFormat()
	{
		return privateFormat;
	}
	private void setFormat(EntityModel value)
	{
		privateFormat = value;
	}


	public RemoveStorageModel()
	{
		setHostList(new ListModel());

		setFormat(new EntityModel());
		getFormat().setEntity(false);
	}

	public boolean Validate()
	{
		getHostList().setIsValid(getHostList().getSelectedItem() != null);

		return getHostList().getIsValid();
	}
}
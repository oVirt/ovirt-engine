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

import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ExportVmModel extends Model
{

	private ListModel privateStorage;
	public ListModel getStorage()
	{
		return privateStorage;
	}
	private void setStorage(ListModel value)
	{
		privateStorage = value;
	}
	private EntityModel privateCollapseSnapshots;
	public EntityModel getCollapseSnapshots()
	{
		return privateCollapseSnapshots;
	}
	private void setCollapseSnapshots(EntityModel value)
	{
		privateCollapseSnapshots = value;
	}
	private EntityModel privateForceOverride;
	public EntityModel getForceOverride()
	{
		return privateForceOverride;
	}
	private void setForceOverride(EntityModel value)
	{
		privateForceOverride = value;
	}


	public ExportVmModel()
	{
		setStorage(new ListModel());

		setCollapseSnapshots(new EntityModel());
		getCollapseSnapshots().setEntity(false);

		setForceOverride(new EntityModel());
		getForceOverride().setEntity(false);
	}

	public boolean Validate()
	{
		getStorage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		return getStorage().getIsValid();
	}
}
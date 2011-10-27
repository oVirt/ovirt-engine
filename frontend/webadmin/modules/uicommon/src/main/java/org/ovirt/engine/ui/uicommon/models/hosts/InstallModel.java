package org.ovirt.engine.ui.uicommon.models.hosts;
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
public class InstallModel extends Model
{

	private EntityModel privateRootPassword;
	public EntityModel getRootPassword()
	{
		return privateRootPassword;
	}
	private void setRootPassword(EntityModel value)
	{
		privateRootPassword = value;
	}
	private ListModel privateoVirtISO;
	public ListModel getoVirtISO()
	{
		return privateoVirtISO;
	}
	private void setoVirtISO(ListModel value)
	{
		privateoVirtISO = value;
	}
	private EntityModel privateOverrideIpTables;
	public EntityModel getOverrideIpTables()
	{
		return privateOverrideIpTables;
	}
	private void setOverrideIpTables(EntityModel value)
	{
		privateOverrideIpTables = value;
	}


	public InstallModel()
	{
		setRootPassword(new EntityModel());
		setoVirtISO(new ListModel());
		EntityModel tempVar = new EntityModel();
		tempVar.setEntity(false);
		setOverrideIpTables(tempVar);
	}

	public boolean Validate(boolean isOVirt)
	{
		getoVirtISO().setIsValid(true);
		getRootPassword().setIsValid(true);

		if (isOVirt)
		{
			getoVirtISO().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		}
		else
		{
			getRootPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
		}

		return getRootPassword().getIsValid() && getoVirtISO().getIsValid();
	}
}
package org.ovirt.engine.ui.uicommonweb.models.hosts;
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

import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

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
	private ListModel privateOVirtISO;
	public ListModel getOVirtISO()
	{
		return privateOVirtISO;
	}
	private void setOVirtISO(ListModel value)
	{
		privateOVirtISO = value;
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


    public InstallModel() {
        setRootPassword(new EntityModel());
        setOVirtISO(new ListModel());

        setOverrideIpTables(new EntityModel());
        getOverrideIpTables().setEntity(false);
    }

	public boolean Validate(boolean isOVirt)
	{
		getOVirtISO().setIsValid(true);
		getRootPassword().setIsValid(true);

		if (isOVirt)
		{
			getOVirtISO().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		}
		else
		{
			getRootPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
		}

		return getRootPassword().getIsValid() && getOVirtISO().getIsValid();
	}
}
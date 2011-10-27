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

import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class LocalStorageModel extends Model implements IStorageModel
{

	private UICommand privateUpdateCommand;
	public UICommand getUpdateCommand()
	{
		return privateUpdateCommand;
	}
	private void setUpdateCommand(UICommand value)
	{
		privateUpdateCommand = value;
	}



	private StorageModel privateContainer;
	public StorageModel getContainer()
	{
		return privateContainer;
	}
	public void setContainer(StorageModel value)
	{
		privateContainer = value;
	}
	private StorageDomainType privateRole = StorageDomainType.values()[0];
	public StorageDomainType getRole()
	{
		return privateRole;
	}
	public void setRole(StorageDomainType value)
	{
		privateRole = value;
	}

	private EntityModel privatePath;
	public EntityModel getPath()
	{
		return privatePath;
	}
	public void setPath(EntityModel value)
	{
		privatePath = value;
	}


	public LocalStorageModel()
	{
		setUpdateCommand(new UICommand("Update", this));

		setPath(new EntityModel());
	}

	public boolean Validate()
	{
		getPath().ValidateEntity(new NotEmptyValidation[] { new NotEmptyValidation() });

		return getPath().getIsValid();
	}

	public StorageType getType()
	{
		return StorageType.LOCALFS;
	}
}
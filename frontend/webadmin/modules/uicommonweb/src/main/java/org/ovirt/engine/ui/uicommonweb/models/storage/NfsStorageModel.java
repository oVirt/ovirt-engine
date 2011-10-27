package org.ovirt.engine.ui.uicommonweb.models.storage;
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
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class NfsStorageModel extends Model implements IStorageModel
{

	public static EventDefinition PathChangedEventDefinition;
	private Event privatePathChangedEvent;
	public Event getPathChangedEvent()
	{
		return privatePathChangedEvent;
	}
	private void setPathChangedEvent(Event value)
	{
		privatePathChangedEvent = value;
	}



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


	static
	{
		PathChangedEventDefinition = new EventDefinition("PathChanged", NfsStorageModel.class);
	}

	public NfsStorageModel()
	{
		setPathChangedEvent(new Event(PathChangedEventDefinition));

		setUpdateCommand(new UICommand("Update", this));

		setPath(new EntityModel());
		getPath().getEntityChangedEvent().addListener(this);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getPath())
		{
			Path_EntityChanged();
		}
	}

	private void Path_EntityChanged()
	{
		getPathChangedEvent().raise(this, EventArgs.Empty);
	}

	public boolean Validate()
	{
		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression(DataProvider.GetLinuxMountPointRegex());
		tempVar.setMessage("NFS mount path is illegal, please use [IP:/path or FQDN:/path] convention.");
		getPath().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });


		return getPath().getIsValid();
	}

	public StorageType getType()
	{
		return StorageType.NFS;
	}
}
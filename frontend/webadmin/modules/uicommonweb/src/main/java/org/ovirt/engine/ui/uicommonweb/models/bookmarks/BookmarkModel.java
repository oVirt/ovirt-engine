package org.ovirt.engine.ui.uicommonweb.models.bookmarks;
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

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class BookmarkModel extends Model
{

	private boolean privateIsNew;
	public boolean getIsNew()
	{
		return privateIsNew;
	}
	public void setIsNew(boolean value)
	{
		privateIsNew = value;
	}

	private EntityModel privateName;
	public EntityModel getName()
	{
		return privateName;
	}
	public void setName(EntityModel value)
	{
		privateName = value;
	}
	private EntityModel privateSearchString;
	public EntityModel getSearchString()
	{
		return privateSearchString;
	}
	public void setSearchString(EntityModel value)
	{
		privateSearchString = value;
	}


	public BookmarkModel()
	{
		setName(new EntityModel());
		setSearchString(new EntityModel());
	}

	public boolean Validate()
	{
		LengthValidation tempVar = new LengthValidation();
		tempVar.setMaxLength(40);
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

		getSearchString().ValidateEntity(new IValidation[] { new NotEmptyValidation() });

		return getName().getIsValid() && getSearchString().getIsValid();
	}
}
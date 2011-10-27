package org.ovirt.engine.ui.uicommonweb.models.users;
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

import org.ovirt.engine.ui.uicommonweb.models.tags.*;
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class UserPermissionModel extends Model
{

	private Guid privateId = new Guid();
	public Guid getId()
	{
		return privateId;
	}
	public void setId(Guid value)
	{
		privateId = value;
	}

	private ListModel privateRole;
	public ListModel getRole()
	{
		return privateRole;
	}
	public void setRole(ListModel value)
	{
		privateRole = value;
	}

	private java.util.List<TagModel> tags;
	public java.util.List<TagModel> getTags()
	{
		return tags;
	}
	public void setTags(java.util.List<TagModel> value)
	{
		if (tags != value)
		{
			tags = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Tags"));
		}
	}


	public UserPermissionModel()
	{
		setRole(new ListModel());
	}

	public boolean Validate()
	{
		getRole().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		return getRole().getIsValid();
	}
}
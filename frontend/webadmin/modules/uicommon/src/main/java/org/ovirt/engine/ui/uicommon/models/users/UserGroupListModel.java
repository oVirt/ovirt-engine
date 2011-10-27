package org.ovirt.engine.ui.uicommon.models.users;
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

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class UserGroupListModel extends ListModel
{

	public DbUser getEntity()
	{
		return (DbUser)((super.getEntity() instanceof DbUser) ? super.getEntity() : null);
	}
	public void setEntity(DbUser value)
	{
		super.setEntity(value);
	}


	public UserGroupListModel()
	{
		setTitle("Directory Groups");
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (getEntity() != null)
		{
			java.util.ArrayList<UserGroup> items = new java.util.ArrayList<UserGroup>();
			for (String groupFullName : getEntity().getgroups().split("[,]", -1))
			{
				items.add(CreateUserGroup(groupFullName));
			}

			setItems(items);
		}
		else
		{
			setItems(null);
		}
	}

	private static UserGroup CreateUserGroup(String groupFullName)
	{
		// Parse 'groupFullName' (representation: Domain/OrganizationalUnit/Group)
		int firstIndexOfSlash = groupFullName.indexOf('/');
		int lastIndexOfSlash = groupFullName.lastIndexOf('/');
		String domain = firstIndexOfSlash >= 0 ? groupFullName.substring(0, firstIndexOfSlash) : "";
		String groupName = lastIndexOfSlash >= 0 ? groupFullName.substring(lastIndexOfSlash+1) : "";
		String organizationalUnit = lastIndexOfSlash > firstIndexOfSlash ? groupFullName.substring(0, lastIndexOfSlash).substring(firstIndexOfSlash+1) : "";

		UserGroup tempVar = new UserGroup();
		tempVar.setGroupName(groupName);
		tempVar.setOrganizationalUnit(organizationalUnit);
		tempVar.setDomain(domain);
		return tempVar;
	}

	private static class UserGroup
	{
		private String privateGroupName;
		public String getGroupName()
		{
			return privateGroupName;
		}
		public void setGroupName(String value)
		{
			privateGroupName = value;
		}
		private String privateOrganizationalUnit;
		public String getOrganizationalUnit()
		{
			return privateOrganizationalUnit;
		}
		public void setOrganizationalUnit(String value)
		{
			privateOrganizationalUnit = value;
		}
		private String privateDomain;
		public String getDomain()
		{
			return privateDomain;
		}
		public void setDomain(String value)
		{
			privateDomain = value;
		}
	}
}
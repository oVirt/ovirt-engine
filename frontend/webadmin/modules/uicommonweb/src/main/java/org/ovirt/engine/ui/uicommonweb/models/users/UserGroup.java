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

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class UserGroup
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
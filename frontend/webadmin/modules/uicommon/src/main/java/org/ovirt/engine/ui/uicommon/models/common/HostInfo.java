package org.ovirt.engine.ui.uicommon.models.common;
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

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class HostInfo
{
	private String privateHostName;
	public String getHostName()
	{
		return privateHostName;
	}
	public void setHostName(String value)
	{
		privateHostName = value;
	}
	private String privateOSVersion;
	public String getOSVersion()
	{
		return privateOSVersion;
	}
	public void setOSVersion(String value)
	{
		privateOSVersion = value;
	}
	private String privateVDSMVersion;
	public String getVDSMVersion()
	{
		return privateVDSMVersion;
	}
	public void setVDSMVersion(String value)
	{
		privateVDSMVersion = value;
	}
}
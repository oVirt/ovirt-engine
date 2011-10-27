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

import org.ovirt.engine.ui.uicommonweb.models.clusters.*;
import org.ovirt.engine.ui.uicommonweb.models.common.*;
import org.ovirt.engine.ui.uicommonweb.models.configure.*;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.*;
import org.ovirt.engine.ui.uicommonweb.models.tags.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class DataBag
{
	private Guid privateDataCenterId;
	public Guid getDataCenterId()
	{
		return privateDataCenterId;
	}
	public void setDataCenterId(Guid value)
	{
		privateDataCenterId = value;
	}
	private Guid privateClusterId;
	public Guid getClusterId()
	{
		return privateClusterId;
	}
	public void setClusterId(Guid value)
	{
		privateClusterId = value;
	}
	private Guid privateOldClusterId;
	public Guid getOldClusterId()
	{
		return privateOldClusterId;
	}
	public void setOldClusterId(Guid value)
	{
		privateOldClusterId = value;
	}
}
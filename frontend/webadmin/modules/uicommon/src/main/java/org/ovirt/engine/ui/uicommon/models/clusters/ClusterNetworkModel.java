package org.ovirt.engine.ui.uicommon.models.clusters;
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

import org.ovirt.engine.ui.uicommon.models.datacenters.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ClusterNetworkModel extends NetworkModel
{
	private String dataCenterName;
	public String getDataCenterName()
	{
		return dataCenterName;
	}
	public void setDataCenterName(String value)
	{
		if (!StringHelper.stringsEqual(dataCenterName, value))
		{
			dataCenterName = value;
			OnPropertyChanged(new PropertyChangedEventArgs("DataCenterName"));
		}
	}
}
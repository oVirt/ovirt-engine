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
public abstract class BaseRM implements IEnlistmentNotification
{
	private HostListModel privateModel;
	protected HostListModel getModel()
	{
		return privateModel;
	}
	private void setModel(HostListModel value)
	{
		privateModel = value;
	}
	private DataBag privateData;
	protected DataBag getData()
	{
		return privateData;
	}
	private void setData(DataBag value)
	{
		privateData = value;
	}

	protected BaseRM(HostListModel model, DataBag data)
	{
		setModel(model);
		setData(data);
	}

	public abstract void Prepare(PreparingEnlistment preparingEnlistment) throws TransactionAbortedException;
	public abstract void Commit(Enlistment enlistment);
	public abstract void Rollback(Enlistment enlistment);
	public abstract void InDoubt(Enlistment enlistment);
}
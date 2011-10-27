package org.ovirt.engine.ui.uicommon.models.vms;
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

import org.ovirt.engine.ui.uicommon.dataprovider.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class VmMonitorModel extends EntityModel
{

	private UICommand privateRefreshCommand;
	public UICommand getRefreshCommand()
	{
		return privateRefreshCommand;
	}
	private void setRefreshCommand(UICommand value)
	{
		privateRefreshCommand = value;
	}



	private int cpuUsage;
	public int getCpuUsage()
	{
		return cpuUsage;
	}
	public void setCpuUsage(int value)
	{
		if (cpuUsage != value)
		{
			cpuUsage = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CpuUsage"));
		}
	}

	private int memoryUsage;
	public int getMemoryUsage()
	{
		return memoryUsage;
	}
	public void setMemoryUsage(int value)
	{
		if (memoryUsage != value)
		{
			memoryUsage = value;
			OnPropertyChanged(new PropertyChangedEventArgs("MemoryUsage"));
		}
	}

	private int networkUsage;
	public int getNetworkUsage()
	{
		return networkUsage;
	}
	public void setNetworkUsage(int value)
	{
		if (networkUsage != value)
		{
			networkUsage = value;
			OnPropertyChanged(new PropertyChangedEventArgs("NetworkUsage"));
		}
	}


	public VmMonitorModel()
	{
		setTitle("Monitor");

		setRefreshCommand(new UICommand("Refresh", this));
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getRefreshCommand())
		{
			Refresh();
		}
	}

	private void Refresh()
	{
		if (getEntity() == null)
		{
			return;
		}

		VM vm = (VM)getEntity();
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
		{
			VM vm1 = (VM)result;
			VmMonitorModel vmMonitorModel = (VmMonitorModel) model;
			vmMonitorModel.setCpuUsage((vm1.getusage_cpu_percent() == null ? 0 : vm1.getusage_cpu_percent()));
			vmMonitorModel.setMemoryUsage((vm1.getusage_mem_percent() == null ? 0 : vm1.getusage_mem_percent()));
			vmMonitorModel.setNetworkUsage((vm1.getusage_network_percent() == null ? 0 : vm1.getusage_network_percent()));
		}};
		AsyncDataProvider.GetVmById(_asyncQuery, vm.getvm_guid());
	}
}
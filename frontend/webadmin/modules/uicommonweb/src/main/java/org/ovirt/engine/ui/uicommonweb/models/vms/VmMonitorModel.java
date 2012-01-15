package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

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

        VM vm = (VM) getEntity();
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                VM vm1 = (VM) result;
                VmMonitorModel vmMonitorModel = (VmMonitorModel) model;
                vmMonitorModel.setCpuUsage((vm1.getusage_cpu_percent() == null ? 0 : vm1.getusage_cpu_percent()));
                vmMonitorModel.setMemoryUsage((vm1.getusage_mem_percent() == null ? 0 : vm1.getusage_mem_percent()));
                vmMonitorModel.setNetworkUsage((vm1.getusage_network_percent() == null ? 0
                        : vm1.getusage_network_percent()));
            }
        };
        AsyncDataProvider.GetVmById(_asyncQuery, vm.getvm_guid());
    }
}

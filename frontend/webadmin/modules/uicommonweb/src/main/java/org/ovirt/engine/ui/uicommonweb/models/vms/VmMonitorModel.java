package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GridTimer;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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
            OnPropertyChanged(new PropertyChangedEventArgs("CpuUsage")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("MemoryUsage")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("NetworkUsage")); //$NON-NLS-1$
        }
    }

    private GridTimer refreshTimer;

    private GridTimer getRefreshTimer()
    {
        if (refreshTimer == null)
        {
            refreshTimer = new GridTimer("VmMonitorModel") { //$NON-NLS-1$
                        @Override
                        public void execute() {
                            Refresh();
                        }
                    };
            refreshTimer.setRefreshRate(1000);
        }

        return refreshTimer;
    }

    public VmMonitorModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().monitorTitle());
        setHashName("monitor"); //$NON-NLS-1$

        setRefreshCommand(new UICommand("Refresh", this)); //$NON-NLS-1$
    }

    @Override
    public void setEntity(Object value)
    {
        super.setEntity(value);

        if (value != null)
        {
            getRefreshTimer().start();
        } else {
            getRefreshTimer().stop();
        }
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
            public void onSuccess(Object model, Object result)
            {
                VM vm1 = (VM) result;
                VmMonitorModel vmMonitorModel = (VmMonitorModel) model;
                vmMonitorModel.setCpuUsage((vm1.getUsageCpuPercent() == null ? 0 : vm1.getUsageCpuPercent()));
                vmMonitorModel.setMemoryUsage((vm1.getUsageMemPercent() == null ? 0 : vm1.getUsageMemPercent()));
                vmMonitorModel.setNetworkUsage((vm1.getUsageNetworkPercent() == null ? 0
                        : vm1.getUsageNetworkPercent()));
            }
        };
        AsyncDataProvider.GetVmById(_asyncQuery, vm.getId());
    }
}

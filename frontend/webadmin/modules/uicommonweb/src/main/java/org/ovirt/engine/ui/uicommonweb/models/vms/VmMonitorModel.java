package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GridTimer;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class VmMonitorModel extends EntityModel {

    private UICommand privateRefreshCommand;

    public UICommand getRefreshCommand() {
        return privateRefreshCommand;
    }

    private void setRefreshCommand(UICommand value) {
        privateRefreshCommand = value;
    }

    private int cpuUsage;

    public int getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(int value) {
        if (cpuUsage != value) {
            cpuUsage = value;
            onPropertyChanged(new PropertyChangedEventArgs("CpuUsage")); //$NON-NLS-1$
        }
    }

    private int memoryUsage;

    public int getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(int value) {
        if (memoryUsage != value) {
            memoryUsage = value;
            onPropertyChanged(new PropertyChangedEventArgs("MemoryUsage")); //$NON-NLS-1$
        }
    }

    private int networkUsage;

    public int getNetworkUsage() {
        return networkUsage;
    }

    public void setNetworkUsage(int value) {
        if (networkUsage != value) {
            networkUsage = value;
            onPropertyChanged(new PropertyChangedEventArgs("NetworkUsage")); //$NON-NLS-1$
        }
    }

    private GridTimer refreshTimer;

    private GridTimer getRefreshTimer() {
        if (refreshTimer == null) {
            refreshTimer = new GridTimer("VmMonitorModel", getEventBus()) { //$NON-NLS-1$
                        @Override
                        public void execute() {
                            refresh();
                        }
                    };
            refreshTimer.setRefreshRate(1000);
        }

        return refreshTimer;
    }

    public VmMonitorModel() {
        setTitle(ConstantsManager.getInstance().getConstants().monitorTitle());
        setHelpTag(HelpTag.monitor);
        setHashName("monitor"); //$NON-NLS-1$

        setRefreshCommand(new UICommand("Refresh", this)); //$NON-NLS-1$
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value);

        if (value != null) {
            getRefreshTimer().start();
        } else {
            getRefreshTimer().stop();
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRefreshCommand()) {
            refresh();
        }
    }

    private void refresh() {
        if (getEntity() == null) {
            return;
        }

        VM vm = (VM) getEntity();
        AsyncDataProvider.getInstance().getVmById(new AsyncQuery<>(vm1 -> {
            setCpuUsage(vm1.getUsageCpuPercent() == null ? 0 : vm1.getUsageCpuPercent());
            setMemoryUsage(vm1.getUsageMemPercent() == null ? 0 : vm1.getUsageMemPercent());
            setNetworkUsage(vm1.getUsageNetworkPercent() == null ? 0
                    : vm1.getUsageNetworkPercent());
        }), vm.getId());
    }
}

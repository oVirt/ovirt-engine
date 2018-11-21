package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;

import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class HostMaintenanceConfirmationModel extends ConfirmationModel {

    private EntityModel<Boolean> stopGlusterServices;
    private boolean pinnedVMsInfoPanelVisible;
    private String pinnedVMsInfoMessage;

    public EntityModel<Boolean> getStopGlusterServices() {
        return stopGlusterServices;
    }

    public void setStopGlusterServices(EntityModel<Boolean> stopGlusterServices) {
        this.stopGlusterServices = stopGlusterServices;
    }

    public boolean getPinnedVMsInfoPanelVisible() {
        return pinnedVMsInfoPanelVisible;
    }

    public void setPinnedVMsInfoPanelVisible(boolean value) {
        if (pinnedVMsInfoPanelVisible != value) {
            pinnedVMsInfoPanelVisible = value;
            onPropertyChanged(new PropertyChangedEventArgs("pinnedVMsInfoPanelVisible")); //$NON-NLS-1$
        }

    }

    public String getPinnedVMsInfoMessage() {
        return pinnedVMsInfoMessage;
    }

    public void setPinnedVMsInfoMessage(String pinnedVMsInfoMessage) {
        if (!Objects.equals(getPinnedVMsInfoMessage(), pinnedVMsInfoMessage)) {
            this.pinnedVMsInfoMessage = pinnedVMsInfoMessage;
            onPropertyChanged(new PropertyChangedEventArgs("pinnedVMsInfoMessage")); //$NON-NLS-1$
        }
    }

    public HostMaintenanceConfirmationModel() {
        setStopGlusterServices(new EntityModel<Boolean>());
        getStopGlusterServices().setEntity(false);
        getStopGlusterServices().setIsAvailable(false);

        pinnedVMsInfoPanelVisible = false;
    }
}

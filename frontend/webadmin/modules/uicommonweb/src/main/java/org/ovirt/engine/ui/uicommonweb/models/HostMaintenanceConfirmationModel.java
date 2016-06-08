package org.ovirt.engine.ui.uicommonweb.models;

public class HostMaintenanceConfirmationModel extends ConfirmationModel {

    private EntityModel<Boolean> stopGlusterServices;

    public EntityModel<Boolean> getStopGlusterServices() {
        return stopGlusterServices;
    }

    public void setStopGlusterServices(EntityModel<Boolean> stopGlusterServices) {
        this.stopGlusterServices = stopGlusterServices;
    }

    public HostMaintenanceConfirmationModel() {
        setStopGlusterServices(new EntityModel<Boolean>());
        getStopGlusterServices().setEntity(false);
        getStopGlusterServices().setIsAvailable(false);
    }
}

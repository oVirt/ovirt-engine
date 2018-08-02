package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;


public class GlusterVolumeGeoRepActionConfirmationModel extends Model {
    private EntityModel<String> masterVolume;
    private EntityModel<String> slaveVolume;
    private EntityModel<String> slaveHost;
    private EntityModel<Boolean> force;
    private String forceLabel;
    private String forceHelp;
    private String message;

    public GlusterVolumeGeoRepActionConfirmationModel() {
        init();
    }

    private void init() {
        setMasterVolume(new EntityModel<String>());
        setSlaveVolume(new EntityModel<String>());
        setSlaveHost(new EntityModel<String>());
        setForce(new EntityModel<Boolean>());

        force.setIsAvailable(false);
    }

    protected void initWindow(String masterVolume, String slaveVolume, String slaveHost) {
        getForce().setEntity(false);
        getMasterVolume().setEntity(masterVolume);
        getSlaveVolume().setEntity(slaveVolume);
        getSlaveHost().setEntity(slaveHost);
    }

    public EntityModel<String> getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(EntityModel<String> masterVolume) {
        this.masterVolume = masterVolume;
    }

    public EntityModel<String> getSlaveVolume() {
        return slaveVolume;
    }

    public void setSlaveVolume(EntityModel<String> slaveVolume) {
        this.slaveVolume = slaveVolume;
    }

    public EntityModel<String> getSlaveHost() {
        return slaveHost;
    }

    public void setSlaveHost(EntityModel<String> slaveHost) {
        this.slaveHost = slaveHost;
    }

    public EntityModel<Boolean> getForce() {
        return force;
    }

    public void setForce(EntityModel<Boolean> force) {
        this.force = force;
    }

    public String getForceLabel() {
        return forceLabel;
    }

    public String getForceHelp() {
        return forceHelp;
    }

    public void setForceHelp(String forceHelp) {
        this.forceHelp = forceHelp;
        onPropertyChanged(new PropertyChangedEventArgs("forceHelp"));//$NON-NLS-1$
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setActionConfirmationMessage(String message) {
        this.message = message;
        onPropertyChanged(new PropertyChangedEventArgs("ActionConfirmationMessage"));//$NON-NLS-1$
    }

    public void setForceLabel(String forceLabel) {
        this.forceLabel = forceLabel;
        onPropertyChanged(new PropertyChangedEventArgs("forceLabel"));//$NON-NLS-1$
    }

    public static void callInitWindow(GlusterVolumeGeoRepActionConfirmationModel cmodel,
            String masterVolume,
            String slaveVolume,
            String slaveHost) {
        cmodel.initWindow(masterVolume, slaveVolume, slaveHost);
    }
}

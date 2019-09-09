package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class HostRestartConfirmationModel extends ConfirmationModel {
    private EntityModel<Boolean> forceToMaintenance;

    public EntityModel<Boolean> getForceToMaintenance() {
        return forceToMaintenance;
    }

    public void setForceToMaintenance(EntityModel<Boolean> forceToMaintenance) {
        this.forceToMaintenance = forceToMaintenance;
    }

    public HostRestartConfirmationModel() {
        super();
        setForceToMaintenance(new EntityModel<>());
        getForceToMaintenance().setEntity(false);
    }

}

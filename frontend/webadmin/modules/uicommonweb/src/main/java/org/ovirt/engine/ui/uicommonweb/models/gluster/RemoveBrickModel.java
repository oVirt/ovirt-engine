package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class RemoveBrickModel extends ConfirmationModel {
    private int replicaCount;

    private int stripeCount;

    private boolean isReduceReplica;

    private String validationMessage;

    private EntityModel<Boolean> migrateData;

    private boolean isMigrationSupported;

    public RemoveBrickModel() {
        setReduceReplica(false);
        setMigrateData(new EntityModel<>(false));
        setMigrationSupported(true);
    }

    public int getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(int replicaCount) {
        this.replicaCount = replicaCount;
    }

    public int getStripeCount() {
        return stripeCount;
    }

    public void setStripeCount(int stripeCount) {
        this.stripeCount = stripeCount;
    }

    public boolean isReduceReplica() {
        return isReduceReplica;
    }

    public void setReduceReplica(boolean isReduceReplica) {
        this.isReduceReplica = isReduceReplica;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public EntityModel<Boolean> getMigrateData() {
        return migrateData;
    }

    public void setMigrateData(EntityModel<Boolean> migrateData) {
        this.migrateData = migrateData;
    }

    public boolean isMigrationSupported() {
        return isMigrationSupported;
    }

    public void setMigrationSupported(boolean isMigrationSupported) {
        if (this.isMigrationSupported != isMigrationSupported) {
            this.isMigrationSupported = isMigrationSupported;
            onPropertyChanged(new PropertyChangedEventArgs("IsMigrationSupported")); //$NON-NLS-1$
        }
    }
}

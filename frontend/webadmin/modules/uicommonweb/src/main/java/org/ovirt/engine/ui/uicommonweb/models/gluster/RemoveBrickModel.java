package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.Model;

public class RemoveBrickModel extends Model {
    int replicaCount;
    int stripeCount;
    boolean isReduceReplica;
    String validationMessage;

    public RemoveBrickModel() {
        setReduceReplica(false);
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

}

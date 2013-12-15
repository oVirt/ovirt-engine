package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class NetworkQosValidator {

    private final NetworkQoS qos;
    private NetworkQoS oldQos;
    private List<NetworkQoS> allQos;

    public NetworkQosValidator(NetworkQoS qos) {
        this.qos = qos;
    }

    protected NetworkQoS getOldQos() {
        if (oldQos == null) {
            oldQos = DbFacade.getInstance().getQosDao().get(qos.getId());
        }
        return oldQos;
    }

    protected List<NetworkQoS> getAllQosInDc() {
        if (allQos == null) {
            allQos = DbFacade.getInstance().getQosDao().getAllForStoragePoolId(qos.getStoragePoolId());
        }
        return allQos;
    }

    /**
     * Verify that the QoS entity had previously existed in the database.
     */
    public ValidationResult qosExists() {
        return (qos != null && getOldQos() == null)
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_NOT_FOUND)
                : ValidationResult.VALID;
    }

    /**
     * Verify that the QoS entity has the same DC ID as the one stored in the database.
     */
    public ValidationResult consistentDataCenter() {
        return (qos != null && (getOldQos() == null || !qos.getStoragePoolId().equals(getOldQos().getStoragePoolId())))
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_INVALID_DC_ID)
                : ValidationResult.VALID;
    }

    /**
     * Verify that a name isn't already taken by another QoS entity in the same DC.
     */
    public ValidationResult nameNotTakenInDc() {
        if (getAllQosInDc() != null) {
            for (NetworkQoS networkQoS : getAllQosInDc()) {
                if (networkQoS.getName().equals(qos.getName())) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_NAME_EXIST);
                }
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * Verify that a QoS entity's name hasn't changed. This assumes that QoS entity has been verified to exist.
     */
    public ValidationResult nameNotChangedOrNotTaken() {
        if (!getOldQos().getName().equals(qos.getName())) {
            return nameNotTakenInDc();
        } else {
            return ValidationResult.VALID;
        }
    }

}

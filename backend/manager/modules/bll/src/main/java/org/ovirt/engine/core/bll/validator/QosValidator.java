package org.ovirt.engine.core.bll.validator;

import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.qos.QosDao;

public abstract class QosValidator<T extends QosBase> {

    private final T qos;
    private T oldQos;
    private List<T> allQos;

    public QosValidator(T qos) {
        this.qos = qos;
    }

    protected T getQos() {
        return qos;
    }

    protected T getOldQos() {
        if (oldQos == null && qos != null) {
            oldQos = getQosDao().get(qos.getId());
        }
        return oldQos;
    }

    protected abstract QosDao<T> getQosDao();

    protected List<T> getAllQosInDcByType() {
        if (allQos == null) {
            allQos = getQosDao().getAllForStoragePoolId(qos.getStoragePoolId());
        }
        return allQos;
    }

    /**
     * Verify that the QoS entity had previously existed in the database.
     */
    public ValidationResult qosExists() {
        return (qos != null && getOldQos() == null)
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QOS_NOT_FOUND)
                : ValidationResult.VALID;
    }

    /**
     * Verify that the QoS entity has the same DC ID as the one stored in the database.
     */
    public ValidationResult consistentDataCenter() {
        return (qos != null && (getOldQos() == null || !qos.getStoragePoolId().equals(getOldQos().getStoragePoolId())))
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QOS_STORAGE_POOL_NOT_CONSISTENT)
                : ValidationResult.VALID;
    }

    /**
     * Verify that a name isn't already taken by another QoS entity in the same DC.
     */
    public ValidationResult nameNotTakenInDc() {
        List<T> allQosInDcByType = getAllQosInDcByType();
        if (allQosInDcByType != null) {
            for (T iterQos : allQosInDcByType) {
                if (Objects.equals(iterQos.getName(), qos.getName())) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QOS_NAME_EXIST);
                }
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * Verify that a QoS entity's name hasn't changed. This assumes that QoS entity has been verified to exist.
     */
    public ValidationResult nameNotChangedOrNotTaken() {
        if (!getOldQos().getName().equals(getQos().getName())) {
            return nameNotTakenInDc();
        } else {
            return ValidationResult.VALID;
        }
    }

    /**
     * Verify that if any capping was specified, that all required parameters are present.
     */
    public abstract ValidationResult requiredValuesPresent();
}

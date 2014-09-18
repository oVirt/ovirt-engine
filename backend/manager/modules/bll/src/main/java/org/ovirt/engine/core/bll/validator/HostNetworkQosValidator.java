package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.qos.QosDao;

public class HostNetworkQosValidator extends QosValidator<HostNetworkQos> {

    public HostNetworkQosValidator(HostNetworkQos qos) {
        super(qos);
    }

    @Override
    public ValidationResult requiredValuesPresent() {
        return (getQos() != null && getQos().getOutAverageLinkshare() == null)
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_MISSING_VALUES)
                : ValidationResult.VALID;
    }

    /**
     * Verify that if upper limit and real time rates are provided, real time isn't lower than upper limit.
     */
    public ValidationResult valuesConsistent() {
        HostNetworkQos qos = getQos();
        if (qos == null) {
            return ValidationResult.VALID;
        }

        Integer outUpperlimit = qos.getOutAverageUpperlimit();
        Integer outRealtime = qos.getOutAverageRealtime();
        return (outUpperlimit != null && outRealtime != null && outUpperlimit < outRealtime)
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INCONSISTENT_VALUES)
                : ValidationResult.VALID;
    }

    @Override
    protected QosDao<HostNetworkQos> getQosDao() {
        return DbFacade.getInstance().getHostNetworkQosDao();
    }

}

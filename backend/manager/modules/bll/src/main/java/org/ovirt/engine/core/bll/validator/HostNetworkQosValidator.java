package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.qos.QosDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class HostNetworkQosValidator extends QosValidator<HostNetworkQos> {

    public HostNetworkQosValidator(HostNetworkQos qos) {
        super(qos);
    }

    @Override
    public ValidationResult requiredValuesPresent() {
        /*
        only getOutAverageLinkshare is mandatory, getOutAverageUpperlimit(), getOutAverageRealtime() are not.
        * */
        HostNetworkQos qos = getQos();
        boolean shouldFail = qos != null && qos.getOutAverageLinkshare() == null;
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_MISSING_VALUES).when(
            shouldFail);
    }

    public ValidationResult requiredQosValuesPresentForOverriding(String networkName) {
        HostNetworkQos qos = getQos();
        boolean shouldFail = qos != null && !qos.isEmpty() && qos.getOutAverageLinkshare() == null;
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_SETUP_NETWORKS_MISSING_VALUES,
            ReplacementUtils.createSetVariableString(
                "ACTION_TYPE_FAILED_HOST_NETWORK_QOS_SETUP_NETWORKS_MISSING_VALUES_LIST",
                networkName))
            .when(shouldFail);
    }

    /**
     * Verify that if upper limit and real time rates are provided, real time isn't lower than upper limit.
     */
    public ValidationResult valuesConsistent() {
        return valuesConsistent(null);
    }

    public ValidationResult valuesConsistent(String networkName) {
        HostNetworkQos qos = getQos();
        if (qos == null) {
            return ValidationResult.VALID;
        }

        Integer outUpperlimit = qos.getOutAverageUpperlimit();
        Integer outRealtime = qos.getOutAverageRealtime();
        boolean shouldFail = outUpperlimit != null && outRealtime != null && outUpperlimit < outRealtime;

        if (networkName == null) {
            return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INCONSISTENT_VALUES)
                .when(shouldFail);
        } else {
            return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_SETUP_NETWORKS_INCONSISTENT_VALUES,
                    ReplacementUtils.createSetVariableString(
                        "ACTION_TYPE_FAILED_HOST_NETWORK_QOS_SETUP_NETWORKS_INCONSISTENT_VALUES_LIST",
                        networkName))
                .when(shouldFail);
        }

    }

    @Override
    protected QosDao<HostNetworkQos> getQosDao() {
        return Injector.get(HostNetworkQosDao.class);
    }

}

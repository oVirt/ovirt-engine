package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.qos.QosDao;
import org.ovirt.engine.core.di.Injector;

public class NetworkQosValidator extends QosValidator<NetworkQoS> {

    public NetworkQosValidator(NetworkQoS qos) {
        super(qos);
    }

    /**
     * Verify that if any inbound/outbound capping was specified, that all three parameters are present.
     */
    @Override
    public ValidationResult requiredValuesPresent() {
        return (getQos() != null)
                && (missingValue(getQos().getInboundAverage(), getQos().getInboundPeak(), getQos().getInboundBurst())
                || missingValue(getQos().getOutboundAverage(), getQos().getOutboundPeak(), getQos().getOutboundBurst()))
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES)
                : ValidationResult.VALID;
    }

    private boolean missingValue(Integer average, Integer peak, Integer burst) {
        return (average != null || peak != null || burst != null) && (average == null || peak == null || burst == null);
    }

    /**
     * Verify that the specified peak value isn't lower than the specified average value.
     */
    public ValidationResult peakConsistentWithAverage() {
        return (getQos() != null) && (peakLowerThanAverage(getQos().getInboundAverage(), getQos().getInboundPeak())
                || peakLowerThanAverage(getQos().getOutboundAverage(), getQos().getOutboundPeak()))
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_PEAK_LOWER_THAN_AVERAGE)
                : ValidationResult.VALID;
    }

    private boolean peakLowerThanAverage(Integer average, Integer peak) {
        return peak != null && peak < average;
    }

    @Override
    protected QosDao<NetworkQoS> getQosDao() {
        return Injector.get(NetworkQoSDao.class);
    }

}

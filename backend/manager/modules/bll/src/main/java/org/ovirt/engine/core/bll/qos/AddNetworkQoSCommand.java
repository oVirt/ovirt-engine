package org.ovirt.engine.core.bll.qos;


import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.NetworkQosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;

public class AddNetworkQoSCommand extends AddQosCommand<NetworkQoS, NetworkQosValidator> {

    public AddNetworkQoSCommand(QosParametersBase<NetworkQoS> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected NetworkQoSDao getQosDao() {
        return networkQosDao;
    }

    @Override
    protected NetworkQosValidator getQosValidator(NetworkQoS networkQos) {
        return new NetworkQosValidator(networkQos);
    }

    @Override
    protected boolean validate() {
        return super.validate() &&
                validate(getQosValidator(getQos()).peakConsistentWithAverage());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_NETWORK_QOS : AuditLogType.USER_FAILED_TO_ADD_NETWORK_QOS;
    }
}

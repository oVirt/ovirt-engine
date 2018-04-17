package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

public class AddHostNetworkQosCommand extends AddQosCommand<HostNetworkQos, HostNetworkQosValidator> {

    public AddHostNetworkQosCommand(QosParametersBase<HostNetworkQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected HostNetworkQosDao getQosDao() {
        return hostNetworkQosDao;
    }

    @Override
    protected HostNetworkQosValidator getQosValidator(HostNetworkQos qos) {
        return new HostNetworkQosValidator(qos);
    }

    @Override
    protected boolean validate() {
        HostNetworkQosValidator validator = getQosValidator(getQos());
        return super.validate()
                && validate(validator.requiredValuesPresent()) &&
                validate(validator.valuesConsistent());
    }

}

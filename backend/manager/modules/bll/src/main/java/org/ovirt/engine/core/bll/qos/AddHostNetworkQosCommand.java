package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

public class AddHostNetworkQosCommand extends AddQosCommand<HostNetworkQos, HostNetworkQosValidator> {

    public AddHostNetworkQosCommand(QosParametersBase<HostNetworkQos> parameters) {
        super(parameters);
    }

    @Override
    protected HostNetworkQosDao getQosDao() {
        return getDbFacade().getHostNetworkQosDao();
    }

    @Override
    protected HostNetworkQosValidator getQosValidator(HostNetworkQos qos) {
        return new HostNetworkQosValidator(qos);
    }

    @Override
    protected boolean canDoAction() {
        HostNetworkQosValidator validator = getQosValidator(getQos());
        return super.canDoAction()
                && validate(validator.requiredValuesPresent()) &&
                validate(validator.valuesConsistent());
    }

}

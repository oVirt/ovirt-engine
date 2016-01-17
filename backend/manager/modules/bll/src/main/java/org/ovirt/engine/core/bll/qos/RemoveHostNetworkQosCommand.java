package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

public class RemoveHostNetworkQosCommand extends RemoveQosCommandBase<HostNetworkQos, HostNetworkQosValidator> {

    public RemoveHostNetworkQosCommand(QosParametersBase<HostNetworkQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected HostNetworkQosDao getQosDao() {
        return getDbFacade().getHostNetworkQosDao();
    }

    @Override
    protected HostNetworkQosValidator getQosValidator(HostNetworkQos qos) {
        return new HostNetworkQosValidator(qos);
    }

}

package org.ovirt.engine.core.bll.qos;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class RemoveHostNetworkQosCommand extends RemoveQosCommandBase<HostNetworkQos, HostNetworkQosValidator> {
    @Inject
    private RefreshNetworksParametersFactory refreshNetworksParametersFactory;
    @Inject
    private NetworkDao networkDao;

    public RemoveHostNetworkQosCommand(QosParametersBase<HostNetworkQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected HostNetworkQosDao getQosDao() {
        //might NOT be replaced with injection; superclass constructor accesses this and thus this dao would be null.
        return hostNetworkQosDao;
    }

    @Override
    protected HostNetworkQosValidator getQosValidator(HostNetworkQos qos) {
        return new HostNetworkQosValidator(qos);
    }

    @Override
    protected void executeCommand() {
        List<Network> networksHavingQos = networkDao.getAllForQos(getQosId());

        //remove qos.
        super.executeCommand();

        refreshNetworks(refreshNetworksParametersFactory.create(networksHavingQos));
    }
}

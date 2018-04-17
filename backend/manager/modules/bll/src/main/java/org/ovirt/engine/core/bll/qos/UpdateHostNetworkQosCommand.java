package org.ovirt.engine.core.bll.qos;

import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

public class UpdateHostNetworkQosCommand extends UpdateQosCommandBase<HostNetworkQos, HostNetworkQosValidator> {

    @Inject
    private RefreshNetworksParametersFactory refreshNetworksParametersFactory;

    public UpdateHostNetworkQosCommand(QosParametersBase<HostNetworkQos> parameters, CommandContext cmdContext) {
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
                && validate(validator.requiredValuesPresent())
                && validate(validator.valuesConsistent());
    }

    @Override
    protected void executeCommand() {
        Guid qosId = getQosId();
        HostNetworkQos oldQos = getQosDao().get(qosId);
        HostNetworkQos newQos = getQos();

        super.executeCommand();

        if (networkUpdateRequired(oldQos, newQos)) {
            refreshNetworks(refreshNetworksParametersFactory.create(qosId));
        }
    }

    /**
     * @param oldQos Qos entity before update
     * @param newQos Qos entity after update
     * @return true if Qos entity was update in such way, which needs to update related networks.
     */
    private boolean networkUpdateRequired(HostNetworkQos oldQos, HostNetworkQos newQos) {
        boolean noChange = Objects.equals(newQos.getOutAverageLinkshare(), oldQos.getOutAverageLinkshare())
        && Objects.equals(newQos.getOutAverageRealtime(), oldQos.getOutAverageRealtime())
        && Objects.equals(newQos.getOutAverageUpperlimit(), oldQos.getOutAverageUpperlimit());

        return !noChange;
    }
}

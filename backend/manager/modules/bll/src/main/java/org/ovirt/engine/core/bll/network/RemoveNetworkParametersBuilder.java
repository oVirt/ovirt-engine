package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;

public class RemoveNetworkParametersBuilder extends NetworkParametersBuilder {

    private final Network network;
    private final ManagementNetworkUtil managementNetworkUtil;

    public RemoveNetworkParametersBuilder(Network network,
                                          CommandContext commandContext,
                                          ManagementNetworkUtil managementNetworkUtil) {
        super(commandContext);

        Validate.notNull(network, "network cannot be null");
        Validate.notNull(managementNetworkUtil, "managementNetworkUtil cannot be null");

        this.network = network;
        this.managementNetworkUtil = managementNetworkUtil;
    }

    public ArrayList<VdcActionParametersBase> buildParameters(List<VdsNetworkInterface> nics) {
        Set<Guid> nonUpdateableHosts = new HashSet<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        if (managementNetworkUtil.isManagementNetwork(network.getId())) {
            return parameters;
        }

        boolean vlanNetwork = NetworkUtils.isVlan(network);

        for (VdsNetworkInterface nic : nics) {
            PersistentSetupNetworksParameters setupNetworkParams = createSetupNetworksParameters(nic.getVdsId());
            setupNetworkParams.setNetworkNames(network.getName());
            VdsNetworkInterface nicToConfigure = getNicToConfigure(setupNetworkParams.getInterfaces(), nic.getId());
            if (nicToConfigure == null) {
                throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            if (network.getName().equals(nicToConfigure.getNetworkName())) {
                nicToConfigure.setNetworkName(null);
            } else if (vlanNetwork) {
                VdsNetworkInterface vlan = getVlanDevice(setupNetworkParams.getInterfaces(), nicToConfigure, network);

                if (vlan == null) {
                    nonUpdateableHosts.add(nic.getVdsId());
                } else {
                    setupNetworkParams.getInterfaces().remove(vlan);
                }
            } else {
                // if a network is assigned to nic other than the labeled one
                nonUpdateableHosts.add(nic.getVdsId());
                continue;
            }

            parameters.add(setupNetworkParams);
        }

        reportNonUpdateableHosts(AuditLogType.REMOVE_NETWORK_BY_LABEL_FAILED, nonUpdateableHosts);
        return parameters;
    }

    @Override
    protected void addValuesToLog(AuditLogableBase logable) {
        logable.setStoragePoolId(network.getDataCenterId());
        logable.addCustomValue("Network", network.getName());
    }
}

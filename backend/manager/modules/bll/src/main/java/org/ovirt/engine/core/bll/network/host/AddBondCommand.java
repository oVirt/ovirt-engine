package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.utils.NetworkUtils;

public class AddBondCommand<T extends AddBondParameters> extends VdsBondCommand<T> {

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    public AddBondCommand(T parameters) {
        super(parameters);
        if (parameters.getNics() != null) {
            for (String nic : parameters.getNics()) {
                appendCustomValue("Interfaces", nic, ", ");
            }
        }
    }

    @Override
    protected void executeCommand() {
        T params = getParameters();
        String address = params.getAddress();
        String subnet = StringUtils.isEmpty(params.getSubnet()) ? params.getNetwork()
                .getSubnet() : params.getSubnet();
        String gateway = StringUtils.isEmpty(params.getGateway()) ? params.getNetwork()
                .getGateway() : params.getGateway();

        NetworkVdsmVDSCommandParameters vdsParams = new NetworkVdsmVDSCommandParameters(params.getVdsId(),
                params.getNetwork(),
                params.getBondName(),
                params.getNics(),
                address,
                subnet,
                gateway,
                params.getBondingOptions(),
                params.getBootProtocol());

        VDSReturnValue retVal = runVdsCommand(VDSCommandType.AddNetwork, vdsParams);

        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = runVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new CollectHostNetworkDataVdsCommandParameters(getVds()));

            if (retVal.getSucceeded()) {
                // set network status (this can change the network status to
                // operational)
                VdsStatic vdsStatic = getDbFacade().getVdsStaticDao().get(params.getVdsId());
                NetworkClusterHelper.setStatus(vdsStatic.getVdsGroupId(), params
                        .getNetwork());
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean validate() {
        // check minimum 2 nics in bond
        if (getParameters().getNics().length < 2) {
            return failValidation(EngineMessage.NETWORK_BOND_PARAMETERS_INVALID);
        }

        if (getParameters().getNetwork() == null) {
            return failValidation(EngineMessage.NETWORK_NOT_EXISTS);
        }

        List<VdsNetworkInterface> interfaces = getDbFacade().getInterfaceDao().getAllInterfacesForVds(
                getParameters().getVdsId());

        // check that bond exists
        boolean bondNotExists =
                interfaces.stream().noneMatch(anInterface -> anInterface.getName().equals(getParameters().getBondName()));

        if (bondNotExists) {
            return failValidation(EngineMessage.NETWORK_BOND_NAME_EXISTS);
        }

        // check that each nic is valid
        for (final String nic : getParameters().getNics()) {
            VdsNetworkInterface iface = interfaces.stream().filter(i -> i.getName().equals(nic)).findFirst().orElse(null);

            if (iface == null) {
                return failValidation(EngineMessage.NETWORK_BOND_NAME_EXISTS);
            } else if (StringUtils.isNotEmpty(iface.getBondName())) {
                return failValidation(EngineMessage.NETWORK_INTERFACE_NAME_ALREADY_IN_USE);
            } else if (StringUtils.isNotEmpty(iface.getNetworkName())) {
                return failValidation(EngineMessage.NETWORK_INTERFACE_NAME_ALREADY_IN_USE);
            } else if (NetworkUtils.interfaceHasVlan(iface, interfaces)) {
                // check that one of the nics is not connected to vlan
                return failValidation(EngineMessage.NETWORK_INTERFACE_IN_USE_BY_VLAN);
            }

        }

        // check that the network not in use
        boolean networkInUse = interfaces.stream().anyMatch
                (i ->  i.getNetworkName() != null && i.getNetworkName().equals(getParameters().getNetwork().getName()));

        if (networkInUse) {
            return failValidation(EngineMessage.NETWORK_ALREADY_ATTACHED_TO_INTERFACE);
        }

        // check that the network exists in current cluster
        Network network =
                getNetworkDao().getByNameAndCluster(getParameters().getNetwork().getName(), getVds().getVdsGroupId());
        if (network == null) {
            return failValidation(EngineMessage.NETWORK_NOT_EXISTS_IN_CLUSTER);
        }

        if (StringUtils.isNotEmpty(getParameters().getGateway()) &&
            !managementNetworkUtil.isManagementNetwork(getParameters().getNetwork().getId(), getVdsGroupId())) {
            addValidationMessage(EngineMessage.NETWORK_ATTACH_ILLEGAL_GATEWAY);
            return false;
        }

        if (network.isExternal()) {
            return failValidation(EngineMessage.EXTERNAL_NETWORK_CANNOT_BE_PROVISIONED);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_BOND : AuditLogType.NETWORK_ADD_BOND_FAILED;
    }
}

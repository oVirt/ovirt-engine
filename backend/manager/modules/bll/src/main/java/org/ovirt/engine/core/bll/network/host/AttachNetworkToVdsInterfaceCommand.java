package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.InterfaceAndIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

public class AttachNetworkToVdsInterfaceCommand<T extends AttachNetworkToVdsParameters> extends VdsNetworkCommand<T> {

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    private Network logicalNetwork;

    public AttachNetworkToVdsInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        String bond = null;
        T params = getParameters();
        String address = params.getAddress();
        String subnet = StringUtils.isEmpty(params.getSubnet()) ? logicalNetwork.getSubnet() : params.getSubnet();
        String gateway = StringUtils.isEmpty(params.getGateway()) ? "" : params.getGateway();
        ArrayList<String> nics = new ArrayList<String>();
        nics.add(params.getInterface().getName());

        // check if bond...
        if (params.getInterface().getBonded() != null && params.getInterface().getBonded()) {
            nics.clear();
            bond = params.getInterface().getName();

            List<VdsNetworkInterface> interfaces = getDbFacade().getInterfaceDao().getAllInterfacesForVds(
                    params.getVdsId());

            for (VdsNetworkInterface i : interfaces) {
                if (StringUtils.equals(i.getBondName(), params.getInterface().getName())) {
                    nics.add(i.getName());
                }
            }
        }

        NetworkVdsmVDSCommandParameters parameters = new NetworkVdsmVDSCommandParameters(params.getVdsId(),
                logicalNetwork,
                bond,
                nics.toArray(new String[] {}),
                address,
                subnet,
                gateway,
                params.getBondingOptions(),
                params.getBootProtocol());
        VDSReturnValue retVal = runVdsCommand(VDSCommandType.AddNetwork, parameters);

        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new CollectHostNetworkDataVdsCommandParameters(getVds()));

            if (retVal.getSucceeded()) {
                Guid groupId = getVdsDAO().get(params.getVdsId()).getVdsGroupId();
                NetworkClusterHelper.setStatus(groupId, logicalNetwork);
                setSucceeded(true);
            }
        }
    }

    /**
     * The supported network topologies over a nic are:
     * <ul>
     * <li>Attach a VM network (with/without vlan)</li>
     * <li>Attach a non-VM network (with/without vlan)</li>
     * <li>Attach a non-VM network to a nic which a vlan is already attached to.</li>
     * <li>Attach a vlan to a nic which a non-VM network is already attached to.</li>
     * </ul>
     * </p> The unsupported network topologies are:
     * <ul>
     * <li>Attaching any network to a nic which a non-vlan VM network is already attached to.</li>
     * <li>Attach a non-vlan VM network to a nic which any network is already attached to.</li>
     * <li>Attach a non-VM network to a nic which a non-VM network is already attached to.</li>
     * <li>Attach any network to a nic if unmanaged network is already attached to.</li>
     * </ul>
     */
    @Override
    protected boolean canDoAction() {
        T params = getParameters();
        List<VdsNetworkInterface> interfaces =
                getDbFacade().getInterfaceDao().getAllInterfacesForVds(params.getVdsId());

        // check that interface exists
        VdsNetworkInterface iface = Entities.entitiesByName(interfaces).get(params.getInterface().getName());
        if (iface == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_EXISTS);
            return false;
        }

        // check if the parameters interface is part of a bond
        if (StringUtils.isNotEmpty(params.getInterface().getBondName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_ALREADY_IN_BOND);
            return false;
        }

        Map<String, Network> networksByName =
                Entities.entitiesByName(getNetworkDAO().getAllForCluster(getVds().getVdsGroupId()));

        // check that the network exists in current cluster
        if (!networksByName.containsKey(params.getNetwork().getName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CLUSTER);
            return false;
        } else {
            logicalNetwork = networksByName.get(params.getNetwork().getName());
        }

        if (logicalNetwork.isExternal()) {
            return failCanDoAction(VdcBllMessages.EXTERNAL_NETWORK_CANNOT_BE_PROVISIONED);
        }

        if (!networkConfigurationSupported(iface, networksByName)) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_ALREADY_HAVE_NETWORK);
            return false;
        }

        if (!managementNetworkUtil.isManagementNetwork(logicalNetwork.getName(), getVdsGroupId())
                && StringUtils.isNotEmpty(params.getGateway())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_ATTACH_ILLEGAL_GATEWAY);
            return false;
        }

        // check that the required not attached to other interface
        iface = Entities.hostInterfacesByNetworkName(interfaces).get(logicalNetwork.getName());
        if (iface != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_INTERFACE);
            return false;
        }

        // check address exists in static ip
        if (params.getBootProtocol() == NetworkBootProtocol.STATIC_IP) {
            if (StringUtils.isEmpty(params.getAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_ADDR_MANDATORY_IN_STATIC_IP);
                return false;
            }
        }

        // check that nic have no vlans
        if (vmNetworkNonVlan(logicalNetwork)) {
            VdcQueryReturnValue ret = runInternalQuery(
                    VdcQueryType.GetAllChildVlanInterfaces,
                    new InterfaceAndIdQueryParameters(params.getVdsId(), params
                            .getInterface()));
            ArrayList<VdsNetworkInterface> vlanIfaces = ret.getReturnValue();
            if (vlanIfaces.size() > 0) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_CONNECT_TO_VLAN);
                return false;
            }
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS
                : AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_FAILED;
    }

    /**
     * Validates whether the network configuration is supported.
     *
     * @param iface
     *            The target network interface
     * @param networksByName
     *            A map contains all cluster's networks
     * @return <code>true</code> if the configuration is supported, else <code>false</code>
     */
    private boolean networkConfigurationSupported(VdsNetworkInterface iface, Map<String, Network> networksByName) {
        if (StringUtils.isEmpty(iface.getNetworkName())) {
            return true;
        }

        Network attachedNetwork = networksByName.get(iface.getNetworkName());

        // Prevent attaching a network if unmanaged network is already attached to the nic
        if (attachedNetwork == null) {
            return false;
        }

        // Prevent attaching a VM network when a VM network is already attached
        if (vmNetworkNonVlan(attachedNetwork) || vmNetworkNonVlan(logicalNetwork)) {
            return false;
        }

        // Verify that only VM networks exists on the nic if the non-Vm network feature isn't supported by the cluster
        if (!FeatureSupported.nonVmNetwork(getVds().getVdsGroupCompatibilityVersion()) && (!iface.isBridged())) {
            return false;
        }

        // Prevent attaching non-VM network to a nic which already has an attached non-VM network
        if (NetworkUtils.isNonVmNonVlanNetwork(attachedNetwork) && NetworkUtils.isNonVmNonVlanNetwork(logicalNetwork)) {
            return false;
        }

        return true;
    }

    private boolean vmNetworkNonVlan(Network network) {
        return network.isVmNetwork() && !NetworkUtils.isVlan(network);
    }
}

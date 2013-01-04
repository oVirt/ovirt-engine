package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.InterfaceAndIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.NetworkUtils;

@SuppressWarnings("serial")
public class AttachNetworkToVdsInterfaceCommand<T extends AttachNetworkToVdsParameters> extends VdsNetworkCommand<T> {
    private Network logicalNetwork;

    public AttachNetworkToVdsInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        String bond = null;
        T params = getParameters();
        String address = params.getAddress();
        String subnet = StringUtils.isEmpty(params.getSubnet()) ? params.getNetwork()
                .getSubnet() : params.getSubnet();
        String gateway = StringUtils.isEmpty(params.getGateway()) ? "" : params.getGateway();
        java.util.ArrayList<String> nics = new java.util.ArrayList<String>();
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
        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.AddNetwork, parameters);

        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new VdsIdAndVdsVDSCommandParametersBase(params.getVdsId()));

            if (retVal.getSucceeded()) {
                Guid groupId = getVdsDAO().get(params.getVdsId()).getvds_group_id();
                NetworkClusterHelper.setStatus(groupId, params.getNetwork());
                setSucceeded(true);
            }
        }
    }

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
        if (!StringUtils.isEmpty(params.getInterface().getBondName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_ALREADY_IN_BOND);
            return false;
        }
        // Check that the specify interface has no network
        if (!StringUtils.isEmpty(iface.getNetworkName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_ALREADY_HAVE_NETWORK);
            return false;
        }
        if (!NetworkUtils.getEngineNetwork().equals(params.getNetwork().getName())
                && !StringUtils.isEmpty(params.getGateway())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_ATTACH_ILLEGAL_GATEWAY);
            return false;
        }

        // check that the required not attached to other interface
        iface = Entities.interfacesByNetworkName(interfaces).get(params.getNetwork().getName());
        if (iface != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_INTERFACE);
            return false;
        }

        // check that the network exists in current cluster

        Map<String, Network> networksByName =
                Entities.entitiesByName(getNetworkDAO().getAllForCluster(getVds().getvds_group_id()));

        if (!networksByName.containsKey(params.getNetwork().getName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CLUSTER);
            return false;
        } else {
            logicalNetwork = networksByName.get(params.getNetwork().getName());
        }


        // check address exists in static ip
        if (params.getBootProtocol() == NetworkBootProtocol.STATIC_IP) {
            if (StringUtils.isEmpty(params.getAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_ADDR_MANDATORY_IN_STATIC_IP);
                return false;
            }
        }

        // check that nic have no vlans
        if (params.getNetwork().getVlanId() == null) {
            VdcQueryReturnValue ret = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetAllChildVlanInterfaces,
                    new InterfaceAndIdQueryParameters(params.getVdsId(), params
                            .getInterface()));
            @SuppressWarnings("unchecked")
            ArrayList<VdsNetworkInterface> vlanIfaces = (ArrayList<VdsNetworkInterface>) ret.getReturnValue();
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
}

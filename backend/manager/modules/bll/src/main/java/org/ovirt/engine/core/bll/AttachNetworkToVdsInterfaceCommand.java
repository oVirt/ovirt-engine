package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class AttachNetworkToVdsInterfaceCommand<T extends AttachNetworkToVdsParameters> extends VdsNetworkCommand<T> {
    public AttachNetworkToVdsInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        String bond = null;
        String address = getParameters().getAddress();
        String subnet = StringHelper.isNullOrEmpty(getParameters().getSubnet()) ? getParameters().getNetwork()
                .getsubnet() : getParameters().getSubnet();
        String gateway = StringHelper.isNullOrEmpty(getParameters().getGateway()) ? "" : getParameters().getGateway();
        java.util.ArrayList<String> nics = new java.util.ArrayList<String>();
        nics.add(getParameters().getInterface().getName());

        // check if bond...
        if (getParameters().getInterface().getBonded() != null && getParameters().getInterface().getBonded()) {
            nics.clear();
            bond = getParameters().getInterface().getName();

            List<VdsNetworkInterface> interfaces = DbFacade.getInstance().getInterfaceDAO().getAllInterfacesForVds(
                    getParameters().getVdsId());

            for (VdsNetworkInterface i : interfaces) {
                if (StringHelper.EqOp(i.getBondName(), getParameters().getInterface().getName())) {
                    nics.add(i.getName());
                }
            }
        }

        NetworkVdsmVDSCommandParameters parameters = new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                getParameters().getNetwork().getname(), getParameters().getNetwork().getvlan_id(), bond,
                nics.toArray(new String[] {}), address, subnet, gateway, getParameters().getNetwork().getstp(),
                getParameters().getBondingOptions(), getParameters().getBootProtocol());
        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.AddNetwork, parameters);

        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new VdsIdAndVdsVDSCommandParametersBase(getParameters().getVdsId()));

            if (retVal.getSucceeded()) {
                Guid groupId = DbFacade.getInstance().getVdsDAO().get(getParameters().getVdsId()).getvds_group_id();
                AttachNetworkToVdsGroupCommand.SetNetworkStatus(groupId, getParameters().getNetwork());
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        List<VdsNetworkInterface> interfaces = DbFacade.getInstance().getInterfaceDAO()
                .getAllInterfacesForVds(getParameters().getVdsId());

        // check that interface exists
        // LINQ 31899
        // Interface iface = null; // interfaces.FirstOrDefault(i => i.name ==
        // AttachNetworkToVdsParameters.Interface.name);
        VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                return StringHelper.EqOp(i.getName(), getParameters().getInterface().getName());
            }
        });
        if (iface == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_EXISTS);
            return false;
        }

        // check if the parameters interface is part of a bond
        if (!StringHelper.isNullOrEmpty(getParameters().getInterface().getBondName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_ALREADY_IN_BOND);
            return false;
        }

        // Check that the specify interface has no network
        if (!StringHelper.isNullOrEmpty(iface.getNetworkName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_ALREADY_HAVE_NETWORK);
            return false;
        }

        if (!NetworkUtils.EngineNetwork.equals(getParameters().getNetwork().getname())
                && !StringHelper.isNullOrEmpty(getParameters().getGateway())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_ATTACH_ILLEGAL_GATEWAY);
            return false;
        }

        // check that the required not attached to other interface
        // iface = null; //LINQ 31899 interfaces.FirstOrDefault(i =>
        // i.network_name == AttachNetworkToVdsParameters.Network.name);
        iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                return StringHelper.EqOp(i.getNetworkName(), getParameters().getNetwork().getname());
            }
        });
        if (iface != null) {
            addCanDoActionMessage(VdcBllMessages.NETWROK_ALREADY_ATTACHED_TO_INTERFACE);
            return false;
        }

        // check that the network exists in current cluster
        List<network> networks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(getVds().getvds_group_id());
        // if (true) //LINQ 31899 null == networks.FirstOrDefault(n => n.name ==
        // AttachNetworkToVdsParameters.Network.name))
        if (null == LinqUtils.firstOrNull(networks, new Predicate<network>() {
            @Override
            public boolean eval(network network) {
                return StringHelper.EqOp(network.getname(), getParameters().getNetwork().getname());
            }
        })) {
            addCanDoActionMessage(VdcBllMessages.NETWROK_NOT_EXISTS_IN_CLUSTER);
            return false;
        }

        // check address exists in static ip
        if (getParameters().getBootProtocol() == NetworkBootProtocol.StaticIp) {
            if (StringHelper.isNullOrEmpty(getParameters().getAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWROK_ADDR_MANDATORY_IN_STATIC_IP);
                return false;
            }
        }

        // check that nic have no vlans
        if (getParameters().getNetwork().getvlan_id() == null) {
            VdcQueryReturnValue ret = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetAllChildVlanInterfaces,
                    new GetAllChildVlanInterfacesQueryParameters(getParameters().getVdsId(), getParameters()
                            .getInterface()));
            java.util.ArrayList<VdsNetworkInterface> vlanIfaces = (java.util.ArrayList<VdsNetworkInterface>) ret.getReturnValue();
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

package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class DetachNetworkFromVdsInterfaceCommand<T extends AttachNetworkToVdsParameters> extends VdsNetworkCommand<T> {
    public DetachNetworkFromVdsInterfaceCommand(T paramenters) {
        super(paramenters);
    }

    @Override
    protected void executeCommand() {
        String bond = null;
        java.util.ArrayList<String> nics = new java.util.ArrayList<String>();
        nics.add(NetworkUtils.StripVlan(getParameters().getInterface().getName()));
        Integer vlanId = NetworkUtils.GetVlanId(getParameters().getInterface().getName());

        // vlan with bond
        boolean isBond = getParameters().getInterface().getName().startsWith("bond")
                && getParameters().getInterface().getName().contains(".");
        // or just a bond...
        isBond = isBond
                || (getParameters().getInterface().getBonded() != null && getParameters().getInterface().getBonded());

        // check if bond...
        if (isBond) {
            nics.clear();
            bond = NetworkUtils.StripVlan(getParameters().getInterface().getName());

            List<VdsNetworkInterface> interfaces = DbFacade.getInstance()
                    .getInterfaceDAO().getAllInterfacesForVds(getParameters().getVdsId());

            for (VdsNetworkInterface i : interfaces) {
                if (StringHelper.EqOp(i.getBondName(), bond)) {
                    nics.add(NetworkUtils.StripVlan(i.getName()));
                }
            }
        }

        NetworkVdsmVDSCommandParameters parameters = new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                getParameters().getNetwork().getname(), vlanId, bond, nics.toArray(new String[] {}), getParameters()
                        .getNetwork().getaddr(), getParameters().getNetwork().getsubnet(), getParameters().getNetwork()
                        .getgateway(), getParameters().getNetwork().getstp(), getParameters().getBondingOptions(),
                getParameters().getBootProtocol());
        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.RemoveNetwork, parameters);
        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new VdsIdAndVdsVDSCommandParametersBase(getParameters().getVdsId()));

            if (retVal.getSucceeded()) {
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        List<VdsNetworkInterface> interfaces = DbFacade.getInstance().getInterfaceDAO()
                .getAllInterfacesForVds(getParameters().getVdsId());
        VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                return i.getName().equals(getParameters().getInterface().getName());
            }
        });
        if (iface == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_EXISTS);
            return false;
        }

        if (StringHelper.isNullOrEmpty(getParameters().getInterface().getNetworkName())) {
            getParameters().getInterface().setNetworkName(iface.getNetworkName());
        }

        // set the network object if we don't got in the parameters
        if (getParameters().getNetwork() == null) {
            List<network> networks = DbFacade.getInstance().getNetworkDAO()
                            .getAllForCluster(getVdsGroupId());
            for (network n : networks) {
                if (n.getname().equals(iface.getNetworkName())) {
                    getParameters().setNetwork(n);
                    break;
                }
            }
        }

        if (StringHelper.isNullOrEmpty(iface.getNetworkName())) {
            if (iface.getBonded() != null && iface.getBonded() == true) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_NOT_ATTACCH_TO_NETWORK);
            } else {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_ATTACCH_TO_NETWORK);
            }
            return false;
        } else if (!StringHelper.EqOp(getParameters().getInterface().getNetworkName(), getParameters().getNetwork()
                .getname())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_ATTACCH_TO_NETWORK);
            return false;
        }

        VDS vds = DbFacade.getInstance().getVdsDAO().get(getParameters().getVdsId());

        // check if network in cluster and vds active
        if ((vds.getstatus() == VDSStatus.Up || vds.getstatus() == VDSStatus.Installing)
                && getParameters().getNetwork().getStatus() == NetworkStatus.Operational) {
            List<network> networks = DbFacade.getInstance().getNetworkDAO()
                    .getAllForCluster(vds.getvds_group_id());
            if (null != LinqUtils.firstOrNull(networks, new Predicate<network>() {
                @Override
                public boolean eval(network network) {
                    return network.getname().equals(getParameters().getNetwork().getname());
                }
            })) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_CLUSTER_NETWORK_IN_USE);
                return false;
            }
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_DETACH_NETWORK_FROM_VDS
                : AuditLogType.NETWORK_DETACH_NETWORK_FROM_VDS_FAILED;
    }
}

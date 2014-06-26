package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class DetachNetworkFromVdsInterfaceCommand<T extends AttachNetworkToVdsParameters> extends VdsNetworkCommand<T> {

    private VdsNetworkInterface iface;

    public DetachNetworkFromVdsInterfaceCommand(T paramenters) {
        super(paramenters);
    }

    @Override
    protected void executeCommand() {
        String bond = null;
        List<String> nics = new ArrayList<>();
        String baseNicName = NetworkUtils.stripVlan(iface);
        nics.add(baseNicName);
        Integer vlanId = iface.getVlanId();
        List<VdsNetworkInterface> interfaces = getDbFacade()
                .getInterfaceDao().getAllInterfacesForVds(getParameters().getVdsId());

        // vlan with bond
        boolean isBond = NetworkUtils.isBondVlan(interfaces, iface);
        // or just a bond...
        isBond = isBond || (iface.getBonded() != null && iface.getBonded());

        // check if bond...
        if (isBond) {
            nics.clear();
            bond = baseNicName;

            for (VdsNetworkInterface i : interfaces) {
                if (StringUtils.equals(i.getBondName(), bond)) {
                    nics.add(NetworkUtils.stripVlan(i));
                }
            }
        }

        NetworkVdsmVDSCommandParameters parameters = new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                getParameters().getNetwork().getName(), vlanId, bond, nics.toArray(new String[] {}), getParameters()
                        .getNetwork().getAddr(), getParameters().getNetwork().getSubnet(), getParameters().getNetwork()
                        .getGateway(), getParameters().getNetwork().getStp(), getParameters().getBondingOptions(),
                getParameters().getBootProtocol());
        VDSReturnValue retVal = runVdsCommand(VDSCommandType.RemoveNetwork, parameters);
        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new CollectHostNetworkDataVdsCommandParameters(getVds()));

            if (retVal.getSucceeded()) {
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        List<VdsNetworkInterface> interfaces = getDbFacade().getInterfaceDao()
                .getAllInterfacesForVds(getParameters().getVdsId());
        iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                return i.getName().equals(getParameters().getInterface().getName());
            }
        });
        if (iface == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_EXISTS);
            return false;
        }
        if (StringUtils.isEmpty(getParameters().getInterface().getNetworkName())) {
            getParameters().getInterface().setNetworkName(iface.getNetworkName());
        }

        // set the network object if we don't got in the parameters
        if (getParameters().getNetwork() == null || getParameters().getNetwork().getCluster() == null) {
            List<Network> networks = getDbFacade().getNetworkDao()
                            .getAllForCluster(getVdsGroupId());
            for (Network n : networks) {
                if (n.getName().equals(iface.getNetworkName())) {
                    getParameters().setNetwork(n);
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(iface.getNetworkName())) {
            if (iface.getBonded() != null && iface.getBonded() == true) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_NOT_ATTACH_TO_NETWORK);
            } else {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_ATTACH_TO_NETWORK);
            }
            return false;
        } else if (!StringUtils.equals(getParameters().getInterface().getNetworkName(), getParameters().getNetwork()
                .getName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_ATTACH_TO_NETWORK);
            return false;
        }

        VDS vds = getVdsDAO().get(getParameters().getVdsId());

        // check if network in cluster and vds active
        if ((vds.getStatus() == VDSStatus.Up || vds.getStatus() == VDSStatus.Installing)
                && getParameters().getNetwork().getCluster() != null
                && getParameters().getNetwork().getCluster().getStatus() == NetworkStatus.OPERATIONAL) {
            List<Network> networks = getDbFacade().getNetworkDao().getAllForCluster(vds.getVdsGroupId());
            if (null != LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                @Override
                public boolean eval(Network network) {
                    return network.getName().equals(getParameters().getNetwork().getName());
                }
            })) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_HOST_IS_BUSY);
                return false;
            }
        }

        List<String> vmNames =
                new VmInterfaceManager().findActiveVmsUsingNetworks(vds.getId(),
                        Collections.singletonList(getParameters().getNetwork().getName()));

        if (!vmNames.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS);
            addCanDoActionMessageVariable(String.format("$%s_LIST",
                    VdcBllMessages.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS.name()),
                    StringUtils.join(vmNames, ","));
            return false;
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_DETACH_NETWORK_FROM_VDS
                : AuditLogType.NETWORK_DETACH_NETWORK_FROM_VDS_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DETACH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
    }
}

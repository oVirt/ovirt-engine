package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.utils.NetworkUtils;

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
            retVal = runVdsCommand(VDSCommandType.CollectVdsNetworkData,
                    new CollectHostNetworkDataVdsCommandParameters(getVds()));

            if (retVal.getSucceeded()) {
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean validate() {
        List<VdsNetworkInterface> interfaces = getDbFacade().getInterfaceDao()
                .getAllInterfacesForVds(getParameters().getVdsId());
        iface = interfaces.stream().filter(i -> i.getName().equals(getParameters().getInterface().getName()))
                .findFirst().orElse(null);
        if (iface == null) {
            addValidationMessage(EngineMessage.NETWORK_INTERFACE_NOT_EXISTS);
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
                addValidationMessage(EngineMessage.NETWORK_BOND_NOT_ATTACH_TO_NETWORK);
            } else {
                addValidationMessage(EngineMessage.NETWORK_INTERFACE_NOT_ATTACH_TO_NETWORK);
            }
            return false;
        } else if (!StringUtils.equals(getParameters().getInterface().getNetworkName(), getParameters().getNetwork()
                .getName())) {
            addValidationMessage(EngineMessage.NETWORK_INTERFACE_NOT_ATTACH_TO_NETWORK);
            return false;
        }

        VDS vds = getVdsDao().get(getParameters().getVdsId());

        // check if network in cluster and vds active
        if ((vds.getStatus() == VDSStatus.Up || vds.getStatus() == VDSStatus.Installing)
                && getParameters().getNetwork().getCluster() != null
                && getParameters().getNetwork().getCluster().getStatus() == NetworkStatus.OPERATIONAL) {
            List<Network> networks = getDbFacade().getNetworkDao().getAllForCluster(vds.getVdsGroupId());
            if (networks.stream().anyMatch(network -> network.getName().equals(getParameters().getNetwork().getName()))) {
                addValidationMessage(EngineMessage.NETWORK_HOST_IS_BUSY);
                return false;
            }
        }

        List<String> vmNames =
                new VmInterfaceManager(getMacPool()).findActiveVmsUsingNetworks(vds.getId(),
                        Collections.singletonList(getParameters().getNetwork().getName()));

        if (!vmNames.isEmpty()) {
            addValidationMessage(EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS);
            addValidationMessageVariable("networkNames", getParameters().getNetwork().getName());
            addValidationMessageVariable(String.format("$%s_LIST",
                    EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS.name()),
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
        addValidationMessage(EngineMessage.VAR__ACTION__DETACH);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
    }
}

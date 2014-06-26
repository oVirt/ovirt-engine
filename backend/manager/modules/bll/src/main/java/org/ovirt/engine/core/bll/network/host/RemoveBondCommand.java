package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveBondParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class RemoveBondCommand<T extends RemoveBondParameters> extends VdsBondCommand<T> {

    public RemoveBondCommand(T parameters) {
        super(parameters);
    }

    private String network;
    private ArrayList<String> interfaces;

    @Override
    protected void executeCommand() {
        VdsNetworkInterface bond = null;
        List<VdsNetworkInterface> all =
                getDbFacade().getInterfaceDao().getAllInterfacesForVds(getParameters().getVdsId());
        for (VdsNetworkInterface iface : all) {
            if (StringUtils.equals(iface.getName(), getParameters().getBondName())) {
                bond = iface;
                break;
            }
        }

        VDSReturnValue retVal = null;
        if (bond != null) {
            NetworkVdsmVDSCommandParameters parameters =
                    new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                            network,
                            bond.getVlanId(),
                            NetworkUtils.stripVlan(bond),
                            interfaces.toArray(new String[] {}),
                            null,
                            null,
                            null,
                            false,
                            null,
                            NetworkBootProtocol.NONE);
            retVal = runVdsCommand(VDSCommandType.RemoveNetwork, parameters);
        }

        if (retVal != null && retVal.getSucceeded()) {
            // update vds network data
            retVal = runVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new CollectHostNetworkDataVdsCommandParameters(getVds()));

            if (retVal.getSucceeded()) {
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        List<VdsNetworkInterface> vdsInterfaces =
                getDbFacade().getInterfaceDao().getAllInterfacesForVds(getParameters().getVdsId());

        // check that bond exists
        final VdsNetworkInterface bond = LinqUtils.firstOrNull(vdsInterfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                return i.getName().equals(getParameters().getBondName());
            }
        });
        if (bond == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_NOT_EXISTS);
            return false;
        }

        if (bond.getBonded() != null && bond.getBonded().equals(false)) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_NOT_EXISTS);
            return false;
        }

        network = bond.getNetworkName();

        if (StringUtils.isEmpty(network)) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_HAVE_ATTACHED_VLANS);
            return false;
        }

        vdsInterfaces = LinqUtils.filter(vdsInterfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                return NetworkUtils.interfaceBasedOn(bond, i.getBondName());
            }
        });
        interfaces = new ArrayList<String>();
        for (VdsNetworkInterface iface : vdsInterfaces) {
            interfaces.add(iface.getName());
        }

        VDS vds = getVdsDAO().get(getParameters().getVdsId());
        // check if network in cluster and vds active
        if (vds.getStatus() == VDSStatus.Up || vds.getStatus() == VDSStatus.Installing) {
            List<Network> networks = getNetworkDAO().getAllForCluster(vds.getVdsGroupId());
            if (null != LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                @Override
                public boolean eval(Network n) {
                    return n.getName().equals(bond.getName());
                }
            })) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_CLUSTER_NETWORK_IN_USE);
                return false;
            }
        }

        // check if network in use by vm
        List<VM> vmList = getVmDAO().getAllForVdsGroup(vds.getVdsGroupId());
        for (VM vm : vmList) {
            if (vm.getStatus() != VMStatus.Down) {
                List<VmNetworkInterface> vmInterfaces = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
                VmNetworkInterface iface = LinqUtils.firstOrNull(vmInterfaces, new Predicate<VmNetworkInterface>() {
                    @Override
                    public boolean eval(VmNetworkInterface i) {
                        if (i.getNetworkName() != null) {
                            return i.getNetworkName().equals(bond.getNetworkName());
                        }
                        return false;
                    }
                });
                if (iface != null) {
                    addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VM);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_BOND : AuditLogType.NETWORK_REMOVE_BOND_FAILED;
    }
}

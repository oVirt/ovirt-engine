package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class AddBondCommand<T extends AddBondParameters> extends VdsBondCommand<T> {

    public AddBondCommand(T parameters) {
        super(parameters);
        if (parameters.getNics() != null) {
            for (String nic : parameters.getNics()) {
                AppendCustomValue("Interfaces", nic, ", ");
            }
        }
    }

    @Override
    protected void executeCommand() {
        T params = getParameters();
        String address = params.getAddress();
        String subnet = StringUtils.isEmpty(params.getSubnet()) ? params.getNetwork()
                .getsubnet() : params.getSubnet();
        String gateway = StringUtils.isEmpty(params.getGateway()) ? params.getNetwork()
                .getgateway() : params.getGateway();

        NetworkVdsmVDSCommandParameters vdsParams = new NetworkVdsmVDSCommandParameters(params.getVdsId(),
                params.getNetwork(),
                params.getBondName(),
                params.getNics(),
                address,
                subnet,
                gateway,
                params.getBondingOptions(),
                params.getBootProtocol());

        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.AddNetwork, vdsParams);

        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new VdsIdAndVdsVDSCommandParametersBase(params.getVdsId()));

            if (retVal.getSucceeded()) {
                // set network status (this can change the network status to
                // operational)
                VdsStatic vdsStatic = DbFacade.getInstance().getVdsStaticDao().get(params.getVdsId());
                NetworkClusterHelper.setStatus(vdsStatic.getvds_group_id(), params
                        .getNetwork());
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        // check minimum 2 nics in bond
        if (getParameters().getNics().length < 2) {
            return failCanDoAction(VdcBllMessages.NETWORK_BOND_PARAMETERS_INVALID);
        }

        if (getParameters().getNetwork() == null) {
            return failCanDoAction(VdcBllMessages.NETWORK_NOT_EXISTS);
        }

        List<VdsNetworkInterface> interfaces = DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(
                getParameters().getVdsId());

        // check that bond exists
        VdsNetworkInterface bond = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface anInterface) {
                return anInterface.getName().equals(getParameters().getBondName());
            }
        });

        if (bond == null) {
            return failCanDoAction(VdcBllMessages.NETWORK_BOND_NAME_EXISTS);
        }

        // check that each nic is valid
        for (final String nic : getParameters().getNics()) {
            VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                @Override
                public boolean eval(VdsNetworkInterface i) {
                    return i.getName().equals(nic);
                }
            });

            if (iface == null) {
                return failCanDoAction(VdcBllMessages.NETWORK_BOND_NAME_EXISTS);
            } else if (StringUtils.isNotEmpty(iface.getBondName())) {
                return failCanDoAction(VdcBllMessages.NETWORK_INTERFACE_NAME_ALREADY_IN_USE);
            } else if (StringUtils.isNotEmpty(iface.getNetworkName())) {
                return failCanDoAction(VdcBllMessages.NETWORK_INTERFACE_NAME_ALREADY_IN_USE);
            } else if (NetworkUtils.interfaceHasVlan(iface, interfaces)) {
                // check that one of the nics is not connected to vlan
                return failCanDoAction(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VLAN);
            }

        }

        // check that the network not in use
        VdsNetworkInterface I = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                if (i.getNetworkName() != null) {
                    return i.getNetworkName().equals(getParameters().getNetwork().getname());
                }
                return false;
            }
        });

        if (I != null) {
            return failCanDoAction(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_INTERFACE);
        }

        // check that the network exists in current cluster
        List<Network> networks = DbFacade.getInstance().getNetworkDao().getAllForCluster(getVds().getvds_group_id());
        if (null == LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network network) {
                return network.getname().equals(getParameters().getNetwork().getname());
            }
        })) {
            return failCanDoAction(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CLUSTER);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_BOND : AuditLogType.NETWORK_ADD_BOND_FAILED;
    }
}

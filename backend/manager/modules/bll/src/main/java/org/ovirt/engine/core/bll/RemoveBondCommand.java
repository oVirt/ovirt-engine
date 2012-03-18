package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveBondParameters;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.SearchReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
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

public class RemoveBondCommand<T extends RemoveBondParameters> extends VdsBondCommand<T> {

    private static final long serialVersionUID = -3746344133983175272L;

    public RemoveBondCommand(T parameters) {
        super(parameters);
    }

    private String _network;
    private ArrayList<String> _interfaces;

    @Override
    protected void executeCommand() {
        VdsNetworkInterface bond = null;
        List<VdsNetworkInterface> all = DbFacade.getInstance().getInterfaceDAO()
                .getAllInterfacesForVds(getParameters().getVdsId());
        for (VdsNetworkInterface iface : all) {
            if (StringHelper.EqOp(iface.getName(), getParameters().getBondName())) {
                bond = iface;
                break;
            }
        }

        VDSReturnValue retVal = null;
        if (bond != null) {
            NetworkVdsmVDSCommandParameters parameters =
                    new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                            _network,
                            NetworkUtils.GetVlanId(bond.getName()),
                            NetworkUtils.StripVlan(getParameters().getBondName()),
                            _interfaces.toArray(new String[] {}),
                            null,
                            null,
                            null,
                            false,
                            null,
                            NetworkBootProtocol.None);
            retVal = Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.RemoveNetwork, parameters);
        }

        if (retVal != null && retVal.getSucceeded()) {
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
        List<VdsNetworkInterface> vdsInterfaces =
                DbFacade.getInstance().getInterfaceDAO().getAllInterfacesForVds(getParameters().getVdsId());

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

        _network = bond.getNetworkName();

        if (StringHelper.isNullOrEmpty(_network)) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_HAVE_ATTACHED_VLANS);
            return false;
        }

        vdsInterfaces = LinqUtils.filter(vdsInterfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                if (i.getBondName() != null) {
                    return i.getBondName().equals(NetworkUtils.StripVlan(bond.getName()));
                }
                return false;
            }
        });
        _interfaces = new ArrayList<String>();
        for (VdsNetworkInterface iface : vdsInterfaces) {
            _interfaces.add(iface.getName());
        }

        VDS vds = DbFacade.getInstance().getVdsDAO().get(getParameters().getVdsId());
        // check if network in cluster and vds active
        if (vds.getstatus() == VDSStatus.Up || vds.getstatus() == VDSStatus.Installing) {
            List<network> networks = DbFacade.getInstance().getNetworkDAO()
                    .getAllForCluster(vds.getvds_group_id());
            if (null != LinqUtils.firstOrNull(networks, new Predicate<network>() {
                @Override
                public boolean eval(network n) {
                    return n.getname().equals(bond.getName());
                }
            })) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_CLUSTER_NETWORK_IN_USE);
                return false;
            }
        }

        // check if network in use by vm
        String query = "Vms: cluster = " + vds.getvds_group_name();
        SearchParameters searchParams = new SearchParameters(query, SearchType.VM);

        searchParams.setMaxCount(Integer.MAX_VALUE);
        VdcQueryReturnValue tempVar = Backend.getInstance().runInternalQuery(VdcQueryType.Search, searchParams);
        SearchReturnValue ret = (SearchReturnValue) ((tempVar instanceof SearchReturnValue) ? tempVar : null);
        if (ret != null && ret.getSucceeded()) {
            Iterable<IVdcQueryable> vmList = (Iterable<IVdcQueryable>) ret.getReturnValue();
            for (IVdcQueryable vm_helper : vmList) {
                VM vm = (VM) vm_helper;
                if (vm.getstatus() != VMStatus.Down) {
                    List<VmNetworkInterface> vmInterfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                            .getAllForVm(vm.getId());
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
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_BOND : AuditLogType.NETWORK_REMOVE_BOND_FAILED;
    }
}

package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName") })
public class AttachNetworkToVdsGroupCommand<T extends AttachNetworkToVdsGroupParameter> extends
        VdsGroupCommandBase<T> {

    private static LogCompat log = LogFactoryCompat.getLog(RunVmCommand.class);

    public AttachNetworkToVdsGroupCommand(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getVdsGroupId());
    }

    private network getNetwork() {
        return getParameters().getNetwork();
    }

    public String getNetworkName() {
        return getNetwork().getname();
    }

    @Override
    protected void executeCommand() {
        if(createNetworkCluster()) {
            SetNetworkStatus(getVdsGroupId(), getNetwork());
            setSucceeded(true);
        }
    }

    private boolean createNetworkCluster() {
        DbFacade.getInstance().getNetworkClusterDAO().save(
                new network_cluster(getVdsGroupId(), getNetwork().getId(),
                            NetworkStatus.Operational.getValue(), false));

        return true;
    }

    public static void SetNetworkStatus(Guid vdsGroupId, final network net) {
        NetworkStatus status = NetworkStatus.Operational;
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDAO().get(vdsGroupId);
        // Search all vds in cluster that have the specify network, if not the
        // network is not active
        SearchParameters searchParams = new SearchParameters("hosts: cluster = "
                + vdsGroup.getname(), SearchType.VDS);
        searchParams.setMaxCount(Integer.MAX_VALUE);
        List<VDS> vdsList = (List) Backend.getInstance()
                .runInternalQuery(VdcQueryType.Search, searchParams).getReturnValue();

        for (VDS vds : vdsList) {
            if (vds.getstatus() != VDSStatus.Up) {
                continue;
            }
            List<VdsNetworkInterface> interfaces = (List<VdsNetworkInterface>) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.GetVdsInterfacesByVdsId,
                            new GetVdsByVdsIdParameters(vds.getvds_id())).getReturnValue();
            // Interface iface = null; //LINQ 31899 interfaces.FirstOrDefault(i
            // => i.network_name == net.name);
            VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                @Override
                public boolean eval(VdsNetworkInterface i) {
                    return StringHelper.EqOp(i.getNetworkName(), net.getname());
                }
            });
            if (iface == null) {
                status = NetworkStatus.NonOperational;
                break;
            }
        }

        List<network_cluster> all = DbFacade.getInstance()
                .getNetworkClusterDAO().getAllForCluster(vdsGroupId);
        for (network_cluster nc : all) {
            if (net.getId().equals(nc.getnetwork_id())) {
                nc.setstatus(status.getValue());
                DbFacade.getInstance().getNetworkClusterDAO().updateStatus(nc);
                break;
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && noConflictingNetwork() && VdsGroupExists();
    }


    private boolean noConflictingNetwork() {
        if (networkExists()) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_ALREADY_ATTACH_TO_CLUSTER);
            return false;
        }
        return true;
    }

    private boolean networkExists() {
        List<network> networks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(getVdsGroupId());
        return LinqUtils.firstOrNull(networks, new Predicate<network>() {
            @Override
            public boolean eval(network nw) {
                return StringHelper.EqOp(nw.getname(), getNetworkName());
            }
        }) != null;
    }

    private boolean VdsGroupExists() {
        if(!vdsGroupInDb()) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        return true;
    }

    private boolean vdsGroupInDb() {
       return getVdsGroup() != null;
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP
                : AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED;
    }
}

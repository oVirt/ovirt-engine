package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName") })
public class AttachNetworkToVdsGroupCommand<T extends AttachNetworkToVdsGroupParameter> extends
        VdsGroupCommandBase<T> {

    public AttachNetworkToVdsGroupCommand(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getVdsGroupId());
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    public String getNetworkName() {
        return getNetwork().getname();
    }

    @Override
    protected void executeCommand() {
        if (networkExists()) {
            getNetworkClusterDAO().update(getParameters().getNetworkCluster());
        } else {
            getNetworkClusterDAO().save(new network_cluster(getVdsGroupId(), getNetwork().getId(),
                    NetworkStatus.Operational, false, getParameters().getNetworkCluster().isRequired()));
        }
        if (getNetwork().getCluster().getis_display()) {
            getNetworkClusterDAO().setNetworkExclusivelyAsDisplay(getVdsGroupId(), getNetwork().getId());
        }
        SetNetworkStatus(getVdsGroupId(), getNetwork());
        setSucceeded(true);
    }

    public static void SetNetworkStatus(Guid vdsGroupId, final Network net) {
        NetworkStatus status = NetworkStatus.Operational;
        network_cluster networkCluster =
                DbFacade.getInstance().getNetworkClusterDAO().get(new NetworkClusterId(vdsGroupId, net.getId()));

        if (networkCluster != null) {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDAO().get(vdsGroupId);

            // Search all vds in cluster that have the specify network, if not the
            // network is not active
            SearchParameters searchParams = new SearchParameters("hosts: cluster = "
                    + vdsGroup.getname(), SearchType.VDS);
            searchParams.setMaxCount(Integer.MAX_VALUE);
            List<VDS> vdsList = (List) Backend.getInstance()
                    .runInternalQuery(VdcQueryType.Search, searchParams).getReturnValue();

            if (networkCluster.isRequired()) {
                for (VDS vds : vdsList) {
                    if (vds.getstatus() != VDSStatus.Up) {
                        continue;
                    }
                    List<VdsNetworkInterface> interfaces = (List<VdsNetworkInterface>) Backend
                            .getInstance()
                            .runInternalQuery(VdcQueryType.GetVdsInterfacesByVdsId,
                                    new GetVdsByVdsIdParameters(vds.getId())).getReturnValue();
                    VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                        @Override
                        public boolean eval(VdsNetworkInterface i) {
                            return StringUtils.equals(i.getNetworkName(), net.getname());
                        }
                    });
                    if (iface == null) {
                        status = NetworkStatus.NonOperational;
                        break;
                    }
                }
            }

            networkCluster.setstatus(status);
            DbFacade.getInstance().getNetworkClusterDAO().updateStatus(networkCluster);
        }
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && VdsGroupExists() && changesAreClusterCompatible() && logicalNetworkExists();
    }

    private boolean logicalNetworkExists() {
        if (getNetworkDAO().get(getParameters().getNetworkCluster().getnetwork_id()) != null) {
            return true;
        }

        addCanDoActionMessage(VdcBllMessages.NETWROK_NOT_EXISTS);
        return false;
    }

    private boolean changesAreClusterCompatible() {
        if (getParameters().getNetwork().isVmNetwork() == false) {
            boolean isSupported = Config.<Boolean> GetValue(ConfigValues.NonVmNetworkSupported, getVdsGroup().getcompatibility_version().getValue());
            if (!isSupported) {
                addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
                return false;
            }
        }
        return true;
    }

    private boolean networkExists() {
        List<network_cluster> networks = getNetworkClusterDAO().getAllForCluster(getVdsGroupId());
        for (network_cluster network_cluster : networks) {
            if (network_cluster.getnetwork_id().equals(
                    getParameters().getNetworkCluster().getnetwork_id())) {
                return true;
            }
        }

        return false;
    }

    private boolean VdsGroupExists() {
        if (!vdsGroupInDb()) {
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

package org.ovirt.engine.core.bll;

import java.util.List;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * This command will try to migrate all the vds vms (if needed) and move the vds
 * to Non-Operational state
 */
public class SetNonOperationalVdsCommand<T extends SetNonOperationalVdsParameters> extends MaintananceVdsCommand<T> {

    public SetNonOperationalVdsCommand(T parameters) {
        super(parameters);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getSaveToDb()) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVdsId(),
                                    VDSStatus.NonOperational,
                                    getParameters().getNonOperationalReason()));
        }

        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                // migrate vms according to cluster migrateOnError option
                switch (getVdsGroup().getMigrateOnError()) {
                    case YES:
                        MigrateAllVms();
                        break;
                    case HA_ONLY:
                        MigrateAllVms(true);
                        break;
                }
            }
        });

        StringBuilder sb = new StringBuilder();

        if (getParameters().getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE) {
            AddCustomValue("VdsGroupName", getVds().getvds_group_name());
            List<network> clusterNetworks =
                    DbFacade.getInstance().getNetworkDAO().getAllForCluster(getVds().getvds_group_id());
            List<VdsNetworkInterface> interfaces =
                    DbFacade.getInstance().getInterfaceDAO().getAllInterfacesForVds(getVdsId());
            for (final network net : clusterNetworks) {
                // LINQ 31899
                // if (interfaces.FirstOrDefault(i => i.network_name ==
                // net.name) == null)
                // {
                // AppendCustomValue("Networks", net.name, ", ");
                // }
                if (null == LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                    @Override
                    public boolean eval(VdsNetworkInterface i) {
                        return StringHelper.EqOp(i.getNetworkName(), net.getname());
                    }
                })) {
                    AppendCustomValue("Networks", net.getname(), ", ");
                    sb.append(net.getname()).append(", ");
                }
            }
            if (sb.length() > 0) {
                log.infoFormat("Host '{0}' is set to Non-Operational, it is missing the following networks: '{1}'",
                        getVds().getvds_name(), sb.toString());
            }
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            result = false;
        }
        return result;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getParameters().getNonOperationalReason()) {
            case NETWORK_UNREACHABLE:
                return (getSucceeded()) ? AuditLogType.VDS_SET_NONOPERATIONAL_NETWORK
                        : AuditLogType.VDS_SET_NONOPERATIONAL_FAILED;
            case STORAGE_DOMAIN_UNREACHABLE:
                return (getSucceeded()) ? AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN
                        : AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN_FAILED;
            case TIMEOUT_RECOVERING_FROM_CRASH:
                return AuditLogType.VDS_RECOVER_FAILED;
            case KVM_NOT_RUNNING:
                return AuditLogType.VDS_RUN_IN_NO_KVM_MODE;
            case VERSION_INCOMPATIBLE_WITH_CLUSTER:
                this.AddCustomValue("CompatibilityVersion", getVdsGroup().getcompatibility_version().toString());
                this.AddCustomValue("VdsSupportedVersions", getVds().getsupported_cluster_levels());
                return AuditLogType.VDS_VERSION_NOT_SUPPORTED_FOR_CLUSTER;
            default:
                return (getSucceeded()) ? AuditLogType.VDS_SET_NONOPERATIONAL : AuditLogType.VDS_SET_NONOPERATIONAL_FAILED;
        }
    }

    private static Log log = LogFactory.getLog(SetNonOperationalVdsCommand.class);
}

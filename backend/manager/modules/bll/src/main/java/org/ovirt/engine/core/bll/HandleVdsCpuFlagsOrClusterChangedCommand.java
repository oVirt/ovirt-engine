package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class HandleVdsCpuFlagsOrClusterChangedCommand<T extends VdsActionParameters> extends VdsCommand<T> {
    public HandleVdsCpuFlagsOrClusterChangedCommand(T parameters) {
        super(parameters);
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

    private boolean _hasFlags = true;

    @Override
    protected void executeCommand() {
        String vdsGroupCpuName = getVds().getvds_group_cpu_name();
        boolean foundCPU = true;
        // if cluster doesnt have cpu then get the cpu from the vds
        if (StringHelper.isNullOrEmpty(vdsGroupCpuName)) {
            ServerCpu sc = CpuFlagsManagerHandler.FindMaxServerCpuByFlags(getVds().getcpu_flags(), getVds()
                    .getvds_group_compatibility_version());
            if (sc == null) {
                // if there are flags and no cpu found, mark to be non
                // operational
                if (!StringHelper.isNullOrEmpty(getVds().getcpu_flags())) {
                    foundCPU = false;
                } else {
                    _hasFlags = false;
                }
                log.errorFormat("Could not find server cpu for server {0}:{1}, flags: {2}", getVdsId(), getVds()
                        .getvds_name(), getVds().getcpu_flags());
            } else {
                // update group with the cpu name
                VDSGroup grp = DbFacade.getInstance().getVdsGroupDAO().get(getVds().getvds_group_id());
                grp.setcpu_name(sc.getCpuName());

                // use suppress in order to update group even if action fails
                // (out of the transaction)
                VdsGroupOperationParameters tempVar = new VdsGroupOperationParameters(grp);
                tempVar.setTransactionScopeOption(TransactionScopeOption.Suppress);
                tempVar.setIsInternalCommand(true);
                Backend.getInstance().runInternalAction(VdcActionType.UpdateVdsGroup, tempVar);

                vdsGroupCpuName = sc.getCpuName();
            }
        }

        List<String> missingFlags = CpuFlagsManagerHandler.missingServerCpuFlags(vdsGroupCpuName, getVds()
                .getcpu_flags(), getVds().getvds_group_compatibility_version());
        if (!StringHelper.isNullOrEmpty(getVds().getcpu_flags()) && (!foundCPU || missingFlags != null)) {
            if (missingFlags != null) {
                AddCustomValue("CpuFlags", StringUtils.join(missingFlags, ", "));
                if (missingFlags.contains("nx")) {
                    AuditLogableBase logable = new AuditLogableBase(getVds().getvds_id());
                    AuditLogDirector.log(logable, AuditLogType.CPU_FLAGS_NX_IS_MISSING);
                }
            }

            SetNonOperationalVdsParameters tempVar2 = new SetNonOperationalVdsParameters(getVdsId(),
                    NonOperationalReason.CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER);
            tempVar2.setSaveToDb(true);
            Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar2);
        } else {
            // if no need to change to non operational then dont log the command
            setCommandShouldBeLogged(false);
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        // check first if no flags and then if succeeded
        return (!_hasFlags) ? AuditLogType.VDS_CPU_RETRIEVE_FAILED : AuditLogType.VDS_CPU_LOWER_THAN_CLUSTER;
    }

    private static LogCompat log = LogFactoryCompat.getLog(HandleVdsCpuFlagsOrClusterChangedCommand.class);
}

package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@NonTransactiveCommandAttribute
public class HandleVdsCpuFlagsOrClusterChangedCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    private boolean _hasFlags = true;

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

    @Override
    protected void executeCommand() {
        String vdsGroupCpuName = getVds().getVdsGroupCpuName();
        boolean foundCPU = true;
        // if cluster doesn't have cpu then get the cpu from the vds
        if (StringUtils.isEmpty(vdsGroupCpuName)) {
            ServerCpu sc = CpuFlagsManagerHandler.FindMaxServerCpuByFlags(getVds().getCpuFlags(), getVds()
                    .getVdsGroupCompatibilityVersion());
            if (sc == null) {
                // if there are flags and no cpu found, mark to be non
                // operational
                if (!StringUtils.isEmpty(getVds().getCpuFlags())) {
                    foundCPU = false;
                } else {
                    _hasFlags = false;
                }
                log.errorFormat("Could not find server cpu for server {0}:{1}, flags: {2}", getVdsId(), getVds()
                        .getName(), getVds().getCpuFlags());
            } else {
                // update group with the cpu name
                VDSGroup grp = DbFacade.getInstance().getVdsGroupDao().get(getVds().getVdsGroupId());
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
                .getCpuFlags(), getVds().getVdsGroupCompatibilityVersion());
        if (!StringUtils.isEmpty(getVds().getCpuFlags()) && (!foundCPU || missingFlags != null)) {
            if (missingFlags != null) {
                addCustomValue("CpuFlags", StringUtils.join(missingFlags, ", "));
                if (missingFlags.contains("nx")) {
                    AuditLogableBase logable = new AuditLogableBase(getVds().getId());
                    AuditLogDirector.log(logable, AuditLogType.CPU_FLAGS_NX_IS_MISSING);
                }
            }

            SetNonOperationalVdsParameters tempVar2 = new SetNonOperationalVdsParameters(getVdsId(),
                    NonOperationalReason.CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER);
            tempVar2.setSaveToDb(true);
            Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar2,  ExecutionHandler.createInternalJobContext());
        } else {
            // if no need to change to non operational then don't log the command
            setCommandShouldBeLogged(false);
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        // check first if no flags and then if succeeded
        return (!_hasFlags) ? AuditLogType.VDS_CPU_RETRIEVE_FAILED : AuditLogType.VDS_CPU_LOWER_THAN_CLUSTER;
    }
}

package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

@NonTransactiveCommandAttribute
public class HandleVdsCpuFlagsOrClusterChangedCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    private ServerCpu maxServerCPU;

    public HandleVdsCpuFlagsOrClusterChangedCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean result = true;
        if (getVds() == null) {
            addValidationMessage(EngineMessage.VDS_INVALID_SERVER_ID);
            result = false;
        }
        return result;
    }

    @Override
    protected void executeCommand() {
        setMaxServerCPU(getCpuFlagsManagerHandler().findMaxServerCpuByFlags(
                getVds().getCpuFlags(),
                getVds().getClusterCompatibilityVersion()));

        if (!maxServerFound()) {
            SetNonOperationalVdsParameters params = new SetNonOperationalVdsParameters(getVdsId(),
                    NonOperationalReason.CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION);

            runInternalAction(ActionType.SetNonOperationalVds,
                    params,
                    ExecutionHandler.createInternalJobContext(getContext()));
        } else {
            updateClusterCpuInfo(getMaxServerCpu().getCpuName(), getMaxServerCpu().getArchitecture());

            if (!architecturesMatch()) {
                addCustomValue("VdsArchitecture", getMaxServerCpu().getArchitecture().name());
                addCustomValue("ClusterArchitecture", getCluster().getArchitecture().name());

                SetNonOperationalVdsParameters params = new SetNonOperationalVdsParameters(getVdsId(),
                        NonOperationalReason.ARCHITECTURE_INCOMPATIBLE_WITH_CLUSTER);

                runInternalAction(ActionType.SetNonOperationalVds,
                        params,
                        ExecutionHandler.createInternalJobContext(getContext()));
            } else {
                List<String> missingFlags = getCpuFlagsManagerHandler().missingClusterCpuFlags(
                        getCluster().getCpuFlags(),
                        getVds().getCpuFlags());

                if (!cpuFlagsMatch(missingFlags)) {
                    addCustomValue("CpuFlags", StringUtils.join(missingFlags, ", "));
                    if (missingFlags.contains("nx")) {
                        auditLogDirector.log(this, AuditLogType.CPU_FLAGS_NX_IS_MISSING);
                    }
                    SetNonOperationalVdsParameters params = new SetNonOperationalVdsParameters(getVdsId(),
                            NonOperationalReason.CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER);
                    runInternalAction(ActionType.SetNonOperationalVds,
                            params,
                            ExecutionHandler.createInternalJobContext(getContext()));
                } else {
                    // if no need to change to non operational then don't log the command
                    setCommandShouldBeLogged(false);
                }
            }
        }
        setSucceeded(true);
    }

    private void updateClusterCpuInfo(String cpuName, ArchitectureType architecture) {
        // use suppress in order to update group even if action fails
        // (out of the transaction)
        ClusterOperationParameters params = new ClusterOperationParameters(getCluster());
        params.setTransactionScopeOption(TransactionScopeOption.Suppress);
        params.setIsInternalCommand(true);

        if (maxServerFound() &&
                (StringUtils.isEmpty(getCluster().getCpuName()) ||
                        ArchitectureType.undefined.equals(getCluster().getArchitecture()))) {
            getCluster().setCpuName(cpuName);
            getCluster().setArchitecture(architecture);
        }

        updateMigrateOnError(getCluster());

        runInternalAction(ActionType.UpdateCluster, params);
    }

    private boolean architecturesMatch() {
        return getCluster().getArchitecture() == ArchitectureType.undefined ||
               getCluster().getArchitecture() == getMaxServerCpu().getArchitecture();
    }

    private boolean cpuFlagsMatch(List<String> missingFlags) {
        return missingFlags.isEmpty();
    }

    private boolean vdsContainsFlags() {
        return !StringUtils.isEmpty(getVds().getCpuFlags());
    }

    private void updateMigrateOnError(Cluster group) {
        ArchitectureType arch = getArchitecture(group);

        boolean isMigrationSupported = FeatureSupported.isMigrationSupported(arch, group.getCompatibilityVersion());

        if (!isMigrationSupported) {
            group.setMigrateOnError(MigrateOnErrorOptions.NO);
        }
    }

    protected ArchitectureType getArchitecture(Cluster group) {
        if (StringUtils.isNotEmpty(group.getCpuName())) {
            return getCpuFlagsManagerHandler().getArchitectureByCpuName(group.getCpuName(),
                    group.getCompatibilityVersion());
        }

        return group.getArchitecture();
    }

    public void setMaxServerCPU(ServerCpu maxServerCPU) {
        this.maxServerCPU = maxServerCPU;
    }

    private ServerCpu getMaxServerCpu() {
        return maxServerCPU;
    }

    private boolean maxServerFound() {
        return maxServerCPU != null;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (!maxServerFound()) {
            return AuditLogType.CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION;
        } else if (!architecturesMatch()) {
            return AuditLogType.VDS_ARCHITECTURE_NOT_SUPPORTED_FOR_CLUSTER;
        } else if (!vdsContainsFlags()) {
            return AuditLogType.VDS_CPU_RETRIEVE_FAILED;
        } else {
            return AuditLogType.VDS_CPU_LOWER_THAN_CLUSTER;
        }
    }
}


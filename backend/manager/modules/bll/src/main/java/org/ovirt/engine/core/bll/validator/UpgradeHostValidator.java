package org.ovirt.engine.core.bll.validator;

import java.util.Arrays;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.CpuUtils;

public class UpgradeHostValidator {

    private VDS host;

    private Cluster cluster;

    public UpgradeHostValidator(VDS host, Cluster cluster) {
        this.host = host;
        this.cluster = cluster;
    }

    public ValidationResult hostExists() {
        return ValidationResult.failWith(EngineMessage.VDS_INVALID_SERVER_ID).when(host == null);
    }

    public ValidationResult statusSupportedForHostUpgrade() {
        return ValidationResult.failWith(EngineMessage.CANNOT_UPGRADE_HOST_STATUS_ILLEGAL)
                .unless(ActionUtils.canExecute(Arrays.asList(host), VDS.class, ActionType.UpgradeHost));
    }

    public ValidationResult statusSupportedForHostUpgradeCheck() {
        return ValidationResult.failWith(EngineMessage.CANNOT_CHECK_FOR_HOST_UPGRADE_STATUS_ILLEGAL)
                .unless(ActionUtils.canExecute(Arrays.asList(host), VDS.class, ActionType.HostUpgradeCheck));
    }

    public ValidationResult statusSupportedForHostUpgradeInternal() {
        return ValidationResult.failWith(EngineMessage.CANNOT_UPGRADE_HOST_STATUS_ILLEGAL)
                .when(host.getStatus() != VDSStatus.Maintenance);
    }

    public ValidationResult updatesAvailable() {
        return ValidationResult.failWith(EngineMessage.NO_AVAILABLE_UPDATES_FOR_HOST)
                .unless(host.isUpdateAvailable());
    }

    public ValidationResult hostWasInstalled() {
        return ValidationResult.failWith(EngineMessage.CANNOT_UPGRADE_HOST_WITHOUT_OS)
                .when(host.getHostOs() == null);
    }

    public ValidationResult clusterCpuSecureAndNotAffectedByTsxRemoval() {
        return ValidationResult.failWith(EngineMessage.CANNOT_UPGRADE_HOST_CLUSTER_CPU_AFFECTED_BY_TSX_REMOVAL)
                .when(CpuUtils.isCpuSecureAndAffectedByTsxRemoval(cluster.getCpuName())
                        && !cluster.getCpuFlags().contains("-noTSX"));
    }
}

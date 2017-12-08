package org.ovirt.engine.core.bll.quota;


import java.text.DecimalFormat;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;

public class QuotaManagerAuditLogger {
    private static final DecimalFormat percentageFormatter = new DecimalFormat("#.##");

    private AuditLogable auditLogable;
    private AuditLogDirector auditLogDirector;

    public QuotaManagerAuditLogger(AuditLogable auditLogable,
            AuditLogDirector auditLogDirector) {
        this.auditLogable = auditLogable;
        this.auditLogDirector = auditLogDirector;
    }

    public void log(AuditLogType auditLogType) {
        auditLogDirector.log(auditLogable, auditLogType);
    }

    public void logStorageThresholdExceeded(String quotaName, Guid quotaId, double storageUsagePercentage) {
        addQuotaInfo(quotaName, quotaId);
        addStorageUtilization(storageUsagePercentage);

        auditLogDirector.log(auditLogable, AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD);
    }

    public void logStorageLimitExceeded(String quotaName, Guid quotaId, double storageUsagePercentage) {
        addQuotaInfo(quotaName, quotaId);
        addStorageUtilization(storageUsagePercentage);

        auditLogDirector.log(auditLogable, AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT);
    }

    public void logStorageGraceExceeded(String quotaName,
            Guid quotaId,
            double storageUsagePercentage,
            double storageRequestedPercentage,
            boolean hardEnforcement) {

        addQuotaInfo(quotaName, quotaId);
        addStorageUtilization(storageUsagePercentage);
        addStorageRequested(storageRequestedPercentage);

        auditLogDirector.log(auditLogable, hardEnforcement ?
                AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT :
                AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT_PERMISSIVE_MODE);
    }

    public void logClusterThresholdExceeded(String quotaName,
            Guid quotaId,
            Double cpuCurrentPercentage,
            Double memCurrentPercentage) {

        addQuotaInfo(quotaName, quotaId);
        addClusterUtilization(cpuCurrentPercentage, memCurrentPercentage);

        auditLogDirector.log(auditLogable, AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_THRESHOLD);
    }

    public void logClusterLimitExceeded(String quotaName,
            Guid quotaId,
            Double cpuCurrentPercentage,
            Double memCurrentPercentage) {

        addQuotaInfo(quotaName, quotaId);
        addClusterUtilization(cpuCurrentPercentage, memCurrentPercentage);

        auditLogDirector.log(auditLogable, AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_LIMIT);
    }

    public void logClusterGraceExceeded(String quotaName,
            Guid quotaId,
            Double cpuCurrentPercentage,
            Double cpuRequestPercentage,
            Double memCurrentPercentage,
            Double memRequestPercentage,
            boolean hardEnforcement) {

        addQuotaInfo(quotaName, quotaId);
        addClusterUtilization(cpuCurrentPercentage, memCurrentPercentage);
        addClusterRequested(cpuRequestPercentage, memRequestPercentage);

        auditLogDirector.log(auditLogable, hardEnforcement ?
                AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT:
                AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT_PERMISSIVE_MODE);
    }

    private void addQuotaInfo(String quotaName, Guid quotaId) {
        auditLogable.addCustomValue("QuotaName", quotaName);
        auditLogable.setQuotaNameForLog(quotaName);
        auditLogable.setQuotaIdForLog(quotaId);
    }

    private void addStorageUtilization(double storageUsagePercentage) {
        auditLogable.addCustomValue("CurrentStorage", percentageFormatter.format(storageUsagePercentage));
    }

    private void addStorageRequested(double storageRequestedPercentage) {
        auditLogable.addCustomValue("Requested", percentageFormatter.format(storageRequestedPercentage));
    }

    private void addClusterCustomValue(String valueName, Double cpu, Double memory) {
        StringBuilder sb = new StringBuilder();
        if (cpu != null) {
            sb.append("vcpu:").append(percentageFormatter.format(cpu)).append("% ");
        }
        if (memory != null) {
            sb.append("mem:").append(percentageFormatter.format(memory)).append("% ");
        }
        auditLogable.addCustomValue(valueName, sb.toString());
    }

    private void addClusterUtilization(Double cpu, Double memory) {
        addClusterCustomValue("Utilization", cpu, memory);
    }

    private void addClusterRequested(Double cpu, Double memory) {
        addClusterCustomValue("Requested", cpu, memory);
    }
}

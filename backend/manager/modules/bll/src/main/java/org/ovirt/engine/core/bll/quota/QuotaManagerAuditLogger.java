package org.ovirt.engine.core.bll.quota;


import java.text.DecimalFormat;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class QuotaManagerAuditLogger {
    private static final DecimalFormat percentageFormatter = new DecimalFormat("#.##");

    protected void addCustomValuesStorage(AuditLogableBase auditLogableBase,
            String quotaName,
            Guid quotaId,
            double storageUsagePercentage,
            double storageRequestedPercentage) {
        auditLogableBase.addCustomValue("QuotaName", quotaName);
        auditLogableBase.addCustomValue("CurrentStorage", percentageFormatter.format(storageUsagePercentage));
        auditLogableBase.addCustomValue("Requested", percentageFormatter.format(storageRequestedPercentage));
        auditLogableBase.setQuotaNameForLog(quotaName);
        auditLogableBase.setQuotaIdForLog(quotaId);
    }

    protected void addCustomValuesCluster(AuditLogableBase auditLogableBase,
            String quotaName,
            Guid quotaId,
            double cpuCurrentPercentage,
            double cpuRequestPercentage,
            double memCurrentPercentage,
            double memRequestPercentage,
            boolean cpuOverLimit,
            boolean memOverLimit) {

        auditLogableBase.addCustomValue("QuotaName", quotaName);

        StringBuilder currentUtilization = new StringBuilder();
        if (cpuOverLimit) {
            currentUtilization.append("vcpu:").append(percentageFormatter.format(cpuCurrentPercentage)).append("% ");
        }
        if (memOverLimit) {
            currentUtilization.append("mem:").append(percentageFormatter.format(memCurrentPercentage)).append("%");
        }

        StringBuilder request = new StringBuilder();
        if (cpuOverLimit) {
            request.append("vcpu:").append(percentageFormatter.format(cpuRequestPercentage)).append("% ");
        }
        if (memOverLimit) {
            request.append("mem:").append(percentageFormatter.format(memRequestPercentage)).append("%");
        }

        auditLogableBase.addCustomValue("Utilization", currentUtilization.toString());
        auditLogableBase.addCustomValue("Requested", request.toString());
        auditLogableBase.setQuotaNameForLog(quotaName);
        auditLogableBase.setQuotaIdForLog(quotaId);
    }

    public void auditLog(AuditLogType auditLogType, AuditLogableBase auditLogable) {
        if (auditLogType != null) {
            new AuditLogDirector().log(auditLogable, auditLogType);
        }
    }
}

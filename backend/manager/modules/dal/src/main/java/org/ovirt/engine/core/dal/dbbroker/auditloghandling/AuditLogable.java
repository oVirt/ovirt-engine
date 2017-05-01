package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;

public interface AuditLogable {
    Guid getVmId();

    void setVmId(Guid vmId);

    String getVmName();

    void setVmName(String vmName);

    Guid getUserId();

    void setUserId(Guid userId);

    String getUserName();

    void setUserName(String userName);

    Guid getVdsId();

    void setVdsId(Guid vdsId);

    String getVdsName();

    void setVdsName(String vdsName);

    Guid getVmTemplateId();

    void setVmTemplateId(Guid vmTemplateId);

    String getVmTemplateName();

    void setVmTemplateName(String vmTemplateName);

    Guid getClusterId();

    void setClusterId(Guid clusterId);

    String getClusterName();

    void setClusterName(String clusterName);

    Guid getStoragePoolId();

    void setStoragePoolId(Guid storagePoolId);

    String getStoragePoolName();

    void setStoragePoolName(String storagePoolName);

    Guid getStorageDomainId();

    void setStorageDomainId(Guid storageDomainId);

    String getStorageDomainName();

    void setStorageDomainName(String storageDomainName);

    String getGlusterVolumeName();

    void setGlusterVolumeName(String glusterVolumeName);

    Guid getQuotaIdForLog();

    void setQuotaIdForLog(Guid quotaIdForLog);

    String getQuotaNameForLog();

    void setQuotaNameForLog(String quotaNameForLog);

    String getQuotaEnforcementType();

    void setQuotaEnforcementType(String quotaEnforcementType);

    Guid getJobId();

    void setJobId(Guid jobId);

    Guid getBrickId();

    void setBrickId(Guid brickId);

    String getBrickPath();

    void setBrickPath(String brickPath);

    String getReason();

    void setReason(String reason);

    boolean isRepeatable();

    void setRepeatable(boolean repeatable);

    Map<String, String> getCustomValues();

    String getCorrelationId();

    void setCorrelationId(String correlationId);

    Guid getGlusterVolumeId();

    void setGlusterVolumeId(Guid glusterVolumeId);

    String getCustomId();

    void setCustomId(String customId);

    String getOrigin();

    void setOrigin(String origin);

    int getCustomEventId();

    void setCustomEventId(int customEventId);

    int getEventFloodInSec();

    void setEventFloodInSec(int eventFloodInSec);

    String getCustomData();

    void setCustomData(String customData);

    boolean isExternal();

    void setExternal(boolean external);

    String getCallStack();

    void setCallStack(String callStack);

    String getCompatibilityVersion();

    void setCompatibilityVersion(String compatibilityVersion);

    AuditLogable addCustomValue(final String name, final String value);

    /**
     * Set the properties of a given {@link AuditLog} entity to be persisted as a representative of the current event
     *
     * @param auditLog
     *            an entity which was initially set with main {@link AuditLogableBase} data.
     */
    default void setPropertiesForAuditLog(AuditLog auditLog) {
        auditLog.setStorageDomainId(getStorageDomainId());
        auditLog.setStorageDomainName(getStorageDomainName());
        auditLog.setStoragePoolId(getStoragePoolId());
        auditLog.setStoragePoolName(getStoragePoolName());
        auditLog.setClusterId(getClusterId());
        auditLog.setClusterName(getClusterName());
        auditLog.setCorrelationId(getCorrelationId());
        auditLog.setJobId(getJobId());
        auditLog.setGlusterVolumeId(getGlusterVolumeId());
        auditLog.setGlusterVolumeName(getGlusterVolumeName());
        auditLog.setExternal(isExternal());
        auditLog.setQuotaId(getQuotaIdForLog());
        auditLog.setQuotaName(getQuotaNameForLog());
        auditLog.setCallStack(getCallStack());
        auditLog.setBrickId(getBrickId());
        auditLog.setBrickPath(getBrickPath());
        auditLog.setRepeatable(isRepeatable());
    }

    /**
     * Sets the call stack string from a Throwable object Also, the updateCallStackFromThrowable can be used in case you
     * have a Throwable object with the call stack details
     *
     * @param throwable
     *            the Throwable object containing the call stack. Can be null, which will cause no changes to this
     *            object
     */
    default void updateCallStackFromThrowable(Throwable throwable) {
        if (throwable != null) {
            setCallStack(ExceptionUtils.getStackTrace(throwable));
        }
    }

    /**
     * @return a new instance of {@link AuditLog} representing data of this instance
     */
    AuditLog createAuditLog(AuditLogType logType, String message);
}

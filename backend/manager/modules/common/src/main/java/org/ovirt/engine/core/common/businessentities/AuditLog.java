package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;

public class AuditLog implements Queryable {
    private static final long serialVersionUID = -2808392095455280186L;
    public static final String OVIRT_ORIGIN = "oVirt";

    private long auditLogId;
    private Date logTime;
    private String message;
    private Guid userId;
    @NotNull
    private String userName;
    private Guid quotaId;
    @NotNull
    private String quotaName;
    private Guid vdsId;
    @NotNull
    private String vdsName;
    private Guid vmTemplateId;
    @NotNull
    private String vmTemplateName;
    private Guid vmId;
    @NotNull
    private String vmName;
    private Guid storagePoolId;
    @NotNull
    private String storagePoolName;
    private Guid storageDomainId;
    @NotNull
    private String storageDomainName;
    private Guid clusterId;
    @NotNull
    private String clusterName;
    private int logType;
    private int severity;
    private boolean processed;
    private String correlationId;
    private Guid jobId;
    private Guid glusterVolumeId;
    @NotNull
    private String glusterVolumeName;
    private String origin;
    private String customId;
    private int customEventId;
    private int eventFloodInSec;
    private String customData;
    private boolean external;
    private boolean deleted;
    private String compatibilityVersion;
    private String quotaEnforcementType;
    private String callStack;
    private Guid brickId;
    private String brickPath;

    /**
     * If set to {@code true}, it allows storing to db multiple alerts of the same type for the same host.
     * By default it's set to {@code false}, to ignore (not save) them (backward compatibility). It affects
     * only alerts as other severities are logged always.
     *
     * It's not persisted in db, because it's used only to determine if alert should be stored or not.
     */
    boolean repeatable;

    public AuditLog() {
        this(AuditLogType.UNASSIGNED, AuditLogSeverity.NORMAL);
    }

    public AuditLog(AuditLogType type, AuditLogSeverity severity) {
        this.logType = type.getValue();
        this.severity = severity.getValue();
        this.origin = OVIRT_ORIGIN;
        this.customEventId = -1;
        this.eventFloodInSec = 30;
        this.customData = "";
        this.logTime = new Date();
        this.repeatable = false;
        this.userName = "";
        this.vmName = "";
        this.vdsName = "";
        this.vmTemplateName = "";
        this.quotaName = "";
        this.storagePoolName = "";
        this.storageDomainName = "";
        this.clusterName = "";
        this.glusterVolumeName = "";
    }

    public AuditLog(AuditLogType type,
            AuditLogSeverity severity,
            String message,
            Guid userId,
            String userName,
            Guid vmId,
            String vmName,
            Guid vdsId,
            String vdsName,
            Guid vmTemplateId,
            String vmTemplateName) {
        this(type, severity);
        this.message = message;
        this.userId = userId;
        this.setUserName(userName);
        this.vmId = vmId;
        this.setVmName(vmName);
        this.vdsId = vdsId;
        this.setVdsName(vdsName);
        this.vmTemplateId = vmTemplateId;
        this.setVmTemplateName(vmTemplateName);
        this.quotaName = "";
        this.storagePoolName = "";
        this.storageDomainName = "";
        this.clusterName = "";
        this.glusterVolumeName = "";
    }

    public AuditLog(AuditLogType type,
            AuditLogSeverity severity,
            String message,
            Guid userId,
            String userName,
            Guid vmId,
            String vmName,
            Guid vdsId,
            String vdsName,
            Guid vmTemplateId,
            String vmTemplateName,
            String origin,
            String customId,
            int customEventId,
            int eventFloogInSec,
            String customData) {
        this(type,
                severity,
                message,
                userId,
                userName,
                vmId,
                vmName,
                vdsId,
                vdsName,
                vmTemplateId,
                vmTemplateName);
        this.origin = origin;
        this.customId = customId;
        this.customEventId = customEventId;
        this.eventFloodInSec = eventFloogInSec;
        this.customData = customData;
    }

    public AuditLog(AuditLogType type,
            AuditLogSeverity severity,
            String message,
            Guid userId,
            String userName,
            Guid vmId,
            String vmName,
            Guid vdsId,
            String vdsName,
            Guid vmTemplateId,
            String vmTemplateName,
            String origin,
            String customId,
            int customEventId,
            int eventFloogInSec,
            Guid brickId,
            String brickPath,
            String customData) {
        this(type,
                severity,
                message,
                userId,
                userName,
                vmId,
                vmName,
                vdsId,
                vdsName,
                vmTemplateId,
                vmTemplateName,
                origin,
                customId,
                customEventId,
                eventFloogInSec,
                customData);
        this.brickId = brickId;
        this.brickPath = brickPath;
    }

    public long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(long auditLogId) {
        this.auditLogId = auditLogId;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? "" : userName;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName == null ? "" : quotaName;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public String getVdsName() {
        return vdsName;
    }

    public void setVdsName(String vdsName) {
        this.vdsName = vdsName == null ? "" : vdsName;
    }

    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    public void setVmTemplateId(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    public String getVmTemplateName() {
        return vmTemplateName;
    }

    public void setVmTemplateName(String vmTemplateName) {
        this.vmTemplateName = vmTemplateName == null ? "" : vmTemplateName;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName == null ? "" : vmName;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String storagePoolName) {
        this.storagePoolName = storagePoolName == null ? "" : storagePoolName;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public String getStorageDomainName() {
        return storageDomainName;
    }

    public void setStorageDomainName(String storageDomainName) {
        this.storageDomainName = storageDomainName == null ? "" : storageDomainName;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? "" : clusterName;
    }

    public AuditLogType getLogType() {
        return AuditLogType.forValue(logType);
    }

    public void setLogType(AuditLogType value) {
        logType = value.getValue();
    }

    // We need log_typeValue for the UI,
    // We dont have the AuditLogType enumeration synchronized,
    // WSDL formatter set the enumeration value according to its string value
    // (enums are strings in WSDL)
    public int getLogTypeValue() {
        return getLogType().getValue();
    }

    public void setLogTypeValue(int value) {
        // Do nothing, this is mockup (WSDL need setter)
    }

    public String getLogTypeName() {
        return getLogType().name();
    }

    public AuditLogSeverity getSeverity() {
        return AuditLogSeverity.forValue(severity);
    }

    public void setSeverity(AuditLogSeverity severity) {
        this.severity = severity.getValue();
    }

    @Override
    public Object getQueryableId() {
        return getAuditLogId();
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getGlusterVolumeId() {
        return glusterVolumeId;
    }

    public void setGlusterVolumeId(Guid glusterVolumeId) {
        this.glusterVolumeId = glusterVolumeId;
    }

    public String getGlusterVolumeName() {
        return glusterVolumeName;
    }

    public void setGlusterVolumeName(String glusterVolumeName) {
        this.glusterVolumeName = glusterVolumeName == null ? "" : glusterVolumeName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public int getCustomEventId() {
        return customEventId;
    }

    public void setCustomEventId(int customEventId) {
        this.customEventId = customEventId;
    }

    public int getEventFloodInSec() {
        return eventFloodInSec;
    }

    public void setEventFloodInSec(int eventFloodInSec) {
        this.eventFloodInSec = eventFloodInSec;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public String getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    public void setQuotaEnforcementType(String quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

    public String getCallStack() {
        return callStack;
    }

    public void setCallStack(String callStack) {
        this.callStack = callStack;
    }

    public Guid getBrickId() {
        return brickId;
    }

    public void setBrickId(Guid brickId) {
        this.brickId = brickId;
    }

    public String getBrickPath() {
        return brickPath;
    }

    public void setBrickPath(String brickPath) {
        this.brickPath = brickPath;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                auditLogId,
                logTime,
                logType,
                message,
                storageDomainId,
                storagePoolId,
                severity,
                userId,
                vdsId,
                quotaId,
                vmId,
                vmTemplateId,
                processed,
                correlationId,
                jobId,
                origin,
                customId,
                customEventId,
                eventFloodInSec,
                customData,
                external,
                deleted,
                callStack
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AuditLog)) {
            return false;
        }
        AuditLog other = (AuditLog) obj;
        return auditLogId == other.auditLogId
                && Objects.equals(logTime, other.logTime)
                && logType == other.logType
                && Objects.equals(message, other.message)
                && Objects.equals(storageDomainId, other.storageDomainId)
                && Objects.equals(storagePoolId, other.storagePoolId)
                && severity == other.severity
                && Objects.equals(userId, other.userId)
                && Objects.equals(vdsId, other.vdsId)
                && Objects.equals(quotaId, other.quotaId)
                && Objects.equals(vmId, other.vmId)
                && Objects.equals(vmTemplateId, other.vmTemplateId)
                && processed == other.processed
                && Objects.equals(correlationId, other.correlationId)
                && Objects.equals(jobId, other.jobId)
                && Objects.equals(origin, other.origin)
                && Objects.equals(customId, other.customId)
                && customEventId == other.customEventId
                && eventFloodInSec == other.eventFloodInSec
                && Objects.equals(customData, other.customData)
                && external == other.external
                && deleted == other.deleted
                && Objects.equals(callStack, other.callStack);
    }

    public String toStringForLogging() {
        StringBuilder sb = new StringBuilder();
        sb.append("Correlation ID: ");
        sb.append(correlationId);
        sb.append(", ");
        if(jobId != null){
            sb.append("Job ID: ");
            sb.append(jobId.toString());
            sb.append(", ");
        }
        sb.append("Call Stack: ");
        sb.append(callStack);
        sb.append(", ");
        sb.append("Custom ID: ");
        sb.append(customId);
        sb.append(", ");
        sb.append("Custom Event ID: ");
        sb.append(customEventId);
        sb.append(", ");
        sb.append("Message: ");
        sb.append(message);
        return sb.toString();
    }
}

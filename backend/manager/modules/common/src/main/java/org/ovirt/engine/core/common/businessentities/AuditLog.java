package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class AuditLog extends IVdcQueryable implements Serializable {
    private static final long serialVersionUID = -2808392095455280186L;

    private long auditLogId;
    private Date logTime;
    private String message;
    private Guid userId;
    private String userName;
    private Guid quotaId;
    private String quotaName;
    private Guid vdsId;
    private String vdsName;
    private Guid vmTemplateId;
    private String vmTemplateName;
    private Guid vmId;
    private String vmName;
    private Guid storagePoolId;
    private String storagePoolName;
    private Guid storageDomainId;
    private String storageDomainName;
    private Guid vdsGroupId;
    private String vdsGroupName;
    private int logType;
    private int severity;
    private boolean processed;
    private String correlationId;
    private Guid jobId;
    private Guid glusterVolumeId;
    private String glusterVolumeName;
    private String origin;
    private int customEventId;
    private int eventFloodInSec;
    private String customData;
    private boolean external;
    private boolean deleted;
    private String compatibilityVersion;
    private String quotaEnforcementType;
    private String callStack;

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
        this.origin = "oVirt";
        this.customEventId = -1;
        this.eventFloodInSec = 30;
        this.customData = "";
        this.logTime = new Date();
        this.repeatable = false;
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
        this.userName = userName;
        this.vmId = vmId;
        this.vmName = vmName;
        this.vdsId = vdsId;
        this.vdsName = vdsName;
        this.vmTemplateId = vmTemplateId;
        this.vmTemplateName = vmTemplateName;
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
        this.customEventId = customEventId;
        this.eventFloodInSec = eventFloogInSec;
        this.customData = customData;
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
        this.userName = userName;
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
        this.quotaName = quotaName;
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
        this.vdsName = vdsName;
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
        this.vmTemplateName = vmTemplateName;
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
        this.vmName = vmName;
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
        this.storagePoolName = storagePoolName;
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
        this.storageDomainName = storageDomainName;
    }

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public void setVdsGroupId(Guid vdsGroupId) {
        this.vdsGroupId = vdsGroupId;
    }

    public String getVdsGroupName() {
        return vdsGroupName;
    }

    public void setVdsGroupName(String vdsGroupName) {
        this.vdsGroupName = vdsGroupName;
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
        this.glusterVolumeName = glusterVolumeName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
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

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (auditLogId ^ (auditLogId >>> 32));
        result = prime * result + (logTime == null ? 0 : logTime.hashCode());
        result = prime * result + logType;
        result = prime * result + (message == null ? 0 : message.hashCode());
        result = prime * result + (storageDomainId == null ? 0 : storageDomainId.hashCode());
        result = prime * result + (storagePoolId == null ? 0 : storagePoolId.hashCode());
        result = prime * result + severity;
        result = prime * result + (userId == null ? 0 : userId.hashCode());
        result = prime * result + (vdsId == null ? 0 : vdsId.hashCode());
        result = prime * result + (quotaId == null ? 0 : quotaId.hashCode());
        result = prime * result + (vmId == null ? 0 : vmId.hashCode());
        result = prime * result + (vmTemplateId == null ? 0 : vmTemplateId.hashCode());
        result = prime * result + (processed ? 1231 : 1237);
        result = prime * result + (correlationId == null ? 0 : correlationId.hashCode());
        result = prime * result + (jobId == null ? 0 : jobId.hashCode());
        result = prime * result + (origin == null ? 0 : origin.hashCode());
        result = prime * result + customEventId;
        result = prime * result + eventFloodInSec;
        result = prime * result + (customData == null ? 0 : customData.hashCode());
        result = prime * result + (external ? 1231 : 1237);
        result = prime * result + (deleted ? 1231 : 1237);
        result = prime * result + (callStack == null ? 0 : callStack.hashCode());
        return result;
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
        return (auditLogId == other.auditLogId
                && ObjectUtils.objectsEqual(logTime, other.logTime)
                && logType == other.logType
                && ObjectUtils.objectsEqual(message, other.message)
                && ObjectUtils.objectsEqual(storageDomainId, other.storageDomainId)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && severity == other.severity
                && ObjectUtils.objectsEqual(userId, other.userId)
                && ObjectUtils.objectsEqual(vdsId, other.vdsId)
                && ObjectUtils.objectsEqual(quotaId, other.quotaId)
                && ObjectUtils.objectsEqual(vmId, other.vmId)
                && ObjectUtils.objectsEqual(vmTemplateId, other.vmTemplateId)
                && processed == other.processed
                && ObjectUtils.objectsEqual(correlationId, other.correlationId)
                && ObjectUtils.objectsEqual(jobId, other.jobId)
                && ObjectUtils.objectsEqual(origin, other.origin)
                && customEventId == other.customEventId
                && eventFloodInSec == other.eventFloodInSec
                && ObjectUtils.objectsEqual(customData, other.customData)
                && external == other.external
                && deleted == other.deleted
                && ObjectUtils.objectsEqual(callStack, other.callStack));
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
        sb.append("Custom Event ID: ");
        sb.append(customEventId);
        sb.append(", ");
        sb.append("Message: ");
        sb.append(message);
        return sb.toString();
    }
}

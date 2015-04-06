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
    private Guid brickId;
    private String brickPath;

    public AuditLog() {
        logType = AuditLogType.UNASSIGNED.getValue();
        severity = AuditLogSeverity.NORMAL.getValue();
        origin = "oVirt";
        customEventId = -1;
        eventFloodInSec = 30;
        customData = "";
    }

    public AuditLog(AuditLogType al_type, AuditLogSeverity al_severity, String al_msg, Guid al_user_id,
            String al_user_name, Guid al_vm_id, String al_vm_name, Guid al_vds_id, String al_vds_name,
            Guid al_vmt_id, String al_vmt_name) {
        this();
        logTime = new Date();
        logType = al_type.getValue();
        severity = al_severity.getValue();
        message = al_msg;
        userId = al_user_id;
        userName = al_user_name;
        vmId = al_vm_id;
        vmName = al_vm_name;
        vdsId = al_vds_id;
        vdsName = al_vds_name;
        vmTemplateId = al_vmt_id;
        vmTemplateName = al_vmt_name;
    }

    public AuditLog(AuditLogType al_type,
            AuditLogSeverity al_severity,
            String al_msg,
            Guid al_user_id,
            String al_user_name,
            Guid al_vm_id,
            String al_vm_name,
            Guid al_vds_id,
            String al_vds_name,
            Guid al_vmt_id,
            String al_vmt_name,
            String origin,
            int customEventId,
            int eventFloogInSec,
            String customData) {
        this(al_type,
                al_severity,
                al_msg,
                al_user_id,
                al_user_name,
                al_vm_id,
                al_vm_name,
                al_vds_id,
                al_vds_name,
                al_vmt_id,
                al_vmt_name);
        this.origin = origin;
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
                customEventId,
                eventFloogInSec,
                customData);
        this.brickId = brickId;
        this.brickPath = brickPath;
    }

    public long getaudit_log_id() {
        return auditLogId;
    }

    public void setaudit_log_id(long value) {
        this.auditLogId = value;
    }

    public Date getlog_time() {
        return this.logTime;
    }

    public void setlog_time(Date value) {
        this.logTime = value;
    }

    public String getmessage() {
        return this.message;
    }

    public void setmessage(String value) {
        this.message = value;
    }

    public Guid getuser_id() {
        return this.userId;
    }

    public void setuser_id(Guid value) {
        this.userId = value;
    }

    public String getuser_name() {
        return this.userName;
    }

    public void setuser_name(String value) {
        this.userName = value;
    }

    public Guid getQuotaId() {
        return this.quotaId;
    }

    public void setQuotaId(Guid value) {
        this.quotaId = value;
    }

    public String getQuotaName() {
        return this.quotaName;
    }

    public void setQuotaName(String value) {
        this.quotaName = value;
    }

    public Guid getvds_id() {
        return this.vdsId;
    }

    public void setvds_id(Guid value) {
        this.vdsId = value;
    }

    public String getvds_name() {
        return this.vdsName;
    }

    public void setvds_name(String value) {
        this.vdsName = value;
    }

    public Guid getvm_template_id() {
        return this.vmTemplateId;
    }

    public void setvm_template_id(Guid value) {
        this.vmTemplateId = value;
    }

    public String getvm_template_name() {
        return this.vmTemplateName;
    }

    public void setvm_template_name(String value) {
        this.vmTemplateName = value;
    }

    public Guid getvm_id() {
        return this.vmId;
    }

    public void setvm_id(Guid value) {
        this.vmId = value;
    }

    public String getvm_name() {
        return this.vmName;
    }

    public void setvm_name(String value) {
        this.vmName = value;
    }

    public Guid getstorage_pool_id() {
        return storagePoolId;
    }

    public void setstorage_pool_id(Guid value) {
        storagePoolId = value;
    }

    public String getstorage_pool_name() {
        return storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        storagePoolName = value;
    }

    public Guid getstorage_domain_id() {
        return storageDomainId;
    }

    public void setstorage_domain_id(Guid value) {
        storageDomainId = value;
    }



    public String getstorage_domain_name() {
        return storageDomainName;
    }

    public void setstorage_domain_name(String value) {
        storageDomainName = value;
    }



    public Guid getvds_group_id() {
        return vdsGroupId;
    }

    public void setvds_group_id(Guid value) {
        vdsGroupId = value;
    }

    public String getvds_group_name() {
        return vdsGroupName;
    }

    public void setvds_group_name(String value) {
        vdsGroupName = value;
    }

    public AuditLogType getlog_type() {
        return AuditLogType.forValue(logType);
    }

    public void setlog_type(AuditLogType value) {
        logType = value.getValue();
    }

    // We need log_typeValue for the UI,
    // We dont have the AuditLogType enumeration synchronized,
    // WSDL formatter set the enumeration value according to its string value
    // (enums are strings in WSDL)
    public int getlog_typeValue() {
        return getlog_type().getValue();
    }

    public void setlog_typeValue(int value) {
        // Do nothing, this is mockup (WSDL need setter)
    }

    public String getlog_type_name() {
        return getlog_type().name();
    }

    public AuditLogSeverity getseverity() {
        return AuditLogSeverity.forValue(severity);
    }

    public void setseverity(AuditLogSeverity value) {
        severity = value.getValue();
    }

    @Override
    public Object getQueryableId() {
        return getaudit_log_id();
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getJobId() {
        return jobId;
    }

    public Guid getGlusterVolumeId() {
        return glusterVolumeId;
    }

    public void setGlusterVolumeId(Guid value) {
        glusterVolumeId = value;
    }

    public String getGlusterVolumeName() {
        return glusterVolumeName;
    }

    public void setGlusterVolumeName(String value) {
        glusterVolumeName = value;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (auditLogId ^ (auditLogId >>> 32));
        result = prime * result + ((logTime == null) ? 0 : logTime.hashCode());
        result = prime * result + logType;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((storageDomainId == null) ? 0 : storageDomainId.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + severity;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((vdsId == null) ? 0 : vdsId.hashCode());
        result = prime * result + ((quotaId == null) ? 0 : quotaId.hashCode());
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
        result = prime * result + ((vmTemplateId == null) ? 0 : vmTemplateId.hashCode());
        result = prime * result + (processed ? 1231 : 1237);
        result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + customEventId;
        result = prime * result + eventFloodInSec;
        result = prime * result + ((customData == null) ? 0 : customData.hashCode());
        result = prime * result + (external ? 1231 : 1237);
        result = prime * result + (deleted ? 1231 : 1237);
        result = prime * result + ((callStack == null) ? 0 : callStack.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
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

    public String getCallStack() {
        return callStack;
    }

    public void setCallStack(String callStack) {
        this.callStack = callStack;
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

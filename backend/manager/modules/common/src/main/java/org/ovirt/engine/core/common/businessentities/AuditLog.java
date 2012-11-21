package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;

@TypeDef(name = "guid", typeClass = GuidType.class)
public class AuditLog extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = -2808392095455280186L;

    private long auditLogId;
    private Date logTime = new Date(0);
    private String message;
    private NGuid userId;
    private String userName;
    private NGuid quotaId;
    private String quotaName;
    private NGuid vdsId;
    private String vdsName;
    private NGuid vmTemplateId;
    private String vmTemplateName;
    private NGuid vmId;
    private String vmName;
    private NGuid storagePoolId;
    private String storagePoolName;
    private NGuid storageDomainId;
    private String storageDomainName;
    private NGuid vdsGroupId;
    private String vdsGroupName;
    private int logType = AuditLogType.UNASSIGNED.getValue();
    private int severity = AuditLogSeverity.NORMAL.getValue();
    private boolean processed = false;
    private String correlationId;
    private NGuid jobId;
    private NGuid glusterVolumeId;
    private String glusterVolumeName;

    public AuditLog() {
    }

    public AuditLog(AuditLogType al_type, AuditLogSeverity al_severity, String al_msg, NGuid al_user_id,
            String al_user_name, NGuid al_vm_id, String al_vm_name, NGuid al_vds_id, String al_vds_name,
            NGuid al_vmt_id, String al_vmt_name) {
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

    public long getaudit_log_id() {
        return this.auditLogId;
    }

    public void setaudit_log_id(long value) {
        this.auditLogId = value;
    }

    public java.util.Date getlog_time() {
        return this.logTime;
    }

    public void setlog_time(java.util.Date value) {
        this.logTime = value;
    }

    public String getmessage() {
        return this.message;
    }

    public void setmessage(String value) {
        final String INCOMPLETE_SIGN = "...";
        int maxAuditLogMessageLength = Config.<Integer> GetValue(ConfigValues.MaxAuditLogMessageLength);
        // truncate message if exceeds configured max length.
        // truncated messages will be ended with "..." to indicate that message
        // is incomplete due to size limits.
        if (value.length() > maxAuditLogMessageLength) {
            value = value.substring(0, maxAuditLogMessageLength - (INCOMPLETE_SIGN.length() + 1)) + INCOMPLETE_SIGN;
        }
        this.message = value;
    }

    public NGuid getuser_id() {
        return this.userId;
    }

    public void setuser_id(NGuid value) {
        this.userId = value;
    }

    public String getuser_name() {
        return this.userName;
    }

    public void setuser_name(String value) {
        this.userName = value;
    }

    public NGuid getQuotaId() {
        return this.quotaId;
    }

    public void setQuotaId(NGuid value) {
        this.quotaId = value;
    }

    public String getQuotaName() {
        return this.quotaName;
    }

    public void setQuotaName(String value) {
        this.quotaName = value;
    }

    public NGuid getvds_id() {
        return this.vdsId;
    }

    public void setvds_id(NGuid value) {
        this.vdsId = value;
    }

    public String getvds_name() {
        return this.vdsName;
    }

    public void setvds_name(String value) {
        this.vdsName = value;
    }

    public NGuid getvm_template_id() {
        return this.vmTemplateId;
    }

    public void setvm_template_id(NGuid value) {
        this.vmTemplateId = value;
    }

    public String getvm_template_name() {
        return this.vmTemplateName;
    }

    public void setvm_template_name(String value) {
        this.vmTemplateName = value;
    }

    public NGuid getvm_id() {
        return this.vmId;
    }

    public void setvm_id(NGuid value) {
        this.vmId = value;
    }

    public String getvm_name() {
        return this.vmName;
    }

    public void setvm_name(String value) {
        this.vmName = value;
    }

    public NGuid getstorage_pool_id() {
        return storagePoolId;
    }

    public void setstorage_pool_id(NGuid value) {
        storagePoolId = value;
    }

    public String getstorage_pool_name() {
        return storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        storagePoolName = value;
    }

    public NGuid getstorage_domain_id() {
        return storageDomainId;
    }

    public void setstorage_domain_id(NGuid value) {
        storageDomainId = value;
    }



    public String getstorage_domain_name() {
        return storageDomainName;
    }

    public void setstorage_domain_name(String value) {
        storageDomainName = value;
    }



    public NGuid getvds_group_id() {
        return vdsGroupId;
    }

    public void setvds_group_id(NGuid value) {
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

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return new java.util.ArrayList<String>();
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

    public void setJobId(NGuid jobId) {
        this.jobId = jobId;
    }

    public NGuid getJobId() {
        return jobId;
    }

    public NGuid getGlusterVolumeId() {
        return glusterVolumeId;
    }

    public void setGlusterVolumeId(NGuid value) {
        glusterVolumeId = value;
    }

    public String getGlusterVolumeName() {
        return glusterVolumeName;
    }

    public void setGlusterVolumeName(String value) {
        glusterVolumeName = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (auditLogId ^ (auditLogId >>> 32));
        result = prime * result + ((logTime == null) ? 0 : logTime.hashCode());
        result = prime * result + (logType * prime);
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((storageDomainId == null) ? 0 : storageDomainId.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + (severity * prime);
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((vdsId == null) ? 0 : vdsId.hashCode());
        result = prime * result + ((quotaId == null) ? 0 : quotaId.hashCode());
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
        result = prime * result + ((vmTemplateId == null) ? 0 : vmTemplateId.hashCode());
        result = prime * result + (processed ? prime : 0);
        result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AuditLog other = (AuditLog) obj;
        if (auditLogId != other.auditLogId)
            return false;
        if (logTime == null) {
            if (other.logTime != null)
                return false;
        } else if (!logTime.equals(other.logTime))
            return false;
        if (logType != other.logType)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (storageDomainId == null) {
            if (other.storageDomainId != null)
                return false;
        } else if (!storageDomainId.equals(other.storageDomainId))
            return false;
        if (storagePoolId == null) {
            if (other.storagePoolId != null)
                return false;
        } else if (!storagePoolId.equals(other.storagePoolId))
            return false;
        if (severity != other.severity)
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (vdsId == null) {
            if (other.vdsId != null)
                return false;
        } else if (!vdsId.equals(other.vdsId))
            return false;
        if (quotaId == null) {
            if (other.quotaId != null)
                return false;
        } else if (!quotaId.equals(other.quotaId))
            return false;
        if (vmId == null) {
            if (other.vmId != null)
                return false;
        } else if (!vmId.equals(other.vmId))
            return false;
        if (vmTemplateId == null) {
            if (other.vmTemplateId != null)
                return false;
        } else if (!vmTemplateId.equals(other.vmTemplateId))
            return false;
        if(processed != other.processed)
            return false;
        if (correlationId == null) {
            if (other.correlationId != null)
                return false;
        } else if (!correlationId.equals(other.correlationId))
            return false;
        if (jobId == null) {
            if (other.jobId != null)
                return false;
        } else if (!jobId.equals(other.jobId))
            return false;
        return true;
    }
}

package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AuditLog")
@Entity
@Table(name = "audit_log")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class AuditLog extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = -2808392095455280186L;

    @Column(name = "processed")
    private boolean processed = false;

    private String correlationId;
    private NGuid jobId;

    public AuditLog() {
    }

    public AuditLog(long audit_log_id, java.util.Date log_time, int log_type, int severity, String message,
            NGuid user_id, String user_name, NGuid vds_id, String vds_name, NGuid vm_id, String vm_name,
            NGuid vm_template_id, String vm_template_name, String correlationId, NGuid jobId, NGuid quotaId, String quotaName) {
        this.auditLogId = audit_log_id;
        this.logTime = log_time;
        this.logType = log_type;
        this.severity = severity;
        this.message = message;
        this.userId = user_id;
        this.userName = user_name;
        this.vdsId = vds_id;
        this.vdsName = vds_name;
        this.vmId = vm_id;
        this.vmName = vm_name;
        this.vmTemplateId = vm_template_id;
        this.vmTemplateName = vm_template_name;
        this.correlationId = correlationId;
        this.jobId = jobId;
        this.quotaId = quotaId;
        this.quotaName = quotaName;
    }

    @Id
    @Column(name = "audit_log_id")
    private long auditLogId;

    @XmlElement
    public long getaudit_log_id() {
        return this.auditLogId;
    }

    public void setaudit_log_id(long value) {
        this.auditLogId = value;
    }

    @Column(name = "log_time", nullable = false)
    private java.util.Date logTime = new java.util.Date(0);

    @XmlElement
    public java.util.Date getlog_time() {
        return this.logTime;
    }

    public void setlog_time(java.util.Date value) {
        this.logTime = value;
    }

    @Column(name = "message", nullable = false)
    private String message;

    @XmlElement
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

    @Column(name = "user_id")
    @Type(type = "guid")
    private NGuid userId;

    @XmlElement
    public NGuid getuser_id() {
        return this.userId;
    }

    public void setuser_id(NGuid value) {
        this.userId = value;
    }

    @Column(name = "user_name", length = 255)
    private String userName;

    @XmlElement
    public String getuser_name() {
        return this.userName;
    }

    public void setuser_name(String value) {
        this.userName = value;
    }

    private NGuid quotaId;

    public NGuid getQuotaId() {
        return this.quotaId;
    }

    public void setQuotaId(NGuid value) {
        this.quotaId = value;
    }

    private String quotaName;

    public String getQuotaName() {
        return this.quotaName;
    }

    public void setQuotaName(String value) {
        this.quotaName = value;
    }

    @Column(name = "vds_id")
    @Type(type = "guid")
    private NGuid vdsId;

    @XmlElement(nillable = true)
    public NGuid getvds_id() {
        return this.vdsId;
    }

    public void setvds_id(NGuid value) {
        this.vdsId = value;
    }

    @Column(name = "vds_name", length = 255)
    private String vdsName;

    @XmlElement
    public String getvds_name() {
        return this.vdsName;
    }

    public void setvds_name(String value) {
        this.vdsName = value;
    }

    @Column(name = "vm_template_id")
    @Type(type = "guid")
    private NGuid vmTemplateId;

    @XmlElement
    public NGuid getvm_template_id() {
        return this.vmTemplateId;
    }

    public void setvm_template_id(NGuid value) {
        this.vmTemplateId = value;
    }

    @Column(name = "vm_template_name", length = 40)
    private String vmTemplateName;

    @XmlElement
    public String getvm_template_name() {
        return this.vmTemplateName;
    }

    public void setvm_template_name(String value) {
        this.vmTemplateName = value;
    }

    @Column(name = "vm_id")
    @Type(type = "guid")
    private NGuid vmId;

    @XmlElement
    public NGuid getvm_id() {
        return this.vmId;
    }

    public void setvm_id(NGuid value) {
        this.vmId = value;
    }

    @Column(name = "vm_name", length = 255)
    private String vmName;

    @XmlElement
    public String getvm_name() {
        return this.vmName;
    }

    public void setvm_name(String value) {
        this.vmName = value;
    }

    @XmlElement(name = "storage_pool_id")
    @Column(name = "storage_pool_id")
    @Type(type = "guid")
    private NGuid storagePoolId;

    public NGuid getstorage_pool_id() {
        return storagePoolId;
    }

    public void setstorage_pool_id(NGuid value) {
        storagePoolId = value;
    }

    @XmlElement(name = "storage_pool_name")
    @Column(name = "storage_pool_name", length = 40)
    private String storagePoolName;

    public String getstorage_pool_name() {
        return storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        storagePoolName = value;
    }

    @XmlElement(name = "storage_domain_id")
    @Column(name = "storage_domain_id")
    @Type(type = "guid")
    private NGuid storageDomainId;

    public NGuid getstorage_domain_id() {
        return storageDomainId;
    }

    public void setstorage_domain_id(NGuid value) {
        storageDomainId = value;
    }

    @XmlElement(name = "storage_domain_name")
    @Column(name = "storage_domain_name", length = 250)
    private String storageDomainName;

    public String getstorage_domain_name() {
        return storageDomainName;
    }

    public void setstorage_domain_name(String value) {
        storageDomainName = value;
    }

    @XmlElement(name = "vds_group_id")
    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private NGuid vdsGroupId;

    public NGuid getvds_group_id() {
        return vdsGroupId;
    }

    public void setvds_group_id(NGuid value) {
        vdsGroupId = value;
    }

    @XmlElement(name = "vds_group_name")
    @Column(name = "vds_group_name", length = 255)
    private String vdsGroupName;

    public String getvds_group_name() {
        return vdsGroupName;
    }

    public void setvds_group_name(String value) {
        vdsGroupName = value;
    }

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    /**
     * Vitaly add
     */
    public AuditLog(AuditLogType al_type, AuditLogSeverity al_severity, String al_msg, NGuid al_user_id,
            String al_user_name, NGuid al_vm_id, String al_vm_name, NGuid al_vds_id, String al_vds_name,
            NGuid al_vmt_id, String al_vmt_name) {
        logTime = new java.util.Date();
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

    @Column(name = "log_type", nullable = false)
    @Enumerated
    private int logType = AuditLogType.UNASSIGNED.getValue();

    @XmlElement
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
    @XmlElement
    public int getlog_typeValue() {
        return getlog_type().getValue();
    }

    public void setlog_typeValue(int value) {
        // Do nothing, this is mockup (WSDL need setter)
    }

    public String getlog_type_name() {
        return getlog_type().name();
    }

    @Column(name = "severity", nullable = false)
    private int severity = AuditLogSeverity.NORMAL.getValue();

    @XmlElement
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

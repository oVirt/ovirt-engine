package org.ovirt.engine.core.notifier.transport.smtp;

import java.util.Date;

import org.ovirt.engine.core.common.AuditLogSeverity;

/**
 * Describes a message content
 */
public class MessageBody {
    private String userInfo;
    private String vmInfo;
    private String hostInfo;
    private String templateInfo;
    private String datacenterInfo;
    private String storageDomainInfo;
    private Date logTime;
    private String message;
    private AuditLogSeverity severity;

    public String getUserInfo() {
        return userInfo;
    }
    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
    public String getVmInfo() {
        return vmInfo;
    }
    public void setVmInfo(String vmInfo) {
        this.vmInfo = vmInfo;
    }

    public String getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(String hostInfo) {
        this.hostInfo = hostInfo;
    }
    public String getTemplateInfo() {
        return templateInfo;
    }
    public void setTemplateInfo(String templateInfo) {
        this.templateInfo = templateInfo;
    }
    public String getDatacenterInfo() {
        return datacenterInfo;
    }
    public void setDatacenterInfo(String datacenterInfo) {
        this.datacenterInfo = datacenterInfo;
    }
    public String getStorageDomainInfo() {
        return storageDomainInfo;
    }
    public void setStorageDomainInfo(String storageDomainInfo) {
        this.storageDomainInfo = storageDomainInfo;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public AuditLogSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AuditLogSeverity severity) {
        this.severity = severity;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}

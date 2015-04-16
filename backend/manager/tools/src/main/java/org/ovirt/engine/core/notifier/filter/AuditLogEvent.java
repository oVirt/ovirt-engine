package org.ovirt.engine.core.notifier.filter;

import java.util.Date;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AuditLogEvent {

    private long id;

    private String logTypeName;

    private AuditLogEventType type;

    private Guid userId;

    private String userName;

    private Guid vmId;

    private String vmName;

    private Guid vmTemplateId;

    private String vmTemplateName;

    private Guid vdsId;

    private String vdsName;

    private Guid storagePoolId;

    private String storagePoolName;

    private Guid storageDomainId;

    private String storageDomainName;

    private Date logTime;

    private AuditLogSeverity severity;

    private String message;

    public AuditLogEvent() {
        storagePoolId = Guid.Empty;
        storageDomainId = Guid.Empty;
        logTime = new Date(0);
    }

    public String getName() {
        return logTypeName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogTypeName() {
        return logTypeName;
    }

    public void setLogTypeName(String logTypeName) {
        this.logTypeName = logTypeName;
    }

    public AuditLogEventType getType() {
        return type;
    }

    public void setType(AuditLogEventType type) {
        this.type = type;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", id)
                .append("logTypeName", logTypeName)
                .append("type", type)
                .append("userId", userId)
                .append("userName", userName)
                .append("vmId", vmId)
                .append("vmName", vmName)
                .append("vmTemplateId", vmTemplateId)
                .append("vmTemplateName", vmTemplateName)
                .append("vdsId", vdsId)
                .append("vdsName", vdsName)
                .append("storagePoolId", storagePoolId)
                .append("storagePoolName", storagePoolName)
                .append("storageDomainId", storageDomainId)
                .append("storageDomainName", storageDomainName)
                .append("logTime", logTime)
                .append("severity", severity)
                .append("message", message)
                .build();
    }
}

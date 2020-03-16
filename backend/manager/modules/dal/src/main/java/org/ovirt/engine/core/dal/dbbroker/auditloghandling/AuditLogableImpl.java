package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AuditLogableImpl implements AuditLogable {

    private Guid vmId;
    private String vmName;
    private Guid userId;
    private String userName;
    private Guid vdsId;
    private String vdsName;
    private Guid vmTemplateId;
    private String vmTemplateName;
    private Guid clusterId;
    private String clusterName;
    private Guid storagePoolId;
    private String storagePoolName;
    private Guid storageDomainId;
    private String storageDomainName;
    private String glusterVolumeName;
    private Guid quotaIdForLog;
    private String quotaNameForLog;
    private String quotaEnforcementType;
    private Guid jobId;
    private Guid brickId;
    private String brickPath;
    private String reason;
    private boolean repeatable;
    private Map<String, String> customValues;
    private String correlationId;
    private Guid glusterVolumeId;
    private String customId;
    private String origin;
    private int customEventId;
    private int eventFloodInSec;
    private String customData;
    private boolean external;
    private String callStack;
    private String compatibilityVersion;

    public AuditLogableImpl() {
        vmId = Guid.Empty;
        userId = Guid.Empty;
        customValues = new HashMap<>();
        customEventId = -1;
        customData = "";
        origin = AuditLog.OVIRT_ORIGIN;
    }

    @Override
    public Guid getVmId() {
        return vmId;
    }

    @Override
    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    @Override
    public String getVmName() {
        return vmName;
    }

    @Override
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    @Override
    public Guid getUserId() {
        return userId;
    }

    @Override
    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public Guid getVdsId() {
        return vdsId;
    }

    @Override
    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    @Override
    public String getVdsName() {
        return vdsName;
    }

    @Override
    public void setVdsName(String vdsName) {
        this.vdsName = vdsName;
    }

    @Override
    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    @Override
    public void setVmTemplateId(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    @Override
    public String getVmTemplateName() {
        return vmTemplateName;
    }

    @Override
    public void setVmTemplateName(String vmTemplateName) {
        this.vmTemplateName = vmTemplateName;
    }

    @Override
    public Guid getClusterId() {
        return clusterId;
    }

    @Override
    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    @Override
    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    @Override
    public String getStoragePoolName() {
        return storagePoolName;
    }

    @Override
    public void setStoragePoolName(String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    @Override
    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    @Override
    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    @Override
    public String getStorageDomainName() {
        return storageDomainName;
    }

    @Override
    public void setStorageDomainName(String storageDomainName) {
        this.storageDomainName = storageDomainName;
    }

    @Override
    public String getGlusterVolumeName() {
        return glusterVolumeName;
    }

    @Override
    public void setGlusterVolumeName(String glusterVolumeName) {
        this.glusterVolumeName = glusterVolumeName;
    }

    @Override
    public Guid getQuotaIdForLog() {
        return quotaIdForLog;
    }

    @Override
    public void setQuotaIdForLog(Guid quotaIdForLog) {
        this.quotaIdForLog = quotaIdForLog;
    }

    @Override
    public String getQuotaNameForLog() {
        return quotaNameForLog;
    }

    @Override
    public void setQuotaNameForLog(String quotaNameForLog) {
        this.quotaNameForLog = quotaNameForLog;
    }

    @Override
    public String getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    @Override
    public void setQuotaEnforcementType(String quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

    @Override
    public Guid getJobId() {
        return jobId;
    }

    @Override
    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    @Override
    public Guid getBrickId() {
        return brickId;
    }

    @Override
    public void setBrickId(Guid brickId) {
        this.brickId = brickId;
    }

    @Override
    public String getBrickPath() {
        return brickPath;
    }

    @Override
    public void setBrickPath(String brickPath) {
        this.brickPath = brickPath;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    @Override
    public Map<String, String> getCustomValues() {
        return customValues;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public Guid getGlusterVolumeId() {
        return glusterVolumeId;
    }

    @Override
    public void setGlusterVolumeId(Guid glusterVolumeId) {
        this.glusterVolumeId = glusterVolumeId;
    }

    @Override
    public String getCustomId() {
        return customId;
    }

    @Override
    public void setCustomId(String customId) {
        this.customId = customId;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public int getCustomEventId() {
        return customEventId;
    }

    @Override
    public void setCustomEventId(int customEventId) {
        this.customEventId = customEventId;
    }

    @Override
    public int getEventFloodInSec() {
        return eventFloodInSec;
    }

    @Override
    public void setEventFloodInSec(int eventFloodInSec) {
        this.eventFloodInSec = eventFloodInSec;
    }

    @Override
    public String getCustomData() {
        return customData;
    }

    @Override
    public void setCustomData(String customData) {
        this.customData = customData;
    }

    @Override
    public boolean isExternal() {
        return external;
    }

    @Override
    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public String getCallStack() {
        return callStack;
    }

    @Override
    public void setCallStack(String callStack) {
        this.callStack = callStack;
    }

    @Override
    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    @Override
    public AuditLogable addCustomValue(String name, String value) {
        customValues.put(name.toLowerCase(), value);
        return this;
    }

    @Override
    public AuditLog createAuditLog(AuditLogType logType, String message) {
        return new AuditLog(logType,
                logType.getSeverity(),
                message,
                getUserId(),
                getUserName(),
                getVmId(),
                getVmName(),
                getVdsId(),
                getVdsName(),
                getVmTemplateId(),
                getVmTemplateName(),
                getOrigin(),
                getCustomId(),
                getCustomEventId(),
                getEventFloodInSec(),
                getCustomData());
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("vmId", getVmId())
                .append("vmName", getVmName())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("vdsId", getVdsId())
                .append("vdsName", getVdsName())
                .append("vmTemplateId", getVmTemplateId())
                .append("vmTemplateName", getVmTemplateName())
                .append("clusterId", getClusterId())
                .append("clusterName", getClusterName())
                .append("storagePoolId", getStoragePoolId())
                .append("storagePoolName", getStoragePoolName())
                .append("storageDomainId", getStorageDomainId())
                .append("storageDomainName", getStorageDomainName())
                .append("glusterVolumeName", getGlusterVolumeId())
                .append("glusterVolumeId", getGlusterVolumeId())
                .append("quotaIdForLog", getQuotaIdForLog())
                .append("quotaNameForLog", getQuotaNameForLog())
                .append("quotaEnforcementType", getQuotaEnforcementType())
                .append("jobId", getJobId())
                .append("brickId", getBrickId())
                .append("brickPath", getBrickPath())
                .append("reason", getReason())
                .append("repeatable", isRepeatable())
                .append("customValues", getCustomValues())
                .append("correlationId", getCorrelationId())
                .append("customId", getCustomId())
                .append("origin", getOrigin())
                .append("customId", getCustomId())
                .append("customEventId", getCustomEventId())
                .append("eventFloodInSec", getEventFloodInSec())
                .append("customData", getCustomData())
                .append("external", isExternal())
                .append("callStack", getCallStack())
                .build();
    }

    public static AuditLogable createHostEvent(VDS host, String correlationId, Map<String, String> customValues) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(host.getId());
        logable.setVdsName(host.getName());
        logable.setClusterId(host.getClusterId());
        logable.setClusterName(host.getClusterName());
        logable.setCorrelationId(correlationId);
        customValues.entrySet().stream()
            .forEach(v -> logable.addCustomValue(v.getKey(), v.getValue()));
        return logable;
    }

    public static AuditLogable createEvent(String correlationId, Map<String, String> customValues) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setCorrelationId(correlationId);
        customValues.entrySet().stream()
            .forEach(v -> logable.addCustomValue(v.getKey(), v.getValue()));
        return logable;
    }
}

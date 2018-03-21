package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogableBase implements AuditLogable {
    private static final Logger log = LoggerFactory.getLogger(AuditLogableBase.class);
    private static final String COMMA_SEPARATOR = ", ";

    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    private Guid vmId = Guid.Empty;
    private DbUser dbUser;
    private Guid userId = Guid.Empty;
    private String userName;
    private String vmName;
    private String reason;
    private Map<String, String> customValues = Collections.emptyMap();
    private Guid vdsId;
    private String vdsName;
    private Guid vmTemplateId;
    private String vmTemplateName;
    private VDS vds;
    private VdsStatic cachedVdsStatic;
    private VM vm;
    private VmTemplate vmTemplate;
    private Guid storageDomainId;
    private Guid storagePoolId;
    private Guid clusterId;
    private Cluster cluster;
    private String clusterName;
    private String correlationId;
    private Guid jobId;
    private boolean isInternalExecution = false;
    private Guid glusterVolumeId;
    private String glusterVolumeName;
    private GlusterVolumeEntity glusterVolume;
    private String customId;
    private String origin = "oVirt";
    private int customEventId = -1;
    private int eventFloodInSec = 0;
    private String customData = "";
    private boolean external = false;
    private String compatibilityVersion;
    private String quotaEnforcementType;
    private Guid quotaIdForLog;
    private String quotaNameForLog;
    private String callStack;
    private Guid brickId;
    private String brickPath;

    /**
     * @see org.ovirt.engine.core.common.businessentities.AuditLog#repeatable
     */
    private boolean repeatable;
    private String storagePoolName;
    private String storageDomainName;

    public AuditLogableBase() {
    }

    public AuditLogableBase(final Guid vdsId) {
        this.vdsId = vdsId;
    }

    public AuditLogableBase(final Guid vdsId, final Guid vmId) {
        this(vdsId);
        this.vmId = vmId;
    }

    public AuditLogableBase(final AuditLog auditLog) {
        this(auditLog.getVdsId(), auditLog.getVmId());
        this.storageDomainId = auditLog.getStorageDomainId();
        this.storagePoolId = auditLog.getStoragePoolId();
        this.correlationId = auditLog.getCorrelationId();
        this.customData = auditLog.getCustomData();
        this.customEventId = auditLog.getCustomEventId();
        this.eventFloodInSec = auditLog.getEventFloodInSec();
        this.glusterVolumeId = auditLog.getGlusterVolumeId();
        this.glusterVolumeName = auditLog.getGlusterVolumeName();
        this.jobId = auditLog.getJobId();
        this.userId = auditLog.getUserId();
        this.userName = auditLog.getUserName();
        this.clusterId = auditLog.getClusterId();
        this.vdsName = auditLog.getVdsName();
        this.vmName = auditLog.getVmName();
        this.vmTemplateId = auditLog.getVmTemplateId();
        this.vmTemplateName = auditLog.getVmTemplateName();
        this.origin = auditLog.getOrigin();
        this.external = auditLog.isExternal();
        this.callStack = auditLog.getCallStack();
        this.brickId = auditLog.getBrickId();
        this.brickPath = auditLog.getBrickPath();
    }

    @Override
    public Guid getUserId() {
        if (userId != null && userId.equals(Guid.Empty) && getCurrentUser() != null) {
            userId = getCurrentUser().getId();
        }
        return userId;
    }

    @Override
    public void setUserId(final Guid value) {
        userId = value;
    }

    protected Optional<Guid> getUserIdIfExternal() {
        return isInternalExecution() ? Optional.empty() : Optional.ofNullable(getUserId());
    }

    @Override
    public String getUserName() {
        if (StringUtils.isEmpty(userName) && getCurrentUser() != null) {
            userName = String.format("%s@%s", getCurrentUser().getLoginName(), getCurrentUser().getDomain());
        }
        return userName;
    }

    @Override
    public void setUserName(final String value) {
        userName = value;
    }

    public DbUser getCurrentUser() {
        return dbUser;
    }

    public void setCurrentUser(final DbUser value) {
        dbUser = value;
    }

    @Override
    public Guid getVmTemplateId() {
        return getVmTemplateIdRef() != null ? getVmTemplateIdRef() : Guid.Empty;
    }

    @Override
    public void setVmTemplateId(final Guid value) {
        vmTemplateId = value;
    }

    public Guid getVmTemplateIdRef() {
        if (vmTemplateId == null && getVmTemplate() != null) {
            vmTemplateId = getVmTemplate().getId();
        }
        return vmTemplateId;
    }

    @Override
    public String getVmTemplateName() {
        if (StringUtils.isEmpty(vmTemplateName) && getVmTemplate() != null) {
            vmTemplateName = getVmTemplate().getName();
        }
        return vmTemplateName;
    }

    @Override
    public void setVmTemplateName(final String value) {
        vmTemplateName = value;
    }

    @Override
    public Guid getVmId() {
        return getVmIdRef() != null ? getVmIdRef() : Guid.Empty;
    }

    @Override
    public void setVmId(final Guid value) {
        vmId = value;
    }

    private String privateSnapshotName;

    public String getSnapshotName() {
        return privateSnapshotName;
    }

    public void setSnapshotName(final String value) {
        privateSnapshotName = value;
    }

    public Guid getVmIdRef() {
        if (Guid.isNullOrEmpty(vmId) && getVm() != null) {
            vmId = getVm().getId();
        }
        return vmId;
    }

    @Override
    public String getVmName() {
        if (StringUtils.isEmpty(vmName) && getVm() != null) {
            vmName = getVm().getName();
        }
        return vmName;
    }

    @Override
    public void setVmName(final String value) {
        vmName = value;
    }

    @Override
    public String getReason() {
        if (reason == null && getVm() != null) {
            reason = getVm().getStopReason();
        }
        return reason;
    }

    @Override
    public void setReason(String value) {
        reason = value;
    }

    public Guid getVdsIdRef() {
        if (vdsId == null && getVds() != null) {
            vdsId = getVds().getId();
        }
        return vdsId;
    }

    protected void setVdsIdRef(final Guid value) {
        vdsId = value;
    }

    @Override
    public Guid getVdsId() {
        return vdsId != null ? vdsId : Guid.Empty;
    }

    @Override
    public void setVdsId(final Guid value) {
        vdsId = value;
    }

    @Override
    public String getVdsName() {
        if (StringUtils.isEmpty(vdsName)) {
            if (getVdsNoLoad() == null) {
                if (getVdsStatic() != null) {
                    vdsName = getVdsStatic().getName();
                }
            } else {
                if (getVds() != null) {
                    vdsName = getVds().getName();
                }
            }
        }
        return vdsName;
    }

    @Override
    public void setVdsName(final String value) {
        vdsName = value;
    }

    private StorageDomain storageDomain;

    public StorageDomain getStorageDomain() {
        if (storageDomain == null && getStorageDomainId() != null) {
            if (storagePoolId != null && getStoragePool() != null) {
                storageDomain = storageDomainDao.getForStoragePool(
                        getStorageDomainId(), getStoragePool().getId());
            }
            if (storageDomain == null) {
                final List<StorageDomain> storageDomainList =
                        storageDomainDao.getAllForStorageDomain(getStorageDomainId());
                if (storageDomainList.size() != 0) {
                    storageDomain = storageDomainList.get(0);
                    for (final StorageDomain storageDomainFromList : storageDomainList) {
                        if (storageDomainFromList.getStatus() == StorageDomainStatus.Active) {
                            storageDomain = storageDomainFromList;
                            break;
                        }
                    }
                }
            }
        }
        return storageDomain;
    }

    public void setStorageDomain(final StorageDomain value) {
        storageDomain = value;
    }

    @Override
    public Guid getStorageDomainId() {
        if (storageDomain != null) {
            return storageDomain.getId();
        }
        return storageDomainId;
    }

    @Override
    public void setStorageDomainId(final Guid value) {
        storageDomainId = value;
    }

    @Override
    public String getStorageDomainName() {
        if (StringUtils.isEmpty(storageDomainName) && getStorageDomain() != null) {
            storageDomainName = getStorageDomain().getStorageName();
        }
        return StringUtils.defaultString(storageDomainName);
    }

    @Override
    public void setStorageDomainName(String storageDomainName) {
        this.storageDomainName = storageDomainName;
    }

    private StoragePool storagePool;

    public StoragePool getStoragePool() {
        if (storagePool == null && getStoragePoolId() != null && !Guid.Empty.equals(getStoragePoolId())) {
            storagePool = storagePoolDao.get(getStoragePoolId());
        }
        return storagePool;
    }

    public void setStoragePool(final StoragePool value) {
        storagePool = value;
    }

    @Override
    public Guid getStoragePoolId() {
        if (storagePoolId == null) {
            if (storagePool != null) {
                storagePoolId = storagePool.getId();
            } else if (getStorageDomain() != null) {
                storagePoolId = getStorageDomain().getStoragePoolId();
            }
        }
        return storagePoolId;
    }

    @Override
    public void setStoragePoolId(final Guid value) {
        storagePoolId = value;
    }

    @Override
    public String getStoragePoolName() {
        if (StringUtils.isEmpty(storagePoolName) && getStoragePool() != null) {
            storagePoolName = getStoragePool().getName();
        }
        return StringUtils.defaultString(storagePoolName);
    }

    @Override
    public void setStoragePoolName(String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }

    private VDS getVdsNoLoad() {
        return vds;
    }

    protected VDS getVds() {
        if (vds == null
                && ((vdsId != null && !Guid.Empty.equals(vdsId)) || (getVm() != null && getVm().getRunOnVds() != null))) {
            if (vdsId == null || Guid.Empty.equals(vdsId)) {
                vdsId = getVm().getRunOnVds();
            }
            try {
                vds = vdsDao.get(getVdsId());
            } catch (final RuntimeException e) {
                log.info("Failed to get vds '{}', error {}", vdsId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return vds;
    }

    private VdsStatic getVdsStatic() {
        if (cachedVdsStatic == null
                && ((vdsId != null && !Guid.Empty.equals(vdsId)) || (getVm() != null && getVm().getRunOnVds() != null))) {
            if (vdsId == null || Guid.Empty.equals(vdsId)) {
                vdsId = getVm().getRunOnVds();
            }
            try {
                cachedVdsStatic = vdsStaticDao.get(getVdsId());
            } catch (final RuntimeException e) {
                log.info("Failed to get vds '{}', error: {}", vdsId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return cachedVdsStatic;
    }

    public void setVds(final VDS value) {
        vds = value;
        vdsName = null;
        if (value != null) {
            vdsId = value.getId();
        }
    }

    public VM getVm() {
        if (vm == null && vmId != null && !vmId.equals(Guid.Empty)) {
            try {
                vm = vmDao.get(vmId);

                // TODO: This is done for backwards compatibility with VMDao.getById(Guid)
                // It should probably be removed, but some research is required
                if (vm != null) {
                    vm.setInterfaces(vmNetworkInterfaceDao.getAllForVm(vmId));
                }
            } catch (final Exception e) {
                log.info("Failed to get vm '{}', error {}", vmId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return vm;
    }

    public void setVm(final VM value) {
        vm = value;
    }

    public VmTemplate getVmTemplate() {
        if (vmTemplate == null && (vmTemplateId != null || getVm() != null)) {
            vmTemplate = vmTemplateDao.get(vmTemplateId != null ? getVmTemplateId() : getVm().getVmtGuid());
        }
        return vmTemplate;
    }

    public void setVmTemplate(final VmTemplate value) {
        vmTemplate = value;
    }

    @Override
    public Guid getClusterId() {
        if (clusterId != null) {
            return clusterId;
        } else if (getCluster() != null) {
            clusterId = getCluster().getId();
            return clusterId;
        } else {
            return Guid.Empty;
        }
    }

    @Override
    public void setClusterId(final Guid value) {
        clusterId = value;
    }

    public Cluster getCluster() {
        if (cluster == null) {
            if (clusterId != null) {
                cluster = clusterDao.get(clusterId);
            } else if (getVds() != null) {
                clusterId = getVds().getClusterId();
                cluster = clusterDao.get(clusterId);
            } else if (getVm() != null) {
                clusterId = getVm().getClusterId();
                cluster = clusterDao.get(clusterId);
            } else if (getVmTemplate() != null) {
                clusterId = getVmTemplate().getClusterId();
                cluster = clusterDao.get(clusterId);
            }
        }
        return cluster;
    }

    public void setCluster(final Cluster value) {
        cluster = value;
    }


    @Override
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? "" : clusterName;
    }

    @Override
    public String getClusterName() {
        if (StringUtils.isEmpty(clusterName)) {
            if (getCluster() != null) {
                clusterName = getCluster().getName();
            } else {
                clusterName = "";
            }
        }
        return clusterName;
    }

    @Override
    public AuditLogable addCustomValue(final String name, final String value) {
        allocateCustomValues();
        customValues.put(name.toLowerCase(), value);
        return this;
    }

    public void setCustomCommaSeparatedValues(
            final String name,
            final Collection<String> values) {
        setCustomValues(name, values, COMMA_SEPARATOR);
    }

    public void setCustomValues(
            final String name,
            final Collection<String> values,
            final String separator) {
        final String value = values.stream().collect(Collectors.joining(separator));
        allocateCustomValues();
        customValues.put(name.toLowerCase(), value);
    }

    public void appendCustomCommaSeparatedValue(final String name, final String value) {
        appendCustomValue(name, value, COMMA_SEPARATOR);
    }

    public void appendCustomValue(final String name, final String value, final String separator) {
        final String key = name.toLowerCase();
        String newValue = value;
        allocateCustomValues();
        if (customValues.containsKey(key)) {
            newValue = String.format("%1$s%2$s%3$s", customValues.get(key), separator, value);
        }
        customValues.put(name.toLowerCase(), newValue);
    }

    private void allocateCustomValues() {
        if (Collections.emptyMap().equals(customValues)) {
            customValues = new HashMap<>();
        }
    }

    @Override
    public Map<String, String> getCustomValues() {
        return customValues;
    }

    public String getCustomValue(final String name) {
        if (customValues.containsKey(name)) {
            return customValues.get(name);
        }
        return "";
    }

    @Override
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    @Override
    public Guid getJobId() {
        return jobId;
    }

    public boolean isInternalExecution() {
        return isInternalExecution;
    }

    public void setInternalExecution(boolean isInternalExecution) {
        this.isInternalExecution = isInternalExecution;
    }

    @Override
    public Guid getGlusterVolumeId() {
        return glusterVolumeId != null ? glusterVolumeId : Guid.Empty;
    }

    @Override
    public void setGlusterVolumeId(Guid value) {
        glusterVolumeId = value;
    }

    @Override
    public String getGlusterVolumeName() {
        if (StringUtils.isEmpty(glusterVolumeName) && getGlusterVolume() != null) {
            glusterVolumeName = getGlusterVolume().getName();
        }
        return glusterVolumeName;
    }

    @Override
    public void setGlusterVolumeName(String value) {
        glusterVolumeName = value;
    }

    public void setGlusterVolume(GlusterVolumeEntity glusterVolume) {
        this.glusterVolume = glusterVolume;
        glusterVolumeId = (glusterVolume == null) ? Guid.Empty : glusterVolume.getId();
    }

    protected GlusterVolumeEntity getGlusterVolume() {
        if (glusterVolume == null && glusterVolumeId != null) {
            glusterVolume = glusterVolumeDao.getById(glusterVolumeId);
        }
        return glusterVolume;
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
    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    @Override
    public void setCompatibilityVersion(String compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
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
    public String getCallStack() {
        return callStack;
    }

    /**
     * Sets the call stack string
     * If you have a Throwable object in hand you can use the updateCallStackFromThrowable method
     *
     * @param callStack
     *            the call stack
     */
    @Override
    public void setCallStack(String callStack) {
        this.callStack = callStack;
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
    public AuditLog createAuditLog(AuditLogType logType, String message) {
        return new AuditLog(logType,
                logType.getSeverity(),
                message,
                getUserId(),
                getUserName(),
                getVmIdRef(),
                getVmName(),
                getVdsIdRef(),
                getVdsName(),
                getVmTemplateIdRef(),
                getVmTemplateName(),
                getOrigin(),
                getCustomId(),
                getCustomEventId(),
                getEventFloodInSec(),
                getCustomData());
    }
}

package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transaction;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.RoleGroupMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogableBase extends TimeoutBase {
    private static final Logger log = LoggerFactory.getLogger(AuditLogableBase.class);

    @Inject
    protected AuditLogDirector auditLogDirector;

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

    public Guid getUserId() {
        if (userId != null && userId.equals(Guid.Empty) && getCurrentUser() != null) {
            userId = getCurrentUser().getId();
        }
        return userId;
    }

    public void setUserId(final Guid value) {
        userId = value;
    }

    public String getUserName() {
        if (StringUtils.isEmpty(userName) && getCurrentUser() != null) {
            userName = String.format("%s@%s", getCurrentUser().getLoginName(), getCurrentUser().getDomain());
        }
        return userName;
    }

    public void setUserName(final String value) {
        userName = value;
    }

    public DbUser getCurrentUser() {
        return dbUser;
    }

    public void setCurrentUser(final DbUser value) {
        dbUser = value;
    }

    public Guid getVmTemplateId() {
        return getVmTemplateIdRef() != null ? getVmTemplateIdRef() : Guid.Empty;
    }

    public void setVmTemplateId(final Guid value) {
        vmTemplateId = value;
    }

    public Guid getVmTemplateIdRef() {
        if (vmTemplateId == null && getVmTemplate() != null) {
            vmTemplateId = getVmTemplate().getId();
        }
        return vmTemplateId;
    }

    public String getVmTemplateName() {
        if (vmTemplateName == null && getVmTemplate() != null) {
            vmTemplateName = getVmTemplate().getName();
        }
        return vmTemplateName;
    }

    protected void setVmTemplateName(final String value) {
        vmTemplateName = value;
    }

    public Guid getVmId() {
        return getVmIdRef() != null ? getVmIdRef() : Guid.Empty;
    }

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

    public String getVmName() {
        if (vmName == null && getVm() != null) {
            vmName = getVm().getName();
        }
        return vmName;
    }

    protected void setVmName(final String value) {
        vmName = value;
    }

    public String getReason() {
        if (reason == null && getVm() != null) {
            reason = getVm().getStopReason();
        }
        return reason;
    }

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

    public Guid getVdsId() {
        return vdsId != null ? vdsId : Guid.Empty;
    }

    public void setVdsId(final Guid value) {
        vdsId = value;
    }

    public String getVdsName() {
        if (vdsName == null) {
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

    protected void setVdsName(final String value) {
        vdsName = value;
    }

    private StorageDomain storageDomain;

    public StorageDomain getStorageDomain() {
        if (storageDomain == null && getStorageDomainId() != null) {
            if (storagePoolId != null && getStoragePool() != null) {
                storageDomain = getStorageDomainDao().getForStoragePool(
                        getStorageDomainId(), getStoragePool().getId());
            }
            if (storageDomain == null) {
                final List<StorageDomain> storageDomainList =
                        getStorageDomainDao().getAllForStorageDomain(getStorageDomainId());
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

    public Guid getStorageDomainId() {
        if (storageDomain != null) {
            return storageDomain.getId();
        }
        return storageDomainId;
    }

    public void setStorageDomainId(final Guid value) {
        storageDomainId = value;
    }

    public String getStorageDomainName() {
        if (getStorageDomain() != null) {
            return getStorageDomain().getStorageName();
        }
        return "";
    }

    private StoragePool storagePool;

    public StoragePool getStoragePool() {
        if (storagePool == null && getStoragePoolId() != null && !Guid.Empty.equals(getStoragePoolId())) {
            storagePool = getStoragePoolDao().get(getStoragePoolId());
        }
        return storagePool;
    }

    public void setStoragePool(final StoragePool value) {
        storagePool = value;
    }

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

    public void setStoragePoolId(final Guid value) {
        storagePoolId = value;
    }

    public String getStoragePoolName() {
        if (getStoragePool() != null) {
            return getStoragePool().getName();
        }
        return "";
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
                vds = getVdsDao().get(getVdsId());
            } catch (final RuntimeException e) {
                log.info("Failed to get vds '{}', error {}", vdsId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return vds;
    }

    protected VdsStatic getVdsStatic() {
        if (cachedVdsStatic == null
                && ((vdsId != null && !Guid.Empty.equals(vdsId)) || (getVm() != null && getVm().getRunOnVds() != null))) {
            if (vdsId == null || Guid.Empty.equals(vdsId)) {
                vdsId = getVm().getRunOnVds();
            }
            try {
                cachedVdsStatic = getVdsStaticDao().get(getVdsId());
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
                vm = getVmDao().get(vmId);

                // TODO: This is done for backwards compatibility with VMDao.getById(Guid)
                // It should probably be removed, but some research is required
                if (vm != null) {
                    vm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(vmId));
                }
            } catch (final Exception e) {
                log.info("Failed to get vm '{}', error {}", vmId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return vm;
    }

    protected void setVm(final VM value) {
        vm = value;
    }

    public VmTemplate getVmTemplate() {
        if (vmTemplate == null && (vmTemplateId != null || getVm() != null)) {

            vmTemplate = getVmTemplateDao()
                    .get(vmTemplateId != null ? getVmTemplateId() : getVm()
                            .getVmtGuid());
        }
        return vmTemplate;
    }

    protected void setVmTemplate(final VmTemplate value) {
        vmTemplate = value;
    }

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

    public void setClusterId(final Guid value) {
        clusterId = value;
    }

    public Cluster getCluster() {
        if (cluster == null) {
            if (clusterId != null) {
                cluster = getClusterDao().get(clusterId);
            } else if (getVds() != null) {
                clusterId = getVds().getClusterId();
                cluster = getClusterDao().get(clusterId);
            } else if (getVm() != null) {
                clusterId = getVm().getClusterId();
                cluster = getClusterDao().get(clusterId);
            } else if (getVmTemplate() != null) {
                clusterId = getVmTemplate().getClusterId();
                cluster = getClusterDao().get(clusterId);
            }
        }
        return cluster;
    }

    protected void setCluster(final Cluster value) {
        cluster = value;
    }

    public String getClusterName() {
        if (getCluster() != null) {
            return getCluster().getName();
        }
        return "";
    }

    protected void log() {
        final Transaction transaction = TransactionSupport.suspend();
        try {
            try {
                auditLogDirector.log(this);
            } catch (final RuntimeException ex) {
                log.error("Error during log command: {}. Exception {}", getClass().getName(), ex.getMessage());
                log.debug("Exception", ex);
            }
        } finally {
            TransactionSupport.resume(transaction);
        }
    }

    public AuditLogableBase addCustomValue(final String name, final String value) {
        allocateCustomValues();
        customValues.put(name.toLowerCase(), value);
        return this;
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

    public Map<String, String> getCustomValues() {
        return customValues;
    }

    @Override
    protected String getKey() {
        return getAuditLogTypeValue().toString();
    }

    public String getCustomValue(final String name) {
        if (customValues.containsKey(name)) {
            return customValues.get(name);
        }
        return "";
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

    public boolean isInternalExecution() {
        return isInternalExecution;
    }

    public void setInternalExecution(boolean isInternalExecution) {
        this.isInternalExecution = isInternalExecution;
    }

    public Guid getGlusterVolumeId() {
        return glusterVolumeId != null ? glusterVolumeId : Guid.Empty;
    }

    public void setGlusterVolumeId(Guid value) {
        glusterVolumeId = value;
    }

    public String getGlusterVolumeName() {
        if (glusterVolumeName == null && getGlusterVolume() != null) {
            glusterVolumeName = getGlusterVolume().getName();
        }
        return glusterVolumeName;
    }

    public void setGlusterVolumeName(String value) {
        glusterVolumeName = value;
    }

    public void setGlusterVolume(GlusterVolumeEntity glusterVolume) {
        this.glusterVolume = glusterVolume;
        glusterVolumeId = (glusterVolume == null) ? Guid.Empty : glusterVolume.getId();
    }

    protected GlusterVolumeEntity getGlusterVolume() {
        if (glusterVolume == null && glusterVolumeId != null) {
            glusterVolume = getGlusterVolumeDao().getById(glusterVolumeId);
        }
        return glusterVolume;
    }

    public GlusterVolumeDao getGlusterVolumeDao() {
        return getDbFacade().getGlusterVolumeDao();
    }

    public GlusterHooksDao getGlusterHooksDao() {
        return getDbFacade().getGlusterHooksDao();
    }

    public GlusterBrickDao getGlusterBrickDao() {
        return getDbFacade().getGlusterBrickDao();
    }

    public StorageDomainDao getStorageDomainDao() {
        return getDbFacade().getStorageDomainDao();
    }

    public StoragePoolDao getStoragePoolDao() {
        return getDbFacade().getStoragePoolDao();
    }

    public StorageDomainStaticDao getStorageDomainStaticDao() {
        return getDbFacade().getStorageDomainStaticDao();
    }

    public StorageServerConnectionExtensionDao getStorageServerConnectionExtensionDao() {
        return getDbFacade().getStorageServerConnectionExtensionDao();
    }

    public VdsDao getVdsDao() {
        return getDbFacade().getVdsDao();
    }

    public VdsStaticDao getVdsStaticDao() {
        return getDbFacade().getVdsStaticDao();
    }

    public VdsDynamicDao getVdsDynamicDao() {
        return getDbFacade().getVdsDynamicDao();
    }

    public VmTemplateDao getVmTemplateDao() {
        return getDbFacade().getVmTemplateDao();
    }

    public VmDao getVmDao() {
        return getDbFacade().getVmDao();
    }

    public VmStaticDao getVmStaticDao() {
        return getDbFacade().getVmStaticDao();
    }

    public SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    public VmAndTemplatesGenerationsDao getVmAndTemplatesGenerationsDao() {
        return DbFacade.getInstance().getVmAndTemplatesGenerationsDao();
    }

    public StorageDomainOvfInfoDao getStorageDomainOvfInfoDao() {
        return DbFacade.getInstance().getStorageDomainOvfInfoDao();
    }

    public VmDynamicDao getVmDynamicDao() {
        return getDbFacade().getVmDynamicDao();
    }

    protected VmStatisticsDao getVmStatisticsDao() {
        return getDbFacade().getVmStatisticsDao();
    }

    protected VnicProfileDao getVnicProfileDao() {
        return getDbFacade().getVnicProfileDao();
    }

    public ClusterDao getClusterDao() {
        return getDbFacade().getClusterDao();
    }

    public RoleDao getRoleDao() {
        return getDbFacade().getRoleDao();
    }

    public RoleGroupMapDao getRoleGroupMapDao() {
        return getDbFacade().getRoleGroupMapDao();
    }

    public PermissionDao getPermissionDao() {
        return getDbFacade().getPermissionDao();
    }

    public DbUserDao getDbUserDao() {
        return getDbFacade().getDbUserDao();
    }

    public DbGroupDao getAdGroupDao() {
        return getDbFacade().getDbGroupDao();
    }

    public VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    public VmNicDao getVmNicDao() {
        return getDbFacade().getVmNicDao();
    }

    protected NetworkClusterDao getNetworkClusterDao() {
        return getDbFacade().getNetworkClusterDao();
    }

    protected NetworkDao getNetworkDao() {
        return getDbFacade().getNetworkDao();
    }

    protected InterfaceDao getInterfaceDao() {
        return getDbFacade().getInterfaceDao();
    }

    public AsyncTaskDao getAsyncTaskDao() {
        return getDbFacade().getAsyncTaskDao();
    }

    public StepDao getStepDao() {
        return getDbFacade().getStepDao();
    }
    public ProviderDao getProviderDao() {
        return getDbFacade().getProviderDao();
    }

    public AuditLogDao getAuditLogDao() {
        return getDbFacade().getAuditLogDao();
    }

    public DiskProfileDao getDiskProfileDao() {
        return getDbFacade().getDiskProfileDao();
    }

    public CpuProfileDao getCpuProfileDao() {
        return getDbFacade().getCpuProfileDao();
    }

    public VmIconDao getVmIconDao() {
        return getDbFacade().getVmIconDao();
    }

    public DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
    }

    public DiskImageDao getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    public DiskVmElementDao getDiskVmElementDao() {
        return getDbFacade().getDiskVmElementDao();
    }

    public VmDeviceDao getVmDeviceDao() {
        return getDbFacade().getVmDeviceDao();
    }

    public BaseDiskDao getBaseDiskDao() {
        return getDbFacade().getBaseDiskDao();
    }

    public DiskImageDynamicDao getDiskImageDynamicDao() {
        return getDbFacade().getDiskImageDynamicDao();
    }

    public LunDao getLunDao() {
        return getDbFacade().getLunDao();
    }

    public StorageServerConnectionDao getStorageServerConnectionDao() {
        return getDbFacade().getStorageServerConnectionDao();
    }

    public StoragePoolIsoMapDao getStoragePoolIsoMapDao() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    public TagDao getTagDao() {
        return getDbFacade().getTagDao();
    }

    public ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    public UnregisteredOVFDataDao getUnregisteredOVFDataDao() {
        return getDbFacade().getUnregisteredOVFDataDao();
    }

    public DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
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


    public Guid getQuotaIdForLog() {
        return quotaIdForLog;
    }

    public void setQuotaIdForLog(Guid quotaIdForLog) {
        this.quotaIdForLog = quotaIdForLog;
    }

    public String getQuotaNameForLog() {
        return quotaNameForLog;
    }

    public void setQuotaNameForLog(String quotaNameForLog) {
        this.quotaNameForLog = quotaNameForLog;
    }

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
    public void setCallStack(String callStack) {
        this.callStack = callStack;
    }

    /**
     * Sets the call stack string from a Throwable object
     * Also, the updateCallStackFromThrowable can be used in case you have a Throwable object with the call stack details
     *
     * @param throwable
     *            the Throwable object containing the call stack. Can be null, which will cause no changes to this object
     */
    public void updateCallStackFromThrowable(Throwable throwable) {
        if (throwable != null) {
            setCallStack(ExceptionUtils.getStackTrace(throwable));
        }
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public AuditLogDirector getAuditLogDirector() {
        return auditLogDirector;
    }

    public void setAuditLogDirector(AuditLogDirector auditLogDirector) {
        this.auditLogDirector = auditLogDirector;
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

}

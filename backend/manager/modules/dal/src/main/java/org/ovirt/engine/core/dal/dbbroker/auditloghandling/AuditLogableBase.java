package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transaction;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.DbGroupDAO;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.dao.RoleDAO;
import org.ovirt.engine.core.dao.RoleGroupMapDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmStatisticsDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
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

    private Guid mVmId = Guid.Empty;
    private DbUser dbUser;
    private Guid mUserId = Guid.Empty;
    private String mUserName;
    private String mVmName;
    private String mReason;
    private Map<String, String> customValues = Collections.emptyMap();
    private Guid mVdsId;
    private String mVdsName;
    private Guid mVmTemplateId;
    private String mVmTemplateName;
    private VDS mVds;
    private VdsStatic cachedVdsStatic;
    private VM mVm;
    private VmTemplate mVmTemplate;
    private Guid _storageDomainId;
    private Guid _storagePoolId;
    private Guid mVdsGroupId;
    private VDSGroup mVdsGroup;
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

    /**
     * @see org.ovirt.engine.core.common.businessentities.AuditLog#repeatable
     */
    private boolean repeatable;

    public AuditLogableBase() {
        repeatable = false;
    }

    public AuditLogableBase(final Guid vdsId) {
        this();
        mVdsId = vdsId;
    }

    public AuditLogableBase(final Guid vdsId, final Guid vmId) {
        this(vdsId);
        mVmId = vmId;
    }

    public AuditLogableBase(final AuditLog auditLog) {
        this(auditLog.getVdsId(), auditLog.getVmId());
        this._storageDomainId = auditLog.getStorageDomainId();
        this._storagePoolId = auditLog.getStoragePoolId();
        this.correlationId = auditLog.getCorrelationId();
        this.customData = auditLog.getCustomData();
        this.customEventId = auditLog.getCustomEventId();
        this.eventFloodInSec = auditLog.getEventFloodInSec();
        this.glusterVolumeId = auditLog.getGlusterVolumeId();
        this.glusterVolumeName = auditLog.getGlusterVolumeName();
        this.jobId = auditLog.getJobId();
        this.mUserId = auditLog.getUserId();
        this.mUserName = auditLog.getUserName();
        this.mVdsGroupId = auditLog.getVdsGroupId();
        this.mVdsName = auditLog.getVdsName();
        this.mVmName = auditLog.getVmName();
        this.mVmTemplateId = auditLog.getVmTemplateId();
        this.mVmTemplateName = auditLog.getVmTemplateName();
        this.origin = auditLog.getOrigin();
        this.external = auditLog.isExternal();
        this.callStack = auditLog.getCallStack();
    }

    public Guid getUserId() {
        if (mUserId != null && mUserId.equals(Guid.Empty) && getCurrentUser() != null) {
            mUserId = getCurrentUser().getId();
        }
        return mUserId;
    }

    public void setUserId(final Guid value) {
        mUserId = value;
    }

    public String getUserName() {
        if (StringUtils.isEmpty(mUserName) && getCurrentUser() != null) {
            mUserName = String.format("%s[%s]", getCurrentUser().getLoginName(), getCurrentUser().getDomain());
        }
        return mUserName;
    }

    public void setUserName(final String value) {
        mUserName = value;
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
        mVmTemplateId = value;
    }

    public Guid getVmTemplateIdRef() {
        if (mVmTemplateId == null && getVmTemplate() != null) {
            mVmTemplateId = getVmTemplate().getId();
        }
        return mVmTemplateId;
    }

    public String getVmTemplateName() {
        if (mVmTemplateName == null && getVmTemplate() != null) {
            mVmTemplateName = getVmTemplate().getName();
        }
        return mVmTemplateName;
    }

    protected void setVmTemplateName(final String value) {
        mVmTemplateName = value;
    }

    public Guid getVmId() {
        return getVmIdRef() != null ? getVmIdRef() : Guid.Empty;
    }

    public void setVmId(final Guid value) {
        mVmId = value;
    }

    private String privateSnapshotName;

    public String getSnapshotName() {
        return privateSnapshotName;
    }

    public void setSnapshotName(final String value) {
        privateSnapshotName = value;
    }

    public Guid getVmIdRef() {
        if (Guid.isNullOrEmpty(mVmId) && getVm() != null) {
            mVmId = getVm().getId();
        }
        return mVmId;
    }

    public String getVmName() {
        if (mVmName == null && getVm() != null) {
            mVmName = getVm().getName();
        }
        return mVmName;
    }

    protected void setVmName(final String value) {
        mVmName = value;
    }

    public String getReason() {
        if (mReason == null && getVm() != null) {
            mReason = getVm().getStopReason();
        }
        return mReason;
    }

    public void setReason(String value) {
        mReason = value;
    }

    public Guid getVdsIdRef() {
        if (mVdsId == null && getVds() != null) {
            mVdsId = getVds().getId();
        }
        return mVdsId;
    }

    protected void setVdsIdRef(final Guid value) {
        mVdsId = value;
    }

    public Guid getVdsId() {
        return mVdsId != null ? mVdsId : Guid.Empty;
    }

    public void setVdsId(final Guid value) {
        mVdsId = value;
    }

    public String getVdsName() {
        if (mVdsName == null) {
            if (getVdsNoLoad() == null) {
                if (getVdsStatic() != null) {
                    mVdsName = getVdsStatic().getName();
                }
            } else {
                if (getVds() != null) {
                    mVdsName = getVds().getName();
                }
            }
        }
        return mVdsName;
    }

    protected void setVdsName(final String value) {
        mVdsName = value;
    }

    private StorageDomain _storageDomain;

    public StorageDomain getStorageDomain() {
        if (_storageDomain == null && getStorageDomainId() != null) {
            if (_storagePoolId != null && getStoragePool() != null) {
                _storageDomain = getStorageDomainDAO().getForStoragePool(
                        getStorageDomainId(), getStoragePool().getId());
            }
            if (_storageDomain == null) {
                final List<StorageDomain> storageDomainList =
                        getStorageDomainDAO().getAllForStorageDomain(getStorageDomainId());
                if (storageDomainList.size() != 0) {
                    _storageDomain = storageDomainList.get(0);
                    for (final StorageDomain storageDomainFromList : storageDomainList) {
                        if (storageDomainFromList.getStatus() == StorageDomainStatus.Active) {
                            _storageDomain = storageDomainFromList;
                            break;
                        }
                    }
                }
            }
        }
        return _storageDomain;
    }

    public void setStorageDomain(final StorageDomain value) {
        _storageDomain = value;
    }

    public Guid getStorageDomainId() {
        if (_storageDomain != null) {
            return _storageDomain.getId();
        }
        return _storageDomainId;
    }

    public void setStorageDomainId(final Guid value) {
        _storageDomainId = value;
    }

    public String getStorageDomainName() {
        if (getStorageDomain() != null) {
            return getStorageDomain().getStorageName();
        }
        return "";
    }

    private StoragePool _storagePool;

    public StoragePool getStoragePool() {
        if (_storagePool == null && getStoragePoolId() != null && !Guid.Empty.equals(getStoragePoolId())) {
            _storagePool = getStoragePoolDAO().get(getStoragePoolId());
        }
        return _storagePool;
    }

    public void setStoragePool(final StoragePool value) {
        _storagePool = value;
    }

    public Guid getStoragePoolId() {
        if (_storagePoolId == null) {
            if (_storagePool != null) {
                _storagePoolId = _storagePool.getId();
            } else if (getStorageDomain() != null) {
                _storagePoolId = getStorageDomain().getStoragePoolId();
            }
        }
        return _storagePoolId;
    }

    public void setStoragePoolId(final Guid value) {
        _storagePoolId = value;
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
        return mVds;
    }

    protected VDS getVds() {
        if (mVds == null
                && ((mVdsId != null && !Guid.Empty.equals(mVdsId)) || (getVm() != null && getVm().getRunOnVds() != null))) {
            if (mVdsId == null || Guid.Empty.equals(mVdsId)) {
                mVdsId = getVm().getRunOnVds();
            }
            try {
                mVds = getVdsDAO().get(getVdsId());
            } catch (final RuntimeException e) {
                log.info("Failed to get vds '{}', error {}", mVdsId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return mVds;
    }

    protected VdsStatic getVdsStatic() {
        if (cachedVdsStatic == null
                && ((mVdsId != null && !Guid.Empty.equals(mVdsId)) || (getVm() != null && getVm().getRunOnVds() != null))) {
            if (mVdsId == null || Guid.Empty.equals(mVdsId)) {
                mVdsId = getVm().getRunOnVds();
            }
            try {
                cachedVdsStatic = getVdsStaticDAO().get(getVdsId());
            } catch (final RuntimeException e) {
                log.info("Failed to get vds '{}', error: {}", mVdsId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return cachedVdsStatic;
    }

    public void setVds(final VDS value) {
        mVds = value;
        mVdsName = null;
        if (value != null) {
            mVdsId = value.getId();
        }
    }

    public VM getVm() {
        if (mVm == null && mVmId != null && !mVmId.equals(Guid.Empty)) {
            try {
                mVm = getVmDAO().get(mVmId);

                // TODO: This is done for backwards compatibility with VMDAO.getById(Guid)
                // It should probably be removed, but some research is required
                if (mVm != null) {
                    mVm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(mVmId));
                }
            } catch (final Exception e) {
                log.info("Failed to get vm '{}', error {}", mVmId, e.getMessage());
                log.debug("Exception", e);
            }
        }
        return mVm;
    }

    protected void setVm(final VM value) {
        mVm = value;
    }

    public VmTemplate getVmTemplate() {
        if (mVmTemplate == null && (mVmTemplateId != null || getVm() != null)) {

            mVmTemplate = getVmTemplateDAO()
                    .get(mVmTemplateId != null ? getVmTemplateId() : getVm()
                            .getVmtGuid());
        }
        return mVmTemplate;
    }

    protected void setVmTemplate(final VmTemplate value) {
        mVmTemplate = value;
    }

    public Guid getVdsGroupId() {
        if (mVdsGroupId != null) {
            return mVdsGroupId;
        } else if (getVdsGroup() != null) {
            mVdsGroupId = getVdsGroup().getId();
            return mVdsGroupId;
        } else {
            return Guid.Empty;
        }
    }

    public void setVdsGroupId(final Guid value) {
        mVdsGroupId = value;
    }

    public VDSGroup getVdsGroup() {
        if (mVdsGroup == null) {
            if (mVdsGroupId != null) {
                mVdsGroup = getVdsGroupDAO().get(mVdsGroupId);
            } else if (getVds() != null) {
                mVdsGroupId = getVds().getVdsGroupId();
                mVdsGroup = getVdsGroupDAO().get(mVdsGroupId);
            } else if (getVm() != null) {
                mVdsGroupId = getVm().getVdsGroupId();
                mVdsGroup = getVdsGroupDAO().get(mVdsGroupId);
            } else if (getVmTemplate() != null) {
                mVdsGroupId = getVmTemplate().getVdsGroupId();
                mVdsGroup = getVdsGroupDAO().get(mVdsGroupId);
            }
        }
        return mVdsGroup;
    }

    protected void setVdsGroup(final VDSGroup value) {
        mVdsGroup = value;
    }

    public String getVdsGroupName() {
        if (getVdsGroup() != null) {
            return getVdsGroup().getName();
        }
        return "";
    }

    protected void log() {
        final Transaction transaction = TransactionSupport.suspend();
        try {
            try {
                AuditLogDirector.log(this);
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
        if (Collections.EMPTY_MAP.equals(customValues)) {
            customValues = new HashMap<String, String>();
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

    public StorageDomainDAO getStorageDomainDAO() {
        return getDbFacade().getStorageDomainDao();
    }

    public StoragePoolDAO getStoragePoolDAO() {
        return getDbFacade().getStoragePoolDao();
    }

    public StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return getDbFacade().getStorageDomainStaticDao();
    }

    public VdsDAO getVdsDAO() {
        return getDbFacade().getVdsDao();
    }

    public VdsStaticDAO getVdsStaticDAO() {
        return getDbFacade().getVdsStaticDao();
    }

    public VdsDynamicDAO getVdsDynamicDao() {
        return getDbFacade().getVdsDynamicDao();
    }

    public VmTemplateDAO getVmTemplateDAO() {
        return getDbFacade().getVmTemplateDao();
    }

    public VmDAO getVmDAO() {
        return getDbFacade().getVmDao();
    }

    public VmStaticDAO getVmStaticDAO() {
        return getDbFacade().getVmStaticDao();
    }

    public SnapshotDao getSnapshotDAO() {
        return getDbFacade().getSnapshotDao();
    }

    public VmAndTemplatesGenerationsDAO getVmAndTemplatesGenerationsDAO() {
        return DbFacade.getInstance().getVmAndTemplatesGenerationsDao();
    }

    public StorageDomainOvfInfoDao getStorageDomainOvfInfoDAO() {
        return DbFacade.getInstance().getStorageDomainOvfInfoDao();
    }

    public VmDynamicDAO getVmDynamicDAO() {
        return getDbFacade().getVmDynamicDao();
    }

    protected VmStatisticsDAO getVmStatisticsDAO() {
        return getDbFacade().getVmStatisticsDao();
    }

    protected VnicProfileDao getVnicProfileDao() {
        return getDbFacade().getVnicProfileDao();
    }

    public VdsGroupDAO getVdsGroupDAO() {
        return getDbFacade().getVdsGroupDao();
    }

    public RoleDAO getRoleDao() {
        return getDbFacade().getRoleDao();
    }

    public RoleGroupMapDAO getRoleGroupMapDAO() {
        return getDbFacade().getRoleGroupMapDao();
    }

    public PermissionDAO getPermissionDAO() {
        return getDbFacade().getPermissionDao();
    }

    public DbUserDAO getDbUserDAO() {
        return getDbFacade().getDbUserDao();
    }

    public DbGroupDAO getAdGroupDAO() {
        return getDbFacade().getDbGroupDao();
    }

    public VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected VmNicDao getVmNicDao() {
        return getDbFacade().getVmNicDao();
    }

    protected NetworkClusterDao getNetworkClusterDAO() {
        return getDbFacade().getNetworkClusterDao();
    }

    protected NetworkDao getNetworkDAO() {
        return getDbFacade().getNetworkDao();
    }

    public AsyncTaskDAO getAsyncTaskDao() {
        return getDbFacade().getAsyncTaskDao();
    }

    public StepDao getStepDao() {
        return getDbFacade().getStepDao();
    }
    public ProviderDao getProviderDao() {
        return getDbFacade().getProviderDao();
    }

    public AuditLogDAO getAuditLogDao() {
        return getDbFacade().getAuditLogDao();
    }

    public DiskProfileDao getDiskProfileDao() {
        return getDbFacade().getDiskProfileDao();
    }

    public CpuProfileDao getCpuProfileDao() {
        return getDbFacade().getCpuProfileDao();
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
}

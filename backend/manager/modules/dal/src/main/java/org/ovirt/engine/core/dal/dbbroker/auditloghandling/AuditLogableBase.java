package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transaction;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AdGroupDAO;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.dao.RoleDAO;
import org.ovirt.engine.core.dao.RoleGroupMapDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
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
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AuditLogableBase extends TimeoutBase {
    private static final long serialVersionUID = -4764813076922800727L;
    private NGuid mVmId = Guid.Empty;
    private IVdcUser mVdcUser;
    private NGuid mUserId = Guid.Empty;
    private String mUserName;
    private String mVmName;
    private Map<String, String> customValues = Collections.emptyMap();
    private NGuid mVdsId;
    private String mVdsName;
    private NGuid mVmTemplateId;
    private String mVmTemplateName;
    private VDS mVds;
    private VM mVm;
    private VmTemplate mVmTemplate;
    private NGuid _storageDomainId;
    private NGuid _storagePoolId;
    private Guid mVdsGroupId;
    private VDSGroup mVdsGroup;
    private String correlationId;
    private NGuid jobId;
    private boolean isInternalExecution = false;
    private NGuid glusterVolumeId;
    private String glusterVolumeName;
    private GlusterVolumeEntity glusterVolume;
    private Integer customId = null;
    private String origin = "oVirt";
    private int customEventId = -1;
    private int eventFloodInSec = 30;
    private String customData = "";
    private boolean external = false;
    private String storagePoolType;
    private String compatibilityVersion;
    private String quotaEnforcementType;
    private Guid quotaIdForLog;
    private String quotaNameForLog;

    public AuditLogableBase() {
    }

    public AuditLogableBase(final NGuid vdsId) {
        mVdsId = vdsId;
    }

    public AuditLogableBase(final NGuid vdsId, final Guid vmId) {
        this(vdsId);
        mVmId = vmId;
    }

    public AuditLogableBase(final AuditLog auditLog) {
        this._storageDomainId = auditLog.getstorage_domain_id();
        this._storagePoolId = auditLog.getstorage_pool_id();
        this.correlationId = auditLog.getCorrelationId();
        this.customData = auditLog.getCustomData();
        this.customEventId = auditLog.getCustomEventId();
        this.eventFloodInSec = auditLog.getEventFloodInSec();
        this.glusterVolumeId = auditLog.getGlusterVolumeId();
        this.glusterVolumeName = auditLog.getGlusterVolumeName();
        this.jobId = auditLog.getJobId();
        this.mUserId = auditLog.getuser_id();
        this.mUserName = auditLog.getuser_name();
        this.mVdsGroupId = (Guid) auditLog.getvds_group_id();
        this.mVdsId = auditLog.getvds_id();
        this.mVdsName = auditLog.getvds_name();
        this.mVmId = auditLog.getvm_id();
        this.mVmName = auditLog.getvm_name();
        this.mVmTemplateId = auditLog.getvm_template_id();
        this.mVmTemplateName = auditLog.getvm_template_name();
        this.origin = auditLog.getOrigin();
        this.external = auditLog.isExternal();
    }
    public NGuid getUserId() {
        if (mUserId != null && mUserId.equals(Guid.Empty) && getCurrentUser() != null) {
            mUserId = getCurrentUser().getUserId();
        }
        return mUserId;
    }

    public void setUserId(final NGuid value) {
        mUserId = value.getValue();
    }

    public String getUserName() {
        if (StringUtils.isEmpty(mUserName) && getCurrentUser() != null) {
            mUserName = getCurrentUser().getUserName();
        }
        return mUserName;
    }

    public void setUserName(final String value) {
        mUserName = value;
    }

    public IVdcUser getCurrentUser() {
        return mVdcUser;
    }

    public void setCurrentUser(final IVdcUser value) {
        mVdcUser = value;
    }

    public Guid getVmTemplateId() {
        return getVmTemplateIdRef() != null ? getVmTemplateIdRef().getValue() : Guid.Empty;
    }

    public void setVmTemplateId(final Guid value) {
        mVmTemplateId = value;
    }

    public NGuid getVmTemplateIdRef() {
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
        return getVmIdRef() != null ? getVmIdRef().getValue() : Guid.Empty;
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

    public NGuid getVmIdRef() {
        if (mVmId == null && getVm() != null) {
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

    public NGuid getVdsIdRef() {
        if (mVdsId == null && getVds() != null) {
            mVdsId = getVds().getId();
        }
        return mVdsId;
    }

    protected void setVdsIdRef(final NGuid value) {
        mVdsId = value;
    }

    public Guid getVdsId() {
        return mVdsId != null ? mVdsId.getValue() : Guid.Empty;
    }

    public void setVdsId(final Guid value) {
        mVdsId = value;
    }

    public String getVdsName() {
        if (mVdsName == null && getVds() != null) {
            mVdsName = getVds().getName();
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
                        getStorageDomainId().getValue(), getStoragePool().getId());
            }
            if (_storageDomain == null) {
                final List<StorageDomain> storageDomainList =
                        getStorageDomainDAO().getAllForStorageDomain(getStorageDomainId().getValue());
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

    public NGuid getStorageDomainId() {
        if (_storageDomain != null) {
            return _storageDomain.getId();
        }
        return _storageDomainId;
    }

    public void setStorageDomainId(final NGuid value) {
        _storageDomainId = value;
    }

    public String getStorageDomainName() {
        if (getStorageDomain() != null) {
            return getStorageDomain().getStorageName();
        }
        return "";
    }

    private storage_pool _storagePool;

    public storage_pool getStoragePool() {
        if (_storagePool == null && getStoragePoolId() != null && !Guid.Empty.equals(getStoragePoolId())) {
            _storagePool = getStoragePoolDAO().get(getStoragePoolId().getValue());
        }
        return _storagePool;
    }

    public void setStoragePool(final storage_pool value) {
        _storagePool = value;
    }

    public NGuid getStoragePoolId() {
        if (_storagePoolId == null) {
            if (_storagePool != null) {
                _storagePoolId = _storagePool.getId();
            } else if (getStorageDomain() != null) {
                _storagePoolId = getStorageDomain().getStoragePoolId();
            }
        }
        return _storagePoolId;
    }

    public void setStoragePoolId(final NGuid value) {
        _storagePoolId = value;
    }

    public String getStoragePoolName() {
        if (getStoragePool() != null) {
            return getStoragePool().getname();
        }
        return "";
    }

    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }

    protected VDS getVds() {
        if (mVds == null
                && ((mVdsId != null && !Guid.Empty.equals(mVdsId)) || (getVm() != null && getVm().getRunOnVds() != null))) {
            if (mVdsId == null || Guid.Empty.equals(mVdsId)) {
                mVdsId = getVm().getRunOnVds().getValue();
            }
            try {
                mVds = getVdsDAO().get(getVdsId());
            } catch (final RuntimeException e) {
                log.infoFormat("Failed to get vds {0}\n{1}", mVdsId, e.getMessage());
            }
        }
        return mVds;
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
                mVm = getVmDAO().get(mVmId.getValue());

                // TODO: This is done for backwards compatibility with VMDAO.getById(Guid)
                // It should probably be removed, but some research is required
                if (mVm != null) {
                    mVm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(mVmId.getValue()));
                }
            } catch (final Exception e) {
                log.infoFormat("Failed to get vm {0}", mVmId);
                log.debug(e);
            }
        }
        return mVm;
    }

    protected void setVm(final VM value) {
        mVm = value;
    }

    public VmTemplate getVmTemplate() {
        if (mVmTemplate == null && (mVmTemplateId != null || getVm() != null))

        {

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

    protected VDSGroup getVdsGroup() {
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
            return getVdsGroup().getname();
        }
        return "";
    }

    protected void log() {
        final Transaction transaction = TransactionSupport.suspend();
        try {
            try {
                AuditLogDirector.log(this);
            } catch (final RuntimeException ex) {
                log.errorFormat("Error during log command: {0}. Exception {1}", getClass().getName(), ex);
            }
        } finally {
            TransactionSupport.resume(transaction);
        }
    }

    public void addCustomValue(final String name, final String value) {
        allocateCustomValues();
        customValues.put(name.toLowerCase(), value);
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

    public void setJobId(NGuid jobId) {
        this.jobId = jobId;
    }

    public NGuid getJobId() {
        return jobId;
    }

    public boolean isInternalExecution() {
        return isInternalExecution;
    }

    public void setInternalExecution(boolean isInternalExecution) {
        this.isInternalExecution = isInternalExecution;
    }

    public NGuid getGlusterVolumeId() {
        return glusterVolumeId != null ? glusterVolumeId : Guid.Empty;
    }

    public void setGlusterVolumeId(NGuid value) {
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
            glusterVolume = getGlusterVolumeDao().getById(glusterVolumeId.getValue());
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

    public VdsDAO getVdsDAO() {
        return getDbFacade().getVdsDao();
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

    public VmDynamicDAO getVmDynamicDAO() {
        return getDbFacade().getVmDynamicDao();
    }

    protected VmStatisticsDAO getVmStatisticsDAO() {
        return getDbFacade().getVmStatisticsDao();
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

    public AdGroupDAO getAdGroupDAO() {
        return getDbFacade().getAdGroupDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return getDbFacade().getVmNetworkInterfaceDao();
    }

    protected NetworkClusterDao getNetworkClusterDAO() {
        return getDbFacade().getNetworkClusterDao();
    }

    protected NetworkDao getNetworkDAO() {
        return getDbFacade().getNetworkDao();
    }

    protected AsyncTaskDAO getAsyncTaskDao() {
        return getDbFacade().getAsyncTaskDao();
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
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

    public String getStoragePoolType() {
        return storagePoolType;
    }

    public void setStoragePoolType(String storagePoolType) {
        this.storagePoolType = storagePoolType;
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

    private static final Log log = LogFactory.getLog(AuditLogableBase.class);

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
}

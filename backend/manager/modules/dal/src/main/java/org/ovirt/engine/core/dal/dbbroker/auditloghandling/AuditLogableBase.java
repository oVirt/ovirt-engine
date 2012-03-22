package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.List;

import javax.transaction.Transaction;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmStatisticsDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AuditLogableBase extends TimeoutBase {
    private static final long serialVersionUID = -4764813076922800727L;
    private NGuid mVmId = Guid.Empty;
    private IVdcUser mVdcUser;
    private Guid mUserId = Guid.Empty;
    private String mUserName;
    private String mVmName;
    private final java.util.HashMap<String, String> customValues = new java.util.HashMap<String, String>();
    private NGuid mVdsId;
    private String mVdsName;
    private NGuid mVmTemplateId;
    private String mVmTemplateName;
    private VDS mVds;
    private Quota quota;
    private Guid quotaId;
    private String quotaName;
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

    public AuditLogableBase() {
    }

    public AuditLogableBase(final NGuid vdsId) {
        mVdsId = vdsId;
    }

    public AuditLogableBase(final NGuid vdsId, final Guid vmId) {
        this(vdsId);
        mVmId = vmId;
    }

    public NGuid getUserId() {
        if (mUserId.equals(Guid.Empty) && getCurrentUser() != null) {
            mUserId = getCurrentUser().getUserId();
        }
        return mUserId;
    }

    public void setUserId(final NGuid value) {
        mUserId = value.getValue();
    }

    public String getUserName() {
        if (StringHelper.isNullOrEmpty(mUserName) && getCurrentUser() != null) {
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
            mVmTemplateName = getVmTemplate().getname();
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
            mVmName = getVm().getvm_name();
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
            mVdsName = getVds().getvds_name();
        }
        return mVdsName;
    }

    protected void setVdsName(final String value) {
        mVdsName = value;
    }

    public Guid getQuotaId() {
        return getQuotaIdRef() != null ? getQuotaIdRef().getValue() : Guid.Empty;
    }

    public Guid getQuotaIdRef() {
        if (quotaId == null && getQuota() != null) {
            quotaId = getQuota().getId();
        }
        return quotaId;
    }

    public void setQuotaId(final Guid value) {
        quotaId = value;
    }

    public String getQuotaName() {
        if (quotaName == null && getQuota() != null) {
            quotaName = getQuota().getQuotaName();
        }
        return quotaName;
    }

    protected void setQuotaName(final String value) {
        quotaName = value;
    }

    private storage_domains _storageDomain;

    public storage_domains getStorageDomain() {
        if (_storageDomain == null && getStorageDomainId() != null) {
            if (_storagePoolId != null && getStoragePool() != null) {
                _storageDomain = getStorageDomainDAO().getForStoragePool(
                        getStorageDomainId().getValue(), getStoragePool().getId());
            }
            if (_storageDomain == null) {
                final List<storage_domains> storageDomainList =
                        getStorageDomainDAO().getAllForStorageDomain(getStorageDomainId().getValue());
                if (storageDomainList.size() != 0) {
                    _storageDomain = storageDomainList.get(0);
                    for (final storage_domains storageDomainFromList : storageDomainList) {
                        if (storageDomainFromList.getstatus() != null
                                && storageDomainFromList.getstatus() == StorageDomainStatus.Active) {
                            _storageDomain = storageDomainFromList;
                            break;
                        }
                    }
                }
            }
        }
        return _storageDomain;
    }

    public StorageDomainDAO getStorageDomainDAO() {
        return DbFacade.getInstance().getStorageDomainDAO();
    }

    public void setStorageDomain(final storage_domains value) {
        _storageDomain = value;
    }

    public NGuid getStorageDomainId() {
        if (_storageDomain != null) {
            return _storageDomain.getId();
        } else {
            return _storageDomainId;
        }
    }

    public void setStorageDomainId(final NGuid value) {
        _storageDomainId = value;
    }

    public String getStorageDomainName() {
        if (getStorageDomain() != null) {
            return getStorageDomain().getstorage_name();
        } else {
            return "";
        }
    }

    private storage_pool _storagePool;

    public storage_pool getStoragePool() {
        if (_storagePool == null && getStoragePoolId() != null && !Guid.Empty.equals(getStoragePoolId())) {
            _storagePool = getStoragePoolDAO().get(getStoragePoolId().getValue());
        }
        return _storagePool;
    }

    public StoragePoolDAO getStoragePoolDAO() {
        return DbFacade.getInstance().getStoragePoolDAO();
    }

    public void setStoragePool(final storage_pool value) {
        _storagePool = value;
    }

    public NGuid getStoragePoolId() {
        if (_storagePoolId == null) {
            if (_storagePool != null) {
                _storagePoolId = _storagePool.getId();
            } else if (getStorageDomain() != null) {
                _storagePoolId = getStorageDomain().getstorage_pool_id();
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
        } else {
            return "";
        }
    }

    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }

    protected Quota getQuota() {
        if (quota == null && quotaId != null) {
            try {
                quota = getQuotaDAO().getById(getQuotaId());
            } catch (final RuntimeException e) {
                log.infoFormat("Failed to get quota {0}\n{1}", quotaId, e.getMessage());
            }
        }
        return quota;
    }

    protected void setQuota(final Quota value) {
        quota = value;
        quotaName = null;
    }

    public VdsDAO getVdsDAO() {
        return DbFacade.getInstance().getVdsDAO();
    }

    public QuotaDAO getQuotaDAO() {
        return DbFacade.getInstance().getQuotaDAO();
    }

    protected VDS getVds() {
        if (mVds == null && (mVdsId != null || (getVm() != null && getVm().getrun_on_vds() != null))) {
            if (mVdsId == null) {
                mVdsId = new Guid(getVm().getrun_on_vds().toString());
            }
            try {
                mVds = getVdsDAO().get(getVdsId());
            } catch (final RuntimeException e) {
                log.infoFormat("Failed to get vds {0}\n{1}", mVdsId, e.getMessage());
            }
        }
        return mVds;
    }

    protected void setVds(final VDS value) {
        mVds = value;
        mVdsName = null;
    }

    public VM getVm() {
        if (mVm == null && mVmId != null && !mVmId.equals(Guid.Empty)) {
            try {
                mVm = getVmDAO().getById(mVmId.getValue());
            } catch (final java.lang.Exception e) {
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
                            .getvmt_guid());
        }
        return mVmTemplate;
    }

    public VmTemplateDAO getVmTemplateDAO() {
        return DbFacade.getInstance().getVmTemplateDAO();
    }

    protected VmDAO getVmDAO() {
        return DbFacade.getInstance().getVmDAO();
    }

    public VmStaticDAO getVmStaticDAO() {
        return DbFacade.getInstance().getVmStaticDAO();
    }

    protected VmStatisticsDAO getVmStatisticsDAO() {
        return DbFacade.getInstance().getVmStatisticsDAO();
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
                mVdsGroupId = getVds().getvds_group_id();
                mVdsGroup = getVdsGroupDAO().get(mVdsGroupId);
            } else if (getVm() != null) {
                mVdsGroupId = getVm().getvds_group_id();
                mVdsGroup = getVdsGroupDAO().get(mVdsGroupId);
            }
        }
        return mVdsGroup;
    }

    protected VdsGroupDAO getVdsGroupDAO() {
        return DbFacade.getInstance().getVdsGroupDAO();
    }

    protected void setVdsGroup(final VDSGroup value) {
        mVdsGroup = value;
    }

    public String getVdsGroupName() {
        if (getVdsGroup() != null) {
            return getVdsGroup().getname();
        } else {
            return "";
        }
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

    public void DefaultLog() {
    }

    public void AddCustomValue(final String name, final String value) {
        customValues.put(name.toLowerCase(), value);
    }

    public void AddCustomValue(CustomAuditLogKeys key, final String value) {
        AddCustomValue(key.name(), value);
    }

    public void AppendCustomValue(final String name, final String value, final String separator) {
        final String key = name.toLowerCase();
        String newValue = value;
        if (customValues.containsKey(key)) {
            newValue = String.format("%1$s%2$s%3$s", customValues.get(key), separator, value);
        }
        customValues.put(name.toLowerCase(), newValue);
    }

    public java.util.Map<String, String> getCustomValues() {
        return customValues;
    }

    @Override
    protected String getKey() {
        return getAuditLogTypeValue().toString();
    }

    public String GetCustomValue(final String name) {
        if (customValues.containsKey(name)) {
            return customValues.get(name);
        } else {
            return "";
        }
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

    private static Log log = LogFactory.getLog(AuditLogableBase.class);

}

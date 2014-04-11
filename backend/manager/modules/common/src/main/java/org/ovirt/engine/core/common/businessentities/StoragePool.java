package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class StoragePool extends IVdcQueryable implements BusinessEntityWithStatus<Guid, StoragePoolStatus>, Nameable, Commented {
    private static final long serialVersionUID = 6162262095329980112L;

    private Guid id;

    @ValidName(message = "VALIDATION.DATA_CENTER.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.DATACENTER_NAME_SIZE)
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.ONLY_ASCII_OR_NONE,
            message = "VALIDATION.DATA_CENTER.DESCRIPTION.INVALID",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String description;

    private String comment = "";

    private boolean local;

    private StorageFormatType storagePoolFormatType;

    private StoragePoolStatus status;

    private int masterDomainVersion;

    private Guid spmVdsId;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_VERSION_SIZE)
    private String compatibilityVersion;

    private String LVER;

    private RecoveryMode recovery_mode;

    private Version version;

    private QuotaEnforcementTypeEnum quotaEnforcementType;

    private Guid macPoolId;

    public StoragePool() {
        id = Guid.Empty;
        status = StoragePoolStatus.Uninitialized;
        spmVdsId = Guid.Empty;
        recovery_mode = RecoveryMode.Manual;
        quotaEnforcementType = QuotaEnforcementTypeEnum.DISABLED;
        name = "";
        description = "";
        comment = "";
        masterDomainVersion = 0;
    }

    public String getdescription() {
        if (description == null) {
            description = "";
        }
        return description;
    }

    public void setdescription(String value) {
        this.description = value;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String value) {
        this.comment = (value == null) ? "" : value;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    /**
     * @return True is the Storage Pool is local, false if it's shared
     */
    public boolean isLocal() {
        return local;
    }

    public void setIsLocal(boolean isLocal) {
        this.local = isLocal;
    }

    public StorageFormatType getStoragePoolFormatType() {
        return storagePoolFormatType;
    }

    public void setStoragePoolFormatType(StorageFormatType value) {
        storagePoolFormatType = value;
    }

    @Override
    public StoragePoolStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(StoragePoolStatus value) {
        status = value;
    }

    public int getmaster_domain_version() {
        return masterDomainVersion;
    }

    public void setmaster_domain_version(int value) {
        this.masterDomainVersion = value;
    }

    public Version getcompatibility_version() {
        if (version == null) {
            version = new Version(compatibilityVersion);
        }
        return version;
    }

    public void setcompatibility_version(Version value) {
        compatibilityVersion = value.toString();
        version = null;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public String getLVER() {
        return LVER;
    }

    public void setLVER(String value) {
        LVER = value;
    }

    public RecoveryMode getrecovery_mode() {
        return recovery_mode;
    }

    public void setrecovery_mode(RecoveryMode value) {
        recovery_mode = value;
    }

    public Guid getspm_vds_id() {
        return spmVdsId;
    }

    public void setspm_vds_id(Guid value) {
        spmVdsId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((LVER == null) ? 0 : LVER.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((compatibilityVersion == null) ? 0 : compatibilityVersion.hashCode());
        result = prime * result + masterDomainVersion;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((recovery_mode == null) ? 0 : recovery_mode.hashCode());
        result = prime * result + ((spmVdsId == null) ? 0 : spmVdsId.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + (local ? 1231 : 1237);
        result = prime * result + ((storagePoolFormatType == null) ? 0 : storagePoolFormatType.hashCode());
        result = prime * result + ((quotaEnforcementType == null) ? 0 : quotaEnforcementType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StoragePool other = (StoragePool) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(LVER, other.LVER)
                && ObjectUtils.objectsEqual(description, other.description)
                && ObjectUtils.objectsEqual(comment, other.comment)
                && ObjectUtils.objectsEqual(getcompatibility_version(), other.getcompatibility_version())
                && masterDomainVersion == other.masterDomainVersion
                && ObjectUtils.objectsEqual(name, other.name)
                && recovery_mode == other.recovery_mode
                && ObjectUtils.objectsEqual(spmVdsId, other.spmVdsId)
                && status == other.status
                && local == other.local
                && ObjectUtils.objectsEqual(storagePoolFormatType, other.storagePoolFormatType)
                && quotaEnforcementType == other.quotaEnforcementType);
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

    public Guid getMacPoolId() {
        return macPoolId;
    }

    public void setMacPoolId(Guid macPoolId) {
        this.macPoolId = macPoolId;
    }
}

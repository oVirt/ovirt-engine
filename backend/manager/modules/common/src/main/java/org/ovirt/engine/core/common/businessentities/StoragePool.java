package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class StoragePool implements Queryable, BusinessEntityWithStatus<Guid, StoragePoolStatus>, Nameable, Commented {
    private static final long serialVersionUID = 6162262095329980112L;

    private Guid id;

    @ValidName(message = "VALIDATION_DATA_CENTER_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.DATACENTER_NAME_SIZE)
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.ONLY_ASCII_OR_NONE,
            message = "VALIDATION_DATA_CENTER_DESCRIPTION_INVALID",
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

    private RecoveryMode recoveryMode;

    private Version version;

    private QuotaEnforcementTypeEnum quotaEnforcementType;

    private boolean storagePoolCompatibilityLevelUpgradeNeeded;

    private boolean managed;

    /**
     * Unique mac pool over whole data center. Data center DOES NOT have mac pool associated, only its clusters have.
     * When updating DC, setting this will set mac pool for all its clusters. When getting DC, this property
     * will be/should be set only when all DCs clusters share same mac pool.
     */
    private Guid macPoolId;

    public StoragePool() {
        id = Guid.Empty;
        status = StoragePoolStatus.Uninitialized;
        spmVdsId = Guid.Empty;
        recoveryMode = RecoveryMode.Manual;
        quotaEnforcementType = QuotaEnforcementTypeEnum.DISABLED;
        name = "";
        description = "";
        comment = "";
        masterDomainVersion = 0;
        managed = true;
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

    public int getMasterDomainVersion() {
        return masterDomainVersion;
    }

    public void setMasterDomainVersion(int value) {
        this.masterDomainVersion = value;
    }

    public Version getCompatibilityVersion() {
        if (version == null) {
            version = new Version(compatibilityVersion);
        }
        return version;
    }

    public void setCompatibilityVersion(Version value) {
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

    public RecoveryMode getRecoveryMode() {
        return recoveryMode;
    }

    public void setRecoveryMode(RecoveryMode value) {
        recoveryMode = value;
    }

    public Guid getSpmVdsId() {
        return spmVdsId;
    }

    public void setSpmVdsId(Guid value) {
        spmVdsId = value;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                LVER,
                description,
                comment,
                compatibilityVersion,
                masterDomainVersion,
                name,
                recoveryMode,
                spmVdsId,
                status,
                local,
                storagePoolFormatType,
                quotaEnforcementType,
                managed
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StoragePool)) {
            return false;
        }
        StoragePool other = (StoragePool) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(LVER, other.LVER)
                && Objects.equals(description, other.description)
                && Objects.equals(comment, other.comment)
                && Objects.equals(getCompatibilityVersion(), other.getCompatibilityVersion())
                && masterDomainVersion == other.masterDomainVersion
                && Objects.equals(name, other.name)
                && recoveryMode == other.recoveryMode
                && Objects.equals(spmVdsId, other.spmVdsId)
                && status == other.status
                && local == other.local
                && Objects.equals(storagePoolFormatType, other.storagePoolFormatType)
                && quotaEnforcementType == other.quotaEnforcementType
                && managed == other.managed;
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

    public boolean isStoragePoolCompatibilityLevelUpgradeNeeded() {
        return storagePoolCompatibilityLevelUpgradeNeeded;
    }

    public void setStoragePoolCompatibilityLevelUpgradeNeeded(boolean storagePoolCompatibilityLevelUpgradeNeeded) {
        this.storagePoolCompatibilityLevelUpgradeNeeded = storagePoolCompatibilityLevelUpgradeNeeded;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("name", getName())
                .append("id", getId())
                .build();
    }
}

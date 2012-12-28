package org.ovirt.engine.core.common.businessentities;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;

@Entity
@Table(name = "storage_pool")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries(
        value = {
                @NamedQuery(
                        name = "all_storage_pools_for_storage_domain",
                        query = "from storage_pool p where (from storage_domain_static d where d.id = :storage_domain_id) in elements(p.domains)"),
                @NamedQuery(
                        name = "all_storage_pools_for_vds",
                        query = "select p from storage_pool p where p.id in (select g.storagePool from VDSGroup g where g.storagePool = p.id and g.id in (select v.vdsGroupId from VdsStatic v where v.vdsGroupId = g.id and v.id = :vds_id))"),

                @NamedQuery(
                        name = "all_storage_pools_for_vds_group",
                        query = "select p from storage_pool p, VDSGroup g where (g.id = :vds_group_id) and (g.storagePool = p.id)")
                        })
public class storage_pool extends IVdcQueryable implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 8455998477522459262L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "id")
    @Type(type = "guid")
    private Guid id = new Guid();

    @ValidName(message = "VALIDATION.DATA_CENTER.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.DATACENTER_NAME_SIZE)
    @Column(name = "name")
    private String name = ""; // GREGM prevents NPE

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description")
    @Pattern(regexp = ValidationUtils.ONLY_ASCII_OR_NONE,
            message = "VALIDATION.DATA_CENTER.DESCRIPTION.INVALID",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String description;

    @Column(name = "storage_pool_type")
    private int storagePoolType = StorageType.UNKNOWN.getValue();

    @Column(name = "storage_pool_format_type")
    private StorageFormatType storagePoolFormatType = null;

    @Column(name = "status")
    private StoragePoolStatus status = StoragePoolStatus.Uninitialized;
    @Column(name = "master_domain_version")
    private int masterDomainVersion;

    @Column(name = "spm_vds_id")
    @Type(type = "guid")
    private NGuid spmVdsId = new NGuid();

    @Size(max = BusinessEntitiesDefinitions.GENERAL_VERSION_SIZE)
    @Column(name = "compatibility_version")
    private String compatibilityVersion;

    @Transient
    private String LVER;

    @Transient
    private RecoveryMode recovery_mode = RecoveryMode.forValue(0);

    // TODO this is a hack to get around how the old mappings were done
    // This will be redone in version 3.0 with proper relationship mapping
    @OneToMany(mappedBy = "storagePool", cascade = CascadeType.MERGE)
    private Set<VDSGroup> vdsGroups;

    @ManyToMany
    @JoinTable(name = "storage_pool_iso_map", joinColumns = @JoinColumn(name = "storage_pool_id"),
            inverseJoinColumns = @JoinColumn(name = "storage_id"))
    private List<StorageDomainStatic> domains;

    @Transient
    private Version version;

    private QuotaEnforcementTypeEnum quotaEnforcementType = QuotaEnforcementTypeEnum.DISABLED;

    public storage_pool() {
        description = "";
        masterDomainVersion = 0;
    }

    public storage_pool(String description, Guid id, String name, int storage_pool_type, int status) {
        this();
        this.description = description;
        this.id = id;
        this.name = name;
        setstorage_pool_type(StorageType.forValue(storage_pool_type));
        this.status = StoragePoolStatus.forValue(status);
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
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        this.name = value;
    }

    public StorageType getstorage_pool_type() {
        return StorageType.forValue(storagePoolType);

    }

    public void setstorage_pool_type(StorageType value) {
        storagePoolType = value.getValue();
    }

    public StorageFormatType getStoragePoolFormatType() {
        return storagePoolFormatType;
    }

    public void setStoragePoolFormatType(StorageFormatType value) {
        storagePoolFormatType = value;
    }

    public StoragePoolStatus getstatus() {
        return status;
    }

    public void setstatus(StoragePoolStatus value) {
        status = value;
    }

    public int getmaster_domain_version() {
        return this.masterDomainVersion;
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

    public NGuid getspm_vds_id() {
        return spmVdsId;
    }

    public void setspm_vds_id(NGuid value) {
        spmVdsId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((LVER == null) ? 0 : LVER.hashCode());
        result = prime * result + ((compatibilityVersion == null) ? 0 : compatibilityVersion.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((domains == null) ? 0 : domains.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + masterDomainVersion;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((recovery_mode == null) ? 0 : recovery_mode.hashCode());
        result = prime * result + ((spmVdsId == null) ? 0 : spmVdsId.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + storagePoolType;
        result = prime * result + ((vdsGroups == null) ? 0 : vdsGroups.hashCode());
        result = prime * result + ((storagePoolFormatType == null) ? 0 : storagePoolFormatType.hashCode());
        result = prime * result + ((quotaEnforcementType == null) ? 0 : quotaEnforcementType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        storage_pool other = (storage_pool) obj;
        if (LVER == null) {
            if (other.LVER != null)
                return false;
        } else if (!LVER.equals(other.LVER))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (getcompatibility_version() == null) {
            if (other.getcompatibility_version() != null)
                return false;
        } else if (!getcompatibility_version().equals(other.getcompatibility_version()))
            return false;
        if (domains == null) {
            if (other.domains != null)
                return false;
        } else if (!domains.equals(other.domains))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (masterDomainVersion != other.masterDomainVersion)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (recovery_mode != other.recovery_mode)
            return false;
        if (spmVdsId == null) {
            if (other.spmVdsId != null)
                return false;
        } else if (!spmVdsId.equals(other.spmVdsId))
            return false;
        if (status != other.status)
            return false;
        if (storagePoolType != other.storagePoolType)
            return false;
        if (vdsGroups == null) {
            if (other.vdsGroups != null)
                return false;
        } else if (!vdsGroups.equals(other.vdsGroups))
            return false;
        if (storagePoolFormatType == null) {
            if (other.storagePoolFormatType != null)
                return false;
        } else if (storagePoolFormatType != other.storagePoolFormatType)
            return false;
        if (quotaEnforcementType != other.quotaEnforcementType) {
            return false;
        }
        return true;
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

}

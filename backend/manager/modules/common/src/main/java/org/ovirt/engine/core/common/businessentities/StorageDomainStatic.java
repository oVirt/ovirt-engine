package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "storage_domain_static")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries({
        @NamedQuery(
                name = "all_storage_domain_static_for_storage_pool_of_type",
                query = "select sds from storage_domain_static sds, storage_pool_iso_map map where map.id.storageId = sds.id and map.id.storagePoolId = :pool_id and sds.storagePoolType = :storage_type"),
        @NamedQuery(
                name = "all_storage_domain_static_for_storage_pool",
                query = "select sds from storage_domain_static sds, storage_pool pool, storage_pool_iso_map map where map.id.storageId = sds.id and pool.id = map.id.storagePoolId and pool.id = :pool_id"),
        @NamedQuery(
                name = "storage_domain_static_for_storage_pool",
                query = "select sds from storage_domain_static sds, storage_domain_dynamic sdd, storage_pool pool, VDSGroup vdsg, storage_pool_iso_map spimap, VdsStatic vdss where sds.id = sdd.id and sds.id = spimap.id.storageId and spimap.id.storagePoolId = pool.id and vdsg.storagePool = spimap.id.storagePoolId and vdss.vdsGroupId = vdsg.id and pool.id = :storage_pool_id and sdd.id = :id"),
        @NamedQuery(
                name = "all_storage_domain_static_for_image_group",
                query = "select sds from storage_domain_static sds, storage_domain_dynamic sdd, storage_pool pool, VDSGroup vdsg, storage_pool_iso_map spimap, VdsStatic vdss, image_group_storage_domain_map igsdmap where sds.id = sdd.id and sds.id = spimap.id.storageId and spimap.id.storagePoolId = pool.id and vdsg.storagePool = spimap.id.storagePoolId and vdss.vdsGroupId = vdsg.id and igsdmap.id.storageDomainId = sdd.id and igsdmap.id.imageGroupId = :image_group_id"),
        @NamedQuery(
                name = "all_storage_domain_static_for_storage_domain",
                query = "select sds from storage_domain_static sds, storage_domain_dynamic sdd, storage_pool pool, VDSGroup vdsg, storage_pool_iso_map spimap, VdsStatic vdss where sds.id = sdd.id and sds.id = spimap.id.storageId and spimap.id.storagePoolId = pool.id and vdsg.storagePool = spimap.id.storagePoolId and vdss.vdsGroupId = vdsg.id and sds.id = :storage_domain_id") })
public class StorageDomainStatic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 8635263021145935458L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "Id")
    @Type(type = "guid")
    private Guid id = Guid.Empty;

    @Size(min = 1, max = BusinessEntitiesDefinitions.STORAGE_SIZE)
    @Column(name = "storage")
    private String storage;

    // TODO storage name needs to be made unique
    @ValidName(message = "VALIDATION.STORAGE_DOMAIN.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.STORAGE_NAME_SIZE)
    @Column(name = "storage_name")
    private String name = "";

    @Column(name = "storage_domain_type")
    private StorageDomainType storageType = StorageDomainType.Master;

    @Column(name = "storage_type")
    private StorageType storagePoolType = StorageType.UNKNOWN;

    @Transient
    private StorageServerConnections connection;

    @Column(name = "storage_domain_format_type")
    private StorageFormatType storageFormat = StorageFormatType.V1;

    private boolean autoRecoverable = true;

    @Transient
    private String storagePoolName;

    @Column(name = "last_time_used_as_master")
    private transient long lastTimeUsedAsMaster;

    public StorageDomainStatic() {
    }

    public StorageDomainStatic(Guid id, String storage, int storage_domain_type, String storage_name) {
        this.id = id;
        this.storage = storage;
        this.storageType = StorageDomainType.forValue(storage_domain_type);
        this.name = storage_name;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public String getstorage() {
        return this.storage;
    }

    public void setstorage(String value) {
        this.storage = value;
    }

    public StorageDomainType getstorage_domain_type() {
        return storageType;
    }

    public void setstorage_domain_type(StorageDomainType value) {
        this.storageType = value;
    }

    public String getstorage_pool_name() {
        return this.storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        this.storagePoolName = value;
    }

    public StorageType getstorage_type() {
        return this.storagePoolType;
    }

    public void setstorage_type(StorageType value) {
        this.storagePoolType = value;
    }

    public String getstorage_name() {
        return name;
    }

    public void setstorage_name(String value) {
        name = value;
    }

    public StorageServerConnections getConnection() {
        return connection;
    }

    public void setConnection(StorageServerConnections value) {
        connection = value;
    }

    public StorageFormatType getStorageFormat() {
        return storageFormat;
    }

    public void setStorageFormat(StorageFormatType storage_format) {
        this.storageFormat = storage_format;
    }

    public boolean isAutoRecoverable() {
        return autoRecoverable;
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        this.autoRecoverable = autoRecoverable;
    }

    public long getLastTimeUsedAsMaster() {
        return lastTimeUsedAsMaster;
    }

    public void setLastTimeUsedAsMaster(long lastTimeUsedAsMaster) {
        this.lastTimeUsedAsMaster = lastTimeUsedAsMaster;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (autoRecoverable ? 1231 : 1237);
        result = prime * result + ((connection == null) ? 0 : connection.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((storage == null) ? 0 : storage.hashCode());
        result = prime * result + ((storageFormat == null) ? 0 : storageFormat.hashCode());
        result = prime * result + ((storagePoolType == null) ? 0 : storagePoolType.hashCode());
        result = prime * result + ((storageType == null) ? 0 : storageType.hashCode());
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
        StorageDomainStatic other = (StorageDomainStatic) obj;
        if (autoRecoverable != other.autoRecoverable)
            return false;
        if (connection == null) {
            if (other.connection != null)
                return false;
        } else if (!connection.equals(other.connection))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (storage == null) {
            if (other.storage != null)
                return false;
        } else if (!storage.equals(other.storage))
            return false;
        if (storageFormat != other.storageFormat)
            return false;
        if (storagePoolType != other.storagePoolType)
            return false;
        if (storageType != other.storageType)
            return false;
        return true;
    }
}

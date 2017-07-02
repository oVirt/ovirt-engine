package org.ovirt.engine.core.common.businessentities;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class IscsiBond implements Queryable, BusinessEntity<Guid>, Nameable {

    private static final long serialVersionUID = 6318808440502965971L;

    private Guid id;
    private Guid storagePoolId;

    @NotNull(message = "VALIDATION_ISCSI_BOND_NAME_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = 50, message = "VALIDATION_ISCSI_BOND_NAME_MAX",
            groups = { CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "VALIDATION_ISCSI_BOND_NAME_INVALID_CHARACTER",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE, message = "VALIDATION_ISCSI_BOND_DESCRIPTION_MAX",
            groups = { CreateEntity.class, UpdateEntity.class })
    @ValidDescription(message = "VALIDATION_ISCSI_BOND_DESCRIPTION_INVALID",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String description;
    private List<Guid> networkIds;
    private List<String> storageConnectionIds;

    public IscsiBond() {
        networkIds = new LinkedList<>();
        storageConnectionIds = new LinkedList<>();
    }

    public IscsiBond(Guid id, Guid storagePoolId, String name, String description) {
        this();
        this.id = id;
        this.storagePoolId = storagePoolId;
        this.name = name;
        this.description = description;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Guid> getNetworkIds() {
        return networkIds;
    }

    public void setNetworkIds(List<Guid> networkIds) {
        this.networkIds = networkIds;
    }

    public List<String> getStorageConnectionIds() {
        return storageConnectionIds;
    }

    public void setStorageConnectionIds(List<String> connectionIds) {
        this.storageConnectionIds = connectionIds;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                description,
                storagePoolId,
                networkIds,
                storageConnectionIds
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof IscsiBond)) {
            return false;
        }

        IscsiBond other = (IscsiBond) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(storagePoolId, other.storagePoolId)
                && Objects.equals(networkIds, other.networkIds)
                && Objects.equals(storageConnectionIds, other.storageConnectionIds);
    }
}

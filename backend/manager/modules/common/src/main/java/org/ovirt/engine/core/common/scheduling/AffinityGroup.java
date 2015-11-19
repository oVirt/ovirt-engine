package org.ovirt.engine.core.common.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.compat.Guid;

/**
 * Affinity Group Entity<br>
 * Affinity Group will hold a set of VMs (later on could be other entities) and properties.<br>
 * Each VM can be associated with several groups.<br>
 * The VM will be scheduled according to its affinity groups (properties and members) rules
 */
public class AffinityGroup implements BusinessEntity<Guid>, IVdcQueryable, Nameable {
    private static final long serialVersionUID = 6644745275483134922L;

    private Guid id;
    /**
     * affinity group name
     */
    @NotNull
    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE, message = "AFFINITY_GROUP_NAME_TOO_LONG")
    @ValidI18NName(message = "AFFINITY_GROUP_NAME_INVALID")
    private String name;
    /**
     * affinity group description
     */
    @Size(min = 0, max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE, message = "AFFINITY_GROUP_DESCRIPTION_INVALID")
    private String description;
    /**
     * affinity group associated cluster id
     */
    @NotNull(message = "ACTION_TYPE_FAILED_AFFINITY_GROUP_INVALID_CLUSTER_ID")
    private Guid clusterId;
    /**
     * affinity group polarity: positive(true)/negative(false- anti-affinity)
     */
    private boolean positive;
    /**
     * affinity group enforcement mode: hard(true)/soft(false)<br>
     * true: hard- filtering host that doesn't comply with affinity rule <br>
     * false: soft- best effort to comply with affinity rule
     */
    private boolean enforcing;
    /**
     * list of entity uuids that are included in affinity group.<br>
     * currently supported by vms
     */
    private List<Guid> entityIds;
    /**
     * list of entity names that are included in affinity group<br>
     * each item index matches to entityIds index,<br>
     * i.e. entityNames.get(5) is the name of the entity with entityIds.get(5) id.
     */
    private List<String> entityNames;

    public AffinityGroup() {
        this.positive = true;
        this.enforcing = true;
        this.entityIds = new ArrayList<>();
        this.entityNames = new ArrayList<>();
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public boolean isEnforcing() {
        return enforcing;
    }

    public void setEnforcing(boolean enforcing) {
        this.enforcing = enforcing;
    }

    public List<Guid> getEntityIds() {
        return entityIds;
    }

    public void setEntityIds(List<Guid> entityIds) {
        this.entityIds = entityIds;
        if (entityIds == null) {
            this.entityIds = new ArrayList<>();
        }
    }

    public List<String> getEntityNames() {
        return entityNames;
    }

    public void setEntityNames(List<String> entityNames) {
        this.entityNames = entityNames;
        if (entityNames == null) {
            this.entityNames = new ArrayList<>();
        }
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clusterId,
                description,
                enforcing,
                id,
                name,
                positive,
                entityIds,
                entityNames
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffinityGroup)) {
            return false;
        }
        AffinityGroup other = (AffinityGroup) obj;
        return Objects.equals(clusterId, other.clusterId)
                && Objects.equals(description, other.description)
                && enforcing == other.enforcing
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && positive == other.positive
                && Objects.equals(entityIds, other.entityIds)
                && Objects.equals(entityNames, other.entityNames);
    }
}

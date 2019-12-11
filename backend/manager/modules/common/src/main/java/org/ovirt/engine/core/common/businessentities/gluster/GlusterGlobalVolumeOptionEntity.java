package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.compat.Guid;

/**
 * A global volume option is a tunable parameter on all Gluster Volumes in a cluster. GlusterFS exposes a comprehensive
 * set of global volume options that can be set on all volumes in a cluster to fine-tune the behavior of the volume. e.g
 * : 'cluster.enable_shared_storage' is a global option.
 *
 * @see GlusterVolumeEntity
 */
public class GlusterGlobalVolumeOptionEntity implements Queryable, BusinessEntity<Guid>, Comparable<GlusterGlobalVolumeOptionEntity> {
    private static final long serialVersionUID = 5770623263518245638L;

    private Guid id;

    private Guid clusterId;

    private String key;

    private String value;

    public GlusterGlobalVolumeOptionEntity() {
    }

    public GlusterGlobalVolumeOptionEntity(Guid clusterId, String key, String value) {
        setClusterId(clusterId);
        setKey(key);
        setValue(value);
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                clusterId,
                key,
                value);
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterGlobalVolumeOptionEntity)) {
            return false;
        }

        GlusterGlobalVolumeOptionEntity other = (GlusterGlobalVolumeOptionEntity) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(clusterId, other.clusterId)
                && Objects.equals(key, other.key)
                && Objects.equals(value, other.value);
    }

    /**
     * Generates the id if not present. Volume option doesn't have an id in GlusterFS, and hence is generated on the
     * backend side.
     * @return id of the option
     */
    @Override
    public Guid getId() {
        if (id == null) {
            id = Guid.newGuid();
        }
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int compareTo(GlusterGlobalVolumeOptionEntity obj) {
        return BusinessEntityComparator.<GlusterGlobalVolumeOptionEntity, Guid> newInstance().compare(this, obj);
    }
}

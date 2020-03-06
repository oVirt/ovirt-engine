package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.gluster.SetVolumeOption;
import org.ovirt.engine.core.compat.Guid;

/**
 * A volume option is a tunable parameter on a Gluster Volume. GlusterFS exposes a comprehensive set of volume options
 * that can be set on each volume to fine-tune the behavior of the volume. e.g. The value of the option "auth.allow"
 * configures the list of IP addresses of client machines that should be allowed to access the volume.
 *
 * @see GlusterVolumeEntity
 */
public class GlusterVolumeOptionEntity implements Queryable, BusinessEntity<Guid>, Comparable<GlusterVolumeOptionEntity>{
    private static final long serialVersionUID = 5770623263518245638L;

    @NotNull(message = "VALIDATION_GLUSTER_OPTION_ID_NOT_NULL", groups = RemoveEntity.class)
    private Guid id;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_ID_NOT_NULL", groups = SetVolumeOption.class)
    private Guid volumeId;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_OPTION_KEY_NOT_NULL", groups = SetVolumeOption.class)
    private String key;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_OPTION_VALUE_NOT_NULL", groups = SetVolumeOption.class)
    private String value;

    public GlusterVolumeOptionEntity() {
    }

    public GlusterVolumeOptionEntity(Guid volumeId, String key, String value) {
        setVolumeId(volumeId);
        setKey(key);
        setValue(value);
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
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
                volumeId,
                key,
                value
        );
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
        if (!(obj instanceof GlusterVolumeOptionEntity)) {
            return false;
        }

        GlusterVolumeOptionEntity other = (GlusterVolumeOptionEntity) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(volumeId, other.volumeId)
                && Objects.equals(key, other.key)
                && Objects.equals(value, other.value);
    }

    /**
     * Generates the id if not present. Volume option doesn't have an id in
     * GlusterFS, and hence is generated on the backend side.
     * @return id of the option
     */
    @Override
    public Guid getId() {
        if(id == null) {
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
    public int compareTo(GlusterVolumeOptionEntity obj) {
        return BusinessEntityComparator.<GlusterVolumeOptionEntity, Guid>newInstance().compare(this, obj);
    }
}

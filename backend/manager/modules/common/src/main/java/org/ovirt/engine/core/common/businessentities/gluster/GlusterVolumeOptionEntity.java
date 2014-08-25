package org.ovirt.engine.core.common.businessentities.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
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
public class GlusterVolumeOptionEntity extends IVdcQueryable implements BusinessEntity<Guid>, Comparable<GlusterVolumeOptionEntity>{
    private static final long serialVersionUID = 5770623263518245638L;

    @NotNull(message = "VALIDATION.GLUSTER.OPTION.ID.NOT_NULL", groups = { RemoveEntity.class })
    private Guid id;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.ID.NOT_NULL", groups = { SetVolumeOption.class })
    private Guid volumeId;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.OPTION.KEY.NOT_NULL", groups = { SetVolumeOption.class })
    private String key;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.OPTION.VALUE.NOT_NULL", groups = { SetVolumeOption.class })
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
        final int prime = 31;
        int result = 1;
        result = prime * result + getId().hashCode();
        result = prime * result + ((volumeId == null) ? 0 : volumeId.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterVolumeOptionEntity)) {
            return false;
        }

        GlusterVolumeOptionEntity option = (GlusterVolumeOptionEntity) obj;
        return (getId().equals(option.getId())
                && (volumeId != null && volumeId.equals(option.getVolumeId()))
                && key.equals(option.getKey())
                && value.equals(option.getValue()));
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

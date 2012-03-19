package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

/**
 * A volume option is a tunable parameter on a Gluster Volume. GlusterFS exposes a comprehensive set of volume options
 * that can be set on each volume to fine-tune the behavior of the volume. e.g. The value of the option "auth.allow"
 * configures the list of IP addresses of client machines that should be allowed to access the volume.
 *
 * @see GlusterVolumeEntity
 */
public class GlusterVolumeOption implements Serializable {
    private static final long serialVersionUID = 5770623263518245638L;

    private Guid volumeId;
    private String key;
    private String value;

    public GlusterVolumeOption() {
    }

    public GlusterVolumeOption(Guid volumeId, String key, String value) {
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
        if (!(obj instanceof GlusterVolumeOption)) {
            return false;
        }

        GlusterVolumeOption option = (GlusterVolumeOption) obj;
        return (option.getVolumeId().equals(volumeId) && option.getKey().equals(key) && option.getValue().equals(value));
    }
}

package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ObjectUtils;

/**
 * Class representing information of a Gluster Volume Option
 *
 * @see GlusterVolumeOptionEntity
 */
public class GlusterVolumeOptionInfo implements Serializable, Comparable<GlusterVolumeOptionInfo> {
    private static final long serialVersionUID = -5145858224564431004L;

    private String key;
    private String defaultValue;
    private String description;

    public GlusterVolumeOptionInfo() {
    }

    public GlusterVolumeOptionInfo(String key, String defaultValue, String description) {
        setKey(key);
        setDefaultValue(defaultValue);
        setDescription(description);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterVolumeOptionInfo)) {
            return false;
        }

        GlusterVolumeOptionInfo option = (GlusterVolumeOptionInfo) obj;
        return ObjectUtils.objectsEqual(option.getKey(), key)
                && ObjectUtils.objectsEqual(option.getDefaultValue(), defaultValue);
    }

    @Override
    public int compareTo(GlusterVolumeOptionInfo option) {
        if (this.getKey() != null && option.getKey() != null) {
            return this.getKey().compareTo(option.getKey());
        }
        return 0;
    }
}

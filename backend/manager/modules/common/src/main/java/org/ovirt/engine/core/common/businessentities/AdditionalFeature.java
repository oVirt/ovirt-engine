package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class AdditionalFeature implements Serializable {

    private static final long serialVersionUID = -8387930346405670858L;
    private Guid id;
    private String name;
    private Version version;
    private String description;
    private ApplicationMode category;

    public AdditionalFeature() {
    }

    public AdditionalFeature(Guid id,
            String name,
            Version version,
            String description,
            ApplicationMode category) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.category = category;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApplicationMode getCategory() {
        return category;
    }

    public void setCategory(ApplicationMode category) {
        this.category = category;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof AdditionalFeature)) {
            AdditionalFeature clusterFeature = (AdditionalFeature) obj;
            if (ObjectUtils.objectsEqual(getId(), clusterFeature.getId())
                    && ObjectUtils.objectsEqual(getName(), clusterFeature.getName())
                    && ObjectUtils.objectsEqual(getVersion(), clusterFeature.getVersion())
                    && ObjectUtils.objectsEqual(getDescription(), clusterFeature.getDescription())
                    && ObjectUtils.objectsEqual(getCategory(), clusterFeature.getCategory())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AdditionalFeature {");
        sb.append("id=");
        sb.append(getId());
        sb.append(", name=");
        sb.append(getName());
        sb.append(", version=");
        sb.append(getVersion());
        sb.append(", description=");
        sb.append(getDescription());
        sb.append(", category=");
        sb.append(getCategory());
        sb.append("}");
        return sb.toString();
    }
}

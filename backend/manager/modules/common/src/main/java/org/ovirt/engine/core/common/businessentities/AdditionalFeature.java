package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
        return Objects.hash(
                id,
                name,
                version,
                description,
                category
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AdditionalFeature)) {
            return false;
        }
        AdditionalFeature other = (AdditionalFeature) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(version, other.version)
                && Objects.equals(description, other.description)
                && Objects.equals(category, other.category);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("name", getName())
                .append("version", getVersion())
                .append("description", getDescription())
                .append("category", getCategory())
                .build();
    }
}

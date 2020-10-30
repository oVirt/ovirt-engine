package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class UserProfileProperty implements Serializable {

    private static final long serialVersionUID = 7485699044913857040L;

    public enum PropertyType {
        SSH_PUBLIC_KEY,
        JSON,
        UNKNOWN
    }

    private Guid userId = Guid.Empty;

    private Guid propertyId = Guid.Empty;

    private String name = "";

    private String content;

    private PropertyType type = PropertyType.UNKNOWN;

    public UserProfileProperty() {
    }

    public UserProfileProperty(UserProfileProperty template) {
        this(template.getContent(),
                template.getType(),
                template.getName(),
                template.getUserId(),
                template.getPropertyId());
    }

    private UserProfileProperty(String content, PropertyType type, String name, Guid userId, Guid propertyId) {
        this.content = content;
        this.name = Objects.requireNonNull(name, "Property name cannot be null");
        this.type = Objects.requireNonNull(type, "Property type cannot be null");
        this.userId = Objects.requireNonNull(userId, "Property user ID cannot be null");
        this.propertyId = Objects.requireNonNull(propertyId, "Property ID cannot be null");
    }

    public Guid getUserId() {
        return userId;
    }

    public Guid getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public PropertyType getType() {
        return type;
    }

    public boolean isSshPublicKey() {
        return PropertyType.SSH_PUBLIC_KEY.equals(getType());
    }

    public boolean isJsonProperty() {
        return PropertyType.JSON.equals(getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserProfileProperty that = (UserProfileProperty) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(propertyId, that.propertyId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(content, that.content) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, propertyId, name, content, type);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("userId", userId)
                .append("propertyId", propertyId)
                .append("name", name)
                .append("content", content)
                .append("type", type)
                .toString();
    }

    public static class Builder {
        UserProfileProperty prop = new UserProfileProperty();

        public Builder withContent(String content) {
            prop.content = content;
            return this;
        }

        public Builder withNewIdIfEmpty() {
            if (Guid.Empty.equals(prop.getPropertyId()) || prop.getPropertyId() == null) {
                prop.propertyId = Guid.newGuid();
            }
            return this;
        }

        public Builder withPropertyId(Guid id) {
            prop.propertyId = id;
            return this;
        }

        public UserProfileProperty build() {
            return new UserProfileProperty(prop);
        }

        public Builder from(UserProfileProperty template) {
            prop = new UserProfileProperty(template);
            return this;
        }

        public Builder withDefaultSshProp() {
            prop.name = PropertyType.SSH_PUBLIC_KEY.name();
            prop.type = PropertyType.SSH_PUBLIC_KEY;
            return this;
        }

        public Builder withTypeSsh() {
            prop.type = PropertyType.SSH_PUBLIC_KEY;
            return this;
        }

        public Builder withTypeJson() {
            prop.type = PropertyType.JSON;
            return this;
        }

        public Builder withUserId(Guid userId) {
            prop.userId = userId;
            return this;
        }

        public Builder withNewId() {
            return withPropertyId(Guid.newGuid());
        }

        public Builder withName(String name) {
            prop.name = name;
            return this;
        }

        public Builder withType(PropertyType type) {
            prop.type = type;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}

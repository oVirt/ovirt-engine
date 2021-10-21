package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("FieldMayBeFinal")
public class UserProfile implements Queryable {
    private static final long serialVersionUID = -1712230969022805276L;

    private Guid userId;

    private Map<String, UserProfileProperty> properties;

    // cached string representation for logging purpose
    private String propertyLog;

    public UserProfile() {
        this(Guid.Empty, Collections.emptyList());
    }

    public UserProfile(Guid userId, List<UserProfileProperty> properties) {
        this.userId = Objects.requireNonNull(userId, "User Id cannot be null");

        Objects.requireNonNull(properties, "List of user profile properties cannot be null");
        for (UserProfileProperty property : properties) {
            Objects.requireNonNull(property, "Property cannot be null");
            Objects.requireNonNull(property.getName(), "Property name cannot be null");
            Objects.requireNonNull(property.getUserId(), "Property user ID cannot be null");
            if (!property.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Profile and property have different owner.");
            }
        }

        this.properties = properties.stream()
                .collect(Collectors.toMap(
                        UserProfileProperty::getName,
                        Function.identity()));
        this.propertyLog = new ArrayList<>(this.properties.values()).toString();
    }

    @Override
    public Object getQueryableId() {
        return getUserId();
    }

    public Guid getUserId() {
        return userId;
    }

    /**
     * @return properties of type {@linkplain UserProfileProperty.PropertyType#SSH_PUBLIC_KEY}
     */
    public List<UserProfileProperty> getSshProperties() {
        return properties.values()
                .stream()
                .filter(UserProfileProperty::isSshPublicKey)
                .collect(Collectors.toList());
    }

    public List<UserProfileProperty> getProperties() {
        return new ArrayList<>(properties.values());
    }

    public Optional<UserProfileProperty> getUserProfileProperty(String name, UserProfileProperty.PropertyType type) {
        if (name == null || type == null) {
            return Optional.empty();
        }
        return getProperties().stream()
                .filter(prop -> type.equals(prop.getType()))
                .filter(prop -> name.equals(prop.getName()))
                .findFirst();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        ArrayList<UserProfileProperty> properties = new ArrayList<>();
        private Guid userId = Guid.Empty;

        public Builder withProp(UserProfileProperty property) {
            properties.add(property);
            return this;
        }

        public UserProfile build() {
            return new UserProfile(
                    userId,
                    properties.stream()
                            .map(prop -> UserProfileProperty.builder()
                                    .from(prop)
                                    .withUserId(userId)
                                    .build())
                            .collect(Collectors.toList()));
        }

        public Builder withUserId(Guid userId) {
            this.userId = userId;
            return this;
        }

        public Builder withProperties(List<UserProfileProperty> properties) {
            this.properties.clear();
            this.properties.addAll(properties);
            return this;
        }

        public Builder from(UserProfile template) {
            return withProperties(template.getProperties())
                    .withUserId(template.getUserId());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserProfile profile = (UserProfile) o;
        return Objects.equals(userId, profile.userId) &&
                Objects.equals(properties, profile.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, properties);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("userId", userId)
                .append("properties", propertyLog)
                .toString();
    }

}

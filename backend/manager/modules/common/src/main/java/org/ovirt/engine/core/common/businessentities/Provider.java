package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * A provider can provide external services to be consumed by the system.<br>
 * The provider will be responsible for managing the provided services, and the interaction with it would be done
 * through an API which will be accessible via the URL.
 *
 * @param P
 *            The type of additional properties this provider holds.
 */
public class Provider<P extends AdditionalProperties> implements Queryable, BusinessEntity<Guid>, Nameable {

    private static final long serialVersionUID = 8279455368568715758L;

    @NotNull(message = "VALIDATION_ID_NULL", groups = { UpdateEntity.class, RemoveEntity.class })
    private Guid id;

    @NotNull(message = "VALIDATION_NAME_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "VALIDATION_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    @NotNull
    private String description;

    private String url;

    private boolean isUnmanaged;

    @NotNull(message = "VALIDATION_PROVIDER_TYPE_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private ProviderType type;

    private boolean requiringAuthentication;

    private String username;

    private String password;

    private Map<String, String> customProperties;

    @Valid
    private P additionalProperties;

    private String authUrl;

    public Provider() {
        description = "";
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
        this.description = description == null ? "" : description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public ProviderType getType() {
        return type;
    }

    public void setType(ProviderType type) {
        this.type = type;
    }

    public boolean isRequiringAuthentication() {
        return requiringAuthentication;
    }

    public void setRequiringAuthentication(boolean requiringAuthentication) {
        this.requiringAuthentication = requiringAuthentication;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public P getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(P additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public boolean getIsUnmanaged() {
        return isUnmanaged;
    }

    public void setIsUnmanaged(boolean isUnmanaged) {
        this.isUnmanaged = isUnmanaged;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                requiringAuthentication,
                description,
                id,
                name,
                password,
                type,
                url,
                username,
                customProperties,
                additionalProperties,
                authUrl,
                isUnmanaged
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Provider)) {
            return false;
        }
        Provider<?> other = (Provider<?>) obj;
        return requiringAuthentication == other.requiringAuthentication
                && Objects.equals(description, other.description)
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(password, other.password)
                && Objects.equals(type, other.type)
                && Objects.equals(url, other.url)
                && Objects.equals(username, other.username)
                && Objects.equals(customProperties, other.customProperties)
                && Objects.equals(additionalProperties, other.additionalProperties)
                && Objects.equals(authUrl, other.authUrl)
                && Objects.equals(isUnmanaged, other.isUnmanaged);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("name", getName())
                .append("description", getDescription())
                .append("url", getUrl())
                .append("type", getType())
                .append("requiringAuthentication", isRequiringAuthentication())
                .append("username", getUsername())
                .appendFiltered("password", getPassword())
                .append("customProperties", getCustomProperties())
                .append("additionalProperties", getAdditionalProperties())
                .append("authUrl", getAuthUrl())
                .append("isUnmanaged", getIsUnmanaged())
                .build();
    }

    /**
     * Tag interface for classes that add additional properties to providers.
     */
    public static interface AdditionalProperties extends Serializable {
    }
}

package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * A provider can provide external services to be consumed by the system.<br>
 * The provider will be responsible for managing the provided services, and the interaction with it would be done
 * through an API which will be accessible via the URL.
 */
public class Provider extends IVdcQueryable implements BusinessEntity<Guid>, Nameable {

    private static final long serialVersionUID = 8279455368568715758L;

    @NotNull(message = "VALIDATION_ID_NULL", groups = { UpdateEntity.class, RemoveEntity.class })
    private Guid id;

    @NotNull(message = "VALIDATION_NAME_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "VALIDATION_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    private String description;

    @NotNull(message = "VALIDATION_URL_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private String url;

    @NotNull(message = "VALIDATION_PROVIDER_TYPE_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private ProviderType type;

    private boolean requiringAuthentication;

    private String username;

    private String password;

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
        this.description = description;
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

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isRequiringAuthentication() ? 1231 : 1237);
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getUrl() == null) ? 0 : getUrl().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Provider)) {
            return false;
        }
        Provider other = (Provider) obj;
        if (isRequiringAuthentication() != other.isRequiringAuthentication()) {
            return false;
        }
        if (getDescription() == null) {
            if (other.getDescription() != null) {
                return false;
            }
        } else if (!getDescription().equals(other.getDescription())) {
            return false;
        }
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getPassword() == null) {
            if (other.getPassword() != null) {
                return false;
            }
        } else if (!getPassword().equals(other.getPassword())) {
            return false;
        }
        if (getType() != other.getType()) {
            return false;
        }
        if (getUrl() == null) {
            if (other.getUrl() != null) {
                return false;
            }
        } else if (!getUrl().equals(other.getUrl())) {
            return false;
        }
        if (getUsername() == null) {
            if (other.getUsername() != null) {
                return false;
            }
        } else if (!getUsername().equals(other.getUsername())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Provider [id=")
                .append(getId())
                .append(", name=")
                .append(getName())
                .append(", description=")
                .append(getDescription())
                .append(", url=")
                .append(getUrl())
                .append(", type=")
                .append(getType())
                .append(", requiringAuthentication=")
                .append(isRequiringAuthentication())
                .append(", username=")
                .append(getUsername())
                .append(", password=")
                .append(getPassword() == null ? null : "******")
                .append("]");
        return builder.toString();
    }
}

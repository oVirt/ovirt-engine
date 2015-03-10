package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class MacPool extends IVdcQueryable implements BusinessEntity<Guid>, Nameable {

    private static final long serialVersionUID = -7952435653821354188L;

    @NotNull(groups = { UpdateEntity.class })
    private Guid id;

    @NotEmpty(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY")
    private String name;

    private boolean allowDuplicateMacAddresses;

    private String description;

    @Valid
    @NotEmpty(message = "ACTION_TYPE_FAILED_MAC_POOL_MUST_HAVE_RANGE")
    @NotNull(message = "ACTION_TYPE_FAILED_MAC_POOL_MUST_HAVE_RANGE")
    private List<MacRange> ranges = new ArrayList<MacRange>();

    private boolean defaultPool;

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAllowDuplicateMacAddresses() {
        return allowDuplicateMacAddresses;
    }

    public void setAllowDuplicateMacAddresses(boolean allowDuplicateMacAddresses) {
        this.allowDuplicateMacAddresses = allowDuplicateMacAddresses;
    }

    public List<MacRange> getRanges() {
        return ranges;
    }

    public void setRanges(List<MacRange> ranges) {
        this.ranges = ranges;
    }

    public void setDefaultPool(boolean defaultPool) {
        this.defaultPool = defaultPool;
    }

    /**
     * @return true if this pool is currently flagged as default.
     */
    public boolean isDefaultPool() {
        return defaultPool;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MacPool)) {
            return false;
        }
        MacPool other = (MacPool) obj;
        if (allowDuplicateMacAddresses != other.allowDuplicateMacAddresses) {
            return false;
        }
        if (defaultPool != other.defaultPool) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (ranges == null) {
            if (other.ranges != null) {
                return false;
            }
        } else if (!ranges.equals(other.ranges)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (allowDuplicateMacAddresses ? 1231 : 1237);
        result = prime * result + (defaultPool ? 1231 : 1237);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((ranges == null) ? 0 : ranges.hashCode());
        return result;
    }
}

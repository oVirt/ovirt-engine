package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class MacPool implements Queryable, BusinessEntity<Guid>, Nameable {

    private static final long serialVersionUID = -7952435653821354188L;

    @NotNull(groups = UpdateEntity.class)
    private Guid id;

    @NotEmpty(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY")
    private String name;

    private boolean allowDuplicateMacAddresses;

    private String description;

    @Valid
    @NotEmpty(message = "ACTION_TYPE_FAILED_MAC_POOL_MUST_HAVE_RANGE")
    @NotNull(message = "ACTION_TYPE_FAILED_MAC_POOL_MUST_HAVE_RANGE")
    private List<MacRange> ranges = new ArrayList<>();

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
        if (!(obj instanceof MacPool)) {
            return false;
        }
        MacPool other = (MacPool) obj;
        return allowDuplicateMacAddresses == other.allowDuplicateMacAddresses
                && defaultPool == other.defaultPool
                && Objects.equals(description, other.description)
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(ranges, other.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                allowDuplicateMacAddresses,
                defaultPool,
                description,
                id,
                name,
                ranges
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", id)
                .append("name", name)
                .append("description", description)
                .append("ranges", ranges)
                .append("allowDuplicateMacAddresses", allowDuplicateMacAddresses)
                .append("defaultPool", defaultPool)
                .build();
    }
}

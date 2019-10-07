package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

/**
 * This entity represents a label that can be applies to other business entitites
 * (currently VMs and Hosts). The label is intended to be used for specifying
 * requirements and membership (not only) for VM scheduling purposes.
 */
public class Label implements Serializable, BusinessEntity<Guid>, Nameable, Queryable {
    private static final long serialVersionUID = 3694143455240550873L;

    @NotNull
    private Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.TAG_NAME_SIZE, message = "AFFINITY_LABEL_NAME_SIZE_INVALID")
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS_I18N, message = "AFFINITY_LABEL_BAD_NAME")
    private String name;

    /**
     * Set of Guids of VMs that are associated with this label.
     */
    private Set<Guid> vms;

    /**
     * Set of Guids of Hosts that are associated with this label.
     */
    private Set<Guid> hosts;

    /**
     * A read-only flag prevents user initiated update and delete actions
     */
    private boolean readOnly;

    /**
     * Indicates if the label should behave as a VM to host affinity group,
     * which is the legacy behavior for labels.
     *
     * This field is only used for cluster compatibility 4.3 or less.
     */
    private boolean implicitAffinityGroup;

    @Override
    public Object getQueryableId() {
        return id;
    }

    @Override
    public Guid getId() {
        return id;
    }

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

    public Set<Guid> getVms() {
        return vms;
    }

    public Set<Guid> getHosts() {
        return hosts;
    }

    public boolean addVm(VM entity) {
        return vms.add(entity.getId());
    }

    public boolean removeVm(VM entity) {
        return vms.remove(entity.getId());
    }

    public boolean addHost(VDS entity) {
        return hosts.add(entity.getId());
    }

    public boolean removeHost(VDS entity) {
        return hosts.remove(entity.getId());
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isImplicitAffinityGroup() {
        return implicitAffinityGroup;
    }

    public void setImplicitAffinityGroup(boolean implicitAffinityGroup) {
        this.implicitAffinityGroup = implicitAffinityGroup;
    }

    /**
     * This constructor is meant to be used by introspection frameworks like
     * Jackson, GWT or for de-serialization.
     */
    private Label() {
       this.hosts = new HashSet<>();
       this.vms = new HashSet<>();
    }

    /**
     * This constructor is used by the LabelBuilder.
     *
     * @param id The Guid associated with this Label
     * @param name The printable name of the label without spaces
     * @param vms A set of Guids pointing to all VMs that have this label assigned
     * @param hosts A set of Guids pointing to all Hosts that have this label assigned
     * @param readOnly A read-only flag prevents user initiated update and delete actions
     */
    protected Label(@NotNull Guid id, @NotNull String name, @NotNull Set<Guid> vms,
            @NotNull Set<Guid> hosts, boolean readOnly, boolean implicitAffinityGroup) {
        this.id = id;
        this.name = name;
        this.vms = vms;
        this.hosts = hosts;
        this.readOnly = readOnly;
        this.implicitAffinityGroup = implicitAffinityGroup;
    }

    /* The only business key for Label is the ID. Everything else can be changed
       without any identity change.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Label label = (Label) o;
        return Objects.equals(getId(), label.getId());
    }

    /* The only business key for Label is the ID. Everything else can be changed
       without any identity change.
    */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /* Setters are present for Java Bean, serialization and GWT compatibility.
       Use the builder for preparing a new object and the domain methods (add/remove)
       to do modifications please.
     */

    public void setVms(Set<Guid> vms) {
        this.vms = vms;
    }

    public void setHosts(Set<Guid> hosts) {
        this.hosts = hosts;
    }
}

package org.ovirt.engine.core.common.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.compat.Guid;

/**
 * Affinity Group Entity<br>
 * Affinity Group will hold a set of VMs (later on could be other entities) and properties.<br>
 * Each VM can be associated with several groups.<br>
 * The VM will be scheduled according to its affinity groups (properties and members) rules
 */
public class AffinityGroup implements BusinessEntity<Guid>, Queryable, Nameable {
    private static final long serialVersionUID = 995196097595814894L;

    public static final double PRIORITY_PRECISION = 1E7;

    private Guid id;
    /**
     * affinity group name
     */
    @NotNull
    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE, message = "AFFINITY_GROUP_NAME_TOO_LONG")
    @ValidI18NName(message = "AFFINITY_GROUP_NAME_INVALID")
    private String name;
    /**
     * affinity group description
     */
    @Size(min = 0, max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE, message = "AFFINITY_GROUP_DESCRIPTION_INVALID")
    private String description;
    /**
     * affinity group associated cluster id
     */
    @NotNull(message = "ACTION_TYPE_FAILED_AFFINITY_GROUP_INVALID_CLUSTER_ID")
    private Guid clusterId;
    /**
     * affinity group vms polarity: positive/negative/disable(vm to vm affinity disabled)
     */
    private EntityAffinityRule vmAffinityRule = EntityAffinityRule.DISABLED;
    /**
     * affinity group vms enforcement mode: hard(true)/soft(false)<br>
     * true: hard- filtering host that doesn't comply with affinity rule <br>
     * false: soft- best effort to comply with affinity rule
     */
    private boolean vmEnforcing = false;
    /**
     * list of vms uuids that are included in affinity group.<br>
     */
    private List<Guid> vmIds = new ArrayList<>();
    /**
     * list of VM entity names that are included in affinity group<br>
     * each item index matches to vmIds index,<br>
     * i.e. vmEntityNames.get(5) is the name of the entity with vmIds.get(5) id.
     */
    //TODO remove this list and change it to a set of objects that contain ids and names
    private List<String> vmEntityNames = new ArrayList<>();
    /**
     * list of VDS entity names that are included in affinity group<br>
     * each item index matches to vdsIds index,<br>
     * i.e. vdsEntityNames.get(5) is the name of the entity with vdsIds.get(5) id.
     */
    //TODO remove this list and change it to a set of objects that contain ids and names
    private List<String> vdsEntityNames = new ArrayList<>();
    /**
     * affinity group vds polarity: positive(true)/negative(false- anti-affinity)
     */
    private EntityAffinityRule vdsAffinityRule = EntityAffinityRule.DISABLED;
    /**
     * affinity group vds enforcement mode: hard(true)/soft(false)<br>
     * true: hard- filtering host that doesn't comply with affinity rule <br>
     * false: soft- best effort to comply with affinity rule
     */
    private boolean vdsEnforcing = false;
    /**
     * list of vds uuids that are included in affinity group.<br>
     */
    private List<Guid> vdsIds = new ArrayList<>();

    /**
     * List of label uuids that contain VMs in this group
     */
    private List<Guid> vmLabels = new ArrayList<>();

    /**
     * List of host uuids that contain hosts in this group
     */
    private List<Guid> hostLabels = new ArrayList<>();

    /**
     * List of VM label names that are included in affinity group<br>
     * each item index matches to vmLabels index,<br>
     * i.e. vmLabelNames.get(5) is the name of the label with vmLabels.get(5) id.
     */
    private List<String> vmLabelNames = new ArrayList<>();

    /**
     * List of VM label names that are included in affinity group<br>
     * each item index matches to hostLabels index,<br>
     * i.e. hostLabelNames.get(5) is the name of the label with hostLabels.get(5) id.
     */
    private List<String> hostLabelNames = new ArrayList<>();

    /**
     * Priority of the affinity group.
     *
     * Using 'long' type instead of 'double', because it is important to check
     * if two affinity groups have equal priority. Equality check for doubles can be unstable.
     *
     * The 'double' values are converted to 'long' using fixed precision.
     */
    private long priority = 10000000;

    // Transient filed, not stored in the DB.
    // It is computed by queries returning affinity groups
    private Boolean broken;

    public AffinityGroup() {}

    public AffinityGroup(AffinityGroup other) {
        id = other.id;
        name = other.name;
        description = other.description;
        clusterId = other.clusterId;
        priority = other.priority;
        broken = other.broken;

        vmAffinityRule = other.vmAffinityRule;
        vmEnforcing = other.vmEnforcing;
        vmIds = new ArrayList<>(other.vmIds);
        vmEntityNames = new ArrayList<>(other.vmEntityNames);
        vmLabels = new ArrayList<>(other.vmLabels);

        vdsAffinityRule = other.vdsAffinityRule;
        vdsEnforcing = other.vdsEnforcing;
        vdsIds = new ArrayList<>(other.vdsIds);
        vdsEntityNames = new ArrayList<>(other.vmEntityNames);
        hostLabels = new ArrayList<>(other.hostLabels);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public boolean isVmEnforcing() {
        return vmEnforcing;
    }

    public void setVmEnforcing(boolean vmEnforcing) {
        this.vmEnforcing = vmEnforcing;
    }

    public List<Guid> getVmIds() {
        return vmIds;
    }

    public void setVmIds(List<Guid> vmIds) {
        this.vmIds = vmIds;
        if (vmIds == null) {
            this.vmIds = new ArrayList<>();
        }
    }

    public void setVmAffinityRule(EntityAffinityRule AffinityRule) {
        vmAffinityRule = AffinityRule;
    }

    public EntityAffinityRule getVmAffinityRule() {
        return this.vmAffinityRule;
    }

    public Boolean getVmPolarityBooleanObject() {
        return vmAffinityRule == EntityAffinityRule.DISABLED ? null :
                vmAffinityRule == EntityAffinityRule.POSITIVE;
    }

    public Boolean getVdsPolarityBooleanObject() {
        return vdsAffinityRule == EntityAffinityRule.DISABLED ? null :
                vdsAffinityRule == EntityAffinityRule.POSITIVE;
    }

    public boolean isVmPositive() {
        return vmAffinityRule == EntityAffinityRule.POSITIVE;
    }

    public boolean isVmNegative() {
        return vmAffinityRule == EntityAffinityRule.NEGATIVE;
    }

    public boolean isVmAffinityEnabled() {
        return vmAffinityRule != EntityAffinityRule.DISABLED;
    }

    public boolean isVdsAffinityEnabled() {
        return vdsAffinityRule != EntityAffinityRule.DISABLED;
    }

    public boolean isVdsPositive() {
        return vdsAffinityRule == EntityAffinityRule.POSITIVE;
    }

    public boolean isVdsNegative() {
        return vdsAffinityRule == EntityAffinityRule.NEGATIVE;
    }

    public EntityAffinityRule getVdsAffinityRule() {
        return vdsAffinityRule;
    }

    public void setVdsAffinityRule(EntityAffinityRule vdsAffinityRule) {
        this.vdsAffinityRule = vdsAffinityRule;
    }

    public boolean isVdsEnforcing() {
        return vdsEnforcing;
    }

    public void setVdsEnforcing(boolean vdsEnforcing) {
        this.vdsEnforcing = vdsEnforcing;
    }

    public List<Guid> getVdsIds() {
        return vdsIds;
    }

    public void setVdsIds(List<Guid> vdsIds) {
        this.vdsIds = vdsIds;
        if (vdsIds == null) {
            this.vdsIds = new ArrayList<>();
        }
    }

    public List<Guid> getVmLabels() {
        return vmLabels;
    }

    public void setVmLabels(List<Guid> vmLabels) {
        this.vmLabels = vmLabels;
    }

    public List<Guid> getHostLabels() {
        return hostLabels;
    }

    public void setHostLabels(List<Guid> hostLabels) {
        this.hostLabels = hostLabels;
    }

    public List<String> getVmEntityNames() {
        return vmEntityNames;
    }

    public void setVmEntityNames(List<String> vmEntityNames) {
        this.vmEntityNames = vmEntityNames;
        if (vmEntityNames == null) {
            this.vmEntityNames = new ArrayList<>();
        }
    }

    public List<String> getVdsEntityNames() {
        return vdsEntityNames;
    }

    public void setVdsEntityNames(List<String> vdsEntityNames) {
        this.vdsEntityNames = vdsEntityNames;
        if (vdsEntityNames == null) {
            this.vdsEntityNames = new ArrayList<>();
        }
    }

    public List<String> getVmLabelNames() {
        return vmLabelNames;
    }

    public void setVmLabelNames(List<String> vmLabelNames) {
        this.vmLabelNames = vmLabelNames != null ? vmLabelNames : new ArrayList<>();
    }

    public List<String> getHostLabelNames() {
        return hostLabelNames;
    }

    public void setHostLabelNames(List<String> hostLabelNames) {
        this.hostLabelNames = hostLabelNames != null ? hostLabelNames : new ArrayList<>();
    }

    public long getPriority() {
        return priority;
    }

    public double getPriorityAsDouble() {
        return ((double) getPriority()) / PRIORITY_PRECISION;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void setPriorityFromDouble(double priority) {
        setPriority(Math.round(priority * PRIORITY_PRECISION));
    }

    public Boolean getBroken() {
        return broken;
    }

    public void setBroken(Boolean broken) {
        this.broken = broken;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        } else {
            return super.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffinityGroup)) {
            return false;
        }
        AffinityGroup other = (AffinityGroup) obj;
        // entity without id is always unique
        if (id == null || other.id == null) {
            return false;
        }
        return Objects.equals(id, other.id);
    }
}

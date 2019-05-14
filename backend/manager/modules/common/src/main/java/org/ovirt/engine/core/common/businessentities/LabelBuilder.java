package org.ovirt.engine.core.common.businessentities;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.compat.Guid;

/**
 * A builder class for the Label business entity.
 */
public class LabelBuilder {
    private Guid id;
    private String name;
    private Set<Guid> vms = new HashSet<>();
    private Set<Guid> hosts = new HashSet<>();
    private boolean readOnly = false;
    private boolean implicitAffinityGroup = false;

    public LabelBuilder() {
    }

    public LabelBuilder(Label label) {
        id = label.getId();
        name = label.getName();
        vmIds(label.getVms());
        hostIds(label.getHosts());
        readOnly = label.isReadOnly();
        implicitAffinityGroup = label.isImplicitAffinityGroup();
    }

    public Label build() {
        if (id == null) {
            id = Guid.newGuid();
        }

        return new Label(id, name, vms, hosts, readOnly, implicitAffinityGroup);
    }

    public LabelBuilder id(Guid id) {
        this.id = id;
        return this;
    }

    public LabelBuilder randomId() {
        this.id = Guid.newGuid();
        return this;
    }

    public LabelBuilder name(String name) {
        this.name = name;
        return this;
    }

    public LabelBuilder removeEntity(BusinessEntity<Guid> entity) {
        findTypeAndRemove(entity);
        return this;
    }

    public LabelBuilder vmIds(Set<Guid> vmIds) {
        this.vms.addAll(vmIds);
        return this;
    }

    public LabelBuilder hostIds(Set<Guid> hostIds) {
        this.hosts.addAll(hostIds);
        return this;
    }

    public LabelBuilder vm(Guid vmId) {
        this.vms.add(vmId);
        return this;
    }

    public LabelBuilder host(Guid hostId) {
        this.hosts.add(hostId);
        return this;
    }

    public LabelBuilder entities(Set<BusinessEntity<Guid>> entities) {
        for (BusinessEntity<Guid> entity: entities) {
            findTypeAndAdd(entity);
        }
        return this;
    }

    @SafeVarargs
    public final LabelBuilder entities(BusinessEntity<Guid>... entities) {
        for (BusinessEntity<Guid> entity: entities) {
            findTypeAndAdd(entity);
        }
        return this;
    }

    public LabelBuilder entity(BusinessEntity<Guid> entity) {
        findTypeAndAdd(entity);
        return this;
    }

    public LabelBuilder readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public LabelBuilder implicitAffinityGroup(boolean implicitAffinityGroup) {
        this.implicitAffinityGroup = implicitAffinityGroup;
        return this;
    }

    private void findTypeAndAdd(BusinessEntity<Guid> entity) {
        if (entity instanceof VmBase) {
            this.vms.add(entity.getId());
        } else if (entity instanceof VM) {
            this.vms.add(entity.getId());
        } else if (entity instanceof VDS) {
            this.hosts.add(entity.getId());
        } else if (entity instanceof VdsStatic) {
            this.hosts.add(entity.getId());
        } else {
            throw new IllegalArgumentException("Only VMs and Hosts are supported.");
        }
    }

    private void findTypeAndRemove(BusinessEntity<Guid> entity) {
        if (entity instanceof VmBase) {
            this.vms.remove(entity.getId());
        } else if (entity instanceof VM) {
            this.vms.remove(entity.getId());
        } else if (entity instanceof VDS) {
            this.hosts.remove(entity.getId());
        } else if (entity instanceof VdsStatic) {
            this.hosts.remove(entity.getId());
        } else {
            throw new IllegalArgumentException("Only VMs and Hosts are supported.");
        }
    }
}

/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

package org.ovirt.engine.core.bll;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Derives the emulated machine pin for i440fx VMs on the creation paths (add and
 * import) in clusters at compatibility level >= 4.8, where the cluster default is
 * Q35-only and an i440fx VM must carry an explicit machine type.
 * <p>
 * The pin value is returned to the caller, which applies it and handles its own
 * logging and audit events (the import flow audits the auto-pin). An empty
 * result means no pin can be derived — a mixed cluster whose hosts share no
 * common i440fx machine type — so the caller's validation rejects the VM (add)
 * or imports it as unrunnable (import). Updating an existing VM never re-derives
 * the pin here: on update the pin is user-managed state.
 * <p>
 * The handler is a plain stateful object tied to the lifecycle of the command
 * that creates it: the command constructs it (typically in its {@code init()})
 * with its DAOs and the VM configuration and target cluster, and the pin methods
 * derive from that state without re-passing it. The bound objects are held by
 * reference, so later mutations of the VM/cluster are visible to the pin methods.
 */
public class EmulatedMachinePinningHandler {

    private final VdsDao vdsDao;
    private final VmStatic vmStatic;
    private final Cluster cluster;

    /**
     * @param vdsDao DAO for the cluster hosts' supported machine types
     * @param vmStatic the VM configuration to derive the pin for
     * @param cluster the target cluster (its compatibility version decides whether
     *        the pin rules apply); may be null when the cluster is unresolved
     */
    public EmulatedMachinePinningHandler(VdsDao vdsDao, VmStatic vmStatic, Cluster cluster) {
        this.vdsDao = vdsDao;
        this.vmStatic = vmStatic;
        this.cluster = cluster;
    }

    /**
     * Returns the machine type to pin on a newly created i440fx VM that carries none:
     * the NEWEST machine type common to all hosts of the target cluster — a new VM has
     * no machine-type history, so the newest common type gives it the longest support
     * runway (RHEL drops old machine types every major release). Returns empty when the
     * VM is not an unpinned i440fx VM at a deprecated level, or when no common type
     * exists.
     */
    public Optional<String> getPinForNewVm() {
        if (!isUnpinnedI440fxVm(vmStatic)
                || cluster == null
                || !ClusterEmulatedMachines.isI440fxDeprecated(vmStatic, cluster)) {
            return Optional.empty();
        }
        return Optional.ofNullable(ClusterEmulatedMachines.pickNewVmI440fxMachine(
                vdsDao.getClusterI440fxCommonMachines(cluster.getId())));
    }

    /**
     * True when the VM is an unpinned i440fx VM — the cheap, cluster-independent part
     * of the pin decision. The level-dependent part is checked by the pin methods
     * against the bound cluster.
     */
    public static boolean isUnpinnedI440fxVm(VmStatic vmStatic) {
        return vmStatic != null
                && vmStatic.getBiosType() != null
                && vmStatic.getBiosType().getChipsetType() == ChipsetType.I440FX
                && StringUtils.isEmpty(vmStatic.getCustomEmulatedMachine());
    }

    /**
     * Returns true when the VM this handler was constructed with is due for the import
     * auto-pin: an unpinned i440fx VM in a cluster at compatibility level >= 4.8. When
     * this holds and {@link #pinForImportedVm} still returns empty, the cluster hosts
     * share no common i440fx machine type.
     */
    public boolean isImportPinRequired() {
        return isUnpinnedI440fxVm(vmStatic)
                && cluster != null
                && ClusterEmulatedMachines.isI440fxDeprecated(cluster.getCompatibilityVersion());
    }

    /**
     * Returns the machine type to pin on an imported i440fx VM that carries none: the
     * OLDEST machine type common to all hosts of the target cluster — the smallest
     * hardware change for a VM that ran an older machine type at its source. Callers
     * must check {@link #isImportPinRequired} first; empty here then means the
     * cluster hosts share no common i440fx machine type.
     */
    public Optional<String> pinForImportedVm() {
        if (!isImportPinRequired()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ClusterEmulatedMachines.pickImportI440fxMachine(
                vdsDao.getClusterI440fxCommonMachines(cluster.getId())));
    }
}

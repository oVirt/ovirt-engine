/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





/**
 * The raise-time i440fx migration of a cluster compatibility-version raise: when the
 * level is raised from below the i440fx-deprecation level (4.8) to it or above, every
 * unpinned i440fx VM/template of the cluster gets a pin, the cluster default is
 * rewritten to Q35-only and the whole trail is audited.
 * <p>
 * One instance covers exactly one raise: it is wired once in {@code UpdateClusterCommand.init()}
 * with the old and new cluster entities, the locked VMs/templates of the cluster and its
 * DAOs, and then stepped by the command:
 * <ol>
 *   <li>{@link #validate()} — validate-time check, fails the raise for entities whose
 *   machine type is dead and unfixable (nothing has been changed at that point)</li>
 *   <li>{@link #pinEntities()} — execution-time step, decides and sets each pin on the
 *   in-memory entity; the standard UpdateVm/UpdateVmTemplate flow persists it
 *   compensation-enabled, so a failed raise reverts the pins with everything else</li>
 *   <li>{@link #fireAuditEvents()} — fired by the command only after the raise
 *   completed successfully, so the audit log only ever describes raises that
 *   actually happened</li>
 * </ol>
 */
public class I440fxRaiseHandler {

    private final Cluster oldCluster;
    private final Cluster newCluster;
    private final List<VmStatic> vmsLockedForUpdate;
    private final List<VmTemplate> templatesLockedForUpdate;
    private final VdsDao vdsDao;
    private final VmDynamicDao vmDynamicDao;
    private final UpdateClusterCommand<?> command;

    private final List<I440fxRaiseRecord> raiseRecords = new ArrayList<>();
    private int entitiesPinnedOnRaise = 0;
    private List<VDS> allHostsForCluster;

    private static final Logger log = LoggerFactory.getLogger(I440fxRaiseHandler.class);

    public I440fxRaiseHandler(
            UpdateClusterCommand<?> command,
            Cluster oldCluster,
            Cluster newCluster,
            List<VmStatic> vmsLockedForUpdate,
            List<VmTemplate> templatesLockedForUpdate,
            VdsDao vdsDao,
            VmDynamicDao vmDynamicDao) {
        this.command = command;
        this.oldCluster = oldCluster; // null when the cluster does not exist (failed lookup): every step is a no-op then
        this.newCluster = Objects.requireNonNull(newCluster, "newCluster must not be null");
        this.vmsLockedForUpdate = Objects.requireNonNull(vmsLockedForUpdate, "vmsLockedForUpdate must not be null");
        this.templatesLockedForUpdate = Objects.requireNonNull(templatesLockedForUpdate,
                "templatesLockedForUpdate must not be null");
        this.vdsDao = Objects.requireNonNull(vdsDao, "vdsDao must not be null");
        this.vmDynamicDao = Objects.requireNonNull(vmDynamicDao, "vmDynamicDao must not be null");
    }

    /**
     * True when this raise crosses the i440fx-deprecation level (from below 4.8 to it
     * or above) — the raise-time migration moment of the per-cluster i440fx design.
     */
    public boolean isAcrossDeprecatedLevel() {
        return oldCluster != null
                && newCluster != null
                && !oldCluster.getCompatibilityVersion().equals(newCluster.getCompatibilityVersion())
                && !ClusterEmulatedMachines.isI440fxDeprecated(oldCluster.getCompatibilityVersion())
                && ClusterEmulatedMachines.isI440fxDeprecated(newCluster.getCompatibilityVersion());
    }

    /**
     * Raise-time guard: the raise pins all unpinned i440fx VMs/templates of the cluster.
     * An entity can always be pinned when either its current machine type is runnable on
     * at least one host (preserve) or the hosts share a common i440fx machine type to
     * retarget it to (revive). The raise fails only when neither is possible — a dead
     * machine type and no common type, i.e. a mixed-host cluster — and converting the
     * listed entities to Q35 (the Edit Cluster dialog's chipset-conversion option) or
     * pinning them explicitly unblocks it.
     *
     * @return the names of the unfixable entities; empty when the raise may proceed
     */
    public List<String> validate() {
        if (!isAcrossDeprecatedLevel()) {
            return List.of();
        }
        List<VmBase> unpinned = collectUnpinnedI440fxEntities();
        if (unpinned.isEmpty()) {
            return List.of();
        }
        String i440fxDefault = i440fxDefaultOfOldCluster();
        List<String> commonMachines = vdsDao.getClusterI440fxCommonMachines(newCluster.getId());
        List<String> unfixableNames = new ArrayList<>();
        for (VmBase entity : unpinned) {
            if (resolveRaiseI440fxPin(entity.getId(), i440fxDefault, commonMachines) == null) {
                unfixableNames.add(entity.getName());
            }
        }
        return unfixableNames;
    }

    /**
     * Raise-time migration: decides the pin of every unpinned i440fx VM/template of the
     * cluster and sets it on the in-memory entity — the standard UpdateVm/UpdateVmTemplate
     * flow persists it as part of the raise, compensation-enabled, so a failed raise
     * reverts the pins with everything else (the hardware fields of a running VM reach it
     * through its pending NEXT_RUN configuration, applied at its next stop). The audit
     * trail is recorded here and fired by {@link #fireAuditEvents()} once the raise
     * completed. Hosted Engine VMs are pinned like all others — pinning preserves
     * hardware, and the HE exclusion only applies to chipset conversion.
     */
    public void pinEntities() {
        if (!isAcrossDeprecatedLevel()) {
            return;
        }
        String i440fxDefault = i440fxDefaultOfOldCluster();
        List<String> commonMachines = vdsDao.getClusterI440fxCommonMachines(newCluster.getId());
        for (VmBase entity : collectUnpinnedI440fxEntities()) {
            String derived = resolveI440fxPin(entity.getId(), i440fxDefault);
            String pin = resolveRaiseI440fxPin(entity.getId(), i440fxDefault, commonMachines);
            if (pin == null) {
                // Unreachable after validate() (which fails the raise for such
                // entities); kept as a defensive path so a race can never silently leave
                // an unpinned entity behind.
                log.error("Raising cluster '{}': no i440fx machine type can be determined for"
                                + " unpinned i440fx entity '{}'; leaving it unpinned.",
                        oldCluster.getName(), entity.getName());
                raiseRecords.add(I440fxRaiseRecord.skipped(entity.getName()));
                continue;
            }
            entity.setCustomEmulatedMachine(pin);
            entitiesPinnedOnRaise++;
            if (derived == null) {
                // no machine type at all (not running, no usable default): reviving is
                // the same "newer hardware" event for the admin
                raiseRecords.add(I440fxRaiseRecord.retargeted(entity.getName(), "none", pin));
            } else if (!derived.equals(pin)) {
                raiseRecords.add(I440fxRaiseRecord.retargeted(entity.getName(), derived, pin));
            } else {
                raiseRecords.add(I440fxRaiseRecord.pinned(entity.getName(), pin));
            }
        }
    }

    /**
     * Fires the raise-time migration audit trail — per-entity pin/retarget/skip events
     * and the cluster summary — once the raise completed successfully, so the audit log
     * only ever describes raises that actually happened. Must be invoked after the
     * raise completed (audit logging is command machinery; the command was captured at
     * construction).
     */
    public void fireAuditEvents() {
        if (!isAcrossDeprecatedLevel()) {
            return;
        }
        command.addCustomValue("ClusterName", oldCluster.getName());
        for (I440fxRaiseRecord record : raiseRecords) {
            command.addCustomValue("VmName", record.entityName());
            if (record.skipped()) {
                command.auditLog(command, AuditLogType.CLUSTER_UPDATE_I440FX_VM_NOT_PINNED);
            } else if (record.previousMachine() != null) {
                command.addCustomValue("PreviousMachine", record.previousMachine());
                command.addCustomValue("CustomEmulatedMachine", record.machine());
                command.auditLog(command, AuditLogType.CLUSTER_UPDATE_I440FX_VM_RETARGETED);
            } else {
                command.addCustomValue("CustomEmulatedMachine", record.machine());
                command.auditLog(command, AuditLogType.CLUSTER_UPDATE_I440FX_VM_PINNED);
            }
        }
        command.addCustomValue("VmsPinned", String.valueOf(entitiesPinnedOnRaise));
        command.addCustomValue("CompatibilityVersion", newCluster.getCompatibilityVersion().getValue());
        command.auditLog(command, AuditLogType.CLUSTER_UPDATE_I440FX_DEFAULT_MIGRATED);
    }

    private List<VmBase> collectUnpinnedI440fxEntities() {
        List<VmBase> unpinned = new ArrayList<>();
        for (VmStatic vm : vmsLockedForUpdate) {
            if (isUnpinnedI440fx(vm)) {
                unpinned.add(vm);
            }
        }
        for (VmTemplate template : templatesLockedForUpdate) {
            if (isUnpinnedI440fx(template)) {
                unpinned.add(template);
            }
        }
        return unpinned;
    }

    private String i440fxDefaultOfOldCluster() {
        return ClusterEmulatedMachines.defaultForChipset(
                oldCluster.getEmulatedMachine(), ChipsetType.I440FX);
    }

    private static boolean isUnpinnedI440fx(VmBase vmBase) {
        // Hosted Engine VMs are included: pinning preserves their hardware, skipping the
        // pin is what would break them (an unpinned i440fx VM throws at its next start on
        // a >= 4.8 cluster). The HE exclusion only applies to the chipset-conversion flow
        // (shouldUpdateVmsChipset), which does change hardware.
        return vmBase.getBiosType() != null
                && vmBase.getBiosType().getChipsetType() == ChipsetType.I440FX
                && StringUtils.isEmpty(vmBase.getCustomEmulatedMachine());
    }

    /**
     * Resolves the emulated machine to pin on an unpinned i440fx entity at raise time:
     * what the VM is actually running (vm_dynamic), falling back to the i440fx part of
     * the stored dual cluster default. A candidate is only accepted if it is not a Q35
     * machine type — pinning a Q35 machine onto an i440fx VM would silently change its
     * hardware (a single-Q35 cluster default must not leak through here). Non-x86
     * machine types (null chipset) are accepted.
     */
    private String resolveI440fxPin(Guid vmId, String i440fxDefault) {
        String running = resolveRunningEmulatedMachine(vmId);
        if (StringUtils.isNotEmpty(running)
                && ChipsetType.fromMachineType(running) != ChipsetType.Q35) {
            return running;
        }
        if (StringUtils.isNotEmpty(i440fxDefault)
                && ChipsetType.fromMachineType(i440fxDefault) != ChipsetType.Q35) {
            return i440fxDefault;
        }
        return null;
    }

    /**
     * The final pin decision for an unpinned i440fx entity at raise time: keep the
     * machine type the entity effectively uses today when some host can still run it
     * (pinning preserves hardware — working VMs are never retargeted); otherwise revive
     * it with the NEWEST machine type common to all hosts (a dead machine type helps
     * nobody, and the newest common type has the longest support runway). Returns
     * {@code null} when nothing sound can be derived — such entities fail the raise in
     * {@link #validate()}.
     */
    private String resolveRaiseI440fxPin(Guid entityId, String i440fxDefault, List<String> commonMachines) {
        String derived = resolveI440fxPin(entityId, i440fxDefault);
        if (derived != null && isSupportedByAnyHost(derived)) {
            return derived;
        }
        return ClusterEmulatedMachines.pickNewVmI440fxMachine(commonMachines);
    }

    private boolean isSupportedByAnyHost(String machine) {
        ensureAllHostsForClusterLoaded();
        return allHostsForCluster.stream()
                .filter(vds -> vds.getSupportedEmulatedMachines() != null)
                .anyMatch(vds -> Arrays.stream(vds.getSupportedEmulatedMachines().split(","))
                        .map(String::trim)
                        .anyMatch(machine::equals));
    }

    private void ensureAllHostsForClusterLoaded() {
        if (allHostsForCluster == null) {
            allHostsForCluster = vdsDao.getAllForCluster(newCluster.getId());
        }
    }

    private String resolveRunningEmulatedMachine(Guid vmId) {
        VmDynamic dynamic = vmDynamicDao.get(vmId);
        return dynamic != null ? dynamic.getEmulatedMachine() : null;
    }

    /**
     * One unpinned i440fx entity's outcome of the raise-time migration.
     */
    static final class I440fxRaiseRecord {
        private final String entityName;
        private final String machine;
        private final String previousMachine; // null = kept the machine type the entity already used
        private final boolean skipped;

        private I440fxRaiseRecord(String entityName, String machine, String previousMachine, boolean skipped) {
            this.entityName = entityName;
            this.machine = machine;
            this.previousMachine = previousMachine;
            this.skipped = skipped;
        }

        static I440fxRaiseRecord pinned(String entityName, String machine) {
            return new I440fxRaiseRecord(entityName, machine, null, false);
        }

        static I440fxRaiseRecord retargeted(String entityName, String previousMachine, String machine) {
            return new I440fxRaiseRecord(entityName, machine, previousMachine, false);
        }

        static I440fxRaiseRecord skipped(String entityName) {
            return new I440fxRaiseRecord(entityName, null, null, true);
        }

        String entityName() {
            return entityName;
        }

        String machine() {
            return machine;
        }

        String previousMachine() {
            return previousMachine;
        }

        boolean skipped() {
            return skipped;
        }
    }
}

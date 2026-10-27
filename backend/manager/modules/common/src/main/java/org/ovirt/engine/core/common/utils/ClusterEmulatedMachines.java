package org.ovirt.engine.core.common.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for cluster emulated machine operations.
 * <p>
 * Since version 4.8, the cluster default machine type of a migrated cluster is Q35-only
 * (e.g. "pc-q35-rhel9.6.0"). Clusters below 4.8 — and clusters raised to 4.8 by an admin
 * whose data was migrated at raise time — keep the dual format
 * "i440fxType;q35Type" (e.g. "pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0"); this is a permanently
 * supported format, not a transition state. Use {@link #defaultForChipset} to resolve the
 * effective default for a chipset instead of reading {@code cluster.getEmulatedMachine()}
 * directly. i440fx VMs in clusters at level >= 4.8 must have their emulated machine set
 * explicitly via {@code vm_static.custom_emulated_machine}.
 */
public class ClusterEmulatedMachines {

    /**
     * The first cluster compatibility level where the shared-i440fx-machine-type problem
     * exists: CentOS 10 (introduced with 4.8) removed the legacy i440fx machine types that
     * CentOS 9 still provides, so no i440fx machine type is common to both. Clusters below
     * this level keep the legacy dual-format default and their unpinned i440fx VMs.
     */
    public static final Version I440FX_DEPRECATED_SINCE = new Version(4, 8);

    private static final Logger log = LoggerFactory.getLogger(ClusterEmulatedMachines.class);

    private ClusterEmulatedMachines() {
    }

    /**
     * Returns true when the VM's effective compatibility version (its own custom
     * compatibility version, or the cluster's) is at or above the i440fx-deprecation
     * level — the strict i440fx rules apply for such VMs.
     */
    public static boolean isI440fxDeprecated(VmStatic vmStatic, Cluster cluster) {
        return isI440fxDeprecated(CompatibilityVersionUtils.getEffective(vmStatic, cluster));
    }

    /**
     * Returns true when clusters at the given compatibility level require the strict
     * i440fx rules (pinned emulated machine required, Q35-only cluster default).
     */
    public static boolean isI440fxDeprecated(Version compatibilityVersion) {
        return compatibilityVersion != null
                && compatibilityVersion.greaterOrEquals(I440FX_DEPRECATED_SINCE);
    }

    /**
     * Propagates a pinned emulated machine onto a pending i440fx configuration that
     * carries none — e.g. a NEXT_RUN snapshot created before the pin was written (the
     * engine-setup migration pins vm_static but not the snapshots table; a cluster raise
     * pins entities mid-flow). Pinning preserves hardware: an unpinned pending config
     * predates the pinning, and at cluster level >= 4.8 clearing a pin is rejected by
     * validation anyway, so an unpinned pending config is never deliberate intent there.
     * A pending configuration with an explicit machine type of its own keeps it. Callers
     * are responsible for the level gate: below 4.8 a deliberate pin clear is legal, so
     * this must not be invoked for it.
     *
     * @param pinnedMachine the machine type the entity is pinned to (its current effective hardware)
     * @param pendingConfig the incoming configuration to propagate the pin onto
     */
    public static void propagateI440fxPin(String pinnedMachine, VmBase pendingConfig) {
        if (pinnedMachine == null || pinnedMachine.isEmpty() || pendingConfig == null) {
            return;
        }
        if (pendingConfig.getBiosType() == null
                || pendingConfig.getBiosType().getChipsetType() != ChipsetType.I440FX) {
            return;
        }
        String pendingMachine = pendingConfig.getCustomEmulatedMachine();
        if (pendingMachine != null && !pendingMachine.isEmpty()) {
            return;
        }
        pendingConfig.setCustomEmulatedMachine(pinnedMachine);
    }

    /**
     * Resolves the effective part of a stored cluster default for a chipset:
     * "i440fxType;q35Type" + I440FX -> part 1, + Q35 -> part 2; a single (non-dual)
     * value is returned as-is. Returns {@code null} when the dual value has no part for
     * the chipset (e.g. ";q35Type" with I440FX) or when the stored value itself is null.
     */
    public static String defaultForChipset(String stored, ChipsetType chipset) {
        if (stored == null) {
            return null;
        }
        if (!stored.contains(";")) {
            return stored;
        }
        String[] parts = stored.split(";", -1);
        String part = (chipset == ChipsetType.Q35 ? parts[1] : parts[0]).trim();
        return part.isEmpty() ? null : part;
    }

    /**
     * Builds a stored cluster default from the per-chipset parts: both present -> dual
     * "i440fxType;q35Type", only one present -> that single value.
     */
    public static String build(String i440fxType, String q35Type) {
        if (i440fxType == null) {
            return q35Type;
        }
        return q35Type == null ? i440fxType : i440fxType + ";" + q35Type;
    }

    /**
     * Returns a predicate that matches emulated machine names for the given chipset type.
     * For non-x86 machine types (s390x, and historically pseries), {@link ChipsetType#fromMachineType}
     * returns null. The predicate matches these against I440FX, preserving the legacy convention
     * predating chipset awareness. pseries (ppc64le) KVM host support was dropped in RHEL 9;
     * s390x (IBM Z) remains supported.
     *
     * @param chipsetType the chipset type to match
     * @return a predicate that returns true if the emulated machine name matches the chipset
     */
    public static Predicate<String> chipsetMatches(ChipsetType chipsetType) {
        return emulatedMachine -> {
            ChipsetType emChipsetType = ChipsetType.fromMachineType(emulatedMachine);
            return chipsetType == ChipsetType.I440FX && emChipsetType == null || chipsetType == emChipsetType;
        };
    }

    /**
     * Finds the best emulated machine from the available list that is both
     * supported by the host and matches the given chipset type.
     *
     * @param chipsetType the required chipset type
     * @param supported the set of emulated machines supported by the host
     * @param available the list of emulated machines available at this cluster compatibility version
     * @return the first matching emulated machine, or null if none found
     */
    public static String getSupportedByChipset(ChipsetType chipsetType, Set<String> supported, List<String> available) {
        return available
                .stream()
                .filter(supported::contains)
                .filter(chipsetMatches(chipsetType))
                .findFirst().orElse(null);
    }

    /**
     * Get effective emulated machine type for a VM.
     * <p>
     * Resolution order:
     * <ol>
     *   <li>If the VM has {@code customEmulatedMachine} set, return it</li>
     *   <li>Resolve the cluster default for the VM's chipset
     *       (the chipset's part of a dual "i440fx;q35" value below 4.8; Q35-only above).
     *       An i440fx VM without customEmulatedMachine throws at cluster level >= 4.8;
     *       below 4.8 it inherits the i440fx part. A resolved default of the wrong
     *       chipset fails loudly.</li>
     *   <li>If the VM has a custom compatibility version, find the best matching
     *       older machine type from the config for that version</li>
     * </ol>
     *
     * @param vm the VM entity
     * @param clusterSupplier supplier of non-null Cluster
     * @return the effective emulated machine type
     * @throws IllegalStateException if an i440fx VM without customEmulatedMachine runs in
     *         a cluster at level >= 4.8, or a VM would end up with a default of the wrong chipset
     */
    public static String getEffective(VM vm, Supplier<Cluster> clusterSupplier) {
        if (vm.getCustomEmulatedMachine() != null) {
            return vm.getCustomEmulatedMachine();
        }

        Cluster cluster = clusterSupplier.get();
        String clusterDefault = cluster.getEmulatedMachine();

        // Legacy VMs with no chipset info predate chipset awareness and run i440fx hardware —
        // the chipsetMatches convention maps them (and non-x86 machine types) to I440FX.
        ChipsetType chipsetType = vm.getBiosType() == null
                ? ChipsetType.I440FX
                : vm.getBiosType().getChipsetType();

        if (chipsetType == ChipsetType.I440FX) {
            if (vm.getBiosType() != null) {
                if (isI440fxDeprecated(vm.getStaticData(), cluster)) {
                    // i440fx VMs must have customEmulatedMachine set — the cluster default is Q35-only.
                    // This should never be reached in normal operation because:
                    //   - AddVmCommand/UpdateVmCommand reject i440fx VMs without the field
                    //     (EmulatedMachineValidator.validateEmulatedMachineForChipset)
                    //   - imported unpinned i440fx VMs are auto-pinned on import
                    //     (ImportVmCommandBase.autoPinImportedI440fxMachine) or rejected at run with a
                    //     clear message when no common machine type exists
                    //   - DB migration 04_05_0350 pins it for clusters already at >= 4.8, and
                    //     UpdateClusterCommand pins it when such a cluster is raised to >= 4.8
                    log.error("i440fx VM '{}' ({}) has no custom emulated machine set, and the cluster"
                            + " default is Q35-only at this compatibility level. Set an emulated"
                            + " machine type for the VM explicitly (e.g. pc-i440fx-rhel10.0.0),"
                            + " or convert the VM to Q35.", vm.getName(), vm.getId());
                    throw new IllegalStateException(
                            "i440fx VM '" + vm.getName() + "' (" + vm.getId() + ") must have customEmulatedMachine set");
                }
                // Below 4.8 the dual cluster default is a legitimate, permanently supported state:
                // an unpinned i440fx VM inherits its i440fx part.
                String i440fxDefault = defaultForChipset(clusterDefault, ChipsetType.I440FX);
                if (i440fxDefault == null || ChipsetType.fromMachineType(i440fxDefault) == ChipsetType.Q35) {
                    log.error("i440fx VM '{}' ({}) is in a cluster whose default emulated machine is '{}',"
                                    + " which has no i440fx part. Set an emulated machine type for"
                                    + " the VM explicitly, or convert the VM to Q35.",
                            vm.getName(), vm.getId(), clusterDefault);
                    throw new IllegalStateException(
                            "i440fx VM '" + vm.getName() + "' (" + vm.getId()
                                    + ") cannot use the cluster default emulated machine '" + clusterDefault + "'");
                }
                return resolveWithCcv(vm, cluster, chipsetType, i440fxDefault);
            }
            // Legacy VM with no chipset info: it predates chipset awareness and runs i440fx
            // hardware. Resolve the i440fx part of a dual default so a raw dual value can
            // never reach libvirt; a single stored value passes through unchanged (any chipset).
            // The strict >= 4.8 rules apply here too: without this check a >= 4.8 cluster's
            // Q35-only default would pass through defaultForChipset as a single value and the
            // legacy VM would silently switch chipsets at its next run, instead of failing
            // loudly like an explicit-i440fx VM does.
            if (isI440fxDeprecated(vm.getStaticData(), cluster)) {
                log.error("Legacy i440fx VM '{}' ({}) has no custom emulated machine set, and the cluster"
                        + " default is Q35-only at this compatibility level. Set an emulated"
                        + " machine type for the VM explicitly (e.g. pc-i440fx-rhel10.0.0),"
                        + " or convert the VM to Q35.", vm.getName(), vm.getId());
                throw new IllegalStateException(
                        "legacy i440fx VM '" + vm.getName() + "' (" + vm.getId()
                                + ") must have customEmulatedMachine set");
            }
            return resolveWithCcv(vm, cluster, chipsetType,
                    defaultForChipset(clusterDefault, ChipsetType.I440FX));
        }

        // Q35: a Q35 VM must never end up with an i440fx machine type. For the legacy dual
        // format below 4.8 use its Q35 part; for a plain i440fx default — an inconsistent
        // state for a Q35 VM — fail loudly rather than run the VM with a machine type of
        // the wrong chipset. A null cluster default (an inconsistent state in itself, no
        // host complies with it) passes through as before.
        if (clusterDefault == null) {
            return resolveWithCcv(vm, cluster, chipsetType, null);
        }
        String q35Default = defaultForChipset(clusterDefault, ChipsetType.Q35);
        if (q35Default == null || ChipsetType.fromMachineType(q35Default) == ChipsetType.I440FX) {
            if (clusterDefault != null && clusterDefault.contains(";")) {
                log.warn("Cluster default emulated machine '{}' is the legacy dual format;"
                                + " using its Q35 part for Q35 VM '{}' ({})",
                        clusterDefault, vm.getName(), vm.getId());
            } else {
                log.error("Q35 VM '{}' ({}) is in a cluster whose default emulated machine is '{}',"
                                + " a machine type of the wrong chipset for this VM. Set an emulated"
                                + " machine type for the VM explicitly.",
                        vm.getName(), vm.getId(), clusterDefault);
            }
            throw new IllegalStateException(
                    "Q35 VM '" + vm.getName() + "' (" + vm.getId()
                            + ") cannot use the i440fx cluster default emulated machine '"
                            + clusterDefault + "'");
        }
        return resolveWithCcv(vm, cluster, chipsetType, q35Default);
    }

    /**
     * Second half of {@link #getEffective}: without a custom compatibility version the
     * resolved cluster default is already correct; with one, the best matching machine
     * type from the config list for that version replaces it — each version maps to a
     * different RHEL/QEMU generation. The chipset-resolved default is used both as the
     * similarity reference and as the fallback, so a raw dual value can never leak out.
     */
    private static String resolveWithCcv(
            VM vm, Cluster cluster, ChipsetType chipsetType, String resolvedDefault) {
        if (vm.getCustomCompatibilityVersion() == null) {
            return resolvedDefault;
        }
        String bestMatch = findBestMatchForEmulatedMachine(
                chipsetType,
                resolvedDefault,
                Config.getValue(
                        ConfigValues.ClusterEmulatedMachines,
                        CompatibilityVersionUtils.getEffective(vm.getStaticData(), cluster).getValue()));
        log.info("Emulated machine '{}' which is different than that of the cluster is set for '{}'({})",
                bestMatch, vm.getName(), vm.getId());
        return bestMatch;
    }

    /**
     * Finds the best matching emulated machine from the candidate list for the given chipset type.
     * "Best match" is the candidate with the longest common prefix with the current emulated machine.
     *
     * @param chipsetType the required chipset type
     * @param currentEmulatedMachine the current/default emulated machine
     * @param candidateEmulatedMachines the list of candidate emulated machines
     * @return the best matching candidate, or the current emulated machine if no match is found
     */
    public static String findBestMatchForEmulatedMachine(
            ChipsetType chipsetType,
            String currentEmulatedMachine,
            List<String> candidateEmulatedMachines) {
        return candidateEmulatedMachines
                .stream()
                .filter(chipsetMatches(chipsetType))
                .max(Comparator.comparingInt(s -> commonPrefixLength(currentEmulatedMachine, s)))
                .orElse(currentEmulatedMachine);
    }

    /**
     * Returns the number of matching leading characters between two strings. Used to rank
     * emulated machine name candidates by how closely they match the current machine type.
     * An exact match always ranks highest: no candidate can share more leading characters
     * than the reference string has.
     */
    static int commonPrefixLength(String a, String b) {
        if (a == null || b == null) {
            return 0;
        }
        int minLen = Math.min(a.length(), b.length());
        for (int i = 0; i < minLen; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return i;
            }
        }
        return minLen;
    }

    /**
     * Picks the emulated machine type to pin on an imported i440fx VM that carries none:
     * the OLDEST machine type common to all hosts of the target cluster — the smallest
     * hardware change for a VM that ran an older machine type at its source. The
     * intentional opposite of {@link #pickNewVmI440fxMachine(List)}.
     *
     * @param commonMachines i440fx machine types common to all hosts of the target cluster,
     *                       as returned by {@code GetClusterI440fxCommonMachines}
     * @return the machine type to pin, or {@code null} if none can be derived
     */
    public static String pickImportI440fxMachine(List<String> commonMachines) {
        return pickI440fxMachine(commonMachines, false);
    }

    /**
     * Picks the emulated machine type for a newly created i440fx VM that carries none:
     * the NEWEST machine type common to all hosts of the target cluster — a new VM has
     * no machine-type history, so the newest common type gives it the longest support
     * runway (RHEL drops old machine types every major release). Mirrors the New VM
     * dialog; the intentional opposite of {@link #pickImportI440fxMachine(List)}.
     *
     * @param commonMachines i440fx machine types common to all hosts of the target cluster,
     *                       as returned by {@code GetClusterI440fxCommonMachines}
     * @return the machine type to pin, or {@code null} if none can be derived
     */
    public static String pickNewVmI440fxMachine(List<String> commonMachines) {
        return pickI440fxMachine(commonMachines, true);
    }

    /**
     * Common part of both pickers: the floating {@code pc} alias is excluded because it
     * resolves to a different concrete machine type on every QEMU version and therefore
     * cannot serve as a stable pin.
     */
    private static String pickI440fxMachine(List<String> commonMachines, boolean preferNewest) {
        if (commonMachines == null) {
            return null;
        }
        Stream<String> candidates =
                commonMachines.stream().filter(m -> !"pc".equalsIgnoreCase(m));
        return preferNewest
                ? candidates.max(ClusterEmulatedMachines::compareEmulatedMachineVersions).orElse(null)
                : candidates.min(ClusterEmulatedMachines::compareEmulatedMachineVersions).orElse(null);
    }

    /**
     * Compares emulated machine types by their trailing version segment
     * (e.g. {@code pc-i440fx-rhel7.6.0} or {@code pc-i440fx-2.12}), numerically per
     * component, so that {@code rhel10.0.0} sorts after {@code rhel7.6.0}. Falls back to
     * natural string order when either side carries no parseable version. Public: also
     * used by the webadmin VM dialog (GWT-included).
     */
    public static int compareEmulatedMachineVersions(String left, String right) {
        int[] leftVersion = parseEmulatedMachineVersion(left);
        int[] rightVersion = parseEmulatedMachineVersion(right);
        if (leftVersion != null && rightVersion != null) {
            for (int i = 0; i < Math.max(leftVersion.length, rightVersion.length); i++) {
                int cmp = Integer.compare(i < leftVersion.length ? leftVersion[i] : 0,
                        i < rightVersion.length ? rightVersion[i] : 0);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }
        return left.compareTo(right);
    }

    /**
     * Extracts the numeric version from the last {@code '-'}-separated segment of an emulated
     * machine name, dropping an optional {@code rhel} prefix; returns {@code null} if unparseable.
     */
    static int[] parseEmulatedMachineVersion(String machine) {
        int idx = machine.lastIndexOf('-');
        String tail = idx >= 0 ? machine.substring(idx + 1) : machine;
        if (tail.startsWith("rhel")) {
            tail = tail.substring(4);
        }
        String[] parts = tail.split("\\.");
        int[] version = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                version[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return version;
    }

}

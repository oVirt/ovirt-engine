package org.ovirt.engine.core.bll.scheduling.arem;

import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class is intended to represent one cluster in the Affinity Rules Enforcement manager.
 * It will be used to track last migration done in the cluster, When there is a broken affinity
 * rule, find a VM to migrate and find affinity rules collisions.
 */
public class AffinityRulesEnforcementPerCluster {
    List<MigrationEntryDS> lastMigrations;
    Integer migrationTries;
    Guid clusterId;

    private static final Logger log = LoggerFactory.getLogger(AffinityRulesEnforcementPerCluster.class);

    @Inject
    protected AffinityGroupDao affinityGroupDao;
    @Inject
    protected VmDao vmDao;
    @Inject
    protected VdsDao vdsDao;
    @Inject
    protected VdsGroupDao vdsGroupDao;

    protected final SchedulingManager schedulingManager = SchedulingManager.getInstance();

    protected enum FailMode {
        IMMEDIATELY, // Fail when first violation is detected
        GET_ALL // Collect all violations
    }

    @PostConstruct
    public void wakeup() {
        this.lastMigrations = new ArrayList<>();
        this.migrationTries = 0;
    }


    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void initMigrations() {
        this.lastMigrations = new ArrayList<>();
        this.migrationTries = 0;
    }

    public Boolean checkIfCurrentlyMigrating() {
        if (this.lastMigrations.isEmpty()) {
            return false;
        }

        VdcReturnValueBase migrationStatus = this.lastMigrations.get(0).getMigrationStatus();

        // null migrationStatus indicates that migration is still running.
        return migrationStatus == null || migrationStatus.getSucceeded();

    }

    public boolean lastMigrationFailed() {

        //Checking last migration tail existence and that it's status is failure.
        if( lastMigrations.isEmpty()) {
            return false; //lastMigrations empty so migration didn't fail.
        }

        MigrationEntryDS lastMigrationTail = lastMigrations.get(0);

        //Migration succeeded. Therefore, it did not fail.
        return !lastMigrationTail.getMigrationStatus().getSucceeded();
    }

    public void updateMigrationFailure() {
        migrationTries++;
        lastMigrations.get(0).setMigrationReturnValue(null); //To avoid considering it a migration failure again.
    }

    public Integer getMigrationTries() {
        return migrationTries;
    }

    public void setMigrationTries(Integer migrationTries) {
        this.migrationTries = migrationTries;
    }

    public VM chooseNextVmToMigrate() {
        List<AffinityGroup> allAffinityGroups = getAllAffinityGroups();
        Set<Set<Guid>> unifiedPositiveAffinityGroups = getUnifiedPositiveAffinityGroups(allAffinityGroups);
        List<AffinityGroup> unifiedAffinityGroups = setsToAffinityGroups(unifiedPositiveAffinityGroups);

        // Add negative affinity groups
        for (AffinityGroup ag: allAffinityGroups) {
            if (ag.isPositive()) {
                continue;
            }

            unifiedAffinityGroups.add(ag);
        }

        // Create a set of all VMs in affinity groups
        Set<Guid> allVms = new HashSet<>();
        for (AffinityGroup group: unifiedAffinityGroups) {
            allVms.addAll(group.getEntityIds());
        }

        Map<Guid, Guid> vmToHost = new HashMap<>();

        // There is no need to migrate when no collision was detected
        Set<AffinityGroup> violatedAffinityGroups = checkForAffinityGroupViolations(unifiedAffinityGroups, vmToHost, FailMode.GET_ALL);
        if (violatedAffinityGroups.isEmpty()) {
            log.debug("No affinity group collision detected for cluster {}. Standing by.", clusterId);
            return null;
        }

        // Find a VM that is breaking the affinityGroup and can be theoretically migrated
        // - start with smaller Affinity Groups
        List<AffinityGroup> affGroupsBySize = new ArrayList<>(violatedAffinityGroups);
        Collections.sort(affGroupsBySize, new AffinityGroupComparator());

        for (AffinityGroup affinityGroup: affGroupsBySize) {
            Guid candidateVm;

            if (affinityGroup.isPositive()) {
                candidateVm = findVmViolatingPositiveAg(affinityGroup, vmToHost);
                log.info("Positive affinity group violation detected for VM {}", candidateVm);
            } else {
                candidateVm = findVmViolatingNegativeAg(affinityGroup, vmToHost);
                log.info("Negative affinity group violation detected for VM {}", candidateVm);
            }

            // No candidate found
            if (candidateVm == null) {
                continue;
            }

            // Test whether any migration is possible, this uses current AffinityGroup settings
            // and so won't allow more breakage
            VM vm = vmDao.get(candidateVm);
            VDSGroup cluster = vdsGroupDao.get(clusterId);
            boolean canMove = schedulingManager.canSchedule(cluster, vm,
                    new ArrayList<Guid>(), new ArrayList<Guid>(),
                    null, new ArrayList<String>());

            if (canMove) {
                log.debug("VM {} is a viable candidate for solving affinity group violation situation.", candidateVm);
                lastMigrations.add(new MigrationEntryDS(candidateVm, vmToHost.get(candidateVm)));
                return vm;
            }
            log.debug("VM {} is NOT a viable candidate for solving affinity group violation situation.", candidateVm);

        }

        // No possible migration..
        return null;
    }

    /**
     * Select a VM from the broken affinity group that is running on the same host as some of
     * the other VMs.
     *
     * @param affinityGroup broken affinity rule
     * @param vmToHost vm to host assignments
     * @return a vm that should migrate
     */
    private Guid findVmViolatingNegativeAg(AffinityGroup affinityGroup, Map<Guid, Guid> vmToHost) {
        Map<Guid, Guid> firstAssignment = new HashMap<>();
        Set<Guid> violatingVms = new HashSet<>();

        // When a VM runs on an already occupied host, report both
        // the vm and the previous occupant as candidates for migration
        for (Guid vm: affinityGroup.getEntityIds()) {
            Guid host = vmToHost.get(vm);
            if (firstAssignment.containsKey(host)) {
                violatingVms.add(vm);
                violatingVms.add(firstAssignment.get(host));
            } else {
                firstAssignment.put(host, vm);
            }
        }

        List<Guid> violatingVmsArray = new ArrayList<>(violatingVms);
        // Select random VM from the selected host
        int index = new Random().nextInt(violatingVmsArray.size());
        return violatingVmsArray.get(index);
    }

    /**
     * Select a VM from the broken affinity group that is running on a host with the minimal amount
     * of VMs from the broken affinity group.
     *
     * Ex.: Host1: A, B, C, D  Host2: E, F  -> select E or F
     * @param affinityGroup broken affinity group
     * @param vmToHost vm to host assignments
     * @return a VM that should migrate
     */
    private Guid findVmViolatingPositiveAg(AffinityGroup affinityGroup, Map<Guid, Guid> vmToHost) {
        Map<Guid, List<Guid>> hostCount = new HashMap<>();

        // Prepare affinity group related host counts
        for (Guid vm: affinityGroup.getEntityIds()) {
            Guid host = vmToHost.get(vm);
            if (hostCount.containsKey(host)) {
                hostCount.get(host).add(vm);
            } else {
                hostCount.put(host, new ArrayList<Guid>());
                hostCount.get(host).add(vm);
            }
        }

        // Select the host with the least amount of VMs
        Guid host = chooseCandidateHostForMigration(hostCount);
        if (host == null) {
            return null;
        }

        // Select random VM from the selected host
        int index = new Random().nextInt(hostCount.get(host).size());
        return hostCount.get(host).get(index);
    }

    /**
     * Select a host to source a VM belonging to the Affinity Group. The assumption here is that
     * the host with the lowest amount of VMs from the affinity group is the best source,
     * because the number of needed migrations will be minimal when compared to other solutions.
     */
    protected Guid chooseCandidateHostForMigration(Map<Guid, ? extends Collection<Guid>> mapOfHostsToVms) {
        int maxNumberOfVms = Integer.MAX_VALUE;
        Guid bestHost = null;

        for(Map.Entry<Guid, ? extends Collection<Guid>> entry: mapOfHostsToVms.entrySet()) {
            if (entry.getValue().size() < maxNumberOfVms) {
                maxNumberOfVms = entry.getValue().size();
                bestHost = entry.getKey();
            }
        }

        return bestHost;
    }

    /**
     * Create a map of Host to VMs and VM to Host assignments.
     *
     * The return value is a map where each host (key) has a set of VMs
     * running on it assigned as value.
     *
     * vmToHost is filled with VM -> Host map when not null.
     */
    protected Map<Guid, Set<Guid>> createMapOfHostsToVms(Iterable<Guid> vms,
            Map<Guid, Guid> vmToHost) {
        Map<Guid, Set<Guid>> output = new HashMap<>();
        for(Guid vmId : vms) {
            VM vm = vmDao.get(vmId);
            Guid vdsId = vm.getRunOnVds();

            // We will not add any Vms which are not running on any host.
            if(vdsId == null) {
                continue;
            }

            if (vmToHost != null) {
                vmToHost.put(vm.getId(), vdsId);
            }

            if(!output.containsKey(vdsId)) {
                output.put(vdsId, new HashSet<Guid>());
            }
            else {
                output.get(vdsId).add(vm.getId());
            }
        }

        return output;
    }

    /**
     * Detect whether the current VM to VDS assignment violates current Affinity Groups.
     *
     * @param affinityGroups Unified affinity groups
     * @param vmToHost Mapping of VM to currently assigned VDS
     * @return broken AffinityGroups
     */
    static protected Set<AffinityGroup> checkForAffinityGroupViolations(Iterable<AffinityGroup> affinityGroups,
            Map<Guid, Guid> vmToHost, FailMode mode) {

        Set<AffinityGroup> broken = new HashSet<>();

        for (AffinityGroup affinity: affinityGroups) {
            // Negative groups
            if (!affinity.isPositive()) {
                // Record all hosts that are already occupied by VMs from this group
                Map<Guid, Guid> usedHosts = new HashMap<>();

                for (Guid vm: affinity.getEntityIds()) {
                    Guid host = vmToHost.get(vm);
                    if (host == null) {
                        continue;
                    }

                    // Report a violation when any host has more than one VM from this group
                    if (usedHosts.containsKey(host)) {
                        log.debug("Negative affinity rule violated between VMs {} and {} on host {}",
                                vm, usedHosts.get(host), host);
                        broken.add(affinity);

                        if (mode.equals(FailMode.IMMEDIATELY)) {
                            return broken;
                        }
                    } else {
                        usedHosts.put(host, vm);
                    }
                }

            // Positive groups
            } else {
                // All VMs from this group have to be running on a single host
                Guid targetHost = null;

                for (Guid vm: affinity.getEntityIds()) {
                    Guid host = vmToHost.get(vm);
                    if (host == null) {
                        continue;
                    }

                    // Report a violation when two VMs do not share a common host
                    if (targetHost != null && !targetHost.equals(host)) {
                        log.debug("Positive affinity rule violated by VM {} running at {} when other VM(s) are at {}",
                                vm, host, targetHost);
                        broken.add(affinity);

                        if (mode.equals(FailMode.IMMEDIATELY)) {
                            return broken;
                        }
                    } else if (targetHost == null) {
                        targetHost = host;
                    }
                }
            }
        }

        return broken;
    }

    /**
     * Take the unified positive groups and check whether a conflict exists between positive
     * and negative VM affinity.
     *
     * @param affinityGroups - All affinity groups
     * @param unifiedPositiveGroups - Computed unified groups of positive affinities
     * @return true when a conflict was detected, false otherwise
     */
    protected boolean checkForAffinityGroupConflict(Iterable<AffinityGroup> affinityGroups,
            Set<Set<Guid>> unifiedPositiveGroups) {
        for(AffinityGroup ag : affinityGroups) {
            if(ag.isPositive()) {
                continue;
            }

            for (Set<Guid> positiveGroup : unifiedPositiveGroups) {
                Set<Guid> intersection = new HashSet<>(ag.getEntityIds());
                intersection.retainAll(positiveGroup);

                if(intersection.size() > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Take all positive affinity groups and merge all groups that reference the same VM
     * together.
     *
     * Ex. groups A+B, B+C, D+E, A+D, F+G are in fact just two bigger groups:
     *     A+B+C+D+E and F+G
     *
     * The algorithm starts by creating single element groups from all VMs
     * It then goes through all affinity groups and merges all VM groups that contain
     * VMs from the currently processed AffinityGroup.
     */
    protected Set<Set<Guid>> getUnifiedPositiveAffinityGroups(List<AffinityGroup> affinityGroups) {
        Set<Set<Guid>> uag = new HashSet<>();
        Map<Guid, Set<Guid>> vmIndex = new HashMap<>();

        // Initialize the single element groups by taking all VMs that are referenced
        // from any affinity group
        for(AffinityGroup ag: affinityGroups) {
            for(Guid id : ag.getEntityIds()) {
                Set<Guid> temp = new HashSet<>();
                temp.add(id);
                uag.add(temp);
                vmIndex.put(id, temp);
            }
        }

        // Go through each positive affinity group and merge all existing groups
        // that contain the referenced VMs into one.
        for(AffinityGroup ag : affinityGroups) {
            if(ag.isPositive()) {
                Set<Guid> mergedGroup = new HashSet<>();

                for(Guid id : ag.getEntityIds()) {
                    // Get the current groups VM(id) belongs to
                    Set<Guid> existingGroup = vmIndex.get(id);

                    // Merge it with the currently computed mergeGroup
                    mergedGroup.addAll(existingGroup);

                    // And remove it from the valid groups
                    // (it will be re-added as part of a bigger group)
                    uag.remove(existingGroup);

                    // Update the per-VM index
                    for (Guid vm: existingGroup) {
                        vmIndex.put(vm, mergedGroup);
                    }
                }

                uag.add(mergedGroup);
            }
        }

        return uag;
    }

    /**
     * Convert affinity group sets to a proper AffinityGroup objects
     */
    static private List<AffinityGroup> setsToAffinityGroups(Set<Set<Guid>> uag) {
        List<AffinityGroup> output = new ArrayList<>();

        for(Set<Guid> s : uag) {
            AffinityGroup temp = new AffinityGroup();
            temp.setPositive(true);
            List<Guid> entities = new ArrayList<>();

            entities.addAll(s);
            temp.setEntityIds(entities);
            output.add(temp);
        }

        return output;
    }

    public List<AffinityGroup> getAllAffinityGroups() {
        return affinityGroupDao.getAllAffinityGroupsByClusterId(clusterId);
    }

    public void updateMigrationStatus(VdcReturnValueBase migrationStatus) {
        lastMigrations.get(0).setMigrationReturnValue(migrationStatus);
    }

    private static class AffinityGroupComparator implements Comparator<AffinityGroup>, Serializable {
        @Override
        public int compare(AffinityGroup o1, AffinityGroup o2) {
            return Integer.compare(o1.getEntityIds().size(), o2.getEntityIds().size());
        }
    }
}

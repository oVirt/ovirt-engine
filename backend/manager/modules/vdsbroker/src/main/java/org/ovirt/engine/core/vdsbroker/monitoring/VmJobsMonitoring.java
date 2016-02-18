package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.VmJobDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmJobsMonitoring {

    @Inject
    private VmJobDao vmJobDao;

    private static final Logger log = LoggerFactory.getLogger(VmJobsMonitoring.class);

    void process(Map<Guid, List<VmJob>> vmIdToVmJobs) {
        if (vmIdToVmJobs.isEmpty()) {
            return;
        }
        Map<Guid, VmJob> vmJobsToUpdate = new HashMap<>();
        List<Guid> vmJobIdsToRemove = new ArrayList<>();
        vmIdToVmJobs.entrySet().forEach(entry -> updateVmJobs(
                entry.getKey(), entry.getValue(), vmJobsToUpdate, vmJobIdsToRemove));
        saveVmJobsToDb(vmJobsToUpdate.values(), vmJobIdsToRemove);
    }

    void updateVmJobs(
            Guid vmId,
            List<VmJob> vmJobs,
            Map<Guid, VmJob> vmJobsToUpdate,
            List<Guid> vmJobIdsToRemove) {
        if (vmJobs == null) {
            // If no vmJobs key was returned, we can't presume anything about the jobs; save them all
            log.debug("No vmJob data returned from VDSM, preserving existing jobs");
            return;
        }
        List<Guid> existingVmJobIds = vmJobDao.getAllIds();

        // Only jobs that were in the DB before our update may be updated/removed;
        // others are completely ignored for the time being
        Map<Guid, VmJob> jobsFromDb = vmJobDao.getAllForVm(vmId).stream()
                .filter(job -> existingVmJobIds.contains(job.getId()))
                .collect(Collectors.toMap(VmJob::getId, Function.identity()));

        Set<Guid> vmJobIdsToIgnore = new HashSet<>();
        vmJobs.stream()
        .filter(job -> jobsFromDb.containsKey(job.getId()))
        .forEach(job -> {
            if (jobsFromDb.get(job.getId()).equals(job)) {
                // Same data, no update needed.  It would be nice if a caching
                // layer would take care of this for us.
                vmJobIdsToIgnore.add(job.getId());
                log.info("VM job '{}': In progress (no change)", job.getId());
            } else {
                vmJobsToUpdate.put(job.getId(), job);
                log.info("VM job '{}': In progress, updating", job.getId());
            }
        });

        // Any existing jobs not saved need to be removed
        jobsFromDb.keySet().stream()
        .filter(jobId -> !vmJobsToUpdate.containsKey(jobId) && !vmJobIdsToIgnore.contains(jobId))
        .forEach(jobId -> {
            vmJobIdsToRemove.add(jobId);
            log.info("VM job '{}': Deleting", jobId);
        });
    }

    void saveVmJobsToDb(Collection<VmJob> vmJobsToUpdate, List<Guid> vmJobIdsToRemove) {
        vmJobDao.updateAllInBatch(vmJobsToUpdate);

        if (!vmJobIdsToRemove.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                vmJobDao.removeAll(vmJobIdsToRemove);
                return null;
            });
        }
    }
}

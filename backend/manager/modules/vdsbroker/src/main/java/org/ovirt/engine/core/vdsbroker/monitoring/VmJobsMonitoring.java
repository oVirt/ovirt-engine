package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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

    void process(Map<Guid, List<VmJob>> vmIdToJobs) {
        if (vmIdToJobs.isEmpty()) {
            return;
        }
        Map<Guid, VmJob> jobsToUpdate = new HashMap<>();
        List<Guid> jobIdsToRemove = new ArrayList<>();
        vmIdToJobs.entrySet().forEach(entry -> processVmJobs(
                entry.getKey(), entry.getValue(), jobsToUpdate, jobIdsToRemove));
        updateJobs(jobsToUpdate.values());
        removeJobs(jobIdsToRemove);
    }

    private void processVmJobs(Guid vmId, List<VmJob> jobs, Map<Guid, VmJob> jobsToUpdate, List<Guid> jobIdsToRemove) {
        if (jobs == null) {
            // If no vmJobs key was returned, we can't presume anything about the jobs; save them all
            log.debug("No vmJob data returned from VDSM, preserving existing jobs");
            return;
        }

        // Only jobs that were in the DB before our update may be updated/removed;
        // others are completely ignored for the time being
        List<VmJob> jobsInDb = getExistingJobsForVm(vmId);
        Map<Guid, VmJob> jobIdToJobInDb = jobsInDb.stream().collect(toMap(VmJob::getId, Function.identity()));

        Set<Guid> vmJobIdsToIgnore = new HashSet<>();
        jobs.stream()
        .filter(job -> jobIdToJobInDb.containsKey(job.getId()))
        .forEach(job -> {
            if (jobIdToJobInDb.get(job.getId()).equals(job)) {
                // Same data, no update needed.  It would be nice if a caching
                // layer would take care of this for us.
                vmJobIdsToIgnore.add(job.getId());
                log.info("VM job '{}': In progress (no change)", job.getId());
            } else {
                jobsToUpdate.put(job.getId(), job);
                log.info("VM job '{}': In progress, updating", job.getId());
            }
        });

        // Any existing jobs not saved need to be removed
        jobIdToJobInDb.keySet().stream()
        .filter(jobId -> !jobsToUpdate.containsKey(jobId) && !vmJobIdsToIgnore.contains(jobId))
        .forEach(jobId -> {
            jobIdsToRemove.add(jobId);
            log.info("VM job '{}': Deleting", jobId);
        });
    }

    List<VmJob> getExistingJobsForVm(Guid vmId) {
        return vmJobDao.getAllForVm(vmId);
    }

    void updateJobs(Collection<VmJob> vmJobsToUpdate) {
        vmJobDao.updateAllInBatch(vmJobsToUpdate);
    }

    void removeJobs(List<Guid> vmJobIdsToRemove) {
        if (!vmJobIdsToRemove.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                vmJobDao.removeAll(vmJobIdsToRemove);
                return null;
            });
        }
    }
}

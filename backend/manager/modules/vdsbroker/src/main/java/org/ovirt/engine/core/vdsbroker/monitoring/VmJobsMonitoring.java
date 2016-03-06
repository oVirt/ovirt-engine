package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        List<VmJob> jobsToUpdate = new ArrayList<>();
        List<Guid> jobIdsToRemove = new ArrayList<>();
        vmIdToJobs.entrySet().forEach(entry -> processVmJobs(
                entry.getKey(), entry.getValue(), jobsToUpdate, jobIdsToRemove));
        updateJobs(jobsToUpdate);
        removeJobs(jobIdsToRemove);
    }

    private void processVmJobs(Guid vmId, List<VmJob> jobs, List<VmJob> jobsToUpdate, List<Guid> jobIdsToRemove) {
        if (jobs == null) {
            // If no vmJobs key was returned, we can't presume anything about the jobs; save them all
            log.debug("No vmJob data returned from VDSM, preserving existing jobs");
            return;
        }

        Map<Guid, VmJob> jobIdToReportedJob = jobs.stream().collect(toMap(VmJob::getId, identity()));
        getExistingJobsForVm(vmId).forEach(job -> {
            VmJob reportedJob = jobIdToReportedJob.get(job.getId());
            if (reportedJob != null) {
                if (reportedJob.equals(job)) {
                    // Same data, no update needed.  It would be nice if a caching
                    // layer would take care of this for us.
                    log.info("VM job '{}': In progress (no change)", job.getId());
                } else {
                    jobsToUpdate.add(reportedJob);
                    log.info("VM job '{}': In progress, updating", job.getId());
                }
            } else {
                jobIdsToRemove.add(job.getId());
                log.info("VM job '{}': Deleting", job.getId());
            }
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

package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.qualifiers.VmDeleted;
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
    private final Map<Guid, VmJob> jobsRepository;

    private static final Logger log = LoggerFactory.getLogger(VmJobsMonitoring.class);

    private VmJobsMonitoring() {
        jobsRepository = new ConcurrentHashMap<>();
    }

    @PostConstruct
    void init() {
        jobsRepository.putAll(getVmJobDao().getAll().stream().collect(toMap(VmJob::getId, identity())));
    }

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
        return jobsRepository.values().stream().filter(job -> job.getVmId().equals(vmId)).collect(toList());
    }

    void updateJobs(Collection<VmJob> vmJobsToUpdate) {
        getVmJobDao().updateAllInBatch(vmJobsToUpdate);
        vmJobsToUpdate.forEach(job -> jobsRepository.put(job.getId(), job));
    }

    void removeJobs(List<Guid> vmJobIdsToRemove) {
        removeJobsFromDb(vmJobIdsToRemove);
        vmJobIdsToRemove.forEach(jobsRepository::remove);
    }

    void removeJobsFromDb(List<Guid> vmJobIdsToRemove) {
        if (!vmJobIdsToRemove.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                getVmJobDao().removeAll(vmJobIdsToRemove);
                return null;
            });
        }
    }

    public void addJob(VmJob job) {
        getVmJobDao().save(job);
        jobsRepository.put(job.getId(), job);
        log.info("Stored placeholder for job id '{}'", job.getId());
    }

    void onVmDelete(@Observes @VmDeleted Guid vmId) {
        jobsRepository.values().stream()
        .filter(job -> job.getVmId().equals(vmId))
        .map(VmJob::getId)
        .forEach(jobsRepository::remove);
    }

    VmJobDao getVmJobDao() {
        return vmJobDao;
    }
}

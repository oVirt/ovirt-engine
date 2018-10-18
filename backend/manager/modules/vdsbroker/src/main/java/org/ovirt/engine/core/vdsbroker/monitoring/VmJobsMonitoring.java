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

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.qualifiers.VmDeleted;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmJobDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmJobsMonitoring {

    @Inject
    private VmJobDao vmJobDao;
    @Inject
    private VmDynamicDao vmDynamicDao;

    private final Map<Guid, VmJob> jobsRepository;

    private static final Logger log = LoggerFactory.getLogger(VmJobsMonitoring.class);

    private VmJobsMonitoring() {
        jobsRepository = new ConcurrentHashMap<>();
    }

    @PostConstruct
    void init() {
        jobsRepository.putAll(getVmJobDao().getAll().stream().collect(toMap(VmJob::getId, identity())));
        removeJobsByVmIds(getIdsOfDownVms());
    }

    void process(Map<Guid, List<VmJob>> vmIdToJobs, long vmStatsFetchTime) {
        if (vmIdToJobs.isEmpty()) {
            return;
        }

        List<VmJob> jobsToUpdate = new ArrayList<>();
        List<VmJob> jobsToRemove = new ArrayList<>();
        vmIdToJobs.entrySet().forEach(entry -> processVmJobs(
                entry.getKey(), entry.getValue(), jobsToUpdate, jobsToRemove, vmStatsFetchTime));
        updateJobs(jobsToUpdate);
        removeJobs(jobsToRemove);
    }

    private void processVmJobs(Guid vmId, List<VmJob> jobs, List<VmJob> jobsToUpdate, List<VmJob> jobsToRemove, long vmStatsFetchTime) {
        if (jobs == null) {
            // If no vmJobs key was returned, we can't presume anything about the jobs; save them all
            log.debug("No vmJob data returned from VDSM, preserving existing jobs");
            return;
        }

        Map<Guid, VmJob> jobIdToReportedJob = jobs.stream().collect(toMap(VmJob::getId, identity()));
        getExistingJobsForVm(vmId).forEach(job -> {
            // We need to make sure the VM data was fetched after the job was registered in the
            // repository
            if (vmStatsFetchTime - job.getStartTime() <= 0) {
                log.info("The reported data is not current and should not be used to monitor job {}, ", job.getId());
                return;
            }

            VmJob reportedJob = jobIdToReportedJob.get(job.getId());
            if (reportedJob != null) {
                if (reportedJob.equals(job)) {
                    // Same data, no update needed.  It would be nice if a caching
                    // layer would take care of this for us.
                    log.info("{}: In progress (no change)", job);
                } else {
                    jobsToUpdate.add(reportedJob);
                    log.info("{}: In progress, updating", job);
                }
            } else {
                jobsToRemove.add(job);
                log.info("{}: Deleting", job);
            }
        });
    }

    List<VmJob> getExistingJobsForVm(Guid vmId) {
        return jobsRepository.values().stream()
            .filter(job -> job.getVmId().equals(vmId))
            .collect(toList());
    }

    void updateJobs(Collection<VmJob> jobsToUpdate) {
        getVmJobDao().updateAllInBatch(jobsToUpdate);
        jobsToUpdate.forEach(job -> jobsRepository.put(job.getId(), job));
    }

    void removeJobs(List<VmJob> jobsToRemove) {
        getVmJobDao().removeAllInBatch(jobsToRemove);
        jobsToRemove.stream().map(VmJob::getId).forEach(jobsRepository::remove);
    }

    public void removeJobsByVmIds(Collection<Guid> vmIds) {
        List<VmJob> jobsToRemove = jobsRepository.values().stream()
                .filter(job -> vmIds.contains(job.getVmId()))
                .collect(toList());
        removeJobs(jobsToRemove);
        jobsToRemove.forEach(job -> log.info("{} was removed, VM is down", job));
    }

    public void addJob(VmJob job) {
        getVmJobDao().save(job);
        jobsRepository.put(job.getId(), job);
        log.info("Stored placeholder for {}", job);
    }

    public VmJob getJobById(Guid jobId) {
        return jobId != null ? jobsRepository.get(jobId) : null;
    }

    void onVmDelete(@Observes @VmDeleted Guid vmId) {
        removeJobs(jobsRepository.values().stream()
                .filter(job -> job.getVmId().equals(vmId))
                .collect(toList()));
    }

    VmJobDao getVmJobDao() {
        return vmJobDao;
    }

    List<Guid> getIdsOfDownVms() {
        return vmDynamicDao.getAll().stream()
                .filter(vm -> vm.getStatus() == VMStatus.Down)
                .map(VmDynamic::getId)
                .collect(toList());
    }
}

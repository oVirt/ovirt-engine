package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.LeaseStatus;
import org.ovirt.engine.core.common.businessentities.storage.LeaseJobStatus;
import org.ovirt.engine.core.common.vdscommands.GetLeaseStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ExternalLeasePoller {
    private static final Logger log = LoggerFactory.getLogger(ExternalLeasePoller.class);

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    private ExternalLeasePoller() {
    }

    protected HostJobInfo.HostJobStatus pollLease(Guid storagePoolId,
            Guid leaseDomainId,
            Guid diskLeaseId,
            Guid cmdId) {
        LeaseStatus leaseInfo = (LeaseStatus) vdsCommandsHelper.runVdsCommandWithFailover(
                VDSCommandType.GetLeaseStatus,
                new GetLeaseStatusVDSCommandParameters(storagePoolId, leaseDomainId, diskLeaseId),
                storagePoolId,
                commandCoordinatorUtil.retrieveCommand(cmdId))
                .getReturnValue();

        if (leaseInfo.isFree()) {
            if (leaseInfo.getJobStatus() == LeaseJobStatus.Succeeded) {
                log.info("Lease is free, host finished operation");
                return HostJobInfo.HostJobStatus.done;
            } else if (leaseInfo.getJobStatus() == LeaseJobStatus.Fenced
                    || leaseInfo.getJobStatus() == LeaseJobStatus.Failed) {
                log.info("Lease is free, and the job was fenced");
                return HostJobInfo.HostJobStatus.failed;
            }
        } else {
            log.info("Lease is not free, assuming job is still running");
            return HostJobInfo.HostJobStatus.pending;
        }

        return null;
    }
}

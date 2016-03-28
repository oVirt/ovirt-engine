package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StorageJobVdsCommandParameters extends StorageDomainVdsCommandParameters {
    private Guid jobId;

    public StorageJobVdsCommandParameters(Guid storageDomainId, Guid vdsId, Guid jobId) {
        super(storageDomainId, vdsId);
        this.jobId = jobId;
    }

    public StorageJobVdsCommandParameters(Guid storageDomainId, Guid jobId) {
        this(storageDomainId, null, jobId);
    }

    public StorageJobVdsCommandParameters(Guid storageDomainId) {
        this(storageDomainId, null, null);
    }

    public StorageJobVdsCommandParameters() {
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("jobId", getJobId());
    }
}

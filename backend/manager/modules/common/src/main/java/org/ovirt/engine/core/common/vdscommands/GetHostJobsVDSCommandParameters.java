package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetHostJobsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private List<Guid> jobIds;
    private HostJobType type;

    public GetHostJobsVDSCommandParameters(Guid vdsId, List<Guid> jobIds, HostJobType type) {
        super(vdsId);
        this.jobIds = jobIds;
        this.type = type;
    }

    public GetHostJobsVDSCommandParameters() {
    }

    public List<Guid> getJobIds() {
        return jobIds;
    }

    public void setJobIds(List<Guid> jobIds) {
        this.jobIds = jobIds;
    }

    public HostJobType getType() {
        return type;
    }

    public void setType(HostJobType type) {
        this.type = type;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("type", getType()).append("jobIds", getJobIds());
    }
}

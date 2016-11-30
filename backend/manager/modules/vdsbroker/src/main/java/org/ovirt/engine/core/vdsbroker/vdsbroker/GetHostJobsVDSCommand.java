package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.GetHostJobsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetHostJobsVDSCommand<P extends GetHostJobsVDSCommandParameters> extends VdsBrokerCommand<P> {
    private HostJobsReturn jobResult;

    public GetHostJobsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        List<String> jobIds = getParameters().getJobIds() == null ? null : getParameters().getJobIds().stream().map(e
                -> e.toString()).collect(Collectors.toList());
        jobResult = getBroker().getHostJobs(getParameters().getType().name(), jobIds);
        proceedProxyReturnValue();
        setReturnValue(parseHostJobs(jobResult.getHostJobsInfo()));
    }

    private HostJobInfo parseJob(Map<String, Object> job) {
        Guid id = Guid.createGuidFromString((String) job.get(VdsProperties.jobId));
        HostJobType type = HostJobType.valueOf((String) job.get(VdsProperties.jobType));
        HostJobStatus status = HostJobStatus.valueOf((String) job.get(VdsProperties.jobStatus));
        String description = (String) job.get(VdsProperties.jobDescription);
        Integer jobProgress = job.containsKey(VdsProperties.jobProgress) ?
                ((Double) job.get(VdsProperties.jobProgress)).intValue() : null;
        VDSError error = null;
        if (job.containsKey(VdsProperties.jobError)) {
            Map<String, Object> errorInfo = (Map<String, Object>) job.get(VdsProperties.jobError);
            Integer code = (Integer) errorInfo.get(VdsProperties.jobErrorCode);
            String message = (String) errorInfo.get(VdsProperties.jobErrorMessage);
            error = new VDSError(EngineError.forValue(code), message);
        }
        return new HostJobInfo(id, description, type,
                status, jobProgress, error);
    }

    public Map<Guid, HostJobInfo> parseHostJobs(Map<String, Object> jobsInfo) {
        return jobsInfo.values().stream()
                .map(x -> (Map<String, Object>) x)
                .map(this::parseJob)
                .collect(Collectors.toMap(HostJobInfo::getId, Function.identity()));
    }

    @Override
    protected Status getReturnStatus() {
        return jobResult.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return jobResult;
    }
}

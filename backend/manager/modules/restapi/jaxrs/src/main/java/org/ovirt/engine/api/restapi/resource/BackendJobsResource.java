package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.Jobs;
import org.ovirt.engine.api.resource.JobResource;
import org.ovirt.engine.api.resource.JobsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddExternalJobParameters;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendJobsResource extends AbstractBackendCollectionResource<Job, org.ovirt.engine.core.common.job.Job> implements JobsResource {

    public BackendJobsResource() {
        super(Job.class, org.ovirt.engine.core.common.job.Job.class);
    }

    @Override
    public Jobs list() {
        List<org.ovirt.engine.core.common.job.Job> jobs;
        if (isFiltered()) {
            jobs = getBackendCollection(QueryType.GetAllJobs, new QueryParametersBase());
        } else {
            jobs = getBackendCollection(SearchType.Job);
        }
        return mapCollection(jobs);
    }

    @Override
    public Response add(Job job) {
        validateParameters(job, "description");
        return performCreate(ActionType.AddExternalJob,
                new AddExternalJobParameters(job.getDescription(), job.isSetAutoCleared() ? job.isAutoCleared() : false),
                new QueryIdResolver<Guid>(QueryType.GetJobByJobId, IdQueryParameters.class));
    }

    @Override
    public JobResource getJobResource(String id) {
        return inject(new BackendJobResource(id));
    }

    protected Jobs mapCollection(List<org.ovirt.engine.core.common.job.Job> entities) {
        Jobs collection = new Jobs();
        for (org.ovirt.engine.core.common.job.Job entity : entities) {
            collection.getJobs().add(addLinks(map(entity)));
        }
        return collection;
    }
}

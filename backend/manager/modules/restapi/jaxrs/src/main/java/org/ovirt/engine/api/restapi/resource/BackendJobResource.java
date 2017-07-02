package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.JobResource;
import org.ovirt.engine.api.resource.StepsResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EndExternalJobParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendJobResource extends AbstractBackendActionableResource<Job, org.ovirt.engine.core.common.job.Job> implements JobResource{

    public BackendJobResource(String id) {
        super(id, Job.class, org.ovirt.engine.core.common.job.Job.class);
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public Response end(Action action) {
        EndExternalJobParameters parameters = new EndExternalJobParameters(
            guid,
            action.isSetSucceeded() ? action.isSucceeded() : true,
            action.isSetForce() ? action.isForce() : false
        );
        return doAction(ActionType.EndExternalJob, parameters, action);
    }

    @Override
    public Response clear(Action action) {
        ActionParametersBase params = new ActionParametersBase();
        params.setJobId(guid);
        return doAction(ActionType.ClearExternalJob, params, action);
    }

    @Override
    public Job get() {
        IdQueryParameters params =  new IdQueryParameters(guid);
        return performGet(QueryType.GetJobByJobId, params);
     }

    @Override
    public StepsResource getStepsResource() {
       return inject(new BackendStepsResource(guid));
    }
}

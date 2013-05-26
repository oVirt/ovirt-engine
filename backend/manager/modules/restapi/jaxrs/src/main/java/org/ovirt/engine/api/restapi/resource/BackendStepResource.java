package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.resource.StepResource;
import org.ovirt.engine.core.common.action.EndExternalStepParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStepResource extends AbstractBackendActionableResource<org.ovirt.engine.api.model.Step, org.ovirt.engine.core.common.job.Step> implements StepResource{

    private BackendStepsResource parent;

    public BackendStepsResource getParent() {
        return parent;
    }

    public void setParent(BackendStepsResource parent) {
        this.parent = parent;
    }

    public BackendStepResource(String id, BackendStepsResource parent) {
        super(id, org.ovirt.engine.api.model.Step.class, org.ovirt.engine.core.common.job.Step.class);
        this.setParent(parent);
    }

    @Override
    protected Step doPopulate(Step model, org.ovirt.engine.core.common.job.Step entity) {
        return model;
    }

    @Override
    public Response end(Action action) {
        return doAction(VdcActionType.EndExternalStep,
                new EndExternalStepParameters(guid, action.isSucceeded()), action);
    }

    @Override
    public Step get() {
        return parent.injectParent(getStepById(guid));
    }

    private Step getStepById(Guid id) {
        IdQueryParameters params =  new IdQueryParameters(id);
        Step step = performGet(VdcQueryType.GetStepByStepId, params);
        return step;
    }
}

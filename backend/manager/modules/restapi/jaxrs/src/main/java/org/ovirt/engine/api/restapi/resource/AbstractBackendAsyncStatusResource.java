package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendAsyncStatusResource<R extends BaseResource>
        extends AbstractBackendSubResource<R, List/*<AsyncTaskStatus>*/> {

    protected ArrayList<Guid> ids;

    public AbstractBackendAsyncStatusResource(Class<R> entityType, String ids) {
        super(Guid.Empty.toString(), entityType, List/*<AsyncTaskStatus>*/.class);
        this.ids = new ArrayList<>();
        for (String id : ids.split(ID_SEPARATOR)) {
            this.ids.add(asGuidOr404(id));
        }
    }

    protected R query() {
        // this query never fails, reporting unknown tasks as a success
        return performGet(QueryType.GetTasksStatusesByTasksIDs,
                          new GetTasksStatusesByTasksIDsParameters(ids));
    }

    @Override
    protected R addLinks(R model, Class<? extends BaseResource> suggestedParent, String... excludeSubCollectionMembers) {
        Current current = CurrentManager.get();
        String href = current.getRelativePath();
        model.setHref(href);
        return model;
    }

    protected void setReason(Fault fault) {
        fault.setReason(localize(Messages.ASYNCHRONOUS_TASK_FAILED));
    }
}

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.resource.StatisticResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendStatisticResource<R extends BaseResource, Q>
        extends AbstractBackendSubResource<Statistic, Q>
        implements StatisticResource {

    private final Guid subjectId;
    private AbstractStatisticalQuery<R, Q> query;

    protected BackendStatisticResource(String id, Class<Q> entityType, Guid subjectId, AbstractStatisticalQuery<R, Q> query) {
        super(id, Statistic.class, entityType);
        this.query = query;
        this.subjectId = subjectId;
    }

    void setQuery(AbstractStatisticalQuery<R, Q> query) {
        this.query = query;
    }

    @Override
    public Statistic get() {
        try {
            Q entity = query.resolve(subjectId);
            List<Statistic> currentStats = query.getStatistics(entity);
            for (Statistic statistic : currentStats) {
                if (id.equals(statistic.getId())) {
                    return addLinks(statistic, query.getParentType());
                }
            }
        } catch (BackendFailureException bfe) {
            return handleError(bfe, false);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}

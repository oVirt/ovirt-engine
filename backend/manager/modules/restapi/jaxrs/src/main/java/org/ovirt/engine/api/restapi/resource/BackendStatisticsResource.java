package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.StatisticResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendStatisticsResource<R extends BaseResource, Q>
        extends AbstractBackendCollectionResource<Statistic, Q>
        implements StatisticsResource {

    private Guid subjectId;
    private AbstractStatisticalQuery<R, Q> query;

    public BackendStatisticsResource(Class<Q> entityType, Guid subjectId, AbstractStatisticalQuery<R, Q> query) {
        super(Statistic.class, entityType);
        this.query = query;
        this.subjectId = subjectId;
    }

    void setQuery(AbstractStatisticalQuery<R, Q> query) {
        this.query = query;
    }

    AbstractStatisticalQuery<R, Q> getQuery() {
        return query;
    }

    @Override
    public StatisticResource getStatisticResource(String id) {
        return inject(new BackendStatisticResource<>(id, entityType, subjectId, query));
    }

    @Override
    public Statistics list() {
        try {
            Statistics statistics = new Statistics();
            Q entity = query.resolve(subjectId);
            if (entity != null) {
                List<Statistic> currentStats = query.getStatistics(entity);
                for (Statistic statistic : currentStats) {
                    addLinks(statistic, query.getParentType());
                }
                statistics.getStatistics().addAll(currentStats);
            }
            return statistics;
        } catch (Exception e) {
            return handleError(e, false);
        }
    }
}

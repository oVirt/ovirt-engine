package org.ovirt.engine.api.restapi.resource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticKind;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource.BackendFailureException;
import org.ovirt.engine.api.restapi.utils.StatisticResourceUtils;
import org.ovirt.engine.core.compat.Guid;

/**
 * Subclasses encapsulate the subject-specific aspects of a statistical query
 */
public abstract class AbstractStatisticalQuery<R extends BaseResource, E> {

    protected static final long Kb = 1024L;
    protected static final long Mb = 1024 * Kb;
    protected static final BigDecimal CENT = new BigDecimal(100);
    protected static final StatisticKind GAUGE = StatisticKind.GAUGE;
    protected static final StatisticKind COUNTER = StatisticKind.COUNTER;
    protected static final StatisticUnit NONE = StatisticUnit.NONE;
    protected static final StatisticUnit PERCENT = StatisticUnit.PERCENT;
    protected static final StatisticUnit BYTES = StatisticUnit.BYTES;
    protected static final StatisticUnit BYTES_PER_SECOND = StatisticUnit.BYTES_PER_SECOND;
    protected static final StatisticUnit BITS_PER_SECOND = StatisticUnit.BITS_PER_SECOND;
    protected static final StatisticUnit SECONDS = StatisticUnit.SECONDS;
    protected static final ValueType INTEGER = ValueType.INTEGER;
    protected static final ValueType DECIMAL = ValueType.DECIMAL;
    protected static final ValueType STRING = ValueType.STRING;

    protected Class<R> parentType;
    protected R parent;
    protected AbstractBackendResource<R, E>.EntityIdResolver<Guid> entityResolver;

    public AbstractStatisticalQuery(Class<R> parentType,
                                    R parent,
                                    AbstractBackendResource<R, E>.EntityIdResolver<Guid> entityResolver) {
        this.parentType = parentType;
        this.parent = parent;
        this.entityResolver = entityResolver;
    }

    public Class<R> getParentType() {
        return parentType;
    }

    public E resolve(Guid id) throws BackendFailureException {
        return entityResolver.resolve(id);
    }

    public Statistic setDatum(Statistic statistic, BigDecimal datum) {
        return StatisticResourceUtils.setDatum(statistic, datum);
    }

    public Statistic setDatum(Statistic statistic, String datum) {
        return StatisticResourceUtils.setDatum(statistic, datum);
    }

    public Statistic setDatum(Statistic statistic, Long datum) {
        return StatisticResourceUtils.setDatum(statistic, datum);
    }

    public Statistic setDatum(Statistic statistic, Double datum) {
        return StatisticResourceUtils.setDatum(statistic, datum);
    }

    public Statistic setDatum(Statistic statistic, long datum) {
        return StatisticResourceUtils.setDatum(statistic, datum);
    }

    public Statistic setDatum(Statistic statistic, double datum) {
        return StatisticResourceUtils.setDatum(statistic, datum);
    }

    public Statistic setDatum(Statistic statistic, BigInteger datum) {
        return StatisticResourceUtils.setDatum(statistic, datum);
    }

    public abstract List<Statistic> getStatistics(E entity);

    public abstract Statistic adopt(Statistic statistic);

    public static Statistic create(String name,
                                   String description,
                                   StatisticKind kind,
                                   StatisticUnit unit,
                                   ValueType valueType) {
        return StatisticResourceUtils.create(name, description, kind, unit, valueType);
    }

    public static Statistic clone(Statistic s) {
        return create(s.getName(), s.getDescription(), s.getKind(), s.getUnit(), s.getType());
    }


    public List<Statistic> asList(Statistic...statistics) {
        List<Statistic> list = new ArrayList<>();
        for (Statistic statistic : statistics) {
            list.add(adopt(statistic));
        }
        return list;
    }
}

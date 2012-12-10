package org.ovirt.engine.api.restapi.resource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticType;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.Value;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.model.Values;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource.BackendFailureException;

/**
 * Subclasses encapsulate the subject-specific aspects of a statistical query
 */
public abstract class AbstractStatisticalQuery<R extends BaseResource, E> {

    protected static final long Mb = 1024 * 1024L;
    protected static final BigDecimal CENT = new BigDecimal(100);
    protected static final StatisticType GAUGE = StatisticType.GAUGE;
    protected static final StatisticType COUNTER = StatisticType.COUNTER;
    protected static final StatisticUnit NONE = StatisticUnit.NONE;
    protected static final StatisticUnit PERCENT = StatisticUnit.PERCENT;
    protected static final StatisticUnit BYTES = StatisticUnit.BYTES;
    protected static final StatisticUnit BYTES_PER_SECOND = StatisticUnit.BYTES_PER_SECOND;
    protected static final StatisticUnit BITS_PER_SECOND = StatisticUnit.BITS_PER_SECOND;
    protected static final ValueType INTEGER = ValueType.INTEGER;
    protected static final ValueType DECIMAL = ValueType.DECIMAL;

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
        statistic.getValues().getValues().get(0).setDatum(datum);
        return statistic;
    }

    public Statistic setDatum(Statistic statistic, long datum) {
        return setDatum(statistic, new BigDecimal(datum));
    }

    public Statistic setDatum(Statistic statistic, double datum) {
        return setDatum(statistic, new BigDecimal(datum, new MathContext(2)));
    }

    public abstract List<Statistic> getStatistics(E entity);

    public abstract Statistic adopt(Statistic statistic);

    public static Statistic create(String name,
                                   String description,
                                   StatisticType type,
                                   StatisticUnit unit,
                                   ValueType valueType) {
        Statistic statistic = new Statistic();
        statistic.setId(asId(name));
        statistic.setName(name);
        statistic.setDescription(description);
        statistic.setType(type);
        statistic.setUnit(unit);
        statistic.setValues(new Values());
        statistic.getValues().setType(valueType);
        statistic.getValues().getValues().add(new Value());
        return statistic;
    }

    public static Statistic clone(Statistic s) {
        return create(s.getName(), s.getDescription(), s.getType(), s.getUnit(), s.getValues().getType());
    }

    public static String asId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    public List<Statistic> asList(Statistic...statistics) {
        List<Statistic> list = new ArrayList<Statistic>();
        for (Statistic statistic : statistics) {
            list.add(adopt(statistic));
        }
        return list;
    }
}

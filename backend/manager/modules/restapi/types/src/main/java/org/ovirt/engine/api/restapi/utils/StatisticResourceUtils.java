package org.ovirt.engine.api.restapi.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.UUID;

import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticType;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.Value;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.model.Values;

public class StatisticResourceUtils {

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

    private static String asId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    public static Statistic setDatum(Statistic statistic, BigDecimal datum) {
        statistic.getValues().getValues().get(0).setDatum(datum);
        return statistic;
    }

    public static Statistic setDatum(Statistic statistic, String datum) {
        statistic.getValues().getValues().get(0).setDetail(datum);
        return statistic;
    }

    public static Statistic setDatum(Statistic statistic, Long datum) {
        return setDatum(statistic, datum == null ? null : new BigDecimal(datum));
    }

    public static Statistic setDatum(Statistic statistic, Double datum) {
        return setDatum(statistic, datum == null ? null : new BigDecimal(datum, new MathContext(2)));
    }

    public static Statistic setDatum(Statistic statistic, long datum) {
        return setDatum(statistic, new BigDecimal(datum));
    }

    public static Statistic setDatum(Statistic statistic, double datum) {
        return setDatum(statistic, new BigDecimal(datum, new MathContext(2)));
    }

}

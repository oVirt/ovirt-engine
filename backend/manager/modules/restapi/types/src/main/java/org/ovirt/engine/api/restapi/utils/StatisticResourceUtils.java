package org.ovirt.engine.api.restapi.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.UUID;

import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticKind;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.Value;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.model.Values;

public class StatisticResourceUtils {

    public static Statistic create(String name,
            String description,
            StatisticKind kind,
            StatisticUnit unit,
            ValueType valueType) {
        Statistic statistic = new Statistic();
        statistic.setId(asId(name));
        statistic.setName(name);
        statistic.setDescription(description);
        statistic.setKind(kind);
        statistic.setUnit(unit);
        statistic.setValues(new Values());
        statistic.setType(valueType);
        return statistic;
    }

    private static String asId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    public static Statistic setDatum(Statistic statistic, BigDecimal datum) {
        if (datum != null) {
            Value value = new Value();
            value.setDatum(datum);
            statistic.getValues().getValues().add(value);
        }
        return statistic;
    }

    public static Statistic setDatum(Statistic statistic, String datum) {
        if (datum != null && !datum.isEmpty()) {
            Value value = new Value();
            value.setDetail(datum);
            statistic.getValues().getValues().add(value);
        }
        return statistic;
    }

    public static Statistic setDatum(Statistic statistic, Long datum) {
        if (datum != null) {
            return setDatum(statistic, new BigDecimal(datum));
        }
        return statistic;
    }

    public static Statistic setDatum(Statistic statistic, BigInteger datum) {
        if (datum != null) {
            return setDatum(statistic, new BigDecimal(datum));
        }
        return statistic;
    }

    public static Statistic setDatum(Statistic statistic, Double datum) {
        if (datum != null) {
            return setDatum(statistic, new BigDecimal(datum, new MathContext(2)));
        }
        return statistic;
    }

    public static Statistic setDatum(Statistic statistic, long datum) {
        return setDatum(statistic, new BigDecimal(datum));
    }

    public static Statistic setDatum(Statistic statistic, double datum) {
        return setDatum(statistic, new BigDecimal(datum, new MathContext(2)));
    }

}

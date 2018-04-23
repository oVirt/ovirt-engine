package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DayOfWeek;

public class DateEnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    private Map<String, Integer> enumValues = new HashMap<>();

    public <E extends Enum<? extends E> & Identifiable> DateEnumValueAutoCompleter(Class<E> enumerationType) {
        super();
        for (E val : enumerationType.getEnumConstants()) {
            String ValName = val.name().toUpperCase();
            enumValues.put(ValName, val.getValue());
            verbs.add(ValName);
        }
        addDaysOfWeek();
        buildCompletions();
    }

    private void addDaysOfWeek() {
        String day;
        int dayIndex = 3;
        for (int i = -2; i > -8; i--) {
            day = DateTime.getNow().addDays(i).getDayOfWeek().toString();
            enumValues.put(day, dayIndex);
            verbs.add(day);
            dayIndex++;
        }

    }

    @Override
    public String convertFieldEnumValueToActualValue(String fieldValue) {
        if (DateUtils.parse(fieldValue) != null) {
            return fieldValue;
        }

        // check enum values
        if (enumValues.containsKey(fieldValue.toUpperCase())) {
            return enumValues.get(fieldValue.toUpperCase()).toString();
        }

        // check for rest of the week
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.toString().equalsIgnoreCase(fieldValue)) {
                return day.toString();
            }
        }

        return "";
    }
}

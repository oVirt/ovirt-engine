package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Identifiable;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DayOfWeek;

public class DateEnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    private HashMap<String, Integer> mEnumValues = new HashMap<String, Integer>();

    public <E extends Enum<? extends E> & Identifiable> DateEnumValueAutoCompleter(Class<E> enumerationType) {
        super();
        for (E val : enumerationType.getEnumConstants()) {
            String ValName = val.name().toUpperCase();
            mEnumValues.put(ValName, val.getValue());
            mVerbs.add(ValName);
        }
        AddDaysOfWeek();
        buildCompletions();
    }

    private void AddDaysOfWeek() {
        String day;
        int dayIndex = 3;
        for (int i = -2; i > -8; i--) {
            day = DateTime.getNow().addDays(i).getDayOfWeek().toString();
            mEnumValues.put(day, dayIndex);
            mVerbs.add(day);
            dayIndex++;
        }

    }

    @Override
    public String convertFieldEnumValueToActualValue(String fieldValue) {
        if (DateUtils.parse(fieldValue) != null) {
            return fieldValue;
        }

        // check enum values
        if (mEnumValues.containsKey(fieldValue.toUpperCase())) {
            return mEnumValues.get(fieldValue.toUpperCase()).toString();
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

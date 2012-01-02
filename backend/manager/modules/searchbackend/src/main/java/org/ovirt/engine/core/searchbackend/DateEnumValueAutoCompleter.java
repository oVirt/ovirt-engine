package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.core.compat.EnumCompat;
import org.ovirt.engine.core.compat.StringHelper;

public class DateEnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    private java.util.HashMap<String, Integer> mEnumValues = new java.util.HashMap<String, Integer>();

    public DateEnumValueAutoCompleter(Class enumerationType) {
        super();
        for (int val : EnumCompat.GetIntValues(enumerationType)) {
            String ValName = EnumCompat.GetName(enumerationType, val).toUpperCase();
            mEnumValues.put(ValName, val);
            mVerbs.put(ValName, ValName);
        }
        AddDaysOfWeek();
        buildCompletions();
    }

    private void AddDaysOfWeek() {
        String day;
        int dayIndex = 3;
        for (int i = -2; i > -8; i--) {
            day = DateTime.getNow().AddDays(i).getDayOfWeek().toString();
            mEnumValues.put(day, dayIndex);
            mVerbs.put(day, day);
            dayIndex++;
        }

    }

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
            if (StringHelper.EqOp(fieldValue.toUpperCase(), day.toString().toUpperCase())) {
                return day.toString();
            }
        }

        return "";
    }
}

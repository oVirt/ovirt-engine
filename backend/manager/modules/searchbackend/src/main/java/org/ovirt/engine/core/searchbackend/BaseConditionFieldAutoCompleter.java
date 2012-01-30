package org.ovirt.engine.core.searchbackend;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.*;

/**
 * A base class for all condition field auto completers
 */
public class BaseConditionFieldAutoCompleter extends BaseAutoCompleter implements IConditionFieldAutoCompleter {

    public static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    protected final Map<String, List<valueValidationFunction>> mValidationDict =
            new HashMap<String, List<valueValidationFunction>>();
    private Map<String, Class<?>> mTypeDict = new HashMap<String, Class<?>>();
    protected Map<String, String> mColumnNameDict = new HashMap<String, String>();
    protected List<String> mNotFreeTextSearchableFieldsList = new java.util.ArrayList<String>();

    /**
     * Gets the LIKE clause syntax for non case-sensitive search
     * @return the LIKE syntax according to current DBEngine.
     */
    public static String getLikeSyntax(boolean caseSensitive) {
        // for tests we don't have the Config class initialized
        // also if caseSensitive flag is set we will use LIKE
        if (Config.getConfigUtils() == null || caseSensitive)
            return "LIKE";
        else
            return Config.<String> GetValue(ConfigValues.DBLikeSyntax);
    }

    /**
     * Gets the I18N prefix used for value compare.
     * @return
     */
    public static String getI18NPrefix() {
        // for tests we don't have the Config class initialized
        if (Config.getConfigUtils() == null)
            return "";
        else
            return Config.<String> GetValue(ConfigValues.DBI18NPrefix);

    }

    public static ITagsHandler TagsHandler;

    public Map<String, Class<?>> getTypeDictionary() {
        return mTypeDict;
    }

    protected void buildBasicValidationTable() {
        valueValidationFunction charValidation = validCahracters;
        valueValidationFunction intValidationFunc = validInteger;
        valueValidationFunction decimalValidationFunction = validDecimal;
        valueValidationFunction dateValidationFunc = validDateTime;
        valueValidationFunction timeSpanValidationFunc = validTimeSpan;
        valueValidationFunction valueValidationFunc = validateFieldValueByValueAC;
        valueValidationFunction dateEnumValidationFunc = validateDateEnumValueByValueAC;

        for (String key : mVerbs.keySet()) {
            List<valueValidationFunction> curList = new java.util.ArrayList<valueValidationFunction>();
            Class<?> curType = mTypeDict.get(key);
            if (curType == java.math.BigDecimal.class) {
                curList.add(decimalValidationFunction);
            } else if (curType == Integer.class) {
                curList.add(intValidationFunc);
            } else if (curType == java.util.Date.class) {
                curList.add(dateValidationFunc);
            } else if (curType == TimeSpan.class) {
                curList.add(timeSpanValidationFunc);
            } else {
                curList.add(charValidation);
            }
            IConditionValueAutoCompleter tmp = getFieldValueAutoCompleter(key);
            if (tmp != null) {
                if (tmp.getClass() == DateEnumValueAutoCompleter.class) {
                    curList.add(dateEnumValidationFunc);
                } else {
                    curList.add(valueValidationFunc);
                }
            }
            mValidationDict.put(key, curList);
        }
    }

    public boolean validateFieldValue(String fieldName, String fieldValue) {
        if (mValidationDict.containsKey(fieldName)) {
            List<valueValidationFunction> validationList = mValidationDict.get(fieldName);
            for (valueValidationFunction curValidationFunc : validationList) {
                if (!curValidationFunc.invoke(fieldName, fieldValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getDbFieldName(String fieldName) {
        String retval = null;
        if (mColumnNameDict.containsKey(fieldName)) {
            retval = mColumnNameDict.get(fieldName);
        }
        return retval;
    }

    public Class<?> getDbFieldType(String fieldName) {
        Class<?> retval = null;
        if (mTypeDict.containsKey(fieldName)) {
            retval = mTypeDict.get(fieldName);
        }
        return retval;

    }

    // FIXME Probably Not Hibernate Friendly
    public String buildFreeTextConditionSql(String tableName, String relations, String value, boolean caseSensitive) {
        StringBuilder sb = new StringBuilder(" ( ");
        boolean firstTime = true;
        if (!StringHelper.isNullOrEmpty(value) && !StringHelper.EqOp(value, "''")) {
            value = StringFormat.format(getI18NPrefix() + "'%%%1$s%%'", StringHelper.trim(value, '\''));

        }
        if (StringHelper.EqOp(relations, "=")) {
            relations = getLikeSyntax(caseSensitive);
        } else if (StringHelper.EqOp(relations, "!=")) {
            relations = "NOT " + getLikeSyntax(caseSensitive);
        }
        for (String field : mColumnNameDict.keySet()) {
            if (mTypeDict.get(field) == String.class && !mNotFreeTextSearchableFieldsList.contains(field)) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(" OR ");
                }
                sb.append(StringFormat.format(" %1$s.%2$s %3$s %4$s",
                        tableName,
                        mColumnNameDict.get(field),
                        relations,
                        value));
            }
        }
        sb.append(" ) ");
        return sb.toString();
    }

    public valueValidationFunction validCahracters = new valueValidationFunction() {
        public boolean invoke(String field, String value) {
            Regex validChar = new Regex("^[^\\<\\>&^#!']*$");
            // Regex validChar = new Regex("^[a-zA-Z0-9%-_@:\\*\\. ]*$");
            return validChar.IsMatch(value);
        }
    };

    public valueValidationFunction validDateTime = new valueValidationFunction() {
        public boolean invoke(String field, String value) {
            Date test = DateUtils.parse(value);
            if (test != null) {
                // For some reason this is the oldest possible time in SQL server
                Date dbOldestPossibleDate = new Date(Date.parse("01/01/1753 00:00:00"));
                if (test.compareTo(dbOldestPossibleDate) >= 0) {
                    return true;
                }
            } else { // check for enum
                for (DateEnumForSearch val : DateEnumForSearch.values()) {
                    if (StringHelper.EqOp(value.toUpperCase(), val.name().toUpperCase())) {
                        return true;
                    }
                }
                // check for week before
                for (DayOfWeek day : DayOfWeek.class.getEnumConstants()) {
                    if (StringHelper.EqOp(value.toUpperCase(), day.toString().toUpperCase())) {
                        return true;
                    }
                }
            }
            return false;
        }
    };

    public valueValidationFunction validTimeSpan = new valueValidationFunction() {
        public boolean invoke(String field, String value) {
            TimeSpan test = new TimeSpan();
            boolean retval = false;
            RefObject<TimeSpan> tempRefObject = new RefObject<TimeSpan>(test);
            boolean tempVar = TimeSpan.TryParse(value, tempRefObject);
            test = tempRefObject.argvalue;
            if (tempVar) {
                retval = true;
            }
            return retval;
        }
    };

    public valueValidationFunction validInteger = new valueValidationFunction() {
        public boolean invoke(String field, String value) {
            Integer test = new Integer(0);
            boolean retval = false;
            RefObject<Integer> tempRefObject = new RefObject<Integer>(test);
            boolean tempVar = IntegerCompat.TryParse(value, tempRefObject);
            test = tempRefObject.argvalue;
            if (tempVar) {
                retval = true;
            }
            return retval;
        }
    };

    public valueValidationFunction validDecimal = new valueValidationFunction() {
        public boolean invoke(String field, String value) {
            BigDecimal test = new BigDecimal(0);
            boolean retval = false;
            RefObject<java.math.BigDecimal> tempRefObject = new RefObject<java.math.BigDecimal>(test);
            boolean tempVar = DoubleCompat.TryParse(value, tempRefObject);
            test = tempRefObject.argvalue;
            if (tempVar) {
                retval = true;
            }
            return retval;
        }
    };

    public valueValidationFunction validateDateEnumValueByValueAC = new valueValidationFunction() {
        public boolean invoke(String field, String value) {
            boolean retval = true;
            IConditionValueAutoCompleter vlaueAc = getFieldValueAutoCompleter(field);
            if (vlaueAc != null) // check if this enum first
            {
                retval = vlaueAc.validate(value);
            }
            if (!retval) // check for week before
            {
                for (DayOfWeek day : DayOfWeek.values()) {
                    if (StringHelper.EqOp(value.toUpperCase(), day.toString().toUpperCase())) // Enum.GetName(typeof(DayOfWeek),
                                                                                              // day).ToUpper()
                    {
                        return true;
                    }
                }
            }
            if (!retval) // check for free date
            {
                retval = DateUtils.parse(StringHelper.trim(value, '\'')) != null;
            }

            return retval;
        }
    };

    public valueValidationFunction validateFieldValueByValueAC = new valueValidationFunction() {
        public boolean invoke(String field, String value) {
            boolean retval = true;
            IConditionValueAutoCompleter vlaueAc = getFieldValueAutoCompleter(field);
            if (vlaueAc != null) {
                retval = vlaueAc.validate(value);
            }
            return retval;
        }
    };

    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return null;
    }

    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        return null;
    }

    public void formatValue(String fieldName,
            RefObject<String> relations,
            RefObject<String> value,
            boolean caseSensitive) {
        if (fieldName == null) {
            return;
        }

        if (fieldName.equals("TIME") || fieldName.equals("CREATIONDATE")) {
            Date temp = DateUtils.parse(StringHelper.trim(value.argvalue, '\''));

            DateTime result;
            if (temp == null) {
                result = DealWithDateEnum(value.argvalue);
            } else {
                result = new DateTime(temp);
            }

            if (relations.argvalue != null && relations.argvalue.equals("=")) {
                relations.argvalue = "between";
                DateTime nextDay = result.AddDays(1);
                value.argvalue = StringFormat.format("'%1$s' and '%2$s'",
                        result.toString(DateUtils.getFormat(DateFormatCompat.DEFAULT, DateFormatCompat.SHORT)),
                        nextDay.toString(DateUtils.getFormat(DateFormatCompat.DEFAULT, DateFormatCompat.SHORT)));
            } else { // ">" or "<"
                     // value.argvalue = String.format("'%1$s'", result);
                value.argvalue = StringFormat.format("'%1$s'",
                        result.toString(DateUtils.getFormat(DateFormatCompat.DEFAULT, DateFormatCompat.SHORT)));
            }

        }
        else if (fieldName.equals("TAG")) {
            value.argvalue = value.argvalue.startsWith("N'") ? value.argvalue.substring(2) : value.argvalue;
            if (relations.argvalue != null && relations.argvalue.equals("=")) {
                relations.argvalue = "IN";
                value.argvalue = StringHelper.trim(value.argvalue, '\'');
                tags tag = TagsHandler.GetTagByTagName(value.argvalue);
                if (tag != null) {
                    value.argvalue =
                            StringFormat.format("(%1$s)", TagsHandler.GetTagNameAndChildrenNames(tag.gettag_id()));
                } else {
                    value.argvalue = StringFormat.format("('%1$s')", Guid.Empty);
                }
            } else if (relations.argvalue != null && relations.argvalue.equals("LIKE")) {
                relations.argvalue = "IN";
                value.argvalue = StringHelper.trim(value.argvalue, '\'').replace("%", "*");

                String IDs = TagsHandler.GetTagNamesAndChildrenNamesByRegExp(value.argvalue);
                if (StringHelper.isNullOrEmpty(IDs)) {
                    value.argvalue = StringFormat.format("('%1$s')", Guid.Empty);
                } else {
                    value.argvalue = StringFormat.format("(%1$s)", IDs);
                }
            }
        }
    }

    // private static final String DATE_FORMAT = "MMM dd,yyyy";
    private static DateTime DealWithDateEnum(String value) {
        DateTime formatedValue = new DateTime();
        Integer result = new Integer(0);
        RefObject<Integer> tempRefObject = new RefObject<Integer>(result);
        boolean tempVar = IntegerCompat.TryParse(StringHelper.trim(value, '\''), tempRefObject);
        result = tempRefObject.argvalue;
        if (tempVar) {
            DateEnumForSearch dateEnumVal = DateEnumForSearch.forValue(result);
            switch (dateEnumVal) {
            case Today:
                formatedValue = DateTime.getNow();
                break;
            case Yesterday:
                formatedValue = DateTime.getNow().AddDays(-1);
                break;

            default:
                break;
            }
        } else {
            for (int i = -2; i > -8; i--) {
                if (StringHelper.EqOp(DateUtils.getDayOfWeek(i), StringHelper.trim(value, '\'').toUpperCase())) {
                    formatedValue = DateTime.getNow();
                    return formatedValue.resetToMidnight();
                }
            }
        }
        return formatedValue.resetToMidnight();
    }

    public String buildConditionSql(String fieldName, String customizedValue, String customizedRelation,
            String tableName, boolean caseSensitive) {
        String condition;
        RefObject<String> tempRefObject = new RefObject<String>(customizedRelation);
        RefObject<String> tempRefObject2 = new RefObject<String>(customizedValue);
        formatValue(fieldName, tempRefObject, tempRefObject2, caseSensitive);
        customizedRelation = tempRefObject.argvalue;
        customizedValue = tempRefObject2.argvalue;
        if ((StringHelper.EqOp(customizedValue, "''") || StringHelper.EqOp(customizedValue.toLowerCase(), "n'null'"))
                && ((StringHelper.EqOp(customizedRelation, "=")) || (StringHelper.EqOp(customizedRelation, "!=")))) // handling
                                                                                                                    // the
                                                                                                                    // NULL
                                                                                                                    // value
                                                                                                                    // in
                                                                                                                    // case
                                                                                                                    // the
                                                                                                                    // search
                                                                                                                    // is
                                                                                                                    // for
                                                                                                                    // empty
                                                                                                                    // value;
        {
            String nullRelation = (StringHelper.EqOp(customizedRelation, "=")) ? "IS" : "IS NOT";
            String SqlCond = (StringHelper.EqOp(customizedRelation, "=")) ? "OR" : "AND";
            condition = StringFormat.format("( %1$s.%2$s %3$s %4$s %5$s  %1$s.%2$s %6$s  NULL)", tableName,
                    getDbFieldName(fieldName), customizedRelation, customizedValue, SqlCond, nullRelation);
        } else {
            condition = StringFormat.format(" %1$s.%2$s %3$s %4$s ", tableName, getDbFieldName(fieldName),
                    customizedRelation, customizedValue);
        }
        return condition;
    }
}

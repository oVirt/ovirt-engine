package org.ovirt.engine.core.searchbackend;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.DateEnumForSearch;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeSpan;

/**
 * A base class for all condition field auto completers
 */
public class BaseConditionFieldAutoCompleter extends BaseAutoCompleter implements IConditionFieldAutoCompleter {

    protected final Map<String, List<ValueValidationFunction>> validationDict = new HashMap<>();
    private final Map<String, Class<?>> typeDict = new HashMap<>();
    protected final Map<String, String> columnNameDict = new HashMap<>();
    /**
     * key: UI column identifier
     * value: list of 'ORDER BY' elements
     */
    protected final Map<String, List<SyntaxChecker.SortByElement>> sortableFieldDict = new HashMap<>();
    protected final List<String> notFreeTextSearchableFieldsList = new ArrayList<>();
    protected Set<String> verbsWithMultipleValues = new HashSet<>();

    /**
     * Gets the LIKE clause syntax for non case-sensitive search
     *
     * @return the LIKE syntax according to current DBEngine.
     */
    public static String getLikeSyntax(boolean caseSensitive) {
        // If caseSensitive flag is set we will use LIKE
        if (caseSensitive) {
            return "LIKE";
        } else {
            return Config.getValue(ConfigValues.DBLikeSyntax);
        }
    }

    public String getMatchingSyntax(String fieldName, boolean positive, boolean caseSensitive) {
        return verbsWithMultipleValues.contains(fieldName) ?
                new StringBuilder(positive ? "" : " !").append(caseSensitive ? "~" : "~*").toString()
                : new StringBuilder(positive ? "" : " NOT ").append(getLikeSyntax(caseSensitive)).toString();
    }


    /**
     * Gets the I18N prefix used for value compare.
     */
    public static String getI18NPrefix() {
        return Config.getValue(ConfigValues.DBI18NPrefix);
    }

    public static ITagsHandler tagsHandler = null;

    public Map<String, Class<?>> getTypeDictionary() {
        return typeDict;
    }

    protected void buildBasicValidationTable() {
        for (String key : verbs) {
            final List<ValueValidationFunction> curList = new ArrayList<>();
            final Class<?> curType = typeDict.get(key);
            if (curType == BigDecimal.class) {
                curList.add(validDecimal);
            } else if (curType == Integer.class) {
                curList.add(validInteger);
            } else if (curType == Date.class) {
                curList.add(validDateTime);
            } else if (curType == TimeSpan.class) {
                curList.add(validTimeSpan);
            }
            final IConditionValueAutoCompleter tmp = getFieldValueAutoCompleter(key);
            if (tmp != null) {
                if (tmp.getClass() == DateEnumValueAutoCompleter.class) {
                    curList.add(validateDateEnumValueByValueAC);
                } else {
                    curList.add(validateFieldValueByValueAC);
                }
            }
            validationDict.put(key, curList);
        }
    }

    @Override
    public boolean validateFieldValue(String fieldName, String fieldValue) {
        if (validationDict.containsKey(fieldName)) {
            final List<ValueValidationFunction> validationList = validationDict.get(fieldName);
            for (ValueValidationFunction curValidationFunc : validationList) {
                if (!curValidationFunc.isValid(fieldName, fieldValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getDbFieldName(String fieldName) {
        String retval = null;
        if (columnNameDict.containsKey(fieldName)) {
            retval = columnNameDict.get(fieldName);
        }
        return retval;
    }

    @Override
    public List<SyntaxChecker.SortByElement> getSortByElements(String fieldName) {
        if (sortableFieldDict.containsKey(fieldName)) {
            return sortableFieldDict.get(fieldName);
        }
        return Collections.singletonList(
                new SyntaxChecker.SortByElement(getDbFieldName(fieldName), true));
    }

    @Override
    public Class<?> getDbFieldType(String fieldName) {
        Class<?> retval = null;
        if (typeDict.containsKey(fieldName)) {
            retval = typeDict.get(fieldName);
        }
        return retval;

    }

    // FIXME Probably Not Hibernate Friendly
    @Override
    public final String buildFreeTextConditionSql(String tableName,
            String relations,
            String value,
            boolean caseSensitive) {
        String val;
        if (!StringHelper.isNullOrEmpty(value) && !"''".equals(value)) {
            val= StringFormat.format(getI18NPrefix() + "'%%%1$s%%'", StringHelper.trim(value, '\''));
        } else {
            val = value;
        }

        String rel;
        switch (relations) {
        case "d":
            rel = getLikeSyntax(caseSensitive);
            break;
        case "!=":
            rel = "NOT " + getLikeSyntax(caseSensitive);
            break;
        default:
            rel = relations;
        }

        return columnNameDict.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .filter(e -> typeDict.get(e.getKey()) == String.class && !notFreeTextSearchableFieldsList.contains(e.getKey()))
                .map(e -> StringFormat.format(" %1$s.%2$s %3$s %4$s", tableName, e.getValue(), rel, val))
                .distinct()
                .collect(Collectors.joining(" OR ", " ( ", " ) "));
    }

    static final Regex validChar = new Regex("^[^\\<\\>&^!']*$");

    public static final ValueValidationFunction validCharacters = (field, value) -> validChar.isMatch(value);

    public static final ValueValidationFunction validDateTime = (field, value) -> {
        Date test = DateUtils.parse(value);
        if (test != null) {
            return true;
        } else { // check for enum
            for (DateEnumForSearch val : DateEnumForSearch.values()) {
                if (val.name().equalsIgnoreCase(value)) {
                    return true;
                }
            }
            // check for week before
            for (DayOfWeek day : DayOfWeek.class.getEnumConstants()) {
                if (day.toString().equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }
        return false;
    };

    public static final ValueValidationFunction validTimeSpan = (field, value) -> TimeSpan.tryParse(value) != null;

    public static final ValueValidationFunction validInteger = (field, value) -> IntegerCompat.tryParse(value) != null;

    public static final ValueValidationFunction validDecimal = (field, value) -> {
        try {
            new BigDecimal(value); // No assignment, we just want to create a new instance and to see if there's an
                                   // Exception
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    };

    public final ValueValidationFunction validateDateEnumValueByValueAC = (field, value) -> {
        boolean retval = true;
        IConditionValueAutoCompleter vlaueAc = getFieldValueAutoCompleter(field);
        if (vlaueAc != null) { // check if this enum first
            retval = vlaueAc.validate(value);
        }
        if (!retval) { // check for week before
            for (DayOfWeek day : DayOfWeek.values()) {
                if (day.toString().equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }
        if (!retval) { // check for free date
            retval = DateUtils.parse(StringHelper.trim(value, '\'')) != null;
        }

        return retval;
    };

    public final ValueValidationFunction validateFieldValueByValueAC = (field, value) -> {
        boolean retval = true;
        IConditionValueAutoCompleter vlaueAc = getFieldValueAutoCompleter(field);
        if (vlaueAc != null) {
            retval = vlaueAc.validate(value);
        }
        return retval;
    };

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return null;
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        return null;
    }

    @Override
    public void formatValue(String fieldName, Pair<String, String> pair, boolean caseSensitive) {
        if (fieldName == null) {
            return;
        }

        if ("TIME".equals(fieldName) || "CREATIONDATE".equals(fieldName)) {
            Date temp = DateUtils.parse(StringHelper.trim(pair.getSecond(), '\''));

            DateTime result;
            if (temp == null) {
                result = dealWithDateEnum(pair.getSecond());
            } else {
                result = new DateTime(temp);
            }

            if (pair.getFirst() != null && pair.getFirst().equals("=")) {
                pair.setFirst("between");
                DateTime nextDay = result.addDays(1);
                pair.setSecond(StringFormat.format("'%1$s' and '%2$s'",
                        result.toString(DateUtils.getFormat(DateFormat.DEFAULT, DateFormat.SHORT)),
                        nextDay.toString(DateUtils.getFormat(DateFormat.DEFAULT, DateFormat.SHORT))));
            } else { // ">" or "<"
                     // value.argvalue = String.format("'%1$s'", result);
                pair.setSecond(StringFormat.format("'%1$s'",
                        result.toString(DateUtils.getFormat(DateFormat.DEFAULT, DateFormat.SHORT))));
            }

        } else if ("TAG".equals(fieldName)) {
            pair.setSecond(pair.getSecond().startsWith("N'") ? pair.getSecond().substring(2) : pair.getSecond());
            if (pair.getFirst() != null && pair.getFirst().equals("=")) {
                pair.setFirst("IN");
                pair.setSecond(StringHelper.trim(pair.getSecond(), '\''));
                Tags tag = tagsHandler.getTagByTagName(pair.getSecond());
                if (tag != null) {
                    pair.setSecond(
                            StringFormat.format("(%1$s)", tagsHandler.getTagNameAndChildrenNames(tag.getTagId())));
                } else {
                    pair.setSecond(StringFormat.format("('%1$s')", Guid.Empty));
                }
            } else if (pair.getFirst() != null && (pair.getFirst().equals("LIKE") || pair.getFirst().equals("ILIKE"))) {
                pair.setFirst("IN");
                pair.setSecond(StringHelper.trim(pair.getSecond(), '\'').replace("%", "*"));

                String IDs = tagsHandler.getTagNamesAndChildrenNamesByRegExp(pair.getSecond());
                if (StringHelper.isNullOrEmpty(IDs)) {
                    pair.setSecond(StringFormat.format("('%1$s')", Guid.Empty));
                } else {
                    pair.setSecond(StringFormat.format("(%1$s)", IDs));
                }
            }
        }
    }

    // private static final String DATE_FORMAT = "MMM dd,yyyy";
    private static DateTime dealWithDateEnum(String value) {
        DateTime formatedValue = new DateTime();
        final Integer result = IntegerCompat.tryParse(StringHelper.trim(value, '\''));
        if (result != null) {
            DateEnumForSearch dateEnumVal = DateEnumForSearch.forValue(result);
            switch (dateEnumVal) {
            case Today:
                formatedValue = DateTime.getNow();
                break;
            case Yesterday:
                formatedValue = DateTime.getNow().addDays(-1);
                break;

            default:
                break;
            }
        } else {
            for (int i = -2; i > -8; i--) {
                if (DateUtils.getDayOfWeek(i).equalsIgnoreCase(StringHelper.trim(value, '\''))) {
                    formatedValue = DateTime.getNow().addDays(i);
                    return formatedValue.resetToMidnight();
                }
            }
        }
        return formatedValue.resetToMidnight();
    }

    @Override
    public String buildConditionSql(String objName, String fieldName, String customizedValue, String customizedRelation,
            String tableName, boolean caseSensitive) {
        Pair<String, String> pair = new Pair<>();
        pair.setFirst(customizedRelation);
        pair.setSecond(customizedValue);
        formatValue(fieldName, pair, caseSensitive);
        if (("''".equals(pair.getSecond()) || "'null'".equalsIgnoreCase(pair.getSecond()))
                && ("=".equals(pair.getFirst()) || "!=".equals(pair.getFirst()))) {
            String nullRelation = "=".equals(pair.getFirst()) ? "IS" : "IS NOT";
            return StringFormat.format("(%1$s.%2$s %3$s  NULL)", tableName,
                    getDbFieldName(fieldName), nullRelation);
        } else {
            SearchObjectAutoCompleter.EntitySearchInfo info = SearchObjectAutoCompleter.getEntitySearchInfo(objName);
            // Check if value is comma delimited list, this apply for example to shared disk or shared ISO domain
            if (info.commaDelimitedListColumns != null && info.commaDelimitedListColumns.contains(fieldName.toLowerCase())) {
                String operator = "NOT LIKE".equals(pair.getFirst().trim()) ?
                        " LIKE" :
                        pair.getFirst();
                String conditionFormat = "NOT LIKE".equals(pair.getFirst().trim()) ?
                        "NOT %1$s %2$s ANY(string_to_array(%3$s.%4$s, ','))" :
                        "%1$s %2$s ANY(string_to_array(%3$s.%4$s, ','))";
                return StringFormat.format(conditionFormat,
                        pair.getSecond().replace("\\_", "_"), // if the value is on the left of LIKE/ILIKE it should be without "_"
                        operator,
                        tableName,
                        getDbFieldName(fieldName));
            }
            String formatString;
            if (isOperatorNegative(pair.getFirst())) {
                formatString = " (%1$s.%2$s IS NULL OR %1$s.%2$s %3$s %4$s) ";
            } else {
                formatString = " %1$s.%2$s %3$s %4$s ";
            }
            return StringFormat.format(formatString, tableName, getDbFieldName(fieldName),
                    pair.getFirst(), pair.getSecond());
        }
    }

    private static boolean isOperatorNegative(String operator) {
        switch (operator.trim().toUpperCase()) {
        case "NOT LIKE":
        case "NOT ILIKE":
        case "!=":
        case "<>":
            return true;
        default:
            return false;
        }
    }

    @Override
    public String getWildcard(String fieldName) {
        return verbsWithMultipleValues.contains(fieldName) ? ".*" : "%";
    }
}

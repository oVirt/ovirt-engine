package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.utils.Pair;

/**
 * An interface to be implemented by all Condition fields auto completers
 */
public interface IConditionFieldAutoCompleter extends IAutoCompleter {
    boolean validateFieldValue(String fieldName, String fieldValue);

    String getDbFieldName(String fieldName);

    String getSortableDbField(String fieldName);

    Class<?> getDbFieldType(String fieldName);

    IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName);

    IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName);

    String buildFreeTextConditionSql(String tableName, String relations, String value, boolean caseSensitive);

    String getMatchingSyntax(String fieldName, boolean positive, boolean caseSensitive);

    String getWildcard(String fieldName);

    String buildConditionSql(String fieldName,
            String customizedValue,
            String customizedRelation,
            String tableName,
            boolean caseSensitive);

    void formatValue(String fieldName, Pair<String, String> pair, boolean caseSensitive);
}

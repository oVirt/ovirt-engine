package org.ovirt.engine.core.searchbackend;

import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;

/**
 * An interface to be implemented by all Condition fields auto completers
 */
public interface IConditionFieldAutoCompleter extends IAutoCompleter {
    boolean validateFieldValue(String fieldName, String fieldValue);

    String getDbFieldName(String fieldName);

    /**
     * @param fieldName UI column identifier
     * @return list of 'ORDER BY' elements
     */
    List<SyntaxChecker.SortByElement> getSortByElements(String fieldName);

    Class<?> getDbFieldType(String fieldName);

    IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName);

    IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName);

    String buildFreeTextConditionSql(String tableName, String relations, String value, boolean caseSensitive);

    String getMatchingSyntax(String fieldName, boolean positive, boolean caseSensitive);

    String getWildcard(String fieldName);

    String buildConditionSql(String objName,
            String fieldName,
            String customizedValue,
            String customizedRelation,
            String tableName,
            boolean caseSensitive);

    void formatValue(String fieldName, Pair<String, String> pair, boolean caseSensitive);
}

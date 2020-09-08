package org.ovirt.engine.core.searchbackend;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.SqlInjectionException;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyntaxChecker implements ISyntaxChecker {
    private static final Logger log = LoggerFactory.getLogger(SyntaxChecker.class);

    public static final String SORTBY = "SORTBY";
    public static final String PAGE = "PAGE";
    public static final String SORTDIR_ASC = "ASC";
    public static final String SORTDIR_DESC = "DESC";

    private static final List<Character> DISALLOWED_CHARS = Arrays.asList('\'', ';');

    private final SearchObjectAutoCompleter searchObjectAC;
    private final BaseAutoCompleter colonAC;
    private final BaseAutoCompleter pluralAC;
    private final BaseAutoCompleter sortbyAC;
    private final BaseAutoCompleter pageAC;
    private final BaseAutoCompleter andAC;
    private final BaseAutoCompleter orAC;
    private final BaseAutoCompleter dotAC;
    private final BaseAutoCompleter sortDirectionAC;
    private final Map<SyntaxObjectType, SyntaxObjectType[]> stateMap;

    private final Regex firstDQRegexp;
    private final Regex nonSpaceRegexp;
    private SqlInjectionChecker sqlInjectionChecker;

    public SyntaxChecker() {

        searchObjectAC = new SearchObjectAutoCompleter();
        colonAC = new BaseAutoCompleter(":");
        pluralAC = new BaseAutoCompleter("S");
        sortbyAC = new BaseAutoCompleter(SORTBY);
        pageAC = new BaseAutoCompleter(PAGE);
        sortDirectionAC = new BaseAutoCompleter(SORTDIR_ASC, SORTDIR_DESC);
        andAC = new BaseAutoCompleter("AND");
        orAC = new BaseAutoCompleter("OR");
        dotAC = new BaseAutoCompleter(".");

        firstDQRegexp = new Regex("^\\s*\"$");
        nonSpaceRegexp = new Regex("^\\S+$");

        stateMap = new HashMap<>();
        stateMap.put(SyntaxObjectType.BEGIN, new SyntaxObjectType[] { SyntaxObjectType.SEARCH_OBJECT });
        stateMap.put(SyntaxObjectType.SEARCH_OBJECT, new SyntaxObjectType[] { SyntaxObjectType.COLON });
        SyntaxObjectType[] afterColon =
        { SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD,
                SyntaxObjectType.SORTBY, SyntaxObjectType.PAGE, SyntaxObjectType.CONDITION_VALUE,
                SyntaxObjectType.END };
        stateMap.put(SyntaxObjectType.COLON, afterColon);

        SyntaxObjectType[] afterCrossRefObj = { SyntaxObjectType.DOT, SyntaxObjectType.CONDITION_RELATION };
        stateMap.put(SyntaxObjectType.CROSS_REF_OBJ, afterCrossRefObj);
        stateMap.put(SyntaxObjectType.DOT, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_FIELD });

        stateMap.put(SyntaxObjectType.CONDITION_FIELD, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_RELATION });
        stateMap.put(SyntaxObjectType.CONDITION_RELATION, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_VALUE });
        SyntaxObjectType[] afterConditionValue = { SyntaxObjectType.OR, SyntaxObjectType.AND,
                SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD, SyntaxObjectType.SORTBY,
                SyntaxObjectType.PAGE, SyntaxObjectType.CONDITION_VALUE };
        stateMap.put(SyntaxObjectType.CONDITION_VALUE, afterConditionValue);

        SyntaxObjectType[] AndOrArray = { SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD,
                SyntaxObjectType.CONDITION_VALUE };
        stateMap.put(SyntaxObjectType.AND, AndOrArray);
        stateMap.put(SyntaxObjectType.OR, AndOrArray);

        stateMap.put(SyntaxObjectType.SORTBY, new SyntaxObjectType[] { SyntaxObjectType.SORT_FIELD });
        stateMap.put(SyntaxObjectType.SORT_FIELD, new SyntaxObjectType[] { SyntaxObjectType.SORT_DIRECTION });
        stateMap.put(SyntaxObjectType.SORT_DIRECTION, new SyntaxObjectType[] { SyntaxObjectType.PAGE });

        stateMap.put(SyntaxObjectType.PAGE, new SyntaxObjectType[] { SyntaxObjectType.PAGE_VALUE });
        stateMap.put(SyntaxObjectType.PAGE_VALUE, new SyntaxObjectType[] { SyntaxObjectType.END });
        // get sql injection checker for active database engine.
        try {
            sqlInjectionChecker = getSqlInjectionChecker();
        } catch (Exception e) {
            log.debug("Failed to load Sql Injection Checker: {}", e.getMessage());
        }
    }

    private enum ValueParseResult {
        Err,
        Normal,
        FreeText;
    }

    private ValueParseResult handleValuePhrase(boolean final2, String searchText, int idx, RefObject<Integer> startPos,
            SyntaxContainer container) {
        boolean addObjFlag = false;
        ValueParseResult retval = ValueParseResult.Normal;
        IConditionFieldAutoCompleter curConditionFieldAC;
        char curChar = searchText.charAt(idx);
        String strRealObj = searchText.substring(startPos.argvalue, idx + 1);

        boolean betweenDoubleQuotes = searchText.substring(startPos.argvalue, idx).contains("\"");
        if (curChar == '"') {
            betweenDoubleQuotes = !betweenDoubleQuotes;
            if (betweenDoubleQuotes) {
                if (!firstDQRegexp.isMatch(strRealObj)) {
                    container.setErr(SyntaxError.INVALID_CONDITION_VALUE, startPos.argvalue, idx + 1);
                    return ValueParseResult.Err;
                }
            } else {
                strRealObj = StringHelper.trim(strRealObj, new char[] { '\"' });
                addObjFlag = true;
            }
        }
        // Doing this condition to identify whether this is the last
        // searchObject and no space is predicted !!
        if (final2) {
            if (((curChar == ' ') || (idx + 1 == searchText.length())) && !betweenDoubleQuotes && !addObjFlag) {
                strRealObj = strRealObj.trim();
                if (nonSpaceRegexp.isMatch(strRealObj)) {
                    addObjFlag = true;
                } else {
                    startPos.argvalue = idx + 1;
                }
            }
        } else {
            if ((curChar == ' ') && !betweenDoubleQuotes && !addObjFlag) {
                strRealObj = strRealObj.trim();
                if (nonSpaceRegexp.isMatch(strRealObj)) {
                    addObjFlag = true;
                } else {
                    startPos.argvalue = idx + 1;
                }
            }
        }
        if (addObjFlag) {
            String curRefObj = container.getPreviousSyntaxObject(3, SyntaxObjectType.CROSS_REF_OBJ);
            String curConditionField = container.getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD);
            curConditionFieldAC = searchObjectAC.getFieldAutoCompleter(curRefObj);
            if (curConditionFieldAC == null) {
                container.setErr(SyntaxError.CANT_GET_CONDITION_FIELD_AC, startPos.argvalue, idx);
                return ValueParseResult.Err;
            }
            if (!"".equals(curConditionField)
                    && !curConditionFieldAC.validateFieldValue(curConditionField, strRealObj)) {
                container.setErr(SyntaxError.INVALID_CONDITION_VALUE, startPos.argvalue, idx);
                return ValueParseResult.Err;
            }
            container.addSyntaxObject(SyntaxObjectType.CONDITION_VALUE, strRealObj, startPos.argvalue, idx + 1);
            retval = ValueParseResult.FreeText;
            startPos.argvalue = idx + 1;
            container.setvalid(true);
        }
        return retval;
    }

    /**
     * gets the sql injection checker class for current db vendor.
     */
    private SqlInjectionChecker getSqlInjectionChecker() throws Exception {
        // This can not be done with reflection like:
        // return (SqlInjectionChecker) Class.forName(props.getProperty(SQL_INJECTION)).newInstance();
        // GWT lacks support of reflection.
        if (((String) Config.getValue(ConfigValues.DBEngine)).equalsIgnoreCase("postgres")) {
            return new PostgresSqlInjectionChecker();
        } else {
            throw new IllegalStateException("Failed to get correct sql injection checker instance name :"
                    + SqlInjectionChecker.class);
        }

    }

    @Override
    public SyntaxContainer analyzeSyntaxState(final String searchText, boolean final2) {
        final SyntaxContainer syntaxContainer = new SyntaxContainer(searchText);
        IConditionFieldAutoCompleter curConditionFieldAC = null;
        IAutoCompleter curConditionRelationAC = null;
        final List<String> freeTextObjSearched = new ArrayList<>();
        char[] searchCharArr = searchText.toCharArray();
        int curStartPos = 0;

        String tryNextObj = "";
        boolean keepValid;
        for (int idx = 0; idx < searchCharArr.length; idx++) {
            final SyntaxObjectType curState = syntaxContainer.getState();
            final char curChar = searchCharArr[idx];
            if (DISALLOWED_CHARS.contains(curChar)) {
                syntaxContainer.setErr(SyntaxError.INVALID_CHARECTER, curStartPos, idx + 1);
                return syntaxContainer;
            }
            if ((curChar == ' ') && (curState != SyntaxObjectType.CONDITION_RELATION)
                    && (curState != SyntaxObjectType.COLON) && (curState != SyntaxObjectType.CONDITION_VALUE)
                    && (curState != SyntaxObjectType.OR) && (curState != SyntaxObjectType.AND)) {
                curStartPos += 1;
                continue;
            }
            String strRealObj = searchText.substring(curStartPos, idx + 1);
            String nextObject = strRealObj.toUpperCase();
            switch (curState) {
            case BEGIN:
                // we have found a search-object
                if (!searchObjectAC.validate(nextObject)) {
                    if (!searchObjectAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_SEARCH_OBJECT, curStartPos, idx - curStartPos + 1);
                        return syntaxContainer;
                    }
                } else {
                    if (searchCharArr.length >= idx + 2) { // Check that this
                                                         // maybe a plural
                        // Validate that the next character is an 's'
                        if (pluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                            // Then just move things along.
                            idx++;
                            StringBuilder sb = new StringBuilder(nextObject);
                            sb.append('S');
                            nextObject = sb.toString();
                        }
                    }
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.SEARCH_OBJECT, nextObject, curStartPos, idx + 1);
                    syntaxContainer.setvalid(true);
                    curStartPos = idx + 1;
                }
                break;
            case SEARCH_OBJECT:

                if (!colonAC.validate(nextObject)) {
                    if (!colonAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.COLON_NOT_NEXT_TO_SEARCH_OBJECT, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                } else {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.COLON, nextObject, idx, idx + 1);
                    curStartPos = idx + 1;
                    syntaxContainer.setvalid(true);
                }
                break;
            case CROSS_REF_OBJ:
                String curRefObj = syntaxContainer.getPreviousSyntaxObject(0, SyntaxObjectType.CROSS_REF_OBJ);
                curConditionRelationAC = searchObjectAC.getObjectRelationshipAutoCompleter();
                if (idx + 1 < searchCharArr.length) {
                    tryNextObj = searchText.substring(curStartPos, idx + 2).toUpperCase();
                }
                if (curConditionRelationAC == null) {
                    syntaxContainer.setErr(SyntaxError.CONDITION_CANT_CREATE_RRELATIONS_AC, curStartPos, idx + 1);
                    return syntaxContainer;
                }
                if (dotAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.DOT, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if (!"".equals(tryNextObj) && curConditionRelationAC.validate(tryNextObj)) {
                    break; // i.e. the relation object has another charecter
                } else if (curConditionRelationAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_RELATION,
                            nextObject,
                            curStartPos,
                            idx + 1);
                    curStartPos = idx + 1;

                } else if (!curConditionRelationAC.validateCompletion(nextObject)
                        && !dotAC.validateCompletion(nextObject)) {
                    syntaxContainer.setErr(SyntaxError.INVALID_POST_CROSS_REF_OBJ, curStartPos, idx + 1);
                    return syntaxContainer;
                }
                tryNextObj = "";
                break;
            case DOT:
                curRefObj = syntaxContainer.getPreviousSyntaxObject(1, SyntaxObjectType.CROSS_REF_OBJ);
                curConditionFieldAC = searchObjectAC.getFieldAutoCompleter(curRefObj);
                if (curConditionFieldAC == null) {

                    syntaxContainer.setErr(SyntaxError.CANT_GET_CONDITION_FIELD_AC, curStartPos, idx);
                    return syntaxContainer;
                }
                if (!curConditionFieldAC.validate(nextObject)) {
                    if (!curConditionFieldAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_CONDITION_FILED, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                } else {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                }
                break;
            case AND:
            case OR:
                keepValid = false;
                curConditionFieldAC = searchObjectAC.getFieldAutoCompleter(syntaxContainer.getSearchObjectStr());
                if (curConditionFieldAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;

                } else if (searchObjectAC.isCrossReference(nextObject, syntaxContainer.getFirst().getBody())) {
                    if (searchCharArr.length >= idx + 2) { // Check that this
                                                         // maybe a plural
                        // Validate that the next character is an 's'
                        if (pluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                            // Then just move things along.
                            idx++;
                            StringBuilder sb = new StringBuilder(nextObject);
                            sb.append('S');
                            nextObject = sb.toString();
                        }
                    }
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CROSS_REF_OBJ, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else {
                    RefObject<Integer> tempRefObject = new RefObject<>(curStartPos);
                    ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject, syntaxContainer);
                    curStartPos = tempRefObject.argvalue;
                    if (ans != ValueParseResult.Err) {
                        if (ans == ValueParseResult.FreeText) {
                            curRefObj = syntaxContainer.getSearchObjectStr();
                            if (freeTextObjSearched.contains(curRefObj)) {
                                syntaxContainer.setErr(SyntaxError.FREE_TEXT_ALLOWED_ONCE_PER_OBJ, curStartPos, idx + 1);
                                return syntaxContainer;
                            }
                            freeTextObjSearched.add(curRefObj);
                            syntaxContainer.setvalid(true);
                            keepValid = true;
                        }
                    } else if (!curConditionFieldAC.validateCompletion(nextObject)
                            && !searchObjectAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_POST_OR_AND_PHRASE, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                }
                if (!keepValid) {
                    syntaxContainer.setvalid(false);
                }
                break;
            case COLON:
                keepValid = false;
                curConditionFieldAC = searchObjectAC.getFieldAutoCompleter(syntaxContainer.getSearchObjectStr());
                if (curConditionFieldAC.validate(nextObject)) {
                    // Allow to use free text search on entities that has column name prefix in their values
                    if (searchCharArr.length >= idx + 2) {
                        char c = searchCharArr[idx + 1];
                        // Check that this is a full keyword followed by a blank or by an operator
                        if (c == ' ' || c == '!' || c == '=' || c == '<' || c == '>') {
                            syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD,
                                    nextObject,
                                    curStartPos,
                                    idx + 1);
                            curStartPos = idx + 1;
                        }
                    }

                } else if (sortbyAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.SORTBY, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if (pageAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if (searchObjectAC.isCrossReference(nextObject, syntaxContainer.getFirst().getBody())) {
                    if (searchCharArr.length >= idx + 2) { // Check that this
                                                         // maybe a plural
                        // Validate that the next character is an 's'
                        if (pluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                            // Then just move things along.
                            idx++;
                            StringBuilder sb = new StringBuilder(nextObject);
                            sb.append('S');
                            nextObject = sb.toString();
                        }
                    }
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CROSS_REF_OBJ, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else {
                    RefObject<Integer> tempRefObject2 = new RefObject<>(curStartPos);
                    ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject2, syntaxContainer);
                    curStartPos = tempRefObject2.argvalue;
                    if (ans != ValueParseResult.Err) {
                        if (ans == ValueParseResult.FreeText) {
                            freeTextObjSearched.add(syntaxContainer.getSearchObjectStr());
                        }
                        keepValid = true;
                    } else if (!curConditionFieldAC.validateCompletion(nextObject)
                            && !sortbyAC.validateCompletion(nextObject)
                            && !searchObjectAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_POST_COLON_PHRASE, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                }
                if (!keepValid) {
                    syntaxContainer.setvalid(false);
                }
                break;
            case CONDITION_VALUE:
                nextObject = nextObject.trim();
                if (nextObject.length() > 0) {
                    keepValid = false;
                    curRefObj = syntaxContainer.getSearchObjectStr();
                    curConditionFieldAC = searchObjectAC.getFieldAutoCompleter(curRefObj);
                    if (curConditionFieldAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD,
                                nextObject,
                                curStartPos,
                                idx + 1);
                        curStartPos = idx + 1;

                    } else if (sortbyAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.SORTBY, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (pageAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (searchObjectAC.isCrossReference(nextObject, syntaxContainer.getFirst().getBody())) {
                        if (searchCharArr.length >= idx + 2) { // Check that this
                                                             // maybe a
                                                             // plural
                            // Validate that the next character is an 's'
                            if (pluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                                // Then just move things along.
                                idx++;
                                StringBuilder sb = new StringBuilder(nextObject);
                                sb.append('S');
                                nextObject = sb.toString();
                            }
                        }
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.CROSS_REF_OBJ,
                                nextObject,
                                curStartPos,
                                idx + 1);
                        curStartPos = idx + 1;
                    } else if (andAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.AND, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (orAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.OR, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (!curConditionFieldAC.validateCompletion(nextObject)
                            && !sortbyAC.validateCompletion(nextObject)
                            && !searchObjectAC.validateCompletion(nextObject)
                            && !andAC.validateCompletion(nextObject) && !orAC.validateCompletion(nextObject)) {
                        RefObject<Integer> tempRefObject3 = new RefObject<>(curStartPos);
                        ValueParseResult ans =
                                handleValuePhrase(final2, searchText, idx, tempRefObject3, syntaxContainer);
                        curStartPos = tempRefObject3.argvalue;
                        if (ans != ValueParseResult.Err) {
                            if (ans == ValueParseResult.FreeText) {
                                if (freeTextObjSearched.contains(curRefObj)) {
                                    syntaxContainer.setErr(SyntaxError.FREE_TEXT_ALLOWED_ONCE_PER_OBJ,
                                            curStartPos,
                                            idx + 1);
                                    return syntaxContainer;
                                }
                                freeTextObjSearched.add(curRefObj);
                                syntaxContainer.setvalid(true);
                                keepValid = true;
                            }
                        } else {
                            syntaxContainer.setErr(SyntaxError.INVALID_POST_CONDITION_VALUE_PHRASE,
                                    curStartPos,
                                    idx + 1);
                            return syntaxContainer;
                        }
                    }
                    if (!keepValid) {
                        syntaxContainer.setvalid(false);
                    }
                }
                break;
            case CONDITION_FIELD:
                curRefObj = syntaxContainer.getPreviousSyntaxObject(2, SyntaxObjectType.CROSS_REF_OBJ);
                String curConditionField = syntaxContainer.getPreviousSyntaxObject(0, SyntaxObjectType.CONDITION_FIELD);
                curConditionRelationAC = searchObjectAC
                        .getFieldRelationshipAutoCompleter(curRefObj, curConditionField);
                if (curConditionRelationAC == null) {
                    syntaxContainer.setErr(SyntaxError.CONDITION_CANT_CREATE_RRELATIONS_AC, curStartPos, idx + 1);
                    return syntaxContainer;
                }
                if (idx + 1 < searchCharArr.length) {
                    tryNextObj = searchText.substring(curStartPos, idx + 2).toUpperCase();
                    if (curConditionRelationAC.validate(tryNextObj)) {
                        break;
                    }
                }
                if (!curConditionRelationAC.validate(nextObject)) {
                    if (!curConditionRelationAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_CONDITION_RELATION, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                } else {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_RELATION,
                            nextObject,
                            curStartPos,
                            idx + 1);
                }
                curStartPos = idx + 1;
                syntaxContainer.setvalid(false);
                tryNextObj = "";

                break;
            case CONDITION_RELATION:
                RefObject<Integer> tempRefObject4 = new RefObject<>(curStartPos);
                ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject4, syntaxContainer);
                curStartPos = tempRefObject4.argvalue;
                if (ans == ValueParseResult.Err) {
                    return syntaxContainer;
                }
                if (ans == ValueParseResult.FreeText) {
                    if (syntaxContainer.getPreviousSyntaxObjectType(2) == SyntaxObjectType.CROSS_REF_OBJ) {
                        curRefObj = syntaxContainer.getObjSingularName(syntaxContainer.getPreviousSyntaxObject(2,
                                SyntaxObjectType.CROSS_REF_OBJ));
                        if (freeTextObjSearched.contains(curRefObj)) {
                            syntaxContainer.setErr(SyntaxError.FREE_TEXT_ALLOWED_ONCE_PER_OBJ, curStartPos, idx + 1);
                            return syntaxContainer;
                        }
                        freeTextObjSearched.add(curRefObj);
                    }
                }
                break;
            case SORTBY:
                curConditionFieldAC = searchObjectAC.getFieldAutoCompleter(syntaxContainer.getSearchObjectStr());
                if (!curConditionFieldAC.validate(nextObject)) {
                    if (!curConditionFieldAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_SORT_FIELD, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                } else {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.SORT_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                    syntaxContainer.setvalid(true);
                }
                break;
            case PAGE:
                Integer pageNumber = IntegerCompat.tryParse(nextObject);
                if (pageNumber == null) {
                    syntaxContainer.setErr(SyntaxError.INVALID_CHARECTER, curStartPos, idx + 1);
                    return syntaxContainer;
                } else {
                    final StringBuilder buff = new StringBuilder();
                    int pos = idx;
                    // parsing the whole page number (can be more than one char)
                    while (pos < searchText.length() - 1 && Character.isDigit(nextObject.charAt(0))) {
                        buff.append(nextObject);
                        pos++;
                        strRealObj = searchText.substring(pos, pos + 1);
                        nextObject = strRealObj.toUpperCase();
                    }
                    buff.append(nextObject);
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.PAGE_VALUE, buff.toString(), curStartPos, idx
                            + buff.length());
                    // update index position
                    idx = pos + 1;
                    syntaxContainer.setvalid(true);
                }
                break;
            case SORT_FIELD:
                if (!sortDirectionAC.validate(nextObject)) {
                    if (!sortDirectionAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_SORT_DIRECTION, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                } else {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.SORT_DIRECTION, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                    syntaxContainer.setvalid(true);
                }
                break;
            case PAGE_VALUE:
                if (curChar != ' ') {
                    syntaxContainer.setErr(SyntaxError.NOTHING_COMES_AFTER_PAGE_VALUE, curStartPos, idx + 1);
                    return syntaxContainer;
                }
                break;
            case SORT_DIRECTION:
                if (!pageAC.validate(nextObject)) {
                    if (!pageAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_PAGE_FEILD, curStartPos, idx);
                        return syntaxContainer;
                    }
                } else {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                    syntaxContainer.setvalid(true);
                }
                break;
            default:
                syntaxContainer.setErr(SyntaxError.UNIDENTIFIED_STATE, curStartPos, idx);
                return syntaxContainer;
            }
        }
        return syntaxContainer;

    }

    @Override
    public SyntaxContainer getCompletion(String searchText) {
        SyntaxContainer retval = analyzeSyntaxState(searchText, false);
        if (retval.getError() == SyntaxError.NO_ERROR) {
            IConditionFieldAutoCompleter conditionFieldAC;
            IAutoCompleter conditionRelationAC;
            IConditionValueAutoCompleter conditionValueAC;
            int lastIdx = retval.getLastHandledIndex();
            String curPartialWord = "";
            if (lastIdx < searchText.length()) {
                curPartialWord = searchText.substring(lastIdx, searchText.length());
                curPartialWord = curPartialWord.trim();
            }
            SyntaxObjectType curState = retval.getState();
            for (int idx = 0; idx < stateMap.get(curState).length; idx++) {
                switch (stateMap.get(curState)[idx]) {
                case SEARCH_OBJECT:
                    retval.addToACList(searchObjectAC.getCompletion(curPartialWord));
                    break;
                case CROSS_REF_OBJ:
                    IAutoCompleter crossRefAC = searchObjectAC.getCrossRefAutoCompleter(retval.getFirst().getBody());
                    if (crossRefAC != null) {
                        retval.addToACList(crossRefAC.getCompletion(curPartialWord));
                    }
                    break;
                case DOT:
                    retval.addToACList(dotAC.getCompletion(curPartialWord));
                    break;
                case COLON:
                    retval.addToACList(colonAC.getCompletion(curPartialWord));
                    break;
                case AND:
                    retval.addToACList(andAC.getCompletion(curPartialWord));
                    break;
                case OR:
                    retval.addToACList(orAC.getCompletion(curPartialWord));
                    break;
                case CONDITION_FIELD:
                    String relObj = retval.getPreviousSyntaxObject(1, SyntaxObjectType.CROSS_REF_OBJ);
                    conditionFieldAC = searchObjectAC.getFieldAutoCompleter(relObj);
                    if (conditionFieldAC != null) {
                        retval.addToACList(conditionFieldAC.getCompletion(curPartialWord));
                    }
                    break;
                case CONDITION_RELATION:
                    if (curState == SyntaxObjectType.CONDITION_FIELD) {
                        relObj = retval.getPreviousSyntaxObject(2, SyntaxObjectType.CROSS_REF_OBJ);
                        String fldName = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CONDITION_FIELD);
                        conditionRelationAC = searchObjectAC.getFieldRelationshipAutoCompleter(relObj, fldName);
                    } else { // curState == SyntaxObjectType.CROSS_REF_OBJ
                        relObj = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CROSS_REF_OBJ);
                        conditionRelationAC = searchObjectAC.getObjectRelationshipAutoCompleter();

                    }
                    if (conditionRelationAC != null) {
                        retval.addToACList(conditionRelationAC.getCompletion(curPartialWord));
                    }
                    break;
                case CONDITION_VALUE:
                    relObj = retval.getPreviousSyntaxObject(3, SyntaxObjectType.CROSS_REF_OBJ);
                    String fldName = retval.getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD);
                    conditionValueAC = searchObjectAC.getFieldValueAutoCompleter(relObj, fldName);
                    if (conditionValueAC != null) {
                        retval.addToACList(conditionValueAC.getCompletion(curPartialWord));
                    }
                    break;
                case SORTBY:
                    retval.addToACList(sortbyAC.getCompletion(curPartialWord));
                    break;
                case PAGE:
                    retval.addToACList(pageAC.getCompletion(curPartialWord));
                    break;
                case SORT_FIELD:
                    conditionFieldAC = searchObjectAC.getFieldAutoCompleter(retval.getSearchObjectStr());
                    if (conditionFieldAC != null) {
                        retval.addToACList(conditionFieldAC.getCompletion(curPartialWord));
                    }
                    break;
                case SORT_DIRECTION:
                    retval.addToACList(sortDirectionAC.getCompletion(curPartialWord));
                    break;
                }
            }
        }
        return retval;
    }

    @Override
    public String generateQueryFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe) {
        String retval = "";
        if (syntax.getvalid()) {
            retval = generateSqlFromSyntaxContainer(syntax, isSafe);
        }
        return retval;
    }

    private String generateFromStatement(SyntaxContainer syntax, boolean useTags) {
        LinkedList<String> innerJoins = new LinkedList<>();
        ArrayList<String> refObjList = syntax.getCrossRefObjList();
        String searchObjStr = syntax.getSearchObjectStr();
        if (refObjList.size() > 0) {
            if (SearchObjects.TEMPLATE_OBJ_NAME.equals(searchObjStr)) {
                innerJoins.addFirst(searchObjectAC.getInnerJoin(SearchObjects.TEMPLATE_OBJ_NAME,
                        SearchObjects.VM_OBJ_NAME, useTags));
                if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                    refObjList.remove(SearchObjects.VM_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.VDC_USER_OBJ_NAME)) {
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDC_USER_OBJ_NAME, true));
                    refObjList.remove(SearchObjects.VDC_USER_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.VDS_OBJ_NAME)) {
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDS_OBJ_NAME, true));
                    refObjList.remove(SearchObjects.VDS_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.AUDIT_OBJ_NAME)) {
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.AUDIT_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.AUDIT_OBJ_NAME);
                }
            } else if (SearchObjects.VDS_OBJ_NAME.equals(searchObjStr)) {
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addFirst(searchObjectAC.getInnerJoin(SearchObjects.VDS_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME, useTags));
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
                if (refObjList.contains(SearchObjects.VDC_USER_OBJ_NAME)) {
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VDS_OBJ_NAME,
                            SearchObjects.VDC_USER_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.VDC_USER_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                }
            } else if (SearchObjects.VDC_USER_OBJ_NAME.equals(searchObjStr)) {
                if (refObjList.contains(SearchObjects.VDS_OBJ_NAME)) {
                    innerJoins.addFirst(searchObjectAC.getInnerJoin(SearchObjects.VDC_USER_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME, useTags));
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
                if (refObjList.contains(SearchObjects.VDS_OBJ_NAME)) {
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDS_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.VDS_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VDC_USER_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                }
            } else if (SearchObjects.AUDIT_OBJ_NAME.equals(searchObjStr)) {
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addFirst(searchObjectAC.getInnerJoin(SearchObjects.AUDIT_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME, useTags));
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
            } else if (SearchObjects.DISK_OBJ_NAME.equals(searchObjStr)) {
                if (refObjList.contains(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
                    innerJoins.addFirst(searchObjectAC.getInnerJoin(SearchObjects.DISK_OBJ_NAME,
                            SearchObjects.VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME, useTags));
                    innerJoins.addLast(searchObjectAC.getInnerJoin(SearchObjects.VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME,
                            SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
                }
            }
        }
        for (String cro : refObjList) {
            innerJoins.addLast(searchObjectAC.getInnerJoin(searchObjStr, cro, useTags));
        }
        innerJoins.addFirst(searchObjectAC.getRelatedTableName(searchObjStr, useTags));
        StringBuilder sb = new StringBuilder();
        for (String part : innerJoins) {
            sb.append(" ");
            sb.append(part);
            sb.append(" ");
        }
        return sb.toString();

    }

    private String generateSqlFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe) {
        String retval = "";
        if (syntax.getvalid()) {
            ListIterator<SyntaxObject> objIter = syntax.listIterator(0);
            IConditionFieldAutoCompleter conditionFieldAC;
            LinkedList<String> whereBuilder = new LinkedList<>();
            String searchObjStr = syntax.getSearchObjectStr();
            String sortByPhrase = "";
            String fromStatement = "";
            String pageNumber = "";
            boolean useTags = syntax.isSearchUsingTags();
            List<SortByElement> sortByElements = null;
            boolean sortAscending = true;

            while (objIter.hasNext()) {
                SyntaxObject obj = objIter.next();
                switch (obj.getType()) {
                case SEARCH_OBJECT:
                    fromStatement = generateFromStatement(syntax, useTags);
                    break;
                case OR:
                case AND:
                    whereBuilder.addLast(obj.getBody());
                    break;
                case CONDITION_VALUE:
                    ConditionData conditionData =
                            generateConditionStatment(obj,
                                    syntax.listIterator(objIter.previousIndex()),
                                    searchObjStr,
                                    syntax.getCaseSensitive(),
                                    isSafe,
                                    useTags);
                    whereBuilder.addLast(conditionData.getConditionText());
                    if (conditionData.isFullTableRequired() && !useTags) {
                        useTags = true;
                        fromStatement = generateFromStatement(syntax, useTags);
                    }
                    break;
                case SORTBY:
                    break;
                case PAGE_VALUE:
                    pageNumber = obj.getBody();
                    break;
                case SORT_FIELD:
                    conditionFieldAC = searchObjectAC.getFieldAutoCompleter(searchObjStr);
                    sortByElements = conditionFieldAC.getSortByElements(obj.getBody());
                    break;
                case SORT_DIRECTION:
                    sortAscending = !obj.getBody().equalsIgnoreCase("desc");
                    break;
                default:
                    break;
                }
            }

            if (sortByElements != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(" ORDER BY ");
                for(SortByElement sortByElement: sortByElements) {
                    builder.append(sortByElement.getExpression()).append(" ");
                    final boolean ascending = sortAscending == sortByElement.isAscending();
                    builder.append(ascending ? "ASC NULLS FIRST" : "DESC NULLS LAST").append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                sortByPhrase = builder.toString();
            }

            // implying precedence rules
            String[] lookFor = { "AND", "OR" };
            for (int idx = 0; idx < lookFor.length; idx++) {
                boolean found = true;
                while (found) {
                    found = false;
                    ListIterator<String> iter = whereBuilder.listIterator(0);
                    while (iter.hasNext()) {
                        String queryPart = iter.next();
                        if (lookFor[idx].equals(queryPart)) {
                            iter.remove();
                            String nextPart = iter.next();
                            iter.remove();
                            String prevPart = iter.previous();
                            iter.set(StringFormat.format("( %1$s %2$s %3$s )", prevPart, queryPart, nextPart));
                            found = true;
                            break;
                        }
                    }
                }
            }

            // Since when we search for events we must only search
            // for not deleted events, add this to the where clause
            if (searchObjStr.equalsIgnoreCase("EVENT")) {
                whereBuilder.add("not deleted");
            }

            // adding WHERE if required and All implicit AND
            StringBuilder wherePhrase = new StringBuilder();
            if (whereBuilder.size() > 0) {
                wherePhrase.append(" WHERE ");
                ListIterator<String> iter = whereBuilder.listIterator(0);
                while (iter.hasNext()) {
                    String queryPart = iter.next();
                    wherePhrase.append(queryPart);
                    if (iter.hasNext()) {
                        wherePhrase.append(" AND ");
                    }
                }
            }

            // adding the sorting part if required
            if ("".equals(sortByPhrase)) {
                sortByPhrase = " ORDER BY " + searchObjectAC.getDefaultSort(searchObjStr);
            }
            // adding the paging phrase
            String pagePhrase = getPagePhrase(syntax, pageNumber);
            String primeryKey = searchObjectAC.getPrimeryKeyName(searchObjStr);
            String tableName = searchObjectAC.getRelatedTableName(searchObjStr, useTags);
            boolean usingDistinct = searchObjectAC.isUsingDistinct(searchObjStr);

            // adding a secondary default sort by entity name
            StringBuilder sortExpr = new StringBuilder();
            sortExpr.append(sortByPhrase);
            if (!sortByPhrase.contains(searchObjectAC.getDefaultSort(searchObjStr))) {
                sortExpr.append(",");
                sortExpr.append(searchObjectAC.getDefaultSort(searchObjStr));
            }

            // TODO: The database configuration PostgresSearchTemplate has an extra closing braces. Hence our
            // queries in this code have an extra opening one. Fix it in a future patch.

            String inQuery = "";
            if (useTags) {
                inQuery = StringFormat.format(
                        "SELECT * FROM %1$s WHERE ( %2$s IN (%3$s)",
                                searchObjectAC.getRelatedTableName(searchObjStr, false),
                                primeryKey,
                                getInnerQuery(tableName, primeryKey, fromStatement, wherePhrase, sortExpr, true));
            } else {
                inQuery = "(" + getInnerQuery(tableName, "*", fromStatement, wherePhrase, sortExpr, usingDistinct);
            }
            if (syntax.getSearchFrom() > 0) {
                inQuery = StringFormat.format("%1$s and  %2$s >  %3$s", inQuery, primeryKey, syntax.getSearchFrom());
            }
            // Prevent duplicate records when cross reference is used.
            if (inQuery.contains("LEFT OUTER JOIN") && ! inQuery.contains("distinct")) {
                inQuery = inQuery.replaceFirst("SELECT ", "SELECT  distinct ");
            }
            retval =
                    StringFormat.format(Config.getValue(ConfigValues.DBSearchTemplate),
                            sortExpr.toString(),
                            inQuery,
                            pagePhrase);
            // Check for sql injection if query is not safe
            if (!isSafe) {
                if (sqlInjectionChecker.hasSqlInjection(retval)) {
                    throw new SqlInjectionException();
                }
            }
            log.trace("Search: {}", retval);
        }
        return retval;
    }

    private String getInnerQuery(String tableName, String primeryKey, String fromStatement, StringBuilder wherePhrase, StringBuilder sortExpr, boolean useDistinct) {
        // prevent using distinct when the sort expression has a function call since when distinct is used it is performed first and sorting
        // is done on the result, so all fields in the sort clause should appear in the result set after distinct is applied

        if (sortExpr.indexOf("(") > 0) {
            return StringFormat.format("SELECT %1$s.%2$s FROM %3$s %4$s", tableName, primeryKey, fromStatement, wherePhrase);
        } else {
            return StringFormat.format("SELECT %5$s %1$s.%2$s FROM %3$s %4$s", tableName, primeryKey, fromStatement, wherePhrase, useDistinct ? "distinct" : "");
        }
    }

    protected String getPagePhrase(SyntaxContainer syntax, String pageNumber) {
        String result = "";
        Integer page = IntegerCompat.tryParse(pageNumber);
        if (page == null) {
            page = 1;
        }
        PagingType pagingType = getPagingType();
        if (pagingType != null) {
            String pagingSyntax = Config.getValue(ConfigValues.DBPagingSyntax);
            BigInteger bigPage = BigInteger.valueOf(page);
            BigInteger bigCount = BigInteger.valueOf(syntax.getMaxCount());
            BigInteger bigX = bigPage.subtract(BigInteger.ONE).multiply(bigCount).add(BigInteger.ONE);
            BigInteger bigY = bigPage.multiply(bigCount);
            switch (pagingType) {
            case Range:
                result =
                        StringFormat.format(pagingSyntax, bigX, bigY);
                break;
            case Offset:
                result = StringFormat.format(pagingSyntax, bigX, bigCount);
                break;
            }
        }

        return result;

    }

    private PagingType getPagingType() {
        String val = Config.getValue(ConfigValues.DBPagingType);
        PagingType type = null;
        try {
            type = PagingType.valueOf(val);
        } catch (Exception e) {
            log.error("Unknown paging type '{}'", val);
        }

        return type;
    }

    /**
     * It describes one element of SQL 'ORDER BY' clause
     */
    public static class SortByElement {

        /**
         * DB value expression, usually column just column name
         *
         * <p>Example of non-trivial value:
         * <pre>"COALESCE(nullable_column_name, 'string replacing NULL')"</pre>
         * </p>
         */
        private final String expression;
        /**
         * asc/desc
         */
        private final boolean ascending;

        public SortByElement(String expression) {
            this.expression = expression;
            this.ascending = true;
        }

        public SortByElement(String expression, boolean ascending) {
            this.expression = expression;
            this.ascending = ascending;
        }

        public String getExpression() {
            return expression;
        }

        public boolean isAscending() {
            return ascending;
        }
    }

    private enum ConditionType {
        None,
        FreeText,
        FreeTextSpecificObj,
        ConditionWithDefaultObj,
        ConditionwithSpesificObj;
    }

    private ConditionData generateConditionStatment(SyntaxObject obj, ListIterator<SyntaxObject> objIter,
            final String searchObjStr, final boolean caseSensitive, final boolean issafe, final boolean useTags) {
        final String safeValue = issafe ? obj.getBody() : SqlInjectionChecker.enforceEscapeCharacters(obj.getBody());
        return generateSafeConditionStatement(obj, objIter, searchObjStr, caseSensitive, safeValue, useTags);
    }

    private ConditionData generateSafeConditionStatement(final SyntaxObject obj,
            ListIterator<SyntaxObject> objIter,
            final String searchObjStr,
            final boolean caseSensitive,
            final String safeValue,
            final boolean useTags) {
        IConditionFieldAutoCompleter conditionFieldAC;
        IConditionValueAutoCompleter conditionValueAC = null;
        // check for sql injection
        String fieldName = "";
        String objName;
        ConditionType conditionType;
        SyntaxObject previous = objIter.previous();
        SyntaxObject prev = previous;
        SyntaxObjectType prevType = prev.getType();
        if (prevType != SyntaxObjectType.CONDITION_RELATION) {
            // free text of default search object
            objName = searchObjStr;
            conditionFieldAC = searchObjectAC.getFieldAutoCompleter(searchObjStr);
            conditionType = ConditionType.FreeText;
        } else {
            prev = objIter.previous();
            if (prev.getType() == SyntaxObjectType.CROSS_REF_OBJ) { // free text
                                                                    // search
                                                                    // for some
                                                                    // object
                objName = prev.getBody();
                conditionFieldAC = searchObjectAC.getFieldAutoCompleter(objName);
                conditionType = ConditionType.FreeTextSpecificObj;
            } else { // if (prev.getType() == SyntaxObjectType.CONDITION_FIELD)
                fieldName = prev.getBody();
                prev = objIter.previous();
                if (prev.getType() != SyntaxObjectType.DOT) {
                    // standard condition with default AC (search obj)
                    objName = searchObjStr;
                    conditionFieldAC = searchObjectAC.getFieldAutoCompleter(searchObjStr);
                    conditionType = ConditionType.ConditionWithDefaultObj;
                } else {
                    // standard condition with specific AC
                    prev = objIter.previous();
                    objName = prev.getBody();
                    if (SearchObjects.VDS_OBJ_NAME.equals(objName) && SearchObjects.AUDIT_OBJ_NAME.equals(searchObjStr)) {
                        objName = SearchObjects.VDS_OBJ_NAME + SearchObjects.AUDIT_OBJ_NAME;
                    }
                    conditionFieldAC = searchObjectAC.getFieldAutoCompleter(objName);
                    conditionType = ConditionType.ConditionwithSpesificObj;
                }
            }
            conditionValueAC = conditionFieldAC.getFieldValueAutoCompleter(fieldName);
        }

        final BaseConditionFieldAutoCompleter conditionAsBase =
                (BaseConditionFieldAutoCompleter) ((conditionFieldAC instanceof BaseConditionFieldAutoCompleter) ? conditionFieldAC
                        : null);
        final Class<?> curType = conditionAsBase != null ? conditionAsBase.getTypeDictionary().get(fieldName) : null;
        final String customizedValue =
                buildCustomizedValue(obj, conditionFieldAC, conditionValueAC, safeValue, fieldName, curType);

        final String customizedRelation =
                buildCustomizedRelation(caseSensitive,
                        conditionFieldAC,
                        conditionValueAC,
                        fieldName,
                        previous,
                        prevType);

        return buildCondition(caseSensitive,
                conditionFieldAC,
                escapeUnderScore(customizedValue, customizedRelation),
                customizedRelation,
                fieldName,
                objName,
                conditionType,
                useTags);
    }

    private String buildCustomizedRelation(final boolean caseSensitive,
            IConditionFieldAutoCompleter conditionFieldAC,
            IConditionValueAutoCompleter conditionValueAC,
            String fieldName,
            SyntaxObject previous,
            SyntaxObjectType prevType) {
        String customizedRelation = "=";
        if (prevType == SyntaxObjectType.CONDITION_RELATION) {
            customizedRelation = previous.getBody();
        }

        if (conditionValueAC == null && ("".equals(fieldName) ||
                String.class.equals(conditionFieldAC.getDbFieldType(fieldName)))) {
            /* enable case-insensitive search by changing operation to I/LIKE */
            if ("=".equals(customizedRelation)) {
                customizedRelation = conditionFieldAC.getMatchingSyntax(fieldName, true, caseSensitive);
            } else if ("!=".equals(customizedRelation)) {
                customizedRelation = conditionFieldAC.getMatchingSyntax(fieldName, false, caseSensitive);
            }
        }
        return customizedRelation;
    }

    private String buildCustomizedValue(SyntaxObject obj,
            IConditionFieldAutoCompleter conditionFieldAC,
            IConditionValueAutoCompleter conditionValueAC,
            String safeValue,
            String fieldName,
            final Class<?> curType) {
        String customizedValue = safeValue;
        if (curType == String.class && !StringHelper.isNullOrEmpty(customizedValue)
                && !"''".equals(customizedValue) && !"'*'".equals(customizedValue)) {
            customizedValue = BaseConditionFieldAutoCompleter.getI18NPrefix() + customizedValue;
        }

        if (conditionValueAC != null) {
            customizedValue = StringFormat.format("'%1$s'",
                    conditionValueAC.convertFieldEnumValueToActualValue(obj.getBody()));
        } else if ("".equals(fieldName) /* search on all relevant fields */||
                String.class.equals(conditionFieldAC.getDbFieldType(fieldName))) {
            customizedValue = customizedValue.replace("*", conditionFieldAC.getWildcard(fieldName));
        }
        return customizedValue;
    }

    final ConditionData buildCondition(boolean caseSensitive,
            IConditionFieldAutoCompleter conditionFieldAC,
            String customizedValue,
            String customizedRelation,
            String fieldName,
            String objName,
            ConditionType conditionType,
            boolean useTags) {

        String tableName;

        // We will take the table with tags for all subtables
        // TODO: Optimize this
        if (conditionType == ConditionType.ConditionwithSpesificObj) {
            tableName = searchObjectAC.getRelatedTableName(objName, true);
        } else {
            tableName = searchObjectAC.getRelatedTableName(objName, fieldName, useTags);
        }
        if (tableName.indexOf(")") != -1) {
            tableName = tableName.substring(tableName.indexOf(")") + 1).trim();
        }
        ConditionData conditionData = new ConditionData();
        switch (conditionType) {
        case FreeText:
        case FreeTextSpecificObj:
            conditionData.setConditionText(conditionFieldAC.buildFreeTextConditionSql(tableName,
                    customizedRelation,
                    customizedValue,
                    caseSensitive));
            conditionData.setFullTableRequired(true);
            break;
        case ConditionWithDefaultObj:
        case ConditionwithSpesificObj:
            conditionData.setConditionText(conditionFieldAC.buildConditionSql(objName,
                    fieldName,
                    customizedValue,
                    customizedRelation,
                    tableName,
                    caseSensitive));
            conditionData.setFullTableRequired(false);
            break;
        default:
            conditionData.setConditionText("");
            conditionData.setFullTableRequired(false);
        }
        return conditionData;
    }

    public static String escapeUnderScore(final String customizedValue, final String customizedRelation) {
        String escapedValue = customizedValue;
        if (customizedRelation.equalsIgnoreCase("LIKE") || customizedRelation.equalsIgnoreCase("ILIKE")) {
            // Since '_' is treated in Postgres as '?' when using like, (i.e. match any single character)
            // we have to escape this character in the value to make it treated as a regular character.
            escapedValue = customizedValue.replace("_", "\\_");
        }
        return escapedValue;
    }

    private static class ConditionData {
        private String conditionText;
        private boolean fullTableRequired = false;

        public String getConditionText() {
            return conditionText;
        }

        public void setConditionText(String conditionText) {
            this.conditionText = conditionText;
        }

        public boolean isFullTableRequired() {
            return fullTableRequired;
        }

        public void setFullTableRequired(boolean fullTableRequired) {
            this.fullTableRequired = fullTableRequired;
        }

    }
}

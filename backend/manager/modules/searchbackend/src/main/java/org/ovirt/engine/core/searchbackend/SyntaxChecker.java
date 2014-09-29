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

    public static final String TAG_COLUMN_NAME_IN_CRITERIA = "tag_name";

    private static final Logger log = LoggerFactory.getLogger(SyntaxChecker.class);

    public static final String SORTBY = "SORTBY";
    public static final String PAGE = "PAGE";
    public static final String SORTDIR_ASC = "ASC";
    public static final String SORTDIR_DESC = "DESC";
    public static final String PAGE = "PAGE";

    private final SearchObjectAutoCompleter mSearchObjectAC;
    private final BaseAutoCompleter mColonAC;
    private final BaseAutoCompleter mPluralAC;
    private final BaseAutoCompleter mSortbyAC;
    private final BaseAutoCompleter mPageAC;
    private final BaseAutoCompleter mAndAC;
    private final BaseAutoCompleter mOrAC;
    private final BaseAutoCompleter mDotAC;
    private final BaseAutoCompleter mSortDirectionAC;
    private final Map<SyntaxObjectType, SyntaxObjectType[]> mStateMap;

    private final Regex mFirstDQRegexp;
    private final Regex mNonSpaceRegexp;
    private final List<Character> mDisAllowedChars;
    private SqlInjectionChecker sqlInjectionChecker;

    public SyntaxChecker(int searchReasultsLimit) {

        mSearchObjectAC = new SearchObjectAutoCompleter();
        mColonAC = new BaseAutoCompleter(":");
        mPluralAC = new BaseAutoCompleter("S");
        mSortbyAC = new BaseAutoCompleter(SORTBY);
        mPageAC = new BaseAutoCompleter(PAGE);
        mSortDirectionAC = new BaseAutoCompleter(SORTDIR_ASC, SORTDIR_DESC);
        mAndAC = new BaseAutoCompleter("AND");
        mOrAC = new BaseAutoCompleter("OR");
        mDotAC = new BaseAutoCompleter(".");
        mDisAllowedChars = new ArrayList<Character>(Arrays.asList(new Character[] { '\'', ';' }));

        mFirstDQRegexp = new Regex("^\\s*\"$");
        mNonSpaceRegexp = new Regex("^\\S+$");

        mStateMap = new HashMap<SyntaxObjectType, SyntaxObjectType[]>();
        mStateMap.put(SyntaxObjectType.BEGIN, new SyntaxObjectType[] { SyntaxObjectType.SEARCH_OBJECT });
        mStateMap.put(SyntaxObjectType.SEARCH_OBJECT, new SyntaxObjectType[] { SyntaxObjectType.COLON });
        SyntaxObjectType[] afterColon =
        { SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD,
                SyntaxObjectType.SORTBY, SyntaxObjectType.PAGE, SyntaxObjectType.CONDITION_VALUE,
                SyntaxObjectType.END };
        mStateMap.put(SyntaxObjectType.COLON, afterColon);

        SyntaxObjectType[] afterCrossRefObj = { SyntaxObjectType.DOT, SyntaxObjectType.CONDITION_RELATION };
        mStateMap.put(SyntaxObjectType.CROSS_REF_OBJ, afterCrossRefObj);
        mStateMap.put(SyntaxObjectType.DOT, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_FIELD });

        mStateMap.put(SyntaxObjectType.CONDITION_FIELD, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_RELATION });
        mStateMap.put(SyntaxObjectType.CONDITION_RELATION, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_VALUE });
        SyntaxObjectType[] afterConditionValue = { SyntaxObjectType.OR, SyntaxObjectType.AND,
                SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD, SyntaxObjectType.SORTBY,
                SyntaxObjectType.PAGE, SyntaxObjectType.CONDITION_VALUE };
        mStateMap.put(SyntaxObjectType.CONDITION_VALUE, afterConditionValue);

        SyntaxObjectType[] AndOrArray = { SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD,
                SyntaxObjectType.CONDITION_VALUE };
        mStateMap.put(SyntaxObjectType.AND, AndOrArray);
        mStateMap.put(SyntaxObjectType.OR, AndOrArray);

        mStateMap.put(SyntaxObjectType.SORTBY, new SyntaxObjectType[] { SyntaxObjectType.SORT_FIELD });
        mStateMap.put(SyntaxObjectType.SORT_FIELD, new SyntaxObjectType[] { SyntaxObjectType.SORT_DIRECTION });
        mStateMap.put(SyntaxObjectType.SORT_DIRECTION, new SyntaxObjectType[] { SyntaxObjectType.PAGE });

        mStateMap.put(SyntaxObjectType.PAGE, new SyntaxObjectType[] { SyntaxObjectType.PAGE_VALUE });
        mStateMap.put(SyntaxObjectType.PAGE_VALUE, new SyntaxObjectType[] { SyntaxObjectType.END });
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
            betweenDoubleQuotes = (!betweenDoubleQuotes);
            if (betweenDoubleQuotes) {
                if (!mFirstDQRegexp.IsMatch(strRealObj)) {
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
            if (((curChar == ' ') || (idx + 1 == searchText.length())) && (betweenDoubleQuotes == false)
                    && (addObjFlag == false)) {
                strRealObj = strRealObj.trim();
                if (mNonSpaceRegexp.IsMatch(strRealObj)) {
                    addObjFlag = true;
                } else {
                    startPos.argvalue = idx + 1;
                }
            }
        } else {
            if ((curChar == ' ') && (betweenDoubleQuotes == false) && (addObjFlag == false)) {
                strRealObj = strRealObj.trim();
                if (mNonSpaceRegexp.IsMatch(strRealObj)) {
                    addObjFlag = true;
                } else {
                    startPos.argvalue = idx + 1;
                }
            }
        }
        if (addObjFlag) {
            String curRefObj = container.getPreviousSyntaxObject(3, SyntaxObjectType.CROSS_REF_OBJ);
            String curConditionField = container.getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD);
            curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(curRefObj);
            if (curConditionFieldAC == null) {
                container.setErr(SyntaxError.CANT_GET_CONDITION_FIELD_AC, startPos.argvalue, idx);
                return ValueParseResult.Err;
            }
            if ((!"".equals(curConditionField))
                    && (!curConditionFieldAC.validateFieldValue(curConditionField, strRealObj))) {
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
     *
     * @return SqlInjectionChecker
     * @throws Exception
     */
    private SqlInjectionChecker getSqlInjectionChecker() throws Exception {
        // This can not be done with reflection like:
        // return (SqlInjectionChecker) Class.forName(props.getProperty(SQL_INJECTION)).newInstance();
        // GWT lacks support of reflection.
        if (((String) Config.getValue(ConfigValues.DBEngine)).equalsIgnoreCase("postgres")) {
            return new PostgresSqlInjectionChecker();
        }
        else {
            throw new IllegalStateException("Failed to get correct sql injection checker instance name :"
                    + SqlInjectionChecker.class);
        }

    }

    @Override
    public SyntaxContainer analyzeSyntaxState(final String searchText, boolean final2) {
        final SyntaxContainer syntaxContainer = new SyntaxContainer(searchText);
        IConditionFieldAutoCompleter curConditionFieldAC = null;
        IAutoCompleter curConditionRelationAC = null;
        final List<String> freeTextObjSearched = new ArrayList<String>();
        char[] searchCharArr = searchText.toCharArray();
        int curStartPos = 0;

        String tryNextObj = "";
        boolean keepValid;
        for (int idx = 0; idx < searchCharArr.length; idx++) {
            final SyntaxObjectType curState = syntaxContainer.getState();
            final char curChar = searchCharArr[idx];
            if (mDisAllowedChars.contains(curChar)) {
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
                if (!mSearchObjectAC.validate(nextObject)) {
                    if (!mSearchObjectAC.validateCompletion(nextObject)) {
                        syntaxContainer.setErr(SyntaxError.INVALID_SEARCH_OBJECT, curStartPos, idx - curStartPos + 1);
                        return syntaxContainer;
                    }
                } else {
                    if (searchCharArr.length >= idx + 2) { // Check that this
                                                         // maybe a plural
                        // Validate that the next character is an 's'
                        if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
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

                if (!mColonAC.validate(nextObject)) {
                    if (!mColonAC.validateCompletion(nextObject)) {
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
                curConditionRelationAC = mSearchObjectAC.getObjectRelationshipAutoCompleter();
                if (idx + 1 < searchCharArr.length) {
                    tryNextObj = searchText.substring(curStartPos, idx + 2).toUpperCase();
                }
                if (curConditionRelationAC == null) {
                    syntaxContainer.setErr(SyntaxError.CONDITION_CANT_CREATE_RRELATIONS_AC, curStartPos, idx + 1);
                    return syntaxContainer;
                }
                if (mDotAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.DOT, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if ((!"".equals(tryNextObj)) && (curConditionRelationAC.validate(tryNextObj))) {
                    break; // i.e. the relation object has another charecter
                } else if (curConditionRelationAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_RELATION,
                            nextObject,
                            curStartPos,
                            idx + 1);
                    curStartPos = idx + 1;

                } else if ((!curConditionRelationAC.validateCompletion(nextObject))
                        && (!mDotAC.validateCompletion(nextObject))) {
                    syntaxContainer.setErr(SyntaxError.INVALID_POST_CROSS_REF_OBJ, curStartPos, idx + 1);
                    return syntaxContainer;
                }
                tryNextObj = "";
                break;
            case DOT:
                curRefObj = syntaxContainer.getPreviousSyntaxObject(1, SyntaxObjectType.CROSS_REF_OBJ);
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(curRefObj);
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
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(syntaxContainer.getSearchObjectStr());
                if (curConditionFieldAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;

                } else if (mSearchObjectAC.isCrossReference(nextObject, syntaxContainer.getFirst().getBody())) {
                    if (searchCharArr.length >= idx + 2) { // Check that this
                                                         // maybe a plural
                        // Validate that the next character is an 's'
                        if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
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
                    RefObject<Integer> tempRefObject = new RefObject<Integer>(curStartPos);
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
                    } else if ((!curConditionFieldAC.validateCompletion(nextObject))
                            && (!mSearchObjectAC.validateCompletion(nextObject))) {
                        syntaxContainer.setErr(SyntaxError.INVALID_POST_OR_AND_PHRASE, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                }
                if (keepValid == false) {
                    syntaxContainer.setvalid(false);
                }
                break;
            case COLON:
                keepValid = false;
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(syntaxContainer.getSearchObjectStr());
                if (curConditionFieldAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;

                } else if (mSortbyAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.SORTBY, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if (mPageAC.validate(nextObject)) {
                    syntaxContainer.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if (mSearchObjectAC.isCrossReference(nextObject, syntaxContainer.getFirst().getBody())) {
                    if (searchCharArr.length >= idx + 2) { // Check that this
                                                         // maybe a plural
                        // Validate that the next character is an 's'
                        if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
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
                    RefObject<Integer> tempRefObject2 = new RefObject<Integer>(curStartPos);
                    ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject2, syntaxContainer);
                    curStartPos = tempRefObject2.argvalue;
                    if (ans != ValueParseResult.Err) {
                        if (ans == ValueParseResult.FreeText) {
                            freeTextObjSearched.add(syntaxContainer.getSearchObjectStr());
                        }
                        keepValid = true;
                    } else if ((!curConditionFieldAC.validateCompletion(nextObject))
                            && (!mSortbyAC.validateCompletion(nextObject))
                            && (!mSearchObjectAC.validateCompletion(nextObject))) {
                        syntaxContainer.setErr(SyntaxError.INVALID_POST_COLON_PHRASE, curStartPos, idx + 1);
                        return syntaxContainer;
                    }
                }
                if (keepValid == false) {
                    syntaxContainer.setvalid(false);
                }
                break;
            case CONDITION_VALUE:
                nextObject = nextObject.trim();
                if (nextObject.length() > 0) {
                    keepValid = false;
                    curRefObj = syntaxContainer.getSearchObjectStr();
                    curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(curRefObj);
                    if (curConditionFieldAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD,
                                nextObject,
                                curStartPos,
                                idx + 1);
                        curStartPos = idx + 1;

                    } else if (mSortbyAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.SORTBY, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (mPageAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (mSearchObjectAC.isCrossReference(nextObject, syntaxContainer.getFirst().getBody())) {
                        if (searchCharArr.length >= idx + 2) { // Check that this
                                                             // maybe a
                                                             // plural
                            // Validate that the next character is an 's'
                            if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
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
                    } else if (mAndAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.AND, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (mOrAC.validate(nextObject)) {
                        syntaxContainer.addSyntaxObject(SyntaxObjectType.OR, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    }

                    else if ((!curConditionFieldAC.validateCompletion(nextObject))
                            && (!mSortbyAC.validateCompletion(nextObject))
                            && (!mSearchObjectAC.validateCompletion(nextObject))
                            && (!mAndAC.validateCompletion(nextObject)) && (!mOrAC.validateCompletion(nextObject))) {
                        RefObject<Integer> tempRefObject3 = new RefObject<Integer>(curStartPos);
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
                    if (keepValid == false) {
                        syntaxContainer.setvalid(false);
                    }
                }
                break;
            case CONDITION_FIELD:
                curRefObj = syntaxContainer.getPreviousSyntaxObject(2, SyntaxObjectType.CROSS_REF_OBJ);
                String curConditionField = syntaxContainer.getPreviousSyntaxObject(0, SyntaxObjectType.CONDITION_FIELD);
                curConditionRelationAC = mSearchObjectAC
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
            case CONDITION_RELATION: {
                RefObject<Integer> tempRefObject4 = new RefObject<Integer>(curStartPos);
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
            }
                break;
            case SORTBY:
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(syntaxContainer.getSearchObjectStr());
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
                if (!mSortDirectionAC.validate(nextObject)) {
                    if (!mSortDirectionAC.validateCompletion(nextObject)) {
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
                if (!mPageAC.validate(nextObject)) {
                    if (!mPageAC.validateCompletion(nextObject)) {
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
            for (int idx = 0; idx < mStateMap.get(curState).length; idx++) {
                switch (mStateMap.get(curState)[idx]) {
                case SEARCH_OBJECT:
                    retval.addToACList(mSearchObjectAC.getCompletion(curPartialWord));
                    break;
                case CROSS_REF_OBJ:
                    IAutoCompleter crossRefAC = mSearchObjectAC.getCrossRefAutoCompleter(retval.getFirst().getBody());
                    if (crossRefAC != null) {
                        retval.addToACList(crossRefAC.getCompletion(curPartialWord));
                    }
                    break;
                case DOT:
                    retval.addToACList(mDotAC.getCompletion(curPartialWord));
                    break;
                case COLON:
                    retval.addToACList(mColonAC.getCompletion(curPartialWord));
                    break;
                case AND:
                    retval.addToACList(mAndAC.getCompletion(curPartialWord));
                    break;
                case OR:
                    retval.addToACList(mOrAC.getCompletion(curPartialWord));
                    break;
                case CONDITION_FIELD:
                    String relObj = retval.getPreviousSyntaxObject(1, SyntaxObjectType.CROSS_REF_OBJ);
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(relObj);
                    if (conditionFieldAC != null) {
                        retval.addToACList(conditionFieldAC.getCompletion(curPartialWord));
                    }
                    break;
                case CONDITION_RELATION: {
                    if (curState == SyntaxObjectType.CONDITION_FIELD) {
                        relObj = retval.getPreviousSyntaxObject(2, SyntaxObjectType.CROSS_REF_OBJ);
                        String fldName = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CONDITION_FIELD);
                        conditionRelationAC = mSearchObjectAC.getFieldRelationshipAutoCompleter(relObj, fldName);
                    } else { // curState == SyntaxObjectType.CROSS_REF_OBJ
                        relObj = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CROSS_REF_OBJ);
                        conditionRelationAC = mSearchObjectAC.getObjectRelationshipAutoCompleter();

                    }
                    if (conditionRelationAC != null) {
                        retval.addToACList(conditionRelationAC.getCompletion(curPartialWord));
                    }
                }
                    break;
                case CONDITION_VALUE: {
                    relObj = retval.getPreviousSyntaxObject(3, SyntaxObjectType.CROSS_REF_OBJ);
                    String fldName = retval.getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD);
                    conditionValueAC = mSearchObjectAC.getFieldValueAutoCompleter(relObj, fldName);
                    if (conditionValueAC != null) {
                        retval.addToACList(conditionValueAC.getCompletion(curPartialWord));
                    }
                }
                    break;
                case SORTBY:
                    retval.addToACList(mSortbyAC.getCompletion(curPartialWord));
                    break;
                case PAGE:
                    retval.addToACList(mPageAC.getCompletion(curPartialWord));
                    break;
                case SORT_FIELD:
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(retval.getSearchObjectStr());
                    if (conditionFieldAC != null) {
                        retval.addToACList(conditionFieldAC.getCompletion(curPartialWord));
                    }
                    break;
                case SORT_DIRECTION:
                    retval.addToACList(mSortDirectionAC.getCompletion(curPartialWord));
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
        LinkedList<String> innerJoins = new LinkedList<String>();
        ArrayList<String> refObjList = syntax.getCrossRefObjList();
        String searchObjStr = syntax.getSearchObjectStr();
        if (refObjList.size() > 0) {
            if (SearchObjects.TEMPLATE_OBJ_NAME.equals(searchObjStr)) {
                innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.TEMPLATE_OBJ_NAME,
                        SearchObjects.VM_OBJ_NAME, useTags));
                if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                    refObjList.remove(SearchObjects.VM_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.VDC_USER_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDC_USER_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.VDC_USER_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.VDS_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDS_OBJ_NAME, true));
                    refObjList.remove(SearchObjects.VDS_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.AUDIT_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.AUDIT_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.AUDIT_OBJ_NAME);
                }
            }
            else if (SearchObjects.VDS_OBJ_NAME.equals(searchObjStr)) {
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.VDS_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME, useTags));
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
                if (refObjList.contains(SearchObjects.VDC_USER_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VDS_OBJ_NAME,
                            SearchObjects.VDC_USER_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.VDC_USER_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                }
            }
            else if (SearchObjects.VDC_USER_OBJ_NAME.equals(searchObjStr)) {
                if ((refObjList.contains(SearchObjects.VDS_OBJ_NAME))) {
                    innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.VDC_USER_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME, useTags));
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
                if (refObjList.contains(SearchObjects.VDS_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDS_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.VDS_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VDC_USER_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                }
            }
            else if (SearchObjects.AUDIT_OBJ_NAME.equals(searchObjStr)) {
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.AUDIT_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME, useTags));
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
            }
            else if (SearchObjects.DISK_OBJ_NAME.equals(searchObjStr)) {
                if (refObjList.contains(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
                    innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.DISK_OBJ_NAME,
                            SearchObjects.VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME, useTags));
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME,
                            SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, useTags));
                    refObjList.remove(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
                }
            }
        }
        for (String cro : refObjList) {
            innerJoins.addLast(mSearchObjectAC.getInnerJoin(searchObjStr, cro, useTags));
        }
        innerJoins.addFirst(mSearchObjectAC.getRelatedTableName(searchObjStr, useTags));
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
            LinkedList<String> whereBuilder = new LinkedList<String>();
            String searchObjStr = syntax.getSearchObjectStr();
            String sortByPhrase = "";
            String fromStatement = "";
            String pageNumber = "";
            boolean useTags = syntax.isSearchUsingTags();

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
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(searchObjStr);
                    sortByPhrase =
                            StringFormat.format(" ORDER BY %1$s", conditionFieldAC.getSortableDbField(obj.getBody()));
                    break;
                case SORT_DIRECTION:
                    // Forcing any sorting using DESC to show NULL values last (NULLS FIRST is the default)
                    String direction = (obj.getBody().equalsIgnoreCase("desc")) ? "DESC NULLS LAST" : obj.getBody();
                    sortByPhrase = StringFormat.format("%1$s %2$s", sortByPhrase, direction);
                    break;
                default:
                    break;
                }
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
                sortByPhrase = " ORDER BY " + mSearchObjectAC.getDefaultSort(searchObjStr);
            }
            // adding the paging phrase
            String pagePhrase = getPagePhrase(syntax, pageNumber);
            String primeryKey = mSearchObjectAC.getPrimeryKeyName(searchObjStr);
            String tableName = mSearchObjectAC.getRelatedTableName(searchObjStr, useTags);

            // adding a secondary default sort by entity name
            StringBuilder sortExpr = new StringBuilder();
            sortExpr.append(sortByPhrase);
            if ( sortByPhrase.indexOf(mSearchObjectAC.getDefaultSort(searchObjStr)) < 0) {
                sortExpr.append(",");
                sortExpr.append(mSearchObjectAC.getDefaultSort(searchObjStr));
            }

            // TODO: The database configuration PostgresSearchTemplate has an extra closing braces. Hence our
            // queries in this code have an extra opening one. Fix it in a future patch.

            String inQuery = "";
            if (useTags) {
                inQuery = StringFormat.format(
                        "SELECT * FROM %1$s WHERE ( %2$s IN (%3$s)",
                                mSearchObjectAC.getRelatedTableName(searchObjStr, false),
                                primeryKey,
                                getInnerQuery(tableName, primeryKey, fromStatement,
                                wherePhrase));
            } else {
                inQuery = "(" + getInnerQuery(tableName, "*", fromStatement,
                        wherePhrase);
            }
            if (syntax.getSearchFrom() > 0) {
                inQuery = StringFormat.format("%1$s and  %2$s >  %3$s", inQuery, primeryKey, syntax.getSearchFrom());
            }
            retval =
                    StringFormat.format(Config.<String> getValue(ConfigValues.DBSearchTemplate),
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

    private String getInnerQuery(String tableName, String primeryKey, String fromStatement, StringBuilder wherePhrase) {
        return StringFormat.format("SELECT distinct %1$s.%2$s FROM %3$s %4$s", tableName, primeryKey, fromStatement,
                wherePhrase);
    }

    protected String getPagePhrase(SyntaxContainer syntax, String pageNumber) {
        String result = "";
        Integer page = IntegerCompat.tryParse(pageNumber);
        if (page == null) {
            page = 1;
        }
        PagingType pagingType = getPagingType();
        if (pagingType != null) {
            String pagingSyntax = Config.<String> getValue(ConfigValues.DBPagingSyntax);
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
        String val = Config.<String> getValue(ConfigValues.DBPagingType);
        PagingType type = null;
        try {
            type = PagingType.valueOf(val);
        } catch (Exception e) {
            log.error("Unknown paging type '{}'", val);
        }

        return type;
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
            conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(searchObjStr);
            conditionType = ConditionType.FreeText;
        } else {
            prev = objIter.previous();
            if (prev.getType() == SyntaxObjectType.CROSS_REF_OBJ) { // free text
                                                                    // search
                                                                    // for some
                                                                    // object
                objName = prev.getBody();
                conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(objName);
                conditionType = ConditionType.FreeTextSpecificObj;
            } else { // if (prev.getType() == SyntaxObjectType.CONDITION_FIELD)
                fieldName = prev.getBody();
                prev = objIter.previous();
                if (prev.getType() != SyntaxObjectType.DOT) {
                    // standard condition with default AC (search obj)
                    objName = searchObjStr;
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(searchObjStr);
                    conditionType = ConditionType.ConditionWithDefaultObj;
                } else {
                    // standard condition with specific AC
                    prev = objIter.previous();
                    objName = prev.getBody();
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(objName);
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
                customizedValue,
                customizedRelation,
                fieldName,
                objName,
                conditionType);
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
                (String.class.equals(conditionFieldAC.getDbFieldType(fieldName)))) {
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
            ConditionType conditionType) {

        String tableName;

        // We will take the table with tags for all subtables
        // TODO: Optimize this
        if (conditionType == ConditionType.ConditionwithSpesificObj) {
            tableName = mSearchObjectAC.getRelatedTableName(objName, true);
        } else {
            tableName = mSearchObjectAC.getRelatedTableName(objName, fieldName);
        }
        if (customizedRelation.equalsIgnoreCase("LIKE") || customizedRelation.equalsIgnoreCase("ILIKE")) {
            // Since '_' is treated in Postgres as '?' when using like, (i.e. match any single character)
            // we have to escape this character in the value to make it treated as a regular character.
            // Due to changes between PG8.x and PG9.x on ESCAPE representation in a string, we should
            // figure out what PG Release is running in order to escape the special character(_) correctly
            // This is done in a IF block and not with Method Factory pattern since this is the only change
            // right now, if we encounter other changes, this will be refactored to use the Method Factory pattern.
            String replaceWith = "_";
            int pgMajorRelease = Config.<Integer> getValue(ConfigValues.PgMajorRelease);
            if (pgMajorRelease == PgMajorRelease.PG8.getValue()) {
                replaceWith = "\\\\_";
            }
            else if (pgMajorRelease == PgMajorRelease.PG9.getValue()) {
                replaceWith = "\\_";
            }
            customizedValue = customizedValue.replace("_", replaceWith);
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
            conditionData.setConditionText(conditionFieldAC.buildConditionSql(fieldName,
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

    private static enum PgMajorRelease {
        PG8(8),
        PG9(9);

        private int value;

        private PgMajorRelease(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
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

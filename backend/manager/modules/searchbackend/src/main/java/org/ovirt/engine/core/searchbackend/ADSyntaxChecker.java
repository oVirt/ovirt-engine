package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringHelper;

public class ADSyntaxChecker implements ISyntaxChecker {
    private AdSearchObjecAutoCompleter searchObjectAC;
    private BaseAutoCompleter colonAC;
    private BaseAutoCompleter pluralAC;
    private Map<SyntaxObjectType, SyntaxObjectType[]> stateMap;
    protected static final String USER_ACCOUNT_TYPE = "$USER_ACCOUNT_TYPE";
    private static final String LDAP_GROUP_CATEGORY = "$LDAP_GROUP_CATEGORY";

    private Regex firstDQRegexp;
    private Regex nonSpaceRegexp;

    public ADSyntaxChecker() {
        searchObjectAC = new AdSearchObjecAutoCompleter();
        colonAC = new BaseAutoCompleter(":");
        pluralAC = new BaseAutoCompleter("S");

        firstDQRegexp = new Regex("^\\s*\"$");
        nonSpaceRegexp = new Regex("^\\S+$");

        stateMap = new HashMap<>();
        SyntaxObjectType[] beginArray = { SyntaxObjectType.SEARCH_OBJECT };
        stateMap.put(SyntaxObjectType.BEGIN, beginArray);
        SyntaxObjectType[] searchObjectArray = { SyntaxObjectType.COLON };
        stateMap.put(SyntaxObjectType.SEARCH_OBJECT, searchObjectArray);
        SyntaxObjectType[] colonArray = { SyntaxObjectType.CONDITION_FIELD, SyntaxObjectType.END };
        stateMap.put(SyntaxObjectType.COLON, colonArray);
        SyntaxObjectType[] conditionFieldArray = { SyntaxObjectType.CONDITION_RELATION };
        stateMap.put(SyntaxObjectType.CONDITION_FIELD, conditionFieldArray);
        SyntaxObjectType[] conditionRelationArray = { SyntaxObjectType.CONDITION_VALUE };
        stateMap.put(SyntaxObjectType.CONDITION_RELATION, conditionRelationArray);
        SyntaxObjectType[] conditionValueArray = { SyntaxObjectType.CONDITION_FIELD };
        stateMap.put(SyntaxObjectType.CONDITION_VALUE, conditionValueArray);
    }

    @Override
    public SyntaxContainer analyzeSyntaxState(String searchText, boolean final2) {
        SyntaxContainer retval = new SyntaxContainer(searchText);
        IConditionFieldAutoCompleter AdConditionFieldAC;
        if (searchText.toUpperCase().contains("ADUSER")) {
            AdConditionFieldAC = new AdUserConditionFieldAutoCompleter();
        } else {
            AdConditionFieldAC = new AdGroupConditionFieldAutoCompleter();
        }
        IAutoCompleter conditionRelationAC;
        char[] searchCharArr = searchText.toCharArray();
        boolean betweenDoubleQuotes = false;
        int curStartPos = 0;
        String curConditionField = "";
        for (int idx = 0; idx < searchCharArr.length; idx++) {
            SyntaxObjectType curState = retval.getState();
            char curChar = searchCharArr[idx];
            if ((curChar == ' ') && (curState != SyntaxObjectType.CONDITION_RELATION)) {
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
                        // ERROR INVALID-SEARCH OBJECT
                        retval.setErr(SyntaxError.INVALID_SEARCH_OBJECT, curStartPos, idx - curStartPos + 1);
                        return retval;
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
                    retval.addSyntaxObject(SyntaxObjectType.SEARCH_OBJECT, nextObject, curStartPos, idx + 1);
                    retval.setvalid(true);
                    curStartPos = idx + 1;
                }
                break;

            case SEARCH_OBJECT:

                if (!colonAC.validate(nextObject)) {
                    if (!colonAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.COLON_NOT_NEXT_TO_SEARCH_OBJECT, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.COLON, nextObject, idx, idx + 1);
                    curStartPos = idx + 1;
                    retval.setvalid(true);
                }
                break;

            case COLON:
            case CONDITION_VALUE:
                if (AdConditionFieldAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curConditionField = nextObject;
                    curStartPos = idx + 1;

                } else if (!AdConditionFieldAC.validateCompletion(nextObject)) {
                    retval.setErr(SyntaxError.INVALID_CONDITION_FILED, curStartPos, idx + 1);
                    return retval;
                }
                retval.setvalid(false);
                break;

            case CONDITION_FIELD:
                conditionRelationAC = AdConditionFieldAC.getFieldRelationshipAutoCompleter(curConditionField);
                if (conditionRelationAC == null) {
                    retval.setErr(SyntaxError.CONDITION_CANT_CREATE_RRELATIONS_AC, curStartPos, idx + 1);
                    return retval;
                }
                if (idx + 1 < searchCharArr.length) {
                    String tryNextObj = searchText.substring(curStartPos, idx + 2).toUpperCase();
                    if (conditionRelationAC.validate(tryNextObj)) {
                        break;
                    }
                }
                if (!conditionRelationAC.validate(nextObject)) {
                    if (!conditionRelationAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.INVALID_CONDITION_RELATION, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_RELATION, nextObject, curStartPos, idx + 1);
                }
                curStartPos = idx + 1;
                retval.setvalid(false);

                break;
            case CONDITION_RELATION:
                boolean addObjFlag = false;
                if (curChar == '"') {
                    betweenDoubleQuotes = !betweenDoubleQuotes;
                    if (betweenDoubleQuotes) {
                        if (!firstDQRegexp.isMatch(strRealObj)) {
                            retval.setErr(SyntaxError.INVALID_CONDITION_VALUE, curStartPos, idx + 1);
                            return retval;
                        }
                    } else {
                        strRealObj = StringHelper.trim(strRealObj, new char[] { '\"' });
                        addObjFlag = true;
                    }
                }
                // Doing this condition to identify whether this is the last
                // searchObject and no space is predicted !!
                if (final2) {
                    if (((curChar == ' ') || (idx + 1 == searchCharArr.length)) && !betweenDoubleQuotes &&
                            !addObjFlag) {
                        strRealObj = strRealObj.trim();
                        if (nonSpaceRegexp.isMatch(strRealObj)) {
                            addObjFlag = true;
                        } else {
                            curStartPos = idx + 1;
                        }
                    }
                } else {
                    if ((curChar == ' ') && !betweenDoubleQuotes && !addObjFlag) {
                        strRealObj = strRealObj.trim();
                        if (nonSpaceRegexp.isMatch(strRealObj)) {
                            addObjFlag = true;
                        } else {
                            curStartPos = idx + 1;
                        }
                    }
                }
                if (addObjFlag) {
                    if (!AdConditionFieldAC.validateFieldValue(curConditionField, strRealObj)) {
                        retval.setErr(SyntaxError.INVALID_CONDITION_VALUE, curStartPos, idx);
                        return retval;
                    } else {
                        retval.addSyntaxObject(SyntaxObjectType.CONDITION_VALUE, strRealObj, curStartPos, idx + 1);
                        curConditionField = "";
                    }
                    curStartPos = idx + 1;
                    retval.setvalid(true);
                }
                break;
            default:
                retval.setErr(SyntaxError.UNIDENTIFIED_STATE, curStartPos, idx);
                return retval;
            }
        }
        return retval;

    }

    @Override
    public SyntaxContainer getCompletion(String searchText) {
        SyntaxContainer retval = analyzeSyntaxState(searchText, false);
        IConditionFieldAutoCompleter AdConditionFieldAC;
        if (retval.getError() == SyntaxError.NO_ERROR) {
            if (searchText.toUpperCase().contains("ADUSER")) {
                AdConditionFieldAC = new AdUserConditionFieldAutoCompleter();
            } else {
                AdConditionFieldAC = new AdGroupConditionFieldAutoCompleter();
            }
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
                case COLON:
                    retval.addToACList(colonAC.getCompletion(curPartialWord));
                    break;
                case CONDITION_FIELD:
                    String[] tmpCompletions = AdConditionFieldAC.getCompletion(curPartialWord);
                    ArrayList<String> nonDuplicates = new ArrayList<>();
                    for (int itr = 0; itr < tmpCompletions.length; itr++) {
                        if (!retval.contains(SyntaxObjectType.CONDITION_FIELD, tmpCompletions[itr])) {
                            nonDuplicates.add(tmpCompletions[itr]);
                        }
                    }
                    retval.addToACList(nonDuplicates.toArray(new String[] {}));
                    break;
                case CONDITION_RELATION:
                    conditionRelationAC = AdConditionFieldAC.getFieldRelationshipAutoCompleter(retval
                            .getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD));
                    if (conditionRelationAC != null) {
                        retval.addToACList(conditionRelationAC.getCompletion(curPartialWord));
                    }
                    break;
                case CONDITION_VALUE:
                    conditionValueAC = AdConditionFieldAC.getFieldValueAutoCompleter(retval.getPreviousSyntaxObject(2,
                            SyntaxObjectType.CONDITION_FIELD));
                    if (conditionValueAC != null) {
                        retval.addToACList(conditionValueAC.getCompletion(curPartialWord));
                    }
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
            retval = generateAdQueryFromSyntaxContainer(syntax);
        }
        return retval;
    }

    private static String generateAdQueryFromSyntaxContainer(SyntaxContainer syntax) {
        StringBuilder retval = new StringBuilder();
        if (syntax.getvalid()) {
            IConditionFieldAutoCompleter conditionFieldAC;
            boolean searchingUsers = syntax.getSearchObjectStr().toUpperCase().contains("ADUSER");
            if (searchingUsers) {
                retval.append("(&");
                retval.append("(" + USER_ACCOUNT_TYPE + ")");
                conditionFieldAC = new AdUserConditionFieldAutoCompleter();
            } else {
                retval.append("(&(" + LDAP_GROUP_CATEGORY + ")");
                conditionFieldAC = new AdGroupConditionFieldAutoCompleter();
            }
            StringBuilder phrase = new StringBuilder();
            boolean nonEqual = false;
            boolean findAll = false;
            for (SyntaxObject so : syntax) {
                switch (so.getType()) {
                case CONDITION_FIELD:
                    if ("ALLNAMES".equals(so.getBody())) {
                        if (searchingUsers) {
                            phrase.append(" (|($GIVENNAME={value})(sn={value})($USER_ACCOUNT_NAME={value})($PRINCIPAL_NAME={value}))");
                        } else {
                            phrase.append(" (|($CN={value}))");
                        }
                        /**
                         * mark this search as findAll for later use
                         */
                        findAll = true;
                    } else {
                        phrase.append(" (").append(conditionFieldAC.getDbFieldName(so.getBody()));
                    }
                    break;
                case CONDITION_RELATION:
                    /**
                     * append '=' only if not finding all
                     */
                    if (!findAll) {
                        phrase.append("=");
                    }
                    if ("!=".equals(so.getBody())) {
                        nonEqual = true;
                    }
                    break;
                case CONDITION_VALUE:
                    if (findAll) {
                        /**
                         * replace all {value} occurences with the value searched. We escape the $ here for regex match,
                         * as it is used in replace.
                         */
                        phrase = replaceAll(phrase, "{value}", so.getBody().replace("$", "\\$"));
                    } else {
                        phrase.append(so.getBody()).append(")");
                    }
                    if (nonEqual) {
                        retval.append("(!").append(phrase).append(")");
                    } else {
                        retval.append(phrase.toString());
                    }
                    nonEqual = false;
                    findAll = false;
                    phrase.delete(0, phrase.length());
                    break;
                default:
                    break;
                }

            }

        }
        retval.append(")");
        return retval.toString();

    }

    private static StringBuilder replaceAll(StringBuilder builder, String oldText, String newText) {
        String t = builder.toString();
        return new StringBuilder(t.replaceAll(quote(oldText), newText));
    }

    /**
     * Returns a literal pattern <code>String</code> for the specified <code>String</code>.
     *
     * <p>
     * This method produces a <code>String</code> that can be used to create a <code>Pattern</code> that would match the
     * string <code>s</code> as if it were a literal pattern.
     * </p>
     * Metacharacters or escape sequences in the input sequence will be given no special meaning.
     *
     * Copied from Pattern.java code for GWT compatibility.
     *
     * @param s
     *            The string to be literalized
     * @return A literal string replacement
     * @since 1.5
     */
    private static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1) {
            return "\\Q" + s + "\\E";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }
}

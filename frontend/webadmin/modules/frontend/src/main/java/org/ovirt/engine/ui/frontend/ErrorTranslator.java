package org.ovirt.engine.ui.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.MissingResourceException;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class ErrorTranslator {

    private static final String VARIABLE_PATTERN = "\\$\\{\\w*\\}*"; //$NON-NLS-1$

    private static final RegExp STARTS_WITH_VARIABLE = RegExp.compile("^" + VARIABLE_PATTERN, "i"); //$NON-NLS-1$ //$NON-NLS-2$

    private ConstantsWithLookup errors;

    public ErrorTranslator() {
    }

    public ErrorTranslator(ConstantsWithLookup errors) {
        this.errors = errors;
    }

    /**
     * Translate errors from error types, error messages contains errors and variables. Variable used in messages,
     * Variable definition must be in format: $variableName variableValue. Variable usage must be in format
     * #variableName
     *
     * @param errorMsg
     *            messages to be translated
     * @param changeIfNotFound
     *            If true: if message key is not found in the resource, return a beautified key. If false, returned
     *            unfound key as is.
     */
    public ArrayList<String> translateErrorText(ArrayList<String> errorMsg,
            Boolean changeIfNotFound) {
        ArrayList<String> translatedMessages = translateMessages(errorMsg,
                changeIfNotFound);
        return resolveMessages(translatedMessages);
    }

    /**
     * Translates and resolves errors from error types. error messages contains errors and variables. Variable used in
     * messages. Variable definition must be in format: $variableName variableValue. Variable usage must be in format
     * #variableName Note: Unfound message keys will be beautified!
     *
     * @param errorMsg
     *            messages to be translated
     */
    public ArrayList<String> translateErrorText(ArrayList<String> errorMsg) {
        return translateErrorText(errorMsg, true);
    }

    public ArrayList<String> translateMessages(ArrayList<String> errorMsg,
            Boolean changeIfNotFound) {
        ArrayList<String> translatedMessages = new ArrayList<>();
        if (errorMsg != null && errorMsg.size() > 0) {
            for (String curError : errorMsg) {
                translatedMessages.add(translateErrorTextSingle(curError,
                        changeIfNotFound));
            }
        }
        return translatedMessages;
    }

    /**
     * Translates a single error message.
     *
     * @param errorMsg
     *            The message to be translated
     * @param changeIfNotFound
     *            If true: if message key is not found in the resource, return a beautified key. If false, returned
     *            unfound key as is.
     */
    public String translateErrorTextSingle(String errorMsg,
            Boolean changeIfNotFound) {
        String ret = ""; //$NON-NLS-1$
        try {
            if ((errorMsg != null) && (errorMsg.length() > 0)) {
                String errMsgCopy = errorMsg; // Taking a copy of the error message
                if (!isDynamicVariable(errorMsg)) {
                    errorMsg = errorMsg.replace('.', '_');
                }
                String errorsString = errors.getString(errorMsg);
                if (errorsString != null) {
                    ret = errorsString.replace("\n", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    if (isDynamicVariable(errorMsg) || !changeIfNotFound) {
                        ret = errorMsg;
                    } else {
                        // The error message is not found in the errors map, revert to original one
                        // without replacement of "." with "_"
                        errorMsg = errMsgCopy;
                        // just a message that doesn't have a value in the resource:
                        String[] splitted = errorMsg.toLowerCase().split("_"); //$NON-NLS-1$
                        ret = String.join(" ", splitted); //$NON-NLS-1$
                    }
                }
            }
        } catch (MissingResourceException e) {
            ret = errorMsg;
        }

        return ret;
    }

    /**
     * Translates a single error message. Note: if message key not found, a beautified message will return!
     *
     * @param errorMsg
     *            the message to translate
     * @return the translated message or a beautifed message key
     */
    public String translateErrorTextSingle(String errorMsg) {
        return translateErrorTextSingle(errorMsg, true);
    }

    /**
     * Replacing variables ('#...') within translatedMessages with their values ('$...') that are also within
     * translatedMessages.
     */
    public ArrayList<String> resolveMessages(ArrayList<String> translatedMessages) {
        ArrayList<String> translatedErrors = new ArrayList<>();
        Map<String, LinkedList<String>> variables = new HashMap<>();

        for (String currentMessage : translatedMessages) {
            if (isVariableDeclaration(currentMessage)) {
                addVariable(currentMessage, variables);
            } else {
                translatedErrors.add(currentMessage);
            }
        }

        // /Place to global variable adding
        ArrayList<String> returnValue = new ArrayList<>();
        for (String error : translatedErrors) {
            returnValue.add(resolveMessage(error, variables));
        }

        return returnValue;
    }

    private void addVariable(String variable, Map<String, LinkedList<String>> variables) {
        int firstSpace = variable.indexOf(' ');
        if (firstSpace != -1 && firstSpace < variable.length()) {
            String key = variable.substring(1, firstSpace);
            String value = variable.substring(firstSpace + 1);
            if (variables.get(key) == null) {
                variables.put(key, new LinkedList<String>());
            }
            variables.get(key).add(value);
        }
    }

    private String resolveMessage(String message, Map<String, LinkedList<String>> variables) {
        String returnValue = message;

        RegExp regex = RegExp.compile(VARIABLE_PATTERN, "gi"); //$NON-NLS-1$ //$NON-NLS-2$

        MatchResult result;
        while (returnValue.length() > 0) {
            result = regex.exec(returnValue);
            if (result == null) {
                // No more matches
                break;
            }

            String match = result.getGroup(0);
            String key = match.substring(2, match.length() - 1);

            if (variables.containsKey(key)) {
                LinkedList<String> values = variables.get(key);
                String value = values.size() == 1 ? values.getFirst() :
                        values.size() > 1 ? values.removeFirst() : ""; //$NON-NLS-1$
                returnValue = returnValue.replace(match, value);
            } else {
                // Variable not found, break the cycle to avoid
                // infinite loop
                break;
            }

            // Make the next search start from the beginning
            regex.setLastIndex(0);
        }

        return returnValue;
    }

    /**
     * Returns true if the specified strMessage is in the format: "$variable-name variable-value", false otherwise.
     *
     * @param strMessage
     *            the string that may be a dynamic variable
     * @return true if input is dynamic variable, false otherwise.
     */
    private boolean isDynamicVariable(String strMessage) {
        return strMessage.startsWith("$"); //$NON-NLS-1$
    }

    /**
     * Returns true if and only if the param starts with $ but is not a variable reference (e.g. is not ${something})
     */
    boolean isVariableDeclaration(String msg) {
        boolean startsAsVariable = msg.startsWith("$"); //$NON-NLS-1$
        boolean isVariableReference = STARTS_WITH_VARIABLE.test(msg);
        return startsAsVariable && !isVariableReference;
    }
}

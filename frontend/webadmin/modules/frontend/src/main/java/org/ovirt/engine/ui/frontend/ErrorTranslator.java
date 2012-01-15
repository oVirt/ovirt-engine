package org.ovirt.engine.ui.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import org.ovirt.engine.core.compat.StringHelper;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class ErrorTranslator {
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
     * @return
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
     * @return
     */
    public ArrayList<String> translateErrorText(ArrayList<String> errorMsg) {
        return translateErrorText(errorMsg, true);
    }

    public ArrayList<String> translateMessages(ArrayList<String> errorMsg,
            Boolean changeIfNotFound) {
        ArrayList<String> translatedMessages = new ArrayList<String>();
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
     * @return
     */
    public String translateErrorTextSingle(String errorMsg,
            Boolean changeIfNotFound) {
        String ret = "";
        try {
            if ((errorMsg != null) && (errorMsg.length() > 0)) {
                errorMsg = errorMsg.replace('.', '_');
                if (errors.getString(errorMsg) != null) {
                    // if (mMessages.containsKey(errorMsg)) {
                    ret = errors.getString(errorMsg).replace("\n", "<br/>");
                } else {
                    if ((isDynamicVariable(errorMsg)) || (!changeIfNotFound)) {
                        ret = errorMsg;
                    } else {
                        // just a message that doesn't have a value in the resource:
                        String[] splitted = errorMsg.toLowerCase().split("_");
                        ret = StringHelper.join(" ", splitted);
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
    public String TranslateErrorTextSingle(String errorMsg) {
        return translateErrorTextSingle(errorMsg, true);
    }

    /**
     * Replacing variables ('#...') within translatedMessages with their values ('$...') that are also within
     * translatedMessages.
     *
     * @param translatedMessages
     * @return
     */
    public ArrayList<String> resolveMessages(ArrayList<String> translatedMessages) {
        ArrayList<String> translatedErrors = new ArrayList<String>();
        Map<String, String> variables = new HashMap<String, String>();

        for (String currentMessage : translatedMessages) {
            if (currentMessage.startsWith("$")) {
                addVariable(currentMessage, variables);
            } else {
                translatedErrors.add(currentMessage);
            }
        }

        // /Place to global variable adding
        ArrayList<String> returnValue = new ArrayList<String>();
        for (String error : translatedErrors) {
            returnValue.add(resolveMessage(error, variables));
        }

        return returnValue;
    }

    private void addVariable(String variable, Map<String, String> variables) {
        int firstSpace = variable.indexOf(' ');
        if (firstSpace != -1 && firstSpace < variable.length()) {
            String key = variable.substring(1, firstSpace);
            String value = variable.substring(firstSpace + 1);
            if (!variables.containsKey(key)) {
                variables.put(key, value);
            }
        }
    }

    private String resolveMessage(String message, Map<String, String> variables) {
        String returnValue = message;

        RegExp regex = RegExp.compile("\\$\\{\\w*\\}*", "gi");

        int fromIndex = 0;
        int length = message.length();
        MatchResult result;
        while (fromIndex < length) {
            result = regex.exec(message);
            if (result == null) {
                // No more matches
                break;
            }

            int index = result.getIndex();
            String match = result.getGroup(0);

            String value = match.substring(2, match.length() - 1);
            if (variables.containsKey(value)) {
                returnValue = returnValue.replace(match, variables.get(value));
            }

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
    private final boolean isDynamicVariable(String strMessage) {
        return strMessage.startsWith("$");
    }
}

package org.ovirt.engine.core.common.interfaces;

import java.util.List;
import java.util.Locale;

public interface ErrorTranslator {

    /**
     * Translate errors from error types. error messages contains errors and variables. Variable used in messages.
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
    public abstract List<String> TranslateErrorText(List<String> errorMsg, boolean changeIfNotFound);

    /**
     * Translates and resolves errors from error types. error messages contains errors and variables. Variable used in
     * messages. Variable definition must be in format: $variableName variableValue. Variable usage must be in format
     * #variableName Note: Unfound message keys will be beautified!
     *
     * @param errorMsg
     *            messages to be translated
     * @return
     */
    public abstract List<String> TranslateErrorText(List<String> errorMsg);

    /**
     * Translates and resolves errors from error types. error messages contains errors and variables. Variable used in
     * messages. Variable definition must be in format: $variableName variableValue. Variable usage must be in format
     * #variableName Note: Unfound message keys will be beautified!
     *
     * @param errorMsg
     *            messages to be translated
     * @param locale
     *            the locale to translate into
     * @return
     */
    public abstract List<String> TranslateErrorText(List<String> errorMsg, Locale locale);

    public abstract List<String> TranslateMessages(List<String> errorMsg, boolean changeIfNotFound);

    /**
     * returns true if the specified strMessage is in the format: "$variable-name variable-value", false otherwise.
     *
     * @param strMessage
     *            the string that may be a dynamic variable.
     * @return true if input is dynamic variable, false otherwise.
     */
    public abstract boolean IsDynamicVariable(String strMessage);

    /**
     * Translates a single error message.
     *
     * @param errorMsg
     *            the message to be translated
     * @param changeIfNotFound
     *            If true: if message key is not found in the resource, return a beautified key. If false, returned
     *            unfound key as is.
     * @return
     */
    public abstract String TranslateErrorTextSingle(String errorMsg, boolean changeIfNotFound);

    /**
     * Translates a single error message. Note: if message key not found, a beautified message will return!
     *
     * @param errorMsg
     *            the message to translate
     * @return the translated message or a beautifed message key
     */
    public abstract String TranslateErrorTextSingle(String errorMsg);

    /**
     * Translates a single error message. Note: if message key not found, a beautified message will return!
     *
     * @param errorMsg
     *            the message to translate
     * @param locale
     *            the locale to translate into
     * @return the translated message or a beautifed message key
     */
    public abstract String TranslateErrorTextSingle(String errorMsg, Locale locale);

    /**
     * Replacing variables ('${...}') within translatedMessages with their values ('$key value') that are also within
     * translatedMessages.
     *
     * @param translatedMessages
     * @return
     */
    public abstract List<String> ResolveMessages(List<String> translatedMessages);

}

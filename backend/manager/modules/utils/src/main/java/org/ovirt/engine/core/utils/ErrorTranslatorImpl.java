package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErrorTranslatorImpl implements ErrorTranslator {

    private static final long ONE_HOUR = 60 * 60 * 1000L;
    private static final Logger log = LoggerFactory.getLogger(ErrorTranslatorImpl.class);
    private static final Pattern startsWithVariableDefinitionPattern = Pattern.compile("^\\$[^{}\\s]+ .*");
    private List<String> messageSources;
    private Locale standardLocale;
    private Map<String, String> standardMessages;
    private ReapedMap<Locale, Map<String, String>> messagesByLocale;

    // Will assume these are property files, not ResxFiles.
    public ErrorTranslatorImpl(String... errorFileNames) {
        log.info("Start initializing {}",  getClass().getSimpleName());
        messageSources = asList(errorFileNames);
        standardLocale = Locale.getDefault();
        standardMessages = retrieveByLocale(standardLocale);
        messagesByLocale = new ReapedMap<>(ONE_HOUR, true);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private synchronized Map<String, String> getMessages(Locale locale) {
        Map<String, String> messages = null;
        if (standardLocale.equals(locale)) {
            messages = standardMessages;
        } else {
            if ((messages = messagesByLocale.get(locale)) == null) {
                messages = retrieveByLocale(locale);
                messagesByLocale.put(locale, messages);
                messagesByLocale.reapable(locale);
            }
        }
        return messages;
    }

    private Map<String, String> retrieveByLocale(Locale locale) {
        Map<String, String> messages = new HashMap<>();
        for (String messageSource : messageSources) {
            retrieveByLocale(locale, messageSource, messages);
        }
        return messages;
    }

    private Map<String, String> retrieveByLocale(Locale locale, String messageSource, Map<String, String> messages) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(messageSource, locale);
            for (String key : bundle.keySet()) {
                if (!messages.containsKey(key)) {
                    messages.put(key, bundle.getString(key));
                } else {
                    log.warn("Code '{}' appears more than once in string table.", key);
                }
            }
        } catch (RuntimeException e) {
            log.error("File: '{}' could not be loaded: {}", messageSource, e.getMessage());
            log.debug("Exception", e);
        }
        return messages;
    }

    private List<String> translate(List<String> errorMsg, boolean changeIfNotFound, Locale locale) {
        List<String> translatedMessages = doTranslation(errorMsg, changeIfNotFound, locale);
        return resolveMessages(translatedMessages);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#translateErrorText(java.util.List)
     */
    public List<String> translateErrorText(List<String> errorMsg) {
        return translate(errorMsg, true, Locale.getDefault());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#translateErrorText(java.util.List)
     */
    public List<String> translateErrorText(List<String> errorMsg, Locale locale) {
        return translate(errorMsg, true, locale);
    }

    public final List<String> doTranslation(List<String> errorMsg, boolean changeIfNotFound, Locale locale) {
        ArrayList<String> translatedMessages = new ArrayList<>();
        if (errorMsg != null && errorMsg.size() > 0) {
            for (String curError : errorMsg) {
                translatedMessages.add(translate(curError, changeIfNotFound, locale));
            }
        }
        return translatedMessages;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#isDynamicVariable(java.lang.String )
     */
    public final boolean isDynamicVariable(String strMessage) {
        return startsWithVariableDefinitionPattern.matcher(strMessage).matches();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#translateErrorTextSingle(java. lang.String, boolean)
     */
    public final String translateErrorTextSingle(String errorMsg, boolean changeIfNotFound) {
        return translate(errorMsg, changeIfNotFound, Locale.getDefault());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#translateErrorTextSingle(java. lang.String, boolean)
     */
    public String translateErrorTextSingle(String errorMsg, Locale locale) {
        return translate(errorMsg, true, locale);
    }

    private String translate(String errorMsg, boolean changeIfNotFound, Locale locale) {
        String ret = "";
        Map<String, String> messages = getMessages(locale);
        if (messages != null && messages.containsKey(errorMsg)) {
            ret = messages.get(errorMsg);
        } else {
            if (!(errorMsg == null || errorMsg.isEmpty())) {
                if (isDynamicVariable(errorMsg) || !changeIfNotFound) {
                    ret = errorMsg;
                } else {
                // just a message that doesn't have a value in the resource:
                    String[] splitted = errorMsg.toLowerCase().split("[_]", -1);
                    ret = StringUtils.join(splitted, " ");
                }
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#translateErrorTextSingle(java. lang.String)
     */
    public final String translateErrorTextSingle(String errorMsg) {
        return translateErrorTextSingle(errorMsg, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#resolveMessages(java.util.List)
     */
    public final List<String> resolveMessages(List<String> translatedMessages) {
        List<String> translatedErrors = new ArrayList<>();
        Map<String, String> variables = new HashMap<>();
        for (String currentMessage : translatedMessages) {
            if (isDynamicVariable(currentMessage)) {
                addVariable(currentMessage, variables);
            } else {
                translatedErrors.add(currentMessage);
            }
        }
        /**
         * Place to global variable adding
         */
        List<String> returnValue = new ArrayList<>();
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
        StrSubstitutor sub = new StrSubstitutor(variables);
        return sub.replace(message);
    }

    private static List<String> asList(String[] names) {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            ret.add(trim(names[i]));
        }
        return ret;
    }

    private static String trim(String name) {
        return name != null && name.endsWith(".properties")
                ? name.substring(0, name.lastIndexOf(".properties"))
                : name;
    }

}

package org.ovirt.engine.ui.uicompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.Messages.Optional;
import com.google.gwt.i18n.client.Messages.Select;

/**
 * Validates GWT {@link com.google.gwt.i18n.client.Messages Messages} sub-interfaces to detect errors early,
 * i.e. before GWT compilation phase. Validation logic in this class generally follows GWT compiler's i18n
 * validation rules.
 *
 * <p>Two main kinds of errors are to be detected.  First, detect if a key does not have a defined message
 * associated with it from either the default English or the locale specific files on a per locale basis.
 * This is done with in method {@link #checkForMissingDefault(Method, Properties, Properties, String, List)}.
 * Second, make sure that the message to be used by a locale specific message has to correct number of
 * substitution arguments.  This is done in method {@link #checkPlaceHolders(Method, Properties, String, List)}.
 *
 * @see com.google.gwt.i18n.rebind.MessagesMethodCreator
 */
public class GwtMessagesValidator {
    private static final String PLACE_HOLDER_STRING = "\\{(\\d+)(?:,\\s*list,\\s*\\w+)?\\}";
    private static final Pattern placeHolderPattern = Pattern.compile(PLACE_HOLDER_STRING);

    public static List<String> validateClass(Class<? extends Messages> classUnderTest)
            throws URISyntaxException, IOException {
        List<String> errors = new ArrayList<>();

        if (classUnderTest.isInterface()) {
            File messagesDir = new File(classUnderTest.getResource(".")
                    .toURI().toASCIIString().replaceAll("file:", ""));
            List<Method> messagesMethods = Arrays.asList(classUnderTest.getMethods());

            Properties defaultProperties = loadDefaultProperties(classUnderTest);
            for (Method method : messagesMethods) {
                checkPlaceHolders(method, defaultProperties, classUnderTest.getSimpleName() + ".properties", errors);
            }

            File[] localePropertiesFiles = getMessagesLocalePropertiesFiles(messagesDir, classUnderTest.getSimpleName());
            if (localePropertiesFiles != null) {
                for (File localeFile : localePropertiesFiles) {
                    Properties localeProperties = loadProperties(localeFile);
                    String localeFileName = localeFile.getName();

                    for (Method method : messagesMethods) {
                        checkForMissingDefault(method, defaultProperties, localeProperties, localeFileName, errors);
                        checkPlaceHolders(method, localeProperties, localeFileName, errors);
                    }
                }
            }

            if (defaultProperties.size() == 0 && localePropertiesFiles == null) {
                errors.add("Class under test does not have a default or any locale specific properties files: "
                        + classUnderTest.getName());
            }
        } else {
            errors.add("Class under test is not an interface: " + classUnderTest.getName());
        }

        return errors;
    }

    /**
     * Discover the full Messages hierarchy of a given interface, load and return the default English
     * text for each definition that can be found in properties files.
     *
     * @param leafClass Class to look up from
     * @return Set of default messages from <i>leafClass</i> up to, but not including, the root Messages
     *         interface
     */
    @SuppressWarnings("unchecked")
    private static Properties loadDefaultProperties(Class<? extends Messages> leafClass) throws IOException {
        Properties hierarchyProps = new Properties();
        ArrayList<Class<? extends Messages>> hierarchy = new ArrayList<>();

        // discover from the leafClass up to the root
        ArrayList<Class<? extends Messages>> round = new ArrayList<>();
        round.add(leafClass);
        while (!round.isEmpty()) {
            ArrayList<Class<? extends Messages>> round2 = new ArrayList<>();
            for (Class<? extends Messages> c : round) {
                hierarchy.add(c);

                for (Class<?> up : c.getInterfaces()) {
                    if (Messages.class.isAssignableFrom(up) && Messages.class != up) {
                        round2.add((Class<? extends Messages>)up);
                    }
                }
            }

            round = round2;
        }
        Collections.reverse(hierarchy);

        // load the properties into the hierarchy from the root down
        for (Class<?> theClass : hierarchy) {
            String classPropertyFileName = theClass.getName().replace(".",  "/") + ".properties";

            URL theResource = theClass.getResource(classPropertyFileName);
            if (theResource == null) {
                theResource = theClass.getResource(theClass.getSimpleName() + ".properties");
            }

            Properties classProps = new Properties();
            try (InputStream input = theResource.openStream()) {
                classProps.load(input);
            }
            hierarchyProps.putAll(classProps);
        }

        return hierarchyProps;
    }

    private static void checkPlaceHolders(Method method, Properties localeProperties, String localeFileName, List<String> errors) {
        int count = 0;
        String methodName = method.getName();

        if (localeProperties.getProperty(methodName) != null) {
            Set<Integer> foundIndex = new HashSet<>();
            Set<Integer> requiredIndexes = determineRequiredIndexes(method.getParameterAnnotations());
            int minRequired = requiredIndexes.size();
            int methodParamCount = method.getParameterTypes().length;

            // Check to make sure the number of parameters is inside the range defined.
            Matcher matcher = placeHolderPattern.matcher(localeProperties.getProperty(methodName));

            while (matcher.find()) {
                int placeHolderIndex = -1;
                try {
                    placeHolderIndex = Integer.parseInt(matcher.group(1));
                    if (!foundIndex.contains(placeHolderIndex)) {
                        count++;
                        foundIndex.add(placeHolderIndex);
                        requiredIndexes.remove(placeHolderIndex);
                    }
                    if (placeHolderIndex < 0
                            || placeHolderIndex >= methodParamCount) {
                        errors.add(methodName + " contains out of bound index " + placeHolderIndex + " in " + localeFileName);
                    }
                } catch (NumberFormatException nfe) {
                    errors.add(methodName + " contains invalid key " + matcher.group(0) + " in " + localeFileName);
                }
            }
            if (count < minRequired || count > methodParamCount) {
                errors.add(methodName + " does not match the number of parameters in " + localeFileName);
            }
            if (!requiredIndexes.isEmpty()) {
                errors.add(methodName + " is missing required indexes in " + localeFileName);
            }
        }
    }

    private static Set<Integer> determineRequiredIndexes(Annotation[][] methodParamAnnotations) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < methodParamAnnotations.length; i++) {
            boolean isOptional = false;
            boolean isSelect = false;
            Annotation[] annotations = methodParamAnnotations[i];
            if (annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(Optional.class)) {
                        isOptional = true;
                        break;
                    } else if (annotation.annotationType().equals(Select.class)) {
                        isSelect = true;
                    }
                }
            }
            if (!isOptional && !isSelect) {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * For the given message definition method, make sure a value exists in either the default English
     * properties file(s) or in the locale specific properties file.  The goal is to fail the unit test
     * if a key does not have a corresponding message.
     */
    private static void checkForMissingDefault(Method method, Properties defaultProperties, Properties localeProperties, String localeFileName, List<String> errors) {
        String key = method.getName();
        if (!defaultProperties.containsKey(key)) {
            if (!localeProperties.containsKey(key)) {
                errors.add("Key: " + method.getName() + " not found in properties file: "
                        + localeFileName + " and no default is defined");
            }
        }
    }

    private static Properties loadProperties(File localeFile) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(localeFile)) {
            properties.load(fis);
        }
        return properties;
    }

    /**
     * Locate any existing locale specific properties files.
     *
     * @return An {@code Array} of {@code File} objects.
     * @throws URISyntaxException
     *             If path doesn't exist
     */
    private static File[] getMessagesLocalePropertiesFiles(final File currentDir, final String fileNamePrefix) {
        return currentDir.listFiles((dir, name) -> name.matches("^" + fileNamePrefix + "_[a-zA-Z]{2}.*\\.properties$"));
    }

    /**
     * Format errors to be human readable.
     *
     * @param errors
     *            The {@code List} of error {@code String}s
     * @return A {@code String} containing the human readable errors.
     */
    public static String format(List<String> errors) {
        StringBuilder builder = new StringBuilder();
        for (String error : errors) {
            builder.append(error);
            builder.append("\n");
        }
        return builder.toString();
    }

}

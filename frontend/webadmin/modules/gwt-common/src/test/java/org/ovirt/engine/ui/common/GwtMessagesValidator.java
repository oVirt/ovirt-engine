package org.ovirt.engine.ui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.Messages.DefaultMessage;
import com.google.gwt.i18n.client.Messages.Optional;

/**
 * Validates GWT {@link com.google.gwt.i18n.client.Messages Messages} sub-interfaces to detect errors early,
 * i.e. before GWT compilation phase. Validation logic in this class generally follows GWT compiler's i18n
 * validation rules.
 *
 * @see com.google.gwt.i18n.rebind.MessagesMethodCreator
 */
public class GwtMessagesValidator {

    private static final String PLACE_HOLDER_STRING = "\\{(\\d+)\\}";
    private static final Pattern placeHolderPattern = Pattern.compile(PLACE_HOLDER_STRING);

    static class PropertiesFileInfo {
        private static Properties properties;
        private static String fileName;
    }

    public static List<String> validateClass(Class<? extends Messages> classUnderTest)
            throws URISyntaxException, IOException {
        List<String> errors = new ArrayList<String>();

        if (classUnderTest.isInterface()) {
            File messagesDir = new File(classUnderTest.getResource(".")
                    .toURI().toASCIIString().replaceAll("file:", ""));
            List<Method> messagesMethods = Arrays.asList(classUnderTest.getMethods());

            File[] propertiesFiles = getMessagesPropertiesFiles(messagesDir, classUnderTest.getSimpleName());
            if (propertiesFiles != null) {
                for (File localeFile : propertiesFiles) {
                    PropertiesFileInfo.properties = loadProperties(localeFile);
                    PropertiesFileInfo.fileName = localeFile.getName();

                    for (Method method : messagesMethods) {
                        checkForMissingDefault(method, errors);
                        checkPlaceHolders(method, errors);
                    }
                }
            }
        } else {
            errors.add("Class under test is not an interface: " + classUnderTest.getName());
        }

        return errors;
    }

    private static void checkPlaceHolders(Method method, List<String> errors) {
        int count = 0;
        String methodName = method.getName();

        if (PropertiesFileInfo.properties.getProperty(methodName) != null) {
            Set<Integer> foundIndex = new HashSet<Integer>();
            Set<Integer> requiredIndexes = determineRequiredIndexes(method.getParameterAnnotations());
            int minRequired = requiredIndexes.size();
            int methodParamCount = method.getParameterTypes().length;

            // Check to make sure the number of parameters is inside the range defined.
            Matcher matcher = placeHolderPattern.matcher(PropertiesFileInfo.properties.getProperty(methodName));

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
                        errors.add(methodName + " contains out of bound index " + placeHolderIndex + " in "
                                + PropertiesFileInfo.fileName);
                    }
                } catch (NumberFormatException nfe) {
                    errors.add(methodName + " contains invalid key " + matcher.group(0) + " in "
                            + PropertiesFileInfo.fileName);
                }
            }
            if (count < minRequired || count > methodParamCount) {
                errors.add(methodName + " does not match the number of parameters in "
                        + PropertiesFileInfo.fileName);
            }
            if (!requiredIndexes.isEmpty()) {
                errors.add(methodName + " is missing required indexes in "
                        + PropertiesFileInfo.fileName);
            }
        }
    }

    private static Set<Integer> determineRequiredIndexes(Annotation[][] methodParamAnnotations) {
        Set<Integer> result = new HashSet<Integer>();
        for (int i = 0; i < methodParamAnnotations.length; i++) {
            boolean isOptional = false;
            Annotation[] annotations = methodParamAnnotations[i];
            if (annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(Optional.class)) {
                        isOptional = true;
                        break;
                    }
                }
            }
            if (!isOptional) {
                result.add(i);
            }
        }
        return result;
    }

    private static void checkForMissingDefault(Method method, List<String> errors) {
        // Make sure that the properties file has the key, as there is no default message.
        if (method.getAnnotation(DefaultMessage.class) == null) {
            if (!PropertiesFileInfo.properties.contains(method.getName())) {
                errors.add("Key: " + method.getName() + " not found in properties file: "
                        + PropertiesFileInfo.fileName + " and no default is defined");
            }
        }
    }

    private static Properties loadProperties(File localeFile) throws IOException {
        Properties properties = new Properties();
        Reader fr = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(localeFile);
            fr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            properties.load(fr);
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fr != null) {
                fr.close();
            }
        }
        return properties;
    }

    /**
     * Locate the properties files.
     *
     * @return An {@code Array} of {@code File} objects.
     * @throws URISyntaxException
     *             If path doesn't exist
     */
    private static File[] getMessagesPropertiesFiles(final File currentDir, final String fileNamePrefix)
            throws URISyntaxException {
        return currentDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(fileNamePrefix) && name.endsWith(".properties");
            }
        });
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

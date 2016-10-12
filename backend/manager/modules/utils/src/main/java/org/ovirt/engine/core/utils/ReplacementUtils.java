package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class ReplacementUtils {

    protected static final int DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS = 5;
    protected static final String DEFAULT_SEPARATOR = "," + System.lineSeparator();
    private static final String COUNTER_SUFFIX = "_COUNTER";
    private static final String LIST_SUFFIX = "_LIST";
    private static final String ENTITY_SUFFIX  = "_ENTITY";

    private ReplacementUtils() {
    }

    /**
     * Replace a property defined within a message with a bounded number of elements.<br>
     * In addition, if a counter appears in the message, it will be replaced with the elements size:<br>
     * <ul>
     * <li>The elements' size property name is expected to be {propertyName}_COUNTER</li>
     * </ul>
     *
     * @param propertyName
     *            the property name which represents the collection.
     * @param items
     *            the collection of items to be shown in the message.
     * @param separator
     *            the separator that will separate between the elements.
     * @param maxNumberOfPrintedItems
     *            the bound value to limit the number of printed elements.
     * @return a mutable collection contains two elements:<br>
     *         <ul>
     *         <li>The property name and its replacement items.</li>
     *         <li>The property counter name and the items size.</li>
     *         </ul>
     */
    public static Collection<String> replaceWith(String propertyName,
            Collection<?> items,
            String separator,
            int maxNumberOfPrintedItems) {
        Validate.isTrue(maxNumberOfPrintedItems >= 1);
        Validate.isTrue(StringUtils.isNotEmpty(separator));

        int maxNumOfItems = Math.min(maxNumberOfPrintedItems, items.size());
        List<String> printedItems = new ArrayList<>(maxNumOfItems);

        String itemPrefix = separator.equals(DEFAULT_SEPARATOR) ? "\t" : " ";
        for (Object item : items) {
            if (--maxNumOfItems < 0) {
                break;
            }
            printedItems.add(String.format("%s%s", itemPrefix, String.valueOf(item)));
        }

        if (items.size() > maxNumberOfPrintedItems) {
            printedItems.add(String.format("%s...", itemPrefix));
        }

        ArrayList<String> replacements = new ArrayList<>();
        replacements.add(createSetVariableString(propertyName, StringUtils.join(printedItems, separator)));
        replacements.add(createSetVariableString(propertyName + COUNTER_SUFFIX, items.size()));

        return replacements;
    }

    public static String createSetVariableString(String propertyName, Object value) {
        final String setVariableValueFormat = "$%s %s";
        return String.format(setVariableValueFormat, propertyName, value);
    }

    /**
     * Replace a property defined within a message with a bounded number of elements.<br>
     * In addition, if a counter appears in the message, it will be replaced with the elements size:<br>
     * <ul>
     * <li>The elements' size property name is expected to be {propertyName}_COUNTER</li>
     * </ul>
     *
     * @param propertyName
     *            the property name which represents the collection.
     * @param items
     *            the collection of items to be shown in the message.
     * @return a mutable collection contains two elements:<br>
     *         <ul>
     *         <li>The property name and its replacement items.</li>
     *         <li>The property counter name and the items size.</li>
     *         </ul>
     */
    public static Collection<String> replaceWith(String propertyName, Collection<?> items) {
        return replaceWith(propertyName, items, DEFAULT_SEPARATOR, DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS);
    }

    /**
     * Replace a property defined within a message with a bounded number of elements.<br>
     * In addition, if a counter appears in the message, it will be replaced with the elements size:<br>
     * <ul>
     * <li>The elements' size property name is expected to be {propertyName}_COUNTER</li>
     * </ul>
     *
     * @param propertyName
     *            the property name which represents the collection.
     * @param items
     *            the collection of items to be shown in the message.
     * @param separator
     *            the separator that will separate between the elements.
     * @return a mutable collection contains two elements:<br>
     *         <ul>
     *         <li>The property name and its replacement items.</li>
     *         <li>The property counter name and the items size.</li>
     *         </ul>
     */
    public static Collection<String> replaceWith(String propertyName, Collection<?> items, String separator) {
        return replaceWith(propertyName, items, separator, DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS);
    }

    /**
     * Replace a property defined within a message with a bounded number of elements of {@link Nameable}.<br>
     * In addition, if a counter appears in the message, it will be replaced with the elements size:<br>
     * <ul>
     * <li>The elements' size property name is expected to be {propertyName}_COUNTER</li>
     * </ul>
     *
     * @param propertyName
     *            the property name which represents the collection
     * @param items
     *            the collection of items to be shown in the message
     * @return a mutable collection contains two elements:<br>
     *         <ul>
     *         <li>The property name and its replacement items.</li>
     *         <li>The property counter name and the items size.</li>
     *         </ul>
     */
    public static <T extends Nameable> Collection<String> replaceWithNameable(String propertyName, Collection<T> items) {
        List<Object> printedItems = new ArrayList<>(items.size());

        for (Nameable itemName : items) {
            printedItems.add(itemName.getName());
        }

        return replaceWith(propertyName, printedItems);
    }

    public static String getVariableName(EngineMessage engineMessage) {
        return engineMessage + ENTITY_SUFFIX;
    }

    public static String getListVariableName(EngineMessage engineMessage) {
        return engineMessage + LIST_SUFFIX;
    }

    public static Collection<String> getListVariableAssignmentString(EngineMessage engineMessage, Collection<?> values) {
        return ReplacementUtils.replaceWith(ReplacementUtils.getListVariableName(engineMessage), values);
    }

    public static String getVariableAssignmentStringWithMultipleValues(EngineMessage engineMessage, String value) {
        return createSetVariableString(getListVariableName(engineMessage), value);
    }

    public static String getVariableAssignmentString(EngineMessage engineMessage, String value) {
        return createSetVariableString(getVariableName(engineMessage), value);
    }
}

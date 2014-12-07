package org.ovirt.engine.core.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.Nameable;

public class ReplacementUtils {

    protected static final int DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS = 5;
    protected static final String DEFAULT_SEPARATOR = "," + System.lineSeparator();

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
    public static Collection<String> replaceWith(String propertyName, List<?> items, String separator, int maxNumberOfPrintedItems) {
        Validate.isTrue(maxNumberOfPrintedItems >= 1);
        Validate.isTrue(StringUtils.isNotEmpty(separator));

        int size = Math.min(maxNumberOfPrintedItems, items.size());
        List<String> printedItems = new ArrayList<String>(size);

        for (int i = 0; i < size; i++) {
            printedItems.add(String.format("\t%s", String.valueOf(items.get(i))));
        }

        if (items.size() > maxNumberOfPrintedItems) {
            printedItems.add("\t...");
        }

        ArrayList<String> replacements = new ArrayList<String>();
        replacements.add(MessageFormat.format("${0} {1}", propertyName, StringUtils.join(printedItems, separator)));
        replacements.add(MessageFormat.format("${0}_COUNTER {1}", propertyName, items.size()));

        return replacements;
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
    public static Collection<String> replaceWith(String propertyName, List<?> items) {
        return replaceWith(propertyName, items, DEFAULT_SEPARATOR, DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS);
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
    public static <T extends Nameable> Collection<String> replaceWithNameable(String propertyName, List<T> items) {
        List<Object> printedItems = new ArrayList<Object>(items.size());

        for (Nameable itemName : items) {
            printedItems.add(itemName.getName());
        }

        return replaceWith(propertyName, printedItems);
    }
}

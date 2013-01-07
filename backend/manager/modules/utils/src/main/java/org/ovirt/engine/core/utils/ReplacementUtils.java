package org.ovirt.engine.core.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Nameable;

public class ReplacementUtils {

    protected static final int MAX_NUMBER_OF_PRINTED_ITEMS = 5;

    /**
     * Replace a property defined within a message with a bounded number of elements.<br>
     * In addition, if a counter appears in the message, it will be replaced with the elements size.
     *
     * @param propertyName
     *            the property name which represents the collection
     * @param items
     *            the collection of items to be shown in the message
     * @return an array of two elements contains the property name and its replacement items and a property for its
     *         total size.
     */
    public static String[] replaceWith(String propertyName, List<Object> items) {
        int size = Math.min(MAX_NUMBER_OF_PRINTED_ITEMS, items.size());
        List<String> printedItems = new ArrayList<String>(size);

        for (int i = 0; i < size; i++) {
            printedItems.add(String.format("\t%s", items.get(i).toString()));
        }

        if (items.size() > MAX_NUMBER_OF_PRINTED_ITEMS) {
            printedItems.add("\t...");
        }

        return new String[] { MessageFormat.format("${0} {1}", propertyName, StringUtils.join(printedItems, ",\n")),
                MessageFormat.format("${0}_COUNTER {1}", propertyName, items.size()) };
    }

    /**
     * Replace a property defined within a message with a bounded number of elements of {@link Nameable}.<br>
     * In addition, if a counter appears in the message, it will be replaced with the elements size.
     *
     * @param propertyName
     *            the property name which represents the collection
     * @param items
     *            the collection of items to be shown in the message
     * @return an array of two elements contains the property name and its replacement items and a property for its
     *         total size.
     */
    public static <T extends Nameable> String[] replaceWithNameable(String propertyName, List<T> items) {
        List<Object> printedItems = new ArrayList<Object>(items.size());

        for (Nameable itemName : items) {
            printedItems.add(itemName.getName());
        }

        return replaceWith(propertyName, printedItems);
    }
}

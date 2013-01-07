package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Nameable;

public class ReplacementUtilsTest {

    static private final String PROPERTY_NAME = "MY_SINGLE_ITEM_LIST";
    static private final String PROPERTY_VALUE = "MY_SINGLE_ITEM_VALUE";
    static private final String PROPERTY_COUNTER_NAME = "MY_SINGLE_ITEM_LIST_COUNTER";

    @Test
    public void replaceWithSingleItem() {
        List<Object> items = Collections.<Object> singletonList(PROPERTY_VALUE);
        String[] messageItems = ReplacementUtils.replaceWith(PROPERTY_NAME, items);
        validateMessageItems(messageItems, items);
    }

    @Test
    public void replaceWithNameableCollection() {
        Nameable item = new Nameable() {

            @Override
            public String getName() {
                return PROPERTY_VALUE;
            }
        };

        List<Nameable> items = Collections.<Nameable> singletonList(item);
        String[] messageItems = ReplacementUtils.replaceWithNameable(PROPERTY_NAME, items);
        validateMessageItems(messageItems, items);
    }

    @Test
    public void replaceWithEmptyCollection() {
        String[] messageItems = ReplacementUtils.replaceWith(PROPERTY_NAME, Collections.emptyList());
        validateMessageContainsProperties(messageItems);
    }

    @Test
    public void replaceWithMoreThanMaxItems() {
        List<Object> items = createItems();
        String[] messageItems = ReplacementUtils.replaceWith(PROPERTY_NAME, items);
        validateMessageContainsProperties(messageItems);
        validateMessageDoesNotContainUnexpectedItems(messageItems[0], items);
        assertTrue(messageItems[1].contains(String.valueOf(items.size())));
    }

    private void validateMessageDoesNotContainUnexpectedItems(String message, List<Object> items) {
        for (int i = ReplacementUtils.MAX_NUMBER_OF_PRINTED_ITEMS; i < items.size(); i++) {
            assertFalse(message.contains(buildPropertyValue(i)));
        }
    }

    private List<Object> createItems() {
        List<Object> items = new ArrayList<Object>(ReplacementUtils.MAX_NUMBER_OF_PRINTED_ITEMS * 2);

        for (int i = 0; i < ReplacementUtils.MAX_NUMBER_OF_PRINTED_ITEMS * 2; i++) {
            items.add(buildPropertyValue(i));
        }

        return items;
    }

    private String buildPropertyValue(int id) {
        return PROPERTY_NAME + String.valueOf(id);
    }

    private void validateMessageContainsProperties(String[] messageItems) {
        assertNotNull(messageItems);
        assertTrue(messageItems.length > 0);
        assertTrue(messageItems[0].contains(PROPERTY_NAME));
        assertTrue(messageItems[1].contains(PROPERTY_COUNTER_NAME));
    }

    private <T> void validateMessageItems(String[] messageItems, List<T> items) {
        validateMessageContainsProperties(messageItems);
        assertTrue(messageItems[0].contains(PROPERTY_VALUE));
        assertTrue(messageItems[1].contains(String.valueOf(items.size())));
    }
}

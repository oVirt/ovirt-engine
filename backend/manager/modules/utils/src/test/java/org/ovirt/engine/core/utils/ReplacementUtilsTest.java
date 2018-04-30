package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Nameable;

public class ReplacementUtilsTest {

    private static final String PROPERTY_NAME = "MY_SINGLE_ITEM_LIST";
    private static final String PROPERTY_VALUE = "MY_SINGLE_ITEM_VALUE";
    private static final String PROPERTY_COUNTER_NAME = "MY_SINGLE_ITEM_LIST_COUNTER";

    @Test
    public void replaceWithSingleItem() {
        List<Object> items = Collections.singletonList(PROPERTY_VALUE);
        validateReplacements(ReplacementUtils.replaceWith(PROPERTY_NAME, items), items);
    }

    @Test
    public void replaceWithNullItem() {
        List<Object> items = Collections.singletonList(null);
        Collection<String> replacements = ReplacementUtils.replaceWith(PROPERTY_NAME, items);
        validateReplacementsContainsExpectedProperties(replacements, items);
        assertTrue(validateReplacementContains(replacements, "null"));
    }

    @Test
    public void replaceWithNameableCollection() {
        Nameable item = () -> PROPERTY_VALUE;

        List<Nameable> items = Collections.singletonList(item);
        validateReplacements(ReplacementUtils.replaceWithNameable(PROPERTY_NAME, items), items);
    }

    @Test
    public void replaceWithEmptyCollection() {
        Collection<String> replacements = ReplacementUtils.replaceWith(PROPERTY_NAME, Collections.emptyList());
        validateReplacementsContainsExpectedProperties(replacements, Collections.emptyList());
    }

    @Test
    public void replaceWithMoreThanMaxItems() {
        List<Object> items = createItems();
        Collection<String> replacements = ReplacementUtils.replaceWith(PROPERTY_NAME, items);
        validateReplacementsContainsExpectedProperties(replacements, items);
        validateReplacementsDoNotContainUnexpectedItems(replacements, items);
    }

    @Test
    public void replaceWithCustomSeparator() {
        List<Object> items = createItems();
        String separator = ",";

        Collection<String> replacements = ReplacementUtils.replaceWith(PROPERTY_NAME, items, separator);
        assertTrue(validateReplacementElementCount(replacements, separator, ReplacementUtils.DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS));
    }

    @Test
    public void containLowerThanDefaultNumberOfElements() {
        List<Object> items = createItems();
        String separator = "sep";

        // Less than the default number of elements to show.
        int numOfElementsToShow = 3;
        Collection<String> replacements = ReplacementUtils.replaceWith(PROPERTY_NAME, items, separator , numOfElementsToShow);
        assertTrue(validateReplacementElementCount(replacements, separator,  numOfElementsToShow));
    }

    @Test
    public void separatorNotEmpty() {
        List<Object> items = createItems();
        String separator = "";
        assertThrows(IllegalArgumentException.class,
                () -> ReplacementUtils.replaceWith(PROPERTY_NAME, items, separator , 5));
    }

    @Test
    public void separatorNotNull() {
        List<Object> items = createItems();
        String separator = null;
        assertThrows(IllegalArgumentException.class,
                () -> ReplacementUtils.replaceWith(PROPERTY_NAME, items, separator, 5));
    }

    @Test
    public void failZeroValuesToShow() {
        List<Object> items = createItems();
        String separator = ", ";
        assertThrows(IllegalArgumentException.class,
                () -> ReplacementUtils.replaceWith(PROPERTY_NAME, items, separator , 0));
    }

    @Test
    public void failNegativeNumOfValuesToShow() {
        List<Object> items = createItems();
        String separator = ", ";
        assertThrows(IllegalArgumentException.class,
                () -> ReplacementUtils.replaceWith(PROPERTY_NAME, items, separator , -5));
    }

    @Test
    public void containBiggerThanDefaultNumberOfElements() {
        List<Object> items = createItems();
        String separator = "sep";

        // More than the default number of elements to show.
        int numOfElementsToShow = 8;
        Collection<String> replacements = ReplacementUtils.replaceWith(PROPERTY_NAME, items, separator , numOfElementsToShow);
        assertTrue(validateReplacementElementCount(replacements, separator,  numOfElementsToShow));
    }

    private boolean validateReplacementElementCount(Collection<String> replacements, String separator, int numOfElementsToShow) {
        String replacement = replacements.iterator().next();
        String[] values = replacement.split(separator);
        int numOfElementsFound = (int) Arrays.stream(values).filter(v -> v.contains(PROPERTY_NAME)).count();
        return numOfElementsFound == numOfElementsToShow;
    }

    private <T> void validateReplacementsContainsExpectedProperties(Collection<String> replacements, List<T> items) {
        assertNotNull(replacements);
        assertEquals(2, replacements.size());
        assertTrue(validateReplacementContains(replacements, "$" + PROPERTY_NAME + " "));
        assertTrue(validateReplacementContains(replacements, "$" + PROPERTY_COUNTER_NAME + " "));
        assertTrue(validateReplacementContains(replacements, String.valueOf(items.size())));
    }

    private <T> void validateReplacements(Collection<String> replacements, List<T> items) {
        validateReplacementsContainsExpectedProperties(replacements, items);
        assertTrue(validateReplacementContains(replacements, PROPERTY_VALUE));
    }

    private boolean validateReplacementContains(Collection<String> replacements, String property) {
        return replacements.stream().anyMatch(r -> r.contains(property));
    }

    private void validateReplacementsDoNotContainUnexpectedItems(Collection<String> replacements, List<Object> items) {
        for (String replacement : replacements) {
            for (int i = ReplacementUtils.DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS; i < items.size(); i++) {
                assertFalse(replacement.contains(buildPropertyValue(i)));
            }
        }
    }

    private List<Object> createItems() {
        List<Object> items = new ArrayList<>(ReplacementUtils.DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS * 2);

        for (int i = 0; i < ReplacementUtils.DEFAULT_MAX_NUMBER_OF_PRINTED_ITEMS * 2; i++) {
            items.add(buildPropertyValue(i));
        }

        return items;
    }

    private String buildPropertyValue(int id) {
        return PROPERTY_NAME + String.valueOf(id);
    }
}

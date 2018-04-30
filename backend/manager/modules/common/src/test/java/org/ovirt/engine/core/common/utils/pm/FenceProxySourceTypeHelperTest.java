package org.ovirt.engine.core.common.utils.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType.CLUSTER;
import static org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType.DC;
import static org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType.OTHER_DC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;

public class FenceProxySourceTypeHelperTest {
    /**
     * Tests if empty list is returned when parsing {@code null} string
     */
    @Test
    public void parseNullString() {
        List<FenceProxySourceType> result = FenceProxySourceTypeHelper.parseFromString(null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests if empty list is returned when parsing empty string
     */
    @Test
    public void parseEmptyString() {
        List<FenceProxySourceType> result = FenceProxySourceTypeHelper.parseFromString(null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests parsing valid strings
     */
    @Test
    public void parseValidString() {
        String[] validValues = {
                CLUSTER.getValue(),
                DC.getValue(),
                OTHER_DC.getValue(),
                CLUSTER.getValue() + "," + DC.getValue() + "," + OTHER_DC.getValue()
        };

        for (String invalidValue : validValues) {
                FenceProxySourceTypeHelper.parseFromString(invalidValue);
        }
    }

    /**
     * Tests if {@code IllegalArgumentException} is thrown when parsing invalid string
     */
    @Test
    public void parseInvalidString() {
        String[] invalidValues = {
                "clust",           // invalid fence proxy source type
                "clust,dc",        // invalid fence proxy source type
                "cluster,d",       // invalid fence proxy source type
                "cluster, dc"      // space should not be used
        };

        for (String invalidValue : invalidValues) {
            try {
                FenceProxySourceTypeHelper.parseFromString(invalidValue);
                fail(String.format(
                        "Value '%s' is not valid argument of FenceProxySourceTypeHelper.parseFromString.",
                        invalidValue));
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

    /**
     * Tests if null is returned when saving {@code null} list
     */
    @Test
    public void saveNullList() {
        String result = FenceProxySourceTypeHelper.saveAsString(null);

        assertNull(result);
    }

    /**
     * Tests if null string is returned when saving empty list
     */
    @Test
    public void saveEmptyList() {
        String result = FenceProxySourceTypeHelper.saveAsString(Collections.emptyList());

        assertNull(result);
    }

    /**
     * Tests saving lists with valid values
     */
    @Test
    public void saveListWithValidValues() {
        List<List<FenceProxySourceType>> validLists = new ArrayList<>();
        validLists.add(Collections.singletonList(CLUSTER));
        validLists.add(Collections.singletonList(DC));
        validLists.add(Collections.singletonList(OTHER_DC));
        validLists.add(Arrays.asList(CLUSTER, DC, OTHER_DC));

        for (List<FenceProxySourceType> validList : validLists) {
            FenceProxySourceTypeHelper.saveAsString(validList);
        }
    }

    /**
     * Tests if {@code IllegalArgumentException} is thrown when saving a list containing invalid values
     */
    @Test
    public void saveListWithInvalidValues() {
        List<List<FenceProxySourceType>> invalidLists = new ArrayList<>();

        List<FenceProxySourceType> listWithNullValue = new ArrayList<>();
        listWithNullValue.add(null);
        invalidLists.add(listWithNullValue);

        invalidLists.add(Arrays.asList(CLUSTER, null));

        for (List<FenceProxySourceType> invalidList : invalidLists) {
            try {
                FenceProxySourceTypeHelper.saveAsString(invalidList);
                fail(String.format(
                        "Value '%s' is not valid argument of FenceProxySourceTypeHelper.parseFromString.",
                        Arrays.toString(invalidList.toArray())));
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

}

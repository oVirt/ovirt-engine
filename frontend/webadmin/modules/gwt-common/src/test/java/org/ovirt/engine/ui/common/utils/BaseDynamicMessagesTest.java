package org.ovirt.engine.ui.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.ui.common.utils.BaseDynamicMessages.DynamicMessageKey;

public class BaseDynamicMessagesTest {

    BaseDynamicMessages testMessages;

    @BeforeEach
    public void setUp() {
        testMessages = new BaseDynamicMessages(null);
    }

    @Test
    public void getPlaceHolderListTest1() {
        String testMessage = "This is a test if {0} can actually work out {1}"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertIndexes(result, 2);
    }

    @Test
    public void getPlaceHolderListTest2() {
        String testMessage = "This is a test if {0} can actually work out {1}, {0}, {1}, {1}"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertIndexes(result, 2);
    }

    @Test
    public void getPlaceHolderListTest3OutofOrder() {
        String testMessage = "This is {3} a {4} test {1} if {0} can actually work out {2}"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertIndexes(result, 5);
    }

    private static void assertIndexes(List<Integer> result, int expectedSize) {
        assertEquals(expectedSize, result.size(), String.format("Result should have %d items", expectedSize)); //$NON-NLS-1$
        for (int i = 0; i < expectedSize; ++i) {
            assertEquals(Integer.valueOf(i), result.get(i), String.format("index %d should be %d", i, i)); //$NON-NLS-1$
        }
    }

    @Test
    public void getPlaceHolderListTestInvalidWithGap() {
        String testMessage = "This is a test if {0} can actually work out {2}"; //$NON-NLS-1$
        assertThrows(IllegalArgumentException.class, () -> testMessages.getPlaceHolderList(testMessage));
    }

    @Test
    public void getPlaceHolderListTestInvalidWithGap2() {
        String testMessage = "This is {3} a {4} test if {0} can actually work out {2}"; //$NON-NLS-1$
        assertThrows(IllegalArgumentException.class, () -> testMessages.getPlaceHolderList(testMessage));
    }

    @Test
    public void getPlaceHolderListTestNoPlaceHolder() {
        String testMessage = "This is a test without place holders"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertTrue(result.isEmpty(), "Result should be empty"); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest() {
        testMessages.addFallback(DynamicMessageKey.APPLICATION_TITLE, "Testing 123: {0}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.APPLICATION_TITLE, "1.1.1"); //$NON-NLS-1$
        assertEquals("Testing 123: 1.1.1", result); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest2() {
        testMessages.addFallback(DynamicMessageKey.APPLICATION_TITLE, "Testing 123: {0}.{1}.{2}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.APPLICATION_TITLE,
                "1", "1", "1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("Testing 123: 1.1.1", result); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest3() {
        testMessages.addFallback(DynamicMessageKey.APPLICATION_TITLE, "Testing 123: {0}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.APPLICATION_TITLE,
                "1.1.1", "2.2.2"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Testing 123: 1.1.1", result); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest4() {
        testMessages.addFallback(DynamicMessageKey.APPLICATION_TITLE, "Testing 123: {0}.{1}.{0}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.APPLICATION_TITLE,
                "1", "2", "3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("Testing 123: 1.2.1", result); //$NON-NLS-1$
    }

    @Test
    public void formatStringTestNotEnoughParams() {
        testMessages.addFallback(DynamicMessageKey.APPLICATION_TITLE, "Testing 123: {0}.{1}.{2}"); //$NON-NLS-1$
        assertThrows(IllegalArgumentException.class,
                () -> testMessages.formatString(DynamicMessageKey.APPLICATION_TITLE, "1.1.1")); //$NON-NLS-1$
    }

}

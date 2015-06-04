package org.ovirt.engine.ui.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.ui.common.utils.BaseDynamicMessages.DynamicMessageKey;

@RunWith(MockitoJUnitRunner.class)
public class BaseDynamicMessagesTest {

    BaseDynamicMessages testMessages;

    @Before
    public void setUp() throws Exception {
        testMessages = new BaseDynamicMessages(null);
    }

    @Test
    public void getPlaceHolderListTest1() {
        String testMessage = "This is a test if {0} can actually work out {1}"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertEquals("Result should have 2 items", 2, result.size()); //$NON-NLS-1$
        assertEquals("index 0 should be 0", (Integer) 0, result.get(0)); //$NON-NLS-1$
    }

    @Test
    public void getPlaceHolderListTest2() {
        String testMessage = "This is a test if {0} can actually work out {1}, {0}, {1}, {1}"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertEquals("Result should have 2 items", 2, result.size()); //$NON-NLS-1$
        assertEquals("index 0 should be 0", (Integer) 0, result.get(0)); //$NON-NLS-1$
        assertEquals("index 1 should be 1", (Integer) 1, result.get(1)); //$NON-NLS-1$
    }

    @Test
    public void getPlaceHolderListTest3OutofOrder() {
        String testMessage = "This is {3} a {4} test {1} if {0} can actually work out {2}"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertEquals("Result should have 5 items", 5, result.size()); //$NON-NLS-1$
        assertEquals("index 0 should be 0", (Integer) 0, result.get(0)); //$NON-NLS-1$
        assertEquals("index 1 should be 1", (Integer) 1, result.get(1)); //$NON-NLS-1$
        assertEquals("index 2 should be 2", (Integer) 2, result.get(2)); //$NON-NLS-1$
        assertEquals("index 3 should be 3", (Integer) 3, result.get(3)); //$NON-NLS-1$
        assertEquals("index 4 should be 4", (Integer) 4, result.get(4)); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPlaceHolderListTestInvalidWithGap() {
        String testMessage = "This is a test if {0} can actually work out {2}"; //$NON-NLS-1$
        testMessages.getPlaceHolderList(testMessage);
        fail("Should not get here"); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPlaceHolderListTestInvalidWithGap2() {
        String testMessage = "This is {3} a {4} test if {0} can actually work out {2}"; //$NON-NLS-1$
        testMessages.getPlaceHolderList(testMessage);
        fail("Should not get here"); //$NON-NLS-1$
    }

    @Test
    public void getPlaceHolderListTestNoPlaceHolder() {
        String testMessage = "This is a test without place holders"; //$NON-NLS-1$
        List<Integer> result = testMessages.getPlaceHolderList(testMessage);
        assertTrue("Result should be empty", result.isEmpty()); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest() {
        testMessages.addFallback(DynamicMessageKey.VERSION_ABOUT, "This is version about: {0}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.VERSION_ABOUT, "1.1.1"); //$NON-NLS-1$
        assertNotNull("There should be a result"); //$NON-NLS-1$
        assertEquals("Result should be 'This is version about: 1.1.1'", //$NON-NLS-1$
                "This is version about: 1.1.1", result); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest2() {
        testMessages.addFallback(DynamicMessageKey.VERSION_ABOUT, "This is version about: {0}.{1}.{2}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.VERSION_ABOUT,
                "1", "1", "1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNotNull("There should be a result"); //$NON-NLS-1$
        assertEquals("Result should be 'This is version about: 1.1.1'", //$NON-NLS-1$
                "This is version about: 1.1.1", result); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest3() {
        testMessages.addFallback(DynamicMessageKey.VERSION_ABOUT, "This is version about: {0}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.VERSION_ABOUT,
                "1.1.1", "2.2.2"); //$NON-NLS-1$ //$NON-NLS-2$
        assertNotNull("There should be a result"); //$NON-NLS-1$
        assertEquals("Result should be 'This is version about: 1.1.1'", //$NON-NLS-1$
                "This is version about: 1.1.1", result); //$NON-NLS-1$
    }

    @Test
    public void formatStringTest4() {
        testMessages.addFallback(DynamicMessageKey.VERSION_ABOUT, "This is version about: {0}.{1}.{0}"); //$NON-NLS-1$
        String result = testMessages.formatString(DynamicMessageKey.VERSION_ABOUT,
                "1", "2", "3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNotNull("There should be a result"); //$NON-NLS-1$
        assertEquals("Result should be 'This is version about: 1.1.1'", //$NON-NLS-1$
                "This is version about: 1.2.1", result); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatStringTestNotEnoughParams() {
        testMessages.addFallback(DynamicMessageKey.VERSION_ABOUT, "This is version about: {0}.{1}.{2}"); //$NON-NLS-1$
        testMessages.formatString(DynamicMessageKey.VERSION_ABOUT, "1.1.1"); //$NON-NLS-1$
        fail("Should not get here"); //$NON-NLS-1$
    }

}

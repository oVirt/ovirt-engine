package org.ovirt.engine.ui.common.system;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientStorageImplTest {

    private static final String KEY_PREFIX = "TestPrefix"; //$NON-NLS-1$

    private ClientStorageImpl tested;

    @Before
    public void setUp() {
        tested = spy(new ClientStorageImpl(KEY_PREFIX) {
            @Override
            void initStorage() {
                // No-op to avoid GWT.create() calls
            }
        });

        // Stub un-testable methods, specific tests should re-stub these as needed
        doReturn(true).when(tested).isWebStorageAvailable();
        doReturn(null).when(tested).getLocalItemImpl(any(String.class));
        doNothing().when(tested).setLocalItemImpl(any(String.class), any(String.class));
        doReturn(null).when(tested).getSessionItemImpl(any(String.class));
        doNothing().when(tested).setSessionItemImpl(any(String.class), any(String.class));
    }

    /**
     * Verify that prefix is applied to given key.
     */
    @Test
    public void getPrefixedKey() {
        String prefixedKey = tested.getPrefixedKey("Key"); //$NON-NLS-1$
        assertThat(prefixedKey, equalTo(KEY_PREFIX + "_Key")); //$NON-NLS-1$
    }

    /**
     * When prefixed key exists, return its value.
     */
    @Test
    public void getItem_prefixedKeyExists() {
        doReturn("LocalValue").when(tested).getLocalItemImpl(KEY_PREFIX + "_LocalKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String localValue = tested.getLocalItem("LocalKey"); //$NON-NLS-1$
        assertThat(localValue, equalTo("LocalValue")); //$NON-NLS-1$

        doReturn("SessionValue").when(tested).getSessionItemImpl(KEY_PREFIX + "_SessionKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String sessionValue = tested.getSessionItem("SessionKey"); //$NON-NLS-1$
        assertThat(sessionValue, equalTo("SessionValue")); //$NON-NLS-1$
    }

    /**
     * When prefixed key is missing but un-prefixed key exists, return value of un-prefixed key.
     */
    @Test
    public void getItem_prefixedKeyMissing_unPrefixedKeyExists() {
        doReturn(null).when(tested).getLocalItemImpl(KEY_PREFIX + "_LocalKey"); //$NON-NLS-1$
        doReturn("LocalValue").when(tested).getLocalItemImpl("LocalKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String localValue = tested.getLocalItem("LocalKey"); //$NON-NLS-1$
        assertThat(localValue, equalTo("LocalValue")); //$NON-NLS-1$

        doReturn(null).when(tested).getSessionItemImpl(KEY_PREFIX + "_SessionKey"); //$NON-NLS-1$
        doReturn("SessionValue").when(tested).getSessionItemImpl("SessionKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String sessionValue = tested.getSessionItem("SessionKey"); //$NON-NLS-1$
        assertThat(sessionValue, equalTo("SessionValue")); //$NON-NLS-1$
    }

    /**
     * When both prefixed and un-prefixed keys are missing, return null.
     */
    @Test
    public void getItem_prefixedKeyMissing_unPrefixedKeyMissing() {
        doReturn(null).when(tested).getLocalItemImpl(KEY_PREFIX + "_LocalKey"); //$NON-NLS-1$
        doReturn(null).when(tested).getLocalItemImpl("LocalKey"); //$NON-NLS-1$
        String localValue = tested.getLocalItem("LocalKey"); //$NON-NLS-1$
        assertNull(localValue);

        doReturn(null).when(tested).getSessionItemImpl(KEY_PREFIX + "_SessionKey"); //$NON-NLS-1$
        doReturn(null).when(tested).getSessionItemImpl("SessionKey"); //$NON-NLS-1$
        String sessionValue = tested.getSessionItem("SessionKey"); //$NON-NLS-1$
        assertNull(sessionValue);
    }

    /**
     * Verify that prefix is applied to given key when setting an item.
     */
    @Test
    public void setItem() {
        tested.setLocalItem("LocalKey", "LocalValue"); //$NON-NLS-1$ //$NON-NLS-2$
        verify(tested).setLocalItemImpl(KEY_PREFIX + "_LocalKey", "LocalValue"); //$NON-NLS-1$ //$NON-NLS-2$

        tested.setSessionItem("SessionKey", "SessionValue"); //$NON-NLS-1$ //$NON-NLS-2$
        verify(tested).setSessionItemImpl(KEY_PREFIX + "_SessionKey", "SessionValue"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}

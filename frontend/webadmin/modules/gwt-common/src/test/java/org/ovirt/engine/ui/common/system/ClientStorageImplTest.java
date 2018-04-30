package org.ovirt.engine.ui.common.system;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientStorageImplTest {

    private ClientStorageImpl tested;

    @BeforeEach
    public void setUp() {
        tested = spy(new ClientStorageImpl() {
            @Override
            void initStorage() {
                // No-op to avoid GWT.create() calls
            }
        });

        // Stub un-testable methods, specific tests should re-stub these as needed
        doNothing().when(tested).setLocalItemImpl(any(), any());
        doNothing().when(tested).setSessionItemImpl(any(), any());
    }

    /**
     * Verify that prefix is applied to given key.
     */
    @Test
    public void getPrefixedKey() {
        String prefixedKey = tested.getPrefixedKey("Key"); //$NON-NLS-1$
        assertThat(prefixedKey, equalTo(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "Key")); //$NON-NLS-1$
    }

    /**
     * When prefixed key exists, return its value.
     */
    @Test
    public void getItem_prefixedKeyExists() {
        doReturn("LocalValue").when(tested).getLocalItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "LocalKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String localValue = tested.getLocalItem("LocalKey"); //$NON-NLS-1$
        assertThat(localValue, equalTo("LocalValue")); //$NON-NLS-1$

        doReturn("SessionValue").when(tested).getSessionItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "SessionKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String sessionValue = tested.getSessionItem("SessionKey"); //$NON-NLS-1$
        assertThat(sessionValue, equalTo("SessionValue")); //$NON-NLS-1$
    }

    /**
     * When prefixed key is missing but un-prefixed key exists, return value of un-prefixed key.
     */
    @Test
    public void getItem_prefixedKeyMissing_unPrefixedKeyExists() {
        doReturn(null).when(tested).getLocalItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "LocalKey"); //$NON-NLS-1$
        doReturn("LocalValue").when(tested).getLocalItemImpl("LocalKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String localValue = tested.getLocalItem("LocalKey"); //$NON-NLS-1$
        assertThat(localValue, equalTo("LocalValue")); //$NON-NLS-1$

        doReturn(null).when(tested).getSessionItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "SessionKey"); //$NON-NLS-1$
        doReturn("SessionValue").when(tested).getSessionItemImpl("SessionKey"); //$NON-NLS-1$ //$NON-NLS-2$
        String sessionValue = tested.getSessionItem("SessionKey"); //$NON-NLS-1$
        assertThat(sessionValue, equalTo("SessionValue")); //$NON-NLS-1$
    }

    /**
     * When both prefixed and un-prefixed keys are missing, return null.
     */
    @Test
    public void getItem_prefixedKeyMissing_unPrefixedKeyMissing() {
        doReturn(null).when(tested).getLocalItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "LocalKey"); //$NON-NLS-1$
        doReturn(null).when(tested).getLocalItemImpl("LocalKey"); //$NON-NLS-1$
        String localValue = tested.getLocalItem("LocalKey"); //$NON-NLS-1$
        assertNull(localValue);

        doReturn(null).when(tested).getSessionItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "SessionKey"); //$NON-NLS-1$
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
        verify(tested).setLocalItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "LocalKey", "LocalValue"); //$NON-NLS-1$ //$NON-NLS-2$

        tested.setSessionItem("SessionKey", "SessionValue"); //$NON-NLS-1$ //$NON-NLS-2$
        verify(tested).setSessionItemImpl(ClientStorageImpl.CLIENT_STORAGE_KEY_PREFIX + "SessionKey", "SessionValue"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}

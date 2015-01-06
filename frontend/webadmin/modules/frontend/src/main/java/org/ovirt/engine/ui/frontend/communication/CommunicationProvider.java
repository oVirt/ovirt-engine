package org.ovirt.engine.ui.frontend.communication;

import java.util.List;

/**
 * Interface defining the communication options between the client and the server.
 */
public interface CommunicationProvider {

    /**
     * Transmits a list of operations using the communications provider. The list can contain one or more operations.
     * The provider must properly determine if the caller wants a single response or a list of responses in the
     * {@code VdcOperationCallback} associated with the operations.
     * @param operations The list of {@code VdcOperation}s
     */
    void transmitOperationList(List<VdcOperation<?, ?>> operations);

    /**
     * Log out user, using the communications provider.
     * @param callback The callback object to call after the operation is completed.
     */
    void logout(UserCallback<?> callback);

    /**
     * Store a {@code String} key value pair in the {@code HttpSession} on the server side. As a result these keys
     * will not be persisted across engine server restarts.
     * @param key The key.
     * @param value The value.
     * @param callback The callback to call once the value has been stored.
     */
    void storeInHttpSession(String key, String value, StorageCallback callback);

    /**
     * Retrieve the value associated with the key from the {@code HttpSession} on the server side.
     * @param key The key
     * @param the callback to call with the result.
     */
    void retrieveFromHttpSession(String key, StorageCallback callback);
}

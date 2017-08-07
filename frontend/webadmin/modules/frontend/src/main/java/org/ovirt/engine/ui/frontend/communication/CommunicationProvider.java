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
}

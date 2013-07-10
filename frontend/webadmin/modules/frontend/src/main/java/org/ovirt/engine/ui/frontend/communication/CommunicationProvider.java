package org.ovirt.engine.ui.frontend.communication;

import java.util.List;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;

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
     * Log in user, using the communications provider.
     * @param loginOperation The login operation.
     */
    void login(VdcOperation<VdcActionType, LoginUserParameters> loginOperation);

    /**
     * Log out user, using the communications provider.
     * @param userObject The object with the user information.
     * @param callback The callback object to call after the operation is completed.
     */
    void logout(Object userObject, UserCallback<?> callback);
}

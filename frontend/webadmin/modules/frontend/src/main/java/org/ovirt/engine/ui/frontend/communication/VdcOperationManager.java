package org.ovirt.engine.ui.frontend.communication;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * This class is a singleton and manages how {@code VdcOperation}s are added to the queue to be processed.
 */
public class VdcOperationManager {

    /**
     * Event bus to propagate events related to operation processing.
     */
    private final EventBus eventBus;

    /**
     * The operation queue. It can hold any kind of VdcOperation.
     */
    private final List<VdcOperation<?, ?>> operationQueue = new ArrayList<VdcOperation<?, ?>>();

    /**
     * The operation processor.
     */
    private final OperationProcessor processor;

    /**
     * Flag that tells us if we are logged in or not.
     */
    private boolean loggedIn = false;

    /**
     * Constructor.
     * @param operationProcessor the operation processor.
     */
    @Inject
    public VdcOperationManager(final EventBus eventBus, final OperationProcessor operationProcessor) {
        this.eventBus = eventBus;
        this.processor = operationProcessor;
    }

    /**
     * Add operation to the queue. The user needs to be logged in before the operation will be added.
     * @param operation The {@code VdcOperation} to add.
     * @return {@code true} if the user was allowed to add the operation, {@code false} otherwise
     */
    public boolean addOperation(final VdcOperation<?, ?> operation) {
        return addOperation(operation, false);
    }

    /**
     * Add operation to the queue. The user does not need to be logged in when the operation is added. The added
     * operation requires that it does not allow duplicates to be added to the queue.
     * @param operation The {@code VdcOperation} to add.
     */
    public void addPublicOperation(final VdcOperation<?, ?> operation) {
        addOperation(operation, true);
    }

    /**
     * Add operation to the queue. Fire event when operation is successfully added if fireEvent is true.
     * If the operation determined by equals is already in the queue, do not add it again.
     * @param operation The {@code VdcOperation} to add.
     * @param isPublic flag that says this operation does not require the user to be logged in.
     * @return {@code true} if the user was allowed to add the operation, {@code false} otherwise
     */
    private boolean addOperation(final VdcOperation<?, ?> operation, final boolean isPublic) {
        // If the user is logged in, or the user is not logged in and the operation does not allow duplicates (aka
        // it is a query, and not an action). And the operation is not already in the queue || the operation is
        // an action (allows duplicates). Then add this operation to the queue, and process the queue immediately.
        final boolean isAllowedToExecute = loggedIn || isPublic;
        final boolean operationCanBeAdded = !operationQueue.contains(operation) || operation.allowDuplicates();

        if (isAllowedToExecute) {
            if (operationCanBeAdded && operationQueue.add(operation)) {
                processor.processOperation(this);

                if (engineSessionRefreshed(operation)) {
                    EngineSessionRefreshedEvent.fire(eventBus);
                }
            }
        }

        return isAllowedToExecute;
    }

    /**
     * Add a list of operations to the queue. Once all the operations are added, fire an event that something
     * is in the queue.
     * @param operationList The list of {@code VdcOperation}
     * @return {@code true} if the user was allowed to add the operation, {@code false} otherwise
     */
    public boolean addOperationList(final List<VdcOperation<?, ?>> operationList) {
        boolean allowed = true;
        for (VdcOperation<?, ?> operation: operationList) {
            if (!addOperation(operation, false)) {
                allowed = false;
            }
        }

        // Call the processor.
        processor.processOperation(this);
        return allowed;
    }

    /**
     * Pull the head of the queue and return the operation.
     * @return The head of the queue, or null if empty.
     */
    public VdcOperation<?, ?> pollOperation() {
        return !operationQueue.isEmpty() ? operationQueue.remove(0) : null;
    }

    /**
     * Log in the user.
     * @param loginOperation The login operation.
     */
    @SuppressWarnings("unchecked")
    public void loginUser(final VdcOperation<VdcActionType, LoginUserParameters> loginOperation) {
        // TODO: should we retry failed logins?
        processor.loginUser(new VdcOperation<VdcActionType, LoginUserParameters>(loginOperation,
            new VdcOperationCallback<VdcOperation<VdcActionType, LoginUserParameters>, VdcReturnValueBase>() {

                @Override
                public void onSuccess(final VdcOperation<VdcActionType, LoginUserParameters> operation,
                        final VdcReturnValueBase result) {
                    loggedIn = true;
                    operation.getSource().getCallback().onSuccess(operation.getSource(), result);
                }

                @Override
                public void onFailure(final VdcOperation<VdcActionType, LoginUserParameters> operation,
                        final Throwable caught) {
                    loggedIn = false;
                    operation.getSource().getCallback().onFailure(operation.getSource(), caught);
                }

            })
        );
    }

    /**
     * Log out the user.
     * @param userObject The object containing enough information for the provider to log the user out.
     * @param callback The callback to call when the operation is completed.
     */
    public void logoutUser(final Object userObject, final UserCallback<?> callback) {
        loggedIn = false;
        processor.logoutUser(userObject, callback);
    }

    /**
     * The user logged in status changed externally, and we need to tell the manager that the status changed.
     * @param isLoggedIn The new logged in status.
     */
    public void setLoggedIn(final boolean isLoggedIn) {
        loggedIn = isLoggedIn;
    }

    /**
     * Store a value on the back-end in the session.
     * @param key The key.
     * @param value The value.
     */
    public void storeInHttpSession(final String key, final String value) {
        processor.storeInHttpSession(key, value);
    }

    /**
     * Retrieve a stored value from the back-end session.
     * @param key The key.
     * @param callback The callback to call with the value.
     */
    public void retrieveFromHttpSession(final String key, final StorageCallback callback) {
        processor.retrieveFromHttpSession(key, callback);
    }

    /**
     * Returns {@code true} if execution of given operation caused the Engine session to be refreshed.
     */
    boolean engineSessionRefreshed(VdcOperation<?, ?> operation) {
        // Actions always refresh the Engine session
        if (operation.isAction()) {
            return true;
        }

        // Queries optionally refresh the Engine session
        else if (((VdcQueryParametersBase) operation.getParameter()).getRefresh()) {
            return true;
        }

        return false;
    }

}

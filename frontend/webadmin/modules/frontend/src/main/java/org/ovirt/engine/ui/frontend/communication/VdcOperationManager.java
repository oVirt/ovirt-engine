package org.ovirt.engine.ui.frontend.communication;

import java.util.ArrayList;
import java.util.List;

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
    private final List<VdcOperation<?, ?>> operationQueue = new ArrayList<>();

    /**
     * The operation processor.
     */
    private final OperationProcessor processor;

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
     * Add operation to the queue.
     * @param operation The {@code VdcOperation} to add.
     * @return {@code true} if the user was allowed to add the operation, {@code false} otherwise
     */
    public boolean addOperation(final VdcOperation<?, ?> operation) {
        return addOperationImpl(operation);
    }

    /**
     * Add operation to the queue. Fire event when operation is successfully added if fireEvent is true.
     * If the operation determined by equals is already in the queue, do not add it again.
     * @param operation The {@code VdcOperation} to add.
     * @return {@code true} if the user was allowed to add the operation, {@code false} otherwise
     */
    private boolean addOperationImpl(final VdcOperation<?, ?> operation) {
        // If the operation is not already in the queue || the operation is an action (allows duplicates).
        // Then add this operation to the queue, and process the queue immediately.
        final boolean operationCanBeAdded = !operationQueue.contains(operation) || operation.allowDuplicates();

        if (operationCanBeAdded && operationQueue.add(operation)) {
            processor.processOperation(this);

            if (engineSessionRefreshed(operation)) {
                EngineSessionRefreshedEvent.fire(eventBus);
            }
        }

        return true;
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
            if (!addOperationImpl(operation)) {
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
     * Log out the user.
     * @param callback The callback to call when the operation is completed.
     */
    public void logoutUser(final UserCallback<?> callback) {
        processor.logoutUser(callback);
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

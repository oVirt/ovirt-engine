package org.ovirt.engine.ui.frontend.communication;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.queries.QueryParametersBase;

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
     */
    public void addOperation(final VdcOperation<?, ?> operation) {
        addOperationImpl(operation);
    }

    /**
     * Add operation to the queue. Fire event when operation is successfully added if fireEvent is true.
     * If the operation determined by equals is already in the queue, do not add it again.
     * @param operation The {@code VdcOperation} to add.
     */
    private void addOperationImpl(final VdcOperation<?, ?> operation) {
        // If the operation is not already in the queue || the operation is an action (allows duplicates).
        // Then add this operation to the queue, and process the queue immediately.
        final boolean operationCanBeAdded = !operationQueue.contains(operation) || operation.allowDuplicates();

        if (operationCanBeAdded && operationQueue.add(operation)) {
            processor.processOperation(this);

            if (engineSessionRefreshed(operation)) {
                EngineSessionRefreshedEvent.fire(eventBus);
            }
        }
    }

    /**
     * Add a list of operations to the queue. Once all the operations are added, fire an event that something
     * is in the queue.
     * @param operationList The list of {@code VdcOperation}
     */
    public void addOperationList(final List<VdcOperation<?, ?>> operationList) {
        operationList.stream().forEach(this::addOperation);

        // Call the processor.
        processor.processOperation(this);
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
     * Returns {@code true} if execution of given operation caused the Engine session to be refreshed.
     */
    boolean engineSessionRefreshed(VdcOperation<?, ?> operation) {
        // Actions always refresh the Engine session
        if (operation.isAction()) {
            return true;
        } else if (((QueryParametersBase) operation.getParameter()).getRefresh()) {
            // Queries optionally refresh the Engine session
            return true;
        }

        return false;
    }

}

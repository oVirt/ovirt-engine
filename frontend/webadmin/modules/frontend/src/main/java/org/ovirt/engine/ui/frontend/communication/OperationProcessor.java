package org.ovirt.engine.ui.frontend.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;

/**
 * This class processes the available operations and dispatches them to the appropriate communications
 * provider.
 */
public class OperationProcessor {

    /**
     * Retry threshold.
     */
    private static final int RETRY_THRESHOLD = 5;

    /**
     * The communications provider.
     */
    private final CommunicationProvider communicationProvider;

    /**
     * The collection of pending operations.
     */
    private final Collection<VdcOperation<?, ?>> pending;

    /**
     * GWT scheduler.
     */
    private Scheduler scheduler;

    /**
     * Constructor for {@code OperationProcess}.
     * @param commProvider The communication provider that communicates with the back-end.
     */
    @Inject
    public OperationProcessor(final CommunicationProvider commProvider) {
        this.communicationProvider = commProvider;
        this.pending = new ArrayList<>();
    }

    /**
     * Process any available operations.
     * @param manager The operation manager.
     */
    public void processOperation(final VdcOperationManager manager) {
        if (scheduler == null) {
            scheduler = Scheduler.get();
        }
        scheduler.scheduleDeferred(() -> processAvailableOperations(manager));
    }

    /**
     * Setter for scheduler.
     * @param sched The new scheduler.
     */
    public void setScheduler(final Scheduler sched) {
        this.scheduler = sched;
    }

    /**
     * Process any available operations. Due to this being a deferred operation, we will be able to combine
     * several operations into a single one. For example, lets say that the queue has 4 operations in it. 3
     * of those operations came from an operation list add, and 1 from a single add. The 3 operations will all
     * have the same callback object. The 4th operation will have a different callback. In essence the 4 operations
     * only have 2 call backs.
     * <p>
     * If the operations are all of the same type, we can merge all those operations into a single list of operations
     * that is handed to the communications provider. If the provider has a way of sending that list in a single
     * communications request we will have saved a round trip to the back end. In order to effectively handle error
     * conditions we will want to replace the call backs in the operations with our own so we can add behavior to
     * the call backs. When we replace those call backs with our own we need to make sure that we only create 2
     * call backs and that they line up with the original call backs.
     * <p>
     * When the provider returns the results, we can then use our replaced call backs to handle errors or other things
     * and call the original call backs.
     *
     * @param manager The operations manager.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void processAvailableOperations(final VdcOperationManager manager) {
        List<VdcOperation<?, ?>> operations = new ArrayList<>();
        Map<VdcOperationCallback<VdcOperation<?, ?>, ?>, VdcOperationCallback<VdcOperation<?, ?>, ?>> usedCallbacks =
                new HashMap<>();
        VdcOperation<?, ?> operation;

        while ((operation = manager.pollOperation()) != null) {
            if (!operation.allowDuplicates() && (pending.contains(operation) || operations.contains(operation))) {
                // Skip this one as the result is pending.
                continue;
            }
            // Check if the original callback from the operation has been replaced already. This happens only in
            // the case where the original operation is part of an operation list.
            VdcOperationCallback<?, ?> replacementCallback = usedCallbacks.get(operation.getCallback());
            if (replacementCallback == null) {
                // Replace the original callback with our own callback. This allows us to manipulate the result
                // before calling the original callback. For instance we can implement retries on failure.
                // Only create a replacement callback if we have not encountered the original callback of the
                // operation before. Re-use existing wrapper callback from the usedCallback map.
                if (!(operation.getCallback() instanceof VdcOperationCallbackList)) {
                    replacementCallback = createCallback(manager);
                } else {
                    replacementCallback = createListCallback(manager);
                }
                usedCallbacks.put(operation.getCallback(),
                        (VdcOperationCallback<VdcOperation<?, ?>, ?>) replacementCallback);
            }
            operations.add(new VdcOperation(operation, replacementCallback));
        }

        // Mark the operations pending.
        addPending(operations);
        communicationProvider.transmitOperationList(operations);
    }

    /**
     * Create a callback object for an operation that expects a regular callback. This callback
     * is called by the provider instead of the original callback. We can then do what we want
     * and call the original callback if needed.
     * @param manager The {@code VdcOperationManager} that has the operation.
     * @return A VdcOperationCallback.
     */
    @SuppressWarnings("unchecked")
    private VdcOperationCallback<?, ?> createCallback(final VdcOperationManager manager) {
        return new VdcOperationCallback<VdcOperation<?, ?>, Object>() {
            @Override
            public void onSuccess(final VdcOperation<?, ?> operation, final Object result) {
                // Do nothing more than call original callback.
                VdcOperation<?, ?> originalOperation = getOriginalOperation(operation);
                originalOperation.getCallback().onSuccess(originalOperation, result);
                removePending(operation);
                // Finished, check for more operations.
                processOperation(manager);
            }

            @Override
            public void onFailure(final VdcOperation<?, ?> operation, final Throwable exception) {
                // Remove pending, so it won't accidentally stop the re-add if possible.
                removePending(operation);
                // If the failure is recoverable, then add the request back into the queue.
                if (!operation.allowDuplicates() && operation.getCopyCount() < RETRY_THRESHOLD) {
                    manager.addOperation(operation);
                } else {
                    VdcOperation<?, ?> originalOperation = getOriginalOperation(operation);
                    originalOperation.getCallback().onFailure(originalOperation, exception);
                }
                // Finished, check for more operations.
                processOperation(manager);
            }
        };
    }

    /**
     * Create a callback object for an operation that expects a list callback. This callback
     * is called by the provider instead of the original callback. We can then do what we want
     * and call the original callback if needed.
     * @param manager The {@code VdcOperationManager} that has the operation.
     * @return A VdcOperationCallback for a list callback.
     */
    @SuppressWarnings("unchecked")
    private VdcOperationCallback<?, ?> createListCallback(final VdcOperationManager manager) {
        return new VdcOperationCallbackList<VdcOperation<?, ?>, Object>() {
            @Override
            public void onSuccess(final List<VdcOperation<?, ?>> operationList, final Object result) {
                if (!operationList.isEmpty()) {
                    // All the call-backs in the list are the same, just have to call one.
                    VdcOperation<?, ?> originalOperation = getOriginalOperation(operationList.get(0));
                    VdcOperationCallback<List<VdcOperation<?, ?>>, Object> originalCallback =
                            originalOperation.getCallback();
                    originalCallback.onSuccess(operationList, result);
                    removePending(operationList);
                    // Finished, check for more operations.
                    processOperation(manager);
                }
            }

            @Override
            public void onFailure(final List<VdcOperation<?, ?>> operationList, final Throwable exception) {
                // If the failure is recoverable, then add the request back into the queue.
                removePending(operationList);
                VdcOperation<?, ?> originalOperation = getOriginalOperation(operationList.get(0));
                // If the operation allows duplicates, it means we shouldn't retry the operation.
                if (!operationList.get(0).allowDuplicates()
                        && operationList.get(0).getCopyCount() < RETRY_THRESHOLD) {
                    manager.addOperationList(operationList);
                } else {
                    VdcOperationCallbackList<VdcOperation<?, ?>, Object> originalCallback =
                            (VdcOperationCallbackList<VdcOperation<?, ?>, Object>)
                            originalOperation.getCallback();
                    originalCallback.onFailure(operationList, exception);
                }
                // Finished, check for more operations.
                processOperation(manager);
            }
        };
    }

    /**
     * Add a list of operations to the pending list.
     * @param operations The list to add.
     */
    private void addPending(final List<VdcOperation<?, ?>> operations) {
        pending.addAll(operations);
    }

    /**
     * Remove operations from pending list as the operations are completed.
     * @param operationList The list of operations to remove.
     */
    private void removePending(final List<VdcOperation<?, ?>> operationList) {
        pending.removeAll(operationList);
    }

    /**
     * Remove operation from pending list as the operation completed.
     * @param operation The operation to remove.
     */
    protected void removePending(final VdcOperation<?, ?> operation) {
        pending.remove(operation);
    }

    /**
     * Traverse the source operation tree to find the original one.
     * @param operation The operation to use to traverse.
     * @return The original operation.
     */
    private VdcOperation<?, ?> getOriginalOperation(final VdcOperation<?, ?> operation) {
        // Get the original operation.
        VdcOperation<?, ?> originalOperation = operation;

        while (originalOperation.getSource() != null) {
            originalOperation = originalOperation.getSource();
        }

        return originalOperation;
    }

    /**
     * Log out the user.
     * @param callback The callback to call when the operation is completed.
     */
    public void logoutUser(final UserCallback<?> callback) {
        communicationProvider.logout(callback);
    }
}

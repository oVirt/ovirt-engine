package org.ovirt.engine.ui.frontend.communication;

/**
 * Callback interface for {@code VdcOperation}s.
 * @param <T> the type of operation.
 * @param <R> the resulting type of the operation.
 */
public interface VdcOperationCallback<T, R> {

    /**
     * Success callback.
     * @param operation The operation processed.
     * @param result The result of the operation.
     */
    void onSuccess(T operation, R result);

    /**
     * Failure callback.
     * @param operation The operation processed.
     * @param caught Any exceptions caught during the processing of the operation.
     */
    void onFailure(T operation, Throwable caught);

}

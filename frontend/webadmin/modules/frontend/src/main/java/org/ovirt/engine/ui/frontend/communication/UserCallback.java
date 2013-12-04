package org.ovirt.engine.ui.frontend.communication;

/**
 * The callback interface for user operations such as logging in and out.
 */
public interface UserCallback<R> {

    /**
     * Success callback.
     * @param result The result of the operation.
     */
    void onSuccess(R result);

    /**
     * Failure callback.
     * @param caught Any exceptions caught during the processing of the operation.
     */
    void onFailure(Throwable caught);

}

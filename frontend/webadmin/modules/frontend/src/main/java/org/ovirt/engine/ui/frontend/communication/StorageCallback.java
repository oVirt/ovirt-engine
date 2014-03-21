package org.ovirt.engine.ui.frontend.communication;

public interface StorageCallback {
    /**
     * Success callback.
     * @param result Either the value, or null if there is no value to return.
     */
    void onSuccess(String result);

    /**
     * Failure callback.
     * @param caught Any exceptions caught during the processing of the operation.
     */
    void onFailure(Throwable caught);
}

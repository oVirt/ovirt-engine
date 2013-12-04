package org.ovirt.engine.ui.frontend.communication;

import java.util.List;

/**
 * A {@code VdcOperationCallback} that represents execution of multiple operations.
 */
public interface VdcOperationCallbackList<T, R> extends VdcOperationCallback<List<T>, R> {

}

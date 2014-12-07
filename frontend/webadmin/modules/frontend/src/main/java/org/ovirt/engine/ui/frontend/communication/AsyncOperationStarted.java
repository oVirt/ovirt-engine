package org.ovirt.engine.ui.frontend.communication;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when VdcOperation starts. Used primarily to monitor progress display in dialogs.
 */
@GenEvent
public class AsyncOperationStarted {
    /**
     * the model to which this operation is relevant.
     */
    Object target;
}

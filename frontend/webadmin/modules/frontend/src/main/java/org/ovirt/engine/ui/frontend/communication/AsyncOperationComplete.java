package org.ovirt.engine.ui.frontend.communication;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

/**
 * Event triggered when VdcOperation completes. Used primarily to monitor progress display in dialogs, but also to
 * trigger refresh events on active models.
 */
@GenEvent
public class AsyncOperationComplete {
    /**
     * the model to which this operation is relevant.
     */
    @Order(1)
    Object target;

    /**
     * whether this operation corresponds to an action or a query.
     */
    @Order(2)
    boolean action;

    /**
     * whether this operation was successful.
     */
    @Order(3)
    boolean success;
}
